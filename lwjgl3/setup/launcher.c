/**
 * launcher.c - 氢风 (qingfeng) Native Windows Launcher
 *
 * 编译:
 *   x86_64-w64-mingw32-gcc -O2 -s -static -mwindows \
 *       -D_WIN32_WINNT=0x0601 -D_WIN32_IE=0x0601 \
 *       -o launcher.exe launcher.c -lshlwapi
 *
 * 设计原则:
 *   - 仅使用 Win7 SP1 可用 API（_WIN32_WINNT=0x0601），禁止引用 api-ms-win-core-path-*
 *   - 静态链接 CRT，无运行时库分发需求
 *   - 替换 PyInstaller 启动器，根除其 bootloader 在 Win7 上的崩溃问题
 *
 * Win7 实验性支持说明:
 *   本启动器自身已确保仅调用 Win7 SP1 可用 API，理论上可在 Win7 上正常启动。
 *   但 Java 运行时（JDK 21）对 Win7 的支持取决于补丁安装情况，且部分系统环境
 *   可能因缺少特定组件或安全更新而无法正常运行。若在 Win7 上遇到启动失败，
 *   建议先安装所有可选系统更新（特别是 KB2533623 和 Universal C Runtime），
 *   如问题依旧，则属当前系统环境不满足运行条件，建议升级至 Windows 10 或更高版本。
 */

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <shlwapi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

/* ======================== 常量 ======================== */

#define CONFIG_FILE       L"lib\\set.json"
#define JAR_DIR           L"lib\\jar"
#define JAR_PATTERN       L"qingfeng-*.jar"
#define APP_NAME          L"氢风"
#define JAVA_MIN_MAJOR    17

/* ======================== 错误/警告弹窗 ======================== */

static void
show_error (LPCWSTR message)
{
    MessageBoxW(NULL, message, APP_NAME, MB_OK | MB_ICONERROR);
    ExitProcess(1);
}

static void
show_warning (LPCWSTR message)
{
    MessageBoxW(NULL, message, APP_NAME, MB_OK | MB_ICONWARNING);
}

/* ======================== 路径工具 ======================== */

static void
get_base_dir (LPWSTR buf, DWORD size)
{
    WCHAR path[MAX_PATH];
    DWORD len = GetModuleFileNameW(NULL, path, MAX_PATH);
    if (len == 0 || len >= MAX_PATH)
        show_error(L"无法获取程序路径");
    PathRemoveFileSpecW(path);
    lstrcpynW(buf, path, (int)(size / sizeof(WCHAR)));
}

/* ======================== 极简 JSON 解析 ======================== */

static char *
read_file_to_heap (LPCWSTR path)
{
    HANDLE h = CreateFileW(path, GENERIC_READ, FILE_SHARE_READ, NULL,
                           OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    if (h == INVALID_HANDLE_VALUE) return NULL;
    DWORD size = GetFileSize(h, NULL);
    if (size == INVALID_FILE_SIZE || size == 0) { CloseHandle(h); return NULL; }
    char *buf = (char *)malloc(size + 1);
    if (!buf) { CloseHandle(h); return NULL; }
    DWORD read;
    if (!ReadFile(h, buf, size, &read, NULL)) { free(buf); CloseHandle(h); return NULL; }
    buf[size] = '\0';
    CloseHandle(h);
    return buf;
}

static BOOL
json_get_string (const char *json, const char *key, char *value, int value_size)
{
    char search[128];
    snprintf(search, sizeof(search), "\"%s\"", key);
    const char *p = strstr(json, search);
    if (!p) return FALSE;
    p = strchr(p + strlen(key) + 2, ':');
    if (!p) return FALSE;
    p = strchr(p, '"');
    if (!p) return FALSE;
    p++;
    int i = 0;
    while (*p && *p != '"' && i < value_size - 1) value[i++] = *p++;
    value[i] = '\0';
    return TRUE;
}

static BOOL
json_get_bool (const char *json, const char *key, BOOL *value)
{
    char search[128];
    snprintf(search, sizeof(search), "\"%s\"", key);
    const char *p = strstr(json, search);
    if (!p) return FALSE;
    p = strchr(p + strlen(key) + 2, ':');
    if (!p) return FALSE;
    while (*p == ' ' || *p == '\t' || *p == '\r' || *p == '\n') p++;
    if (strncmp(p, "true", 4) == 0)  { *value = TRUE;  return TRUE; }
    if (strncmp(p, "false", 5) == 0) { *value = FALSE; return TRUE; }
    return FALSE;
}

/* ======================== 控制台管理 ======================== */

static void
show_console (void)
{
    if (!AllocConsole()) return;
    /* 重定向 stdout/stderr 到新控制台 */
    FILE *fp;
    fp = freopen("CONOUT$", "w", stdout);
    fp = freopen("CONOUT$", "w", stderr);
    (void)fp;
    setvbuf(stdout, NULL, _IONBF, 0);
    setvbuf(stderr, NULL, _IONBF, 0);
}

static void
hide_console (void)
{
    HWND hwnd = GetConsoleWindow();
    if (!hwnd) return;
    /* 改为工具窗口以隐藏任务栏图标 */
    SetWindowLongW(hwnd, GWL_EXSTYLE, GetWindowLongW(hwnd, GWL_EXSTYLE) | WS_EX_TOOLWINDOW);
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                 SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    ShowWindow(hwnd, SW_HIDE);
    FreeConsole();
}

/* ======================== Windows 版本检测 ======================== */

/* debug_log 前向声明（实现在 find_jar 之后） */
static void debug_log (LPCSTR format, ...);
static void debug_logW (LPCSTR label, LPCWSTR value);

typedef LONG (WINAPI *fn_RtlGetVersion)(PRTL_OSVERSIONINFOW);

static void
check_windows_version (void)
{
    HMODULE ntdll = GetModuleHandleW(L"ntdll.dll");
    if (!ntdll) return;

    fn_RtlGetVersion pRtlGetVersion = (fn_RtlGetVersion)
        GetProcAddress(ntdll, "RtlGetVersion");
    if (!pRtlGetVersion) return;

    RTL_OSVERSIONINFOW ver = { sizeof(ver) };
    if (pRtlGetVersion(&ver) != 0) return;

    debug_log("[系统] Windows %lu.%lu Build %lu",
              ver.dwMajorVersion, ver.dwMinorVersion, ver.dwBuildNumber);

    if (ver.dwMajorVersion < 6 ||
        (ver.dwMajorVersion == 6 && ver.dwMinorVersion == 0))
    {
        WCHAR msg[256];
        swprintf_s(msg, 256,
                   L"氢风 需要 Windows 7 或更高版本。\n"
                   L"当前系统: Windows %lu.%lu\n\n"
                   L"请升级操作系统至 Windows 7 SP1 或更高版本。",
                   ver.dwMajorVersion, ver.dwMinorVersion);
        show_error(msg);
    }
    else if (ver.dwMajorVersion == 6 && ver.dwMinorVersion == 1)
    {
        show_warning(
            L"氢风 实验性支持 Windows 7。\n\n"
            L"本启动器已针对 Win7 做了兼容处理，但由于 Java 运行时对 Win7 的支持"
            L"有限，且不同机器的补丁安装情况和系统环境存在差异，仍有可能无法正常启动。\n\n"
            L"若遇到启动失败，建议依次尝试：\n"
            L"  1. 安装所有可选系统更新\n"
            L"    （特别是 KB2533623 和 Universal C Runtime）\n"
            L"     https://www.microsoft.com/download/details.aspx?id=48234\n"
            L"  2. 从游戏官网重新下载安装包\n"
            L"  3. 如问题依旧，则属当前系统环境不满足运行条件，\n"
            L"     建议升级至 Windows 10 或更高版本以获得完整支持");
    }
}

/* ======================== Java 查找 ======================== */

static BOOL
check_java_version (LPCWSTR java_exe)
{
    HANDLE hStdoutRead, hStdoutWrite;
    SECURITY_ATTRIBUTES sa = { sizeof(sa), NULL, TRUE };
    if (!CreatePipe(&hStdoutRead, &hStdoutWrite, &sa, 4096))
        return FALSE;
    SetHandleInformation(hStdoutRead, HANDLE_FLAG_INHERIT, HANDLE_FLAG_INHERIT);

    WCHAR cmd[MAX_PATH + 32];
    swprintf_s(cmd, MAX_PATH + 32, L"\"%s\" -version", java_exe);

    STARTUPINFOW si = { sizeof(si), .dwFlags = STARTF_USESTDHANDLES,
                        .hStdOutput = hStdoutWrite, .hStdError = hStdoutWrite };
    PROCESS_INFORMATION pi;
    if (!CreateProcessW(NULL, cmd, NULL, NULL, TRUE,
                        CREATE_NO_WINDOW, NULL, NULL, &si, &pi))
    {
        CloseHandle(hStdoutWrite);
        CloseHandle(hStdoutRead);
        return FALSE;
    }
    CloseHandle(hStdoutWrite);

    char buf[4096] = {0};
    DWORD read;
    if (ReadFile(hStdoutRead, buf, sizeof(buf) - 1, &read, NULL))
        buf[read] = '\0';

    WaitForSingleObject(pi.hProcess, 3000);
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    CloseHandle(hStdoutRead);

    debug_log("[Java] 版本输出: %s", buf);

    /* 解析版本号: "17.0.1" 或 "1.8.0" */
    char *p = strchr(buf, '"');
    if (!p) return FALSE;
    p++;
    int major = 0;
    if (sscanf_s(p, "%d", &major) != 1) return FALSE;
    /* Java 8 及更早格式为 "1.8.x" */
    if (major == 1) {
        p = strchr(p, '.');
        if (p) sscanf_s(p + 1, "%d", &major);
    }
    return major >= JAVA_MIN_MAJOR;
}

static BOOL
find_bundled_jre (LPCWSTR base_dir, LPWSTR java_path, DWORD java_path_size)
{
    /* 优先读取 set.json 中的 jre 配置 */
    WCHAR config_path[MAX_PATH];
    PathCombineW(config_path, base_dir, CONFIG_FILE);

    char *json = read_file_to_heap(config_path);
    if (json)
    {
        char jre_name[64] = {0};
        if (json_get_string(json, "jre", jre_name, sizeof(jre_name)))
        {
            WCHAR wname[64];
            MultiByteToWideChar(CP_UTF8, 0, jre_name, -1, wname, 64);
            swprintf_s(java_path, java_path_size / 2,
                       L"%s\\lib\\%s\\bin\\java.exe", base_dir, wname);
            if (GetFileAttributesW(java_path) != INVALID_FILE_ATTRIBUTES)
            {
                free(json);
                return TRUE;
            }
        }
        free(json);
    }

    /* 回退: 通配查找 lib 下 jre 目录中的 java.exe */
    WCHAR pattern[MAX_PATH];
    swprintf_s(pattern, MAX_PATH, L"%s\\lib\\jre*\\bin\\java.exe", base_dir);
    WIN32_FIND_DATAW ffd;
    HANDLE hFind = FindFirstFileW(pattern, &ffd);
    if (hFind != INVALID_HANDLE_VALUE)
    {
        FindClose(hFind);
        swprintf_s(java_path, java_path_size / 2,
                   L"%s\\lib\\%s\\bin\\java.exe", base_dir, ffd.cFileName);
        return TRUE;
    }
    return FALSE;
}

static BOOL
find_system_java (LPCWSTR base_dir, LPWSTR java_path, DWORD java_path_size)
{
    WCHAR buf[MAX_PATH];

    /* 1. JAVA_HOME */
    if (GetEnvironmentVariableW(L"JAVA_HOME", buf, MAX_PATH) > 0)
    {
        PathCombineW(java_path, buf, L"bin\\java.exe");
        if (GetFileAttributesW(java_path) != INVALID_FILE_ATTRIBUTES)
            return TRUE;
    }

    /* 2. PATH */
    WCHAR env_path[32768];
    DWORD len = GetEnvironmentVariableW(L"PATH", env_path, 32768);
    if (len > 0 && len < 32768)
    {
        LPWSTR ctx = NULL;
        LPWSTR tok = wcstok_s(env_path, L";", &ctx);
        while (tok)
        {
            PathCombineW(java_path, tok, L"java.exe");
            if (GetFileAttributesW(java_path) != INVALID_FILE_ATTRIBUTES)
                return TRUE;
            tok = wcstok_s(NULL, L";", &ctx);
        }
    }

    /* 3. Program Files 常见安装目录 */
    LPCWSTR vendors[] = {
        L"Eclipse Adoptium", L"AdoptOpenJDK", L"Java",
        L"Amazon Corretto", L"BellSoft", L"LibericaJDK"
    };
    for (int pv = 0; pv < 2; pv++)
    {
        LPCWSTR pf_var = (pv == 0) ? L"ProgramFiles" : L"ProgramFiles(x86)";
        if (!GetEnvironmentVariableW(pf_var, buf, MAX_PATH))
            continue;
        for (int v = 0; v < (int)(sizeof(vendors) / sizeof(vendors[0])); v++)
        {
            WCHAR vendor_dir[MAX_PATH];
            PathCombineW(vendor_dir, buf, vendors[v]);
            if (GetFileAttributesW(vendor_dir) == INVALID_FILE_ATTRIBUTES)
                continue;

            WCHAR search[MAX_PATH];
            swprintf_s(search, MAX_PATH, L"%s\\*", vendor_dir);
            WIN32_FIND_DATAW ffd;
            HANDLE hFind = FindFirstFileW(search, &ffd);
            if (hFind == INVALID_HANDLE_VALUE) continue;
            do {
                if (!(ffd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
                    continue;
                if (wcscmp(ffd.cFileName, L".") == 0 ||
                    wcscmp(ffd.cFileName, L"..") == 0)
                    continue;
                WCHAR jdk_bin[MAX_PATH];
                PathCombineW(jdk_bin, vendor_dir, ffd.cFileName);
                PathCombineW(java_path, jdk_bin, L"bin\\java.exe");
                if (GetFileAttributesW(java_path) != INVALID_FILE_ATTRIBUTES)
                {
                    FindClose(hFind);
                    return TRUE;
                }
                /* 再尝试 jre 子目录 */
                PathCombineW(jdk_bin, jdk_bin, L"jre");
                PathCombineW(java_path, jdk_bin, L"bin\\java.exe");
                if (GetFileAttributesW(java_path) != INVALID_FILE_ATTRIBUTES)
                {
                    FindClose(hFind);
                    return TRUE;
                }
            } while (FindNextFileW(hFind, &ffd));
            FindClose(hFind);
        }
    }

    return FALSE;
}

/* ======================== JAR 查找 ======================== */

static void
find_jar (LPCWSTR base_dir, LPWSTR jar_path, DWORD jar_path_size)
{
    WCHAR pattern[MAX_PATH];
    swprintf_s(pattern, MAX_PATH, L"%s\\%s\\%s", base_dir, JAR_DIR, JAR_PATTERN);

    WIN32_FIND_DATAW ffd;
    HANDLE hFind = FindFirstFileW(pattern, &ffd);
    if (hFind == INVALID_HANDLE_VALUE)
        show_error(L"未找到游戏主程序 (qingfeng-*.jar)\n请从游戏官网重新下载安装包。");

    FindClose(hFind);
    swprintf_s(jar_path, jar_path_size / 2,
               L"%s\\%s\\%s", base_dir, JAR_DIR, ffd.cFileName);
}

/* ======================== 调试日志 ======================== */

#define DEBUG_LOG L"launcher_debug.log"

static void
debug_log (LPCSTR format, ...)
{
    WCHAR base[MAX_PATH], log_path[MAX_PATH];
    get_base_dir(base, sizeof(base));
    PathCombineW(log_path, base, DEBUG_LOG);

    char buf[4096];
    va_list args;
    va_start(args, format);
    int len = vsnprintf(buf, sizeof(buf), format, args);
    va_end(args);
    if (len < 0) len = (int)strlen(buf);
    if (len <= 0) return;
    if (len > (int)sizeof(buf) - 1) len = (int)sizeof(buf) - 1;

    HANDLE hFile = CreateFileW(log_path, FILE_APPEND_DATA, FILE_SHARE_READ, NULL,
                                OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE) return;
    SetFilePointer(hFile, 0, NULL, FILE_END);
    DWORD written;
    WriteFile(hFile, buf, (DWORD)len, &written, NULL);
    WriteFile(hFile, "\r\n", 2, &written, NULL);
    CloseHandle(hFile);
}

static void
debug_logW (LPCSTR label, LPCWSTR value)
{
    if (!value) { debug_log("%s: (空)", label); return; }
    char mb[2048];
    int n = WideCharToMultiByte(CP_UTF8, 0, value, -1, mb, sizeof(mb), NULL, NULL);
    if (n <= 0) { debug_log("%s: (转换失败)", label); return; }
    debug_log("%s: %s", label, mb);
}

/* ======================== 主函数 ======================== */

int WINAPI
WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance,
         LPSTR lpCmdLine, int nCmdShow)
{
    (void)hInstance; (void)hPrevInstance; (void)nCmdShow;

    /* 1. 获取程序基础目录 */
    WCHAR base_dir[MAX_PATH];
    get_base_dir(base_dir, sizeof(base_dir));
    SetCurrentDirectoryW(base_dir);

    /* 初始化调试日志 */
    {
        SYSTEMTIME st;
        GetLocalTime(&st);
        debug_log("");
        debug_log("=== 氢风启动器调试日志 ===");
        debug_log("时间: %04d-%02d-%02d %02d:%02d:%02d",
                  st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
    }
    debug_logW("[路径] 程序目录", base_dir);

    /* 2. 读取配置 */
    WCHAR config_path[MAX_PATH];
    PathCombineW(config_path, base_dir, CONFIG_FILE);

    BOOL console_mode = FALSE; /* 默认隐藏控制台（set.json 不存在/解析失败时安全回退） */
    char *json = read_file_to_heap(config_path);
    if (json) {
        json_get_bool(json, "console", &console_mode);
        char jre_name[64] = {0};
        json_get_string(json, "jre", jre_name, sizeof(jre_name));
        debug_log("[配置] set.json: 已读取");
        debug_log("[配置] console: %s", console_mode ? "true" : "false");
        debug_log("[配置] jre: %s", jre_name[0] ? jre_name : "(未配置)");
        free(json);
    }
    else
    {
        char _cfg_utf8[1024];
        WideCharToMultiByte(CP_UTF8, 0, config_path, -1, _cfg_utf8, sizeof(_cfg_utf8), NULL, NULL);
        debug_log("[配置] set.json: 不存在或无法读取（路径: %s）", _cfg_utf8);
        debug_log("[配置] console: false（默认值，因 set.json 缺失）");
    }

    /* 3. 控制台管理 */
    if (console_mode)
    {
        debug_log("[控制台] 模式: 显示（调用 AllocConsole）");
        show_console();
    }
    else
    {
        debug_log("[控制台] 模式: 隐藏");
        hide_console();
    }

    /* 4. Windows 版本检测 */
    check_windows_version();

    /* 5. 查找 Java */
    WCHAR java_exe[MAX_PATH];
    BOOL found = find_bundled_jre(base_dir, java_exe, sizeof(java_exe));

    if (found && check_java_version(java_exe))
    {
        debug_log("[Java] 来源: 内置 JRE");
        debug_logW("[Java] 路径", java_exe);
        debug_log("[Java] 版本检查: 通过");
    }
    else
    {
        if (found)
        {
            debug_logW("[Java] 内置 JRE 版本检查失败", java_exe);
        }
        else
        {
            debug_log("[Java] 内置 JRE: 未找到");
        }

        /* 内置 JRE 不可用，搜索系统 Java */
        if (find_system_java(base_dir, java_exe, sizeof(java_exe)))
        {
            debug_logW("[Java] 来源: 系统 Java", java_exe);
            if (check_java_version(java_exe))
            {
                debug_log("[Java] 版本检查: 通过");
                show_warning(
                    L"未找到内置 JRE，已使用系统 Java 启动。\n\n"
                    L"如需最佳兼容性，请从游戏官网下载完整安装包。");
            }
            else
            {
                debug_log("[Java] 版本检查: 失败（版本过低）");
                WCHAR msg[MAX_PATH + 64];
                swprintf_s(msg, MAX_PATH + 64,
                    L"系统 Java 版本过低（需 %d+）。\n"
                    L"请安装 Java %d 或更高版本，或从游戏官网下载包含内置 JRE 的安装包。",
                    JAVA_MIN_MAJOR, JAVA_MIN_MAJOR);
                show_error(msg);
            }
        }
        else
        {
            debug_log("[Java] 系统 Java: 未找到");
            show_error(
                L"未找到 Java 运行时环境。\n\n"
                L"请安装 Java 17 或更高版本：\n"
                L"  https://adoptium.net/\n\n"
                L"或从游戏官网下载包含内置 JRE 的安装包。");
        }
    }

    /* 6. 查找 JAR */
    WCHAR jar_file[MAX_PATH];
    find_jar(base_dir, jar_file, sizeof(jar_file));
    debug_logW("[JAR] 文件", jar_file);

    /* 7. 构建命令行并启动 Java */
    LPWSTR args = GetCommandLineW();
    /* 跳过本程序名称（可能被引号包裹） */
    LPWSTR arg_start = args;
    if (*arg_start == L'"')
    {
        arg_start++;
        while (*arg_start && *arg_start != L'"') arg_start++;
        if (*arg_start) arg_start++;
    }
    else
    {
        while (*arg_start && *arg_start != L' ') arg_start++;
    }
    while (*arg_start == L' ') arg_start++;

    WCHAR cmd_line[32768];
    if (*arg_start)
        swprintf_s(cmd_line, 32768,
                   L"\"%s\" -jar \"%s\" %s", java_exe, jar_file, arg_start);
    else
        swprintf_s(cmd_line, 32768,
                   L"\"%s\" -jar \"%s\"", java_exe, jar_file);

    /* 创建管道，捕获 Java 的标准输出和错误输出（仅非控制台模式） */
    HANDLE hPipeRead = NULL, hPipeWrite = NULL;
    BOOL pipeOk = FALSE;
    if (!console_mode)
    {
        SECURITY_ATTRIBUTES sa = { sizeof(sa), NULL, TRUE };
        pipeOk = CreatePipe(&hPipeRead, &hPipeWrite, &sa, 65536);
        if (pipeOk)
            SetHandleInformation(hPipeRead, HANDLE_FLAG_INHERIT, HANDLE_FLAG_INHERIT);
    }

    STARTUPINFOW si = { sizeof(si) };
    PROCESS_INFORMATION pi;

    if (pipeOk)
    {
        si.dwFlags = STARTF_USESTDHANDLES;
        si.hStdOutput = hPipeWrite;
        si.hStdError  = hPipeWrite;
    }

    DWORD creation_flags = console_mode ? 0 : CREATE_NO_WINDOW;
    {
        char _cmd_utf8[8192], _dir_utf8[1024];
        WideCharToMultiByte(CP_UTF8, 0, cmd_line, -1, _cmd_utf8, sizeof(_cmd_utf8), NULL, NULL);
        WideCharToMultiByte(CP_UTF8, 0, base_dir, -1, _dir_utf8, sizeof(_dir_utf8), NULL, NULL);
        debug_log("[启动] 命令行: %s", _cmd_utf8);
        debug_log("[启动] 工作目录: %s", _dir_utf8);
    }
    debug_log("[启动] 创建标志: %s", creation_flags == 0 ? "0（继承控制台）" : "CREATE_NO_WINDOW");
    debug_log("[启动] 管道: %s", pipeOk ? "已创建" : "未使用");

    if (!CreateProcessW(NULL, cmd_line, NULL, NULL, pipeOk ? TRUE : FALSE,
                        creation_flags, NULL, base_dir, &si, &pi))
    {
        if (pipeOk) { CloseHandle(hPipeWrite); CloseHandle(hPipeRead); }
        WCHAR msg[MAX_PATH + 128];
        DWORD err = GetLastError();
        swprintf_s(msg, MAX_PATH + 128,
                   L"启动 Java 失败（错误码：%lu）。\n\n"
                   L"这可能是因为当前系统环境不满足 Java 21 的运行要求。\n"
                   L"建议安装所有系统更新，若问题依旧请升级至 Windows 10 或更高版本。",
                   err);
        debug_log("[启动] CreateProcessW 失败，错误码: %lu", err);
        show_error(msg);
    }
    else
    {
        debug_log("[启动] CreateProcessW: 成功（PID: %lu）", pi.dwProcessId);
    }

    /* 关闭写端，否则下面的 ReadFile 会一直阻塞 */
    if (pipeOk) CloseHandle(hPipeWrite);

    /* 8. 等待 Java 退出 */
    WaitForSingleObject(pi.hProcess, INFINITE);

    /* 读取 Java 的输出（含错误信息） */
    char output_buf[16384] = {0};
    DWORD bytes_read = 0;
    if (pipeOk)
        ReadFile(hPipeRead, output_buf, sizeof(output_buf) - 1, &bytes_read, NULL);

    /* 获取退出码 */
    DWORD exit_code = 0;
    GetExitCodeProcess(pi.hProcess, &exit_code);

    /* 关闭进程句柄 */
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    if (pipeOk) CloseHandle(hPipeRead);

    debug_log("[退出] Java 退出码: %lu", exit_code);
    debug_log("[退出] 管道输出字节数: %lu", bytes_read);

    /* 9. 如果 Java 异常退出，弹窗显示错误信息 */
    if (exit_code != 0)
    {
        WCHAR error_detail[32768];
        if (bytes_read > 0)
        {
            MultiByteToWideChar(CP_UTF8, 0, output_buf, -1, error_detail, 32768);
        }
        else
        {
            wcscpy_s(error_detail, 32768, L"（无详细错误信息）");
        }

        WCHAR msg[32768];
        swprintf_s(msg, 32768,
                   L"Java 运行时遇到问题（退出码：%lu）。\n\n"
                   L"错误信息：\n%s\n\n"
                   L"这可能是当前系统环境不满足运行条件，\n"
                   L"建议安装所有系统更新后重试，或升级至 Windows 10 或更高版本。",
                   exit_code, error_detail);
        show_error(msg);
    }

    return 0;
}
