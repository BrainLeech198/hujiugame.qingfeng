/**
 * launcher.c - 氢风 (qingfeng) Native Windows Launcher
 *
 * 编译:
 *   x86_64-w64-mingw32-gcc -O2 -s -static -mwindows \
 *       -o launcher.exe launcher.c -lshlwapi
 *
 * 说明: _WIN32_WINNT / _WIN32_IE 已在文件内声明为 0x0601（Win7 SP1 兼容约束），
 *       编译命令无需再传 -D 参数。
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

/* Win7 SP1 兼容约束：目标 API 版本在文件内声明，编译命令无需额外 -D 参数 */
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0601
#endif
#ifndef _WIN32_IE
#define _WIN32_IE 0x0601
#endif

#include <windows.h>
#include <shlwapi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

/* 数组元素个数（swprintf_s / 缓冲大小等需要"元素个数"而非"字节数"的场合） */
#define ARRAY_LEN(a) (sizeof(a) / sizeof((a)[0]))

/* ================================================== 常量 ================================================== */

#define CONFIG_FILE    L"lib\\set.json"                            /* 启动器配置（jre 路径、console 显隐） */
#define JAR_DIR        L"lib\\jar"                                 /* 游戏 JAR 目录 */
#define JAR_PATTERN    L"qingfeng-*.jar"                           /* 游戏主程序命名模式 */
#define APP_NAME       L"氢风"
#define JAVA_MIN_MAJOR 17                                          /* 最低 Java 主版本 */
#define MUTEX_NAME     L"Local\\com.hujiugame.qingfeng.launcher"   /* 单实例互斥体名 */
#define DEBUG_LOG_FILE L"launcher_debug.log"                       /* 调试日志文件名 */
#define CMD_BUF        32768                                       /* 完整命令行（含透传参数） */
#define CAPTURE_BUF    65536                                       /* Java 输出捕获缓冲 */
#define REPORT_BUF     65536                                       /* 错误弹窗文本缓冲 */
#define PATH_BUF       32768                                       /* PATH 环境变量缓冲 */

/* ================================================== 错误/警告/提示弹窗 ================================================== */

/* 弹出错误框并终止启动器（此函数不返回）。 */
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

static void
show_notice (LPCWSTR message)
{
    MessageBoxW(NULL, message, APP_NAME, MB_OK | MB_ICONINFORMATION);
}

/* ================================================== 路径工具 ================================================== */

/* 获取 launcher.exe 所在目录（不含文件名），写入 buf（size 为元素个数）。 */
static void
get_base_dir (LPWSTR buf, DWORD size)
{
    WCHAR path[MAX_PATH];
    DWORD len = GetModuleFileNameW(NULL, path, MAX_PATH);
    if (len == 0 || len >= MAX_PATH)
        show_error(L"无法获取程序路径");
    PathRemoveFileSpecW(path);
    lstrcpynW(buf, path, (int)size);
}

/* ================================================== 调试日志 ================================================== */

/* 追加一行到 launcher_debug.log（UTF-8），写日志失败静默忽略，不影响启动流程。 */
static void
debug_log (LPCSTR format, ...)
{
    WCHAR base[MAX_PATH], log_path[MAX_PATH];
    get_base_dir(base, ARRAY_LEN(base));
    PathCombineW(log_path, base, DEBUG_LOG_FILE);

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

/* 记录一个宽字符串字段（UTF-16），自动转 UTF-8 后写入日志。 */
static void
debug_logW (LPCSTR label, LPCWSTR value)
{
    if (!value)
    {
        debug_log("%s: (空)", label);
        return;
    }
    char mb[2048];
    int n = WideCharToMultiByte(CP_UTF8, 0, value, -1, mb, sizeof(mb), NULL, NULL);
    if (n <= 0)
    {
        debug_log("%s: (转换失败)", label);
        return;
    }
    debug_log("%s: %s", label, mb);
}

/* ================================================== 极简 JSON 解析 ================================================== */

/* 读取整个文件到堆内存（以 \0 结尾），失败返回 NULL；调用方负责 free。
 * 循环读取以应对常规文件单次 ReadFile 的短读情况。 */
static char *
read_file_to_heap (LPCWSTR path)
{
    HANDLE h = CreateFileW(path, GENERIC_READ, FILE_SHARE_READ, NULL,
                           OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    if (h == INVALID_HANDLE_VALUE) return NULL;
    DWORD size = GetFileSize(h, NULL);
    if (size == INVALID_FILE_SIZE || size == 0)
    {
        CloseHandle(h);
        return NULL;
    }
    char *buf = (char *)malloc((size_t)size + 1);
    if (!buf)
    {
        CloseHandle(h);
        return NULL;
    }
    DWORD total = 0;
    while (total < size)
    {
        DWORD read = 0;
        if (!ReadFile(h, buf + total, size - total, &read, NULL) || read == 0)
        {
            free(buf);
            CloseHandle(h);
            return NULL;
        }
        total += read;
    }
    buf[total] = '\0';
    CloseHandle(h);
    return buf;
}

/* 提取顶层字符串字段的值；字段不存在或格式不符返回 FALSE。 */
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
    while (*p && *p != '"' && i < value_size - 1)
        value[i++] = *p++;
    value[i] = '\0';
    return TRUE;
}

/* 提取顶层布尔字段的值；字段不存在或格式不符返回 FALSE。 */
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

/* ================================================== 控制台管理 ================================================== */

/* 分配控制台并重定向 stdout/stderr（set.json 中 console=true 时）。 */
static void
show_console (void)
{
    if (!AllocConsole()) return;
    FILE *fp;
    fp = freopen("CONOUT$", "w", stdout);
    fp = freopen("CONOUT$", "w", stderr);
    (void)fp;
    setvbuf(stdout, NULL, _IONBF, 0);
    setvbuf(stderr, NULL, _IONBF, 0);
}

/* 隐藏继承的控制台窗口及任务栏图标（默认模式，控制台子系统进程启动时存在）。 */
static void
hide_console (void)
{
    HWND hwnd = GetConsoleWindow();
    if (!hwnd) return;
    SetWindowLongW(hwnd, GWL_EXSTYLE, GetWindowLongW(hwnd, GWL_EXSTYLE) | WS_EX_TOOLWINDOW);
    SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                 SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    ShowWindow(hwnd, SW_HIDE);
    FreeConsole();
}

/* ================================================== Windows 版本检测 ================================================== */

typedef LONG (WINAPI *fn_RtlGetVersion)(PRTL_OSVERSIONINFOW);

/* 校验系统版本：Vista 及更早报错退出，Win7 弹实验性支持警告。 */
static void
check_windows_version (void)
{
    /* 动态获取 RtlGetVersion 而非 GetVersionEx，避免其受 manifest 影响的版本谎报 */
    HMODULE ntdll = GetModuleHandleW(L"ntdll.dll");
    if (!ntdll) return;

    /* 经 (void*) 中转避免 GCC 的 -Wcast-function-type 告警 */
    fn_RtlGetVersion pRtlGetVersion = (fn_RtlGetVersion)(void *)
        GetProcAddress(ntdll, "RtlGetVersion");
    if (!pRtlGetVersion) return;

    RTL_OSVERSIONINFOW ver = { .dwOSVersionInfoSize = sizeof(ver) };
    if (pRtlGetVersion(&ver) != 0) return;

    debug_log("[系统] Windows %lu.%lu Build %lu",
              ver.dwMajorVersion, ver.dwMinorVersion, ver.dwBuildNumber);

    if (ver.dwMajorVersion < 6 ||
        (ver.dwMajorVersion == 6 && ver.dwMinorVersion == 0))
    {
        WCHAR msg[256];
        swprintf_s(msg, ARRAY_LEN(msg),
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

/* ================================================== 单实例检测 ================================================== */

/* 互斥体句柄，进程存活期间保持不释放（进程退出时由系统释放，互斥体随之失效）。
 * 命名使用 Local\ 前缀限定当前登录会话，避免跨 RDP/服务会话误判。 */
static HANDLE single_instance_mutex = NULL;

/* 检测是否已有氢风实例在运行；已有则弹提示框并返回 TRUE（调用方应退出）。
 * 创建失败（罕见）时返回 FALSE 放行，避免误阻止正常启动。 */
static BOOL
check_single_instance (void)
{
    single_instance_mutex = CreateMutexW(NULL, TRUE, MUTEX_NAME);
    if (single_instance_mutex == NULL)
        return FALSE;
    if (GetLastError() == ERROR_ALREADY_EXISTS)
    {
        show_notice(L"氢风已经在运行中了。\n\n请先关闭已运行的氢风窗口，再重新启动。");
        return TRUE;
    }
    return FALSE;
}

/* ================================================== Java 查找 ================================================== */

/* 运行 java.exe -version 并解析主版本号，返回是否满足最低要求。
 * 先限时等待进程退出再读输出，避免 java.exe 异常挂起导致启动器永久阻塞。 */
static BOOL
check_java_version (LPCWSTR java_exe)
{
    HANDLE hStdoutRead, hStdoutWrite;
    SECURITY_ATTRIBUTES sa = { sizeof(sa), NULL, TRUE };
    if (!CreatePipe(&hStdoutRead, &hStdoutWrite, &sa, 4096))
        return FALSE;
    SetHandleInformation(hStdoutRead, HANDLE_FLAG_INHERIT, HANDLE_FLAG_INHERIT);

    WCHAR cmd[MAX_PATH + 32];
    swprintf_s(cmd, ARRAY_LEN(cmd), L"\"%s\" -version", java_exe);

    STARTUPINFOW si = { .cb = sizeof(si), .dwFlags = STARTF_USESTDHANDLES,
                        .hStdOutput = hStdoutWrite, .hStdError = hStdoutWrite };
    PROCESS_INFORMATION pi;
    if (!CreateProcessW(NULL, cmd, NULL, NULL, TRUE, CREATE_NO_WINDOW,
                        NULL, NULL, &si, &pi))
    {
        CloseHandle(hStdoutWrite);
        CloseHandle(hStdoutRead);
        return FALSE;
    }
    CloseHandle(hStdoutWrite);

    /* 先等进程结束、写端关闭后再读，读取不会阻塞；超时则终止避免挂起 */
    if (WaitForSingleObject(pi.hProcess, 5000) != WAIT_OBJECT_0)
    {
        TerminateProcess(pi.hProcess, 1);
        WaitForSingleObject(pi.hProcess, INFINITE);
        CloseHandle(pi.hProcess);
        CloseHandle(pi.hThread);
        CloseHandle(hStdoutRead);
        return FALSE;
    }

    char buf[4096] = {0};
    DWORD read;
    if (ReadFile(hStdoutRead, buf, sizeof(buf) - 1, &read, NULL))
        buf[read] = '\0';

    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    CloseHandle(hStdoutRead);

    debug_log("[Java] 版本输出: %s", buf);

    /* 解析版本号："17.0.1"，或 Java 8 及更早的 "1.8.0" */
    char *p = strchr(buf, '"');
    if (!p) return FALSE;
    p++;
    int major = 0;
    if (sscanf_s(p, "%d", &major) != 1) return FALSE;
    if (major == 1)
    {
        p = strchr(p, '.');
        if (p) sscanf_s(p + 1, "%d", &major);
    }
    return major >= JAVA_MIN_MAJOR;
}

/* 若 jdk_dir 下存在 bin\java.exe 或 jre\bin\java.exe，把路径写入 java_path 并返回 TRUE */
static BOOL
java_in_jdk_dir (LPCWSTR jdk_dir, LPWSTR java_path, DWORD java_path_chars)
{
    WCHAR path[MAX_PATH];
    PathCombineW(path, jdk_dir, L"bin\\java.exe");
    if (GetFileAttributesW(path) != INVALID_FILE_ATTRIBUTES)
    {
        lstrcpynW(java_path, path, (int)java_path_chars);
        return TRUE;
    }
    PathCombineW(path, jdk_dir, L"jre\\bin\\java.exe");
    if (GetFileAttributesW(path) != INVALID_FILE_ATTRIBUTES)
    {
        lstrcpynW(java_path, path, (int)java_path_chars);
        return TRUE;
    }
    return FALSE;
}

/* 优先 set.json 中 jre 字段指定目录，其次通配查找 lib 下任意 jre* 目录 */
static BOOL
find_bundled_jre (LPCWSTR base_dir, LPWSTR java_path, DWORD java_path_chars)
{
    WCHAR config_path[MAX_PATH];
    PathCombineW(config_path, base_dir, CONFIG_FILE);

    char *json = read_file_to_heap(config_path);
    if (json)
    {
        char jre_name[64] = {0};
        if (json_get_string(json, "jre", jre_name, sizeof(jre_name)))
        {
            WCHAR wname[64];
            MultiByteToWideChar(CP_UTF8, 0, jre_name, -1, wname, ARRAY_LEN(wname));
            swprintf_s(java_path, java_path_chars,
                       L"%s\\lib\\%s\\bin\\java.exe", base_dir, wname);
            if (GetFileAttributesW(java_path) != INVALID_FILE_ATTRIBUTES)
            {
                free(json);
                return TRUE;
            }
        }
        free(json);
    }

    WCHAR pattern[MAX_PATH];
    swprintf_s(pattern, ARRAY_LEN(pattern), L"%s\\lib\\jre*\\bin\\java.exe", base_dir);
    WIN32_FIND_DATAW ffd;
    HANDLE hFind = FindFirstFileW(pattern, &ffd);
    if (hFind != INVALID_HANDLE_VALUE)
    {
        FindClose(hFind);
        swprintf_s(java_path, java_path_chars,
                   L"%s\\lib\\%s\\bin\\java.exe", base_dir, ffd.cFileName);
        return TRUE;
    }
    return FALSE;
}

/* 依次在 JAVA_HOME、PATH、Program Files 常见厂商目录中查找 java.exe */
static BOOL
find_system_java (LPWSTR java_path, DWORD java_path_chars)
{
    WCHAR buf[MAX_PATH];

    /* 1. JAVA_HOME */
    if (GetEnvironmentVariableW(L"JAVA_HOME", buf, ARRAY_LEN(buf)) > 0 &&
        java_in_jdk_dir(buf, java_path, java_path_chars))
        return TRUE;

    /* 2. PATH 各条目（可能是 JDK 根目录，也可能是 bin 目录本身） */
    WCHAR env_path[PATH_BUF];
    DWORD len = GetEnvironmentVariableW(L"PATH", env_path, ARRAY_LEN(env_path));
    if (len > 0 && len < ARRAY_LEN(env_path))
    {
        LPWSTR ctx = NULL;
        LPWSTR tok = wcstok_s(env_path, L";", &ctx);
        while (tok)
        {
            if (java_in_jdk_dir(tok, java_path, java_path_chars))
                return TRUE;
            PathCombineW(buf, tok, L"java.exe");
            if (GetFileAttributesW(buf) != INVALID_FILE_ATTRIBUTES)
            {
                lstrcpynW(java_path, buf, (int)java_path_chars);
                return TRUE;
            }
            tok = wcstok_s(NULL, L";", &ctx);
        }
    }

    /* 3. Program Files 常见 JDK 厂商安装目录 */
    static const LPCWSTR vendors[] = {
        L"Eclipse Adoptium", L"AdoptOpenJDK", L"Java",
        L"Amazon Corretto", L"BellSoft", L"LibericaJDK"
    };
    for (int pf = 0; pf < 2; pf++)
    {
        LPCWSTR pf_var = (pf == 0) ? L"ProgramFiles" : L"ProgramFiles(x86)";
        if (GetEnvironmentVariableW(pf_var, buf, ARRAY_LEN(buf)) <= 0)
            continue;

        for (int v = 0; v < (int)ARRAY_LEN(vendors); v++)
        {
            WCHAR vendor_dir[MAX_PATH];
            PathCombineW(vendor_dir, buf, vendors[v]);
            if (GetFileAttributesW(vendor_dir) == INVALID_FILE_ATTRIBUTES)
                continue;

            WCHAR search[MAX_PATH];
            swprintf_s(search, ARRAY_LEN(search), L"%s\\*", vendor_dir);
            WIN32_FIND_DATAW ffd;
            HANDLE hFind = FindFirstFileW(search, &ffd);
            if (hFind == INVALID_HANDLE_VALUE)
                continue;
            do
            {
                if (ffd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
                {
                    /* 跳过 "." ".."，避免把厂商根目录或上级目录当作 JDK 检查 */
                    if (wcscmp(ffd.cFileName, L".") == 0 ||
                        wcscmp(ffd.cFileName, L"..") == 0)
                        continue;
                    WCHAR jdk_dir[MAX_PATH];
                    PathCombineW(jdk_dir, vendor_dir, ffd.cFileName);
                    if (java_in_jdk_dir(jdk_dir, java_path, java_path_chars))
                    {
                        FindClose(hFind);
                        return TRUE;
                    }
                }
            } while (FindNextFileW(hFind, &ffd));
            FindClose(hFind);
        }
    }
    return FALSE;
}

/* ================================================== JAR 查找 ================================================== */

/* 在 lib\jar 下按模式查找游戏主程序 JAR；找不到则报错退出。 */
static void
find_jar (LPCWSTR base_dir, LPWSTR jar_path, DWORD jar_path_chars)
{
    WCHAR pattern[MAX_PATH];
    swprintf_s(pattern, ARRAY_LEN(pattern),
               L"%s\\%s\\%s", base_dir, JAR_DIR, JAR_PATTERN);

    WIN32_FIND_DATAW ffd;
    HANDLE hFind = FindFirstFileW(pattern, &ffd);
    if (hFind == INVALID_HANDLE_VALUE)
        show_error(L"未找到游戏主程序 (qingfeng-*.jar)\n请从游戏官网重新下载安装包。");

    FindClose(hFind);
    swprintf_s(jar_path, jar_path_chars,
               L"%s\\%s\\%s", base_dir, JAR_DIR, ffd.cFileName);
}

/* ================================================== 游戏进程启动 ================================================== */

/* 返回命令行中本程序名称之后的部分（指向 GetCommandLineW 的静态缓冲区，无需释放）。
 * 例如经文件关联打开 .qfg 时，这里携带文件路径参数。 */
static LPCWSTR
get_extra_args (void)
{
    LPCWSTR p = GetCommandLineW();
    if (*p == L'"')
    {
        p++;
        while (*p && *p != L'"') p++;
        if (*p) p++;
    }
    else
    {
        while (*p && *p != L' ') p++;
    }
    while (*p == L' ') p++;
    return p;
}

/* 读取管道直到 EOF（Java 退出时写端关闭）。前 max_store 字节存入 out 并补 \0，
 * 超出部分读取后丢弃，防止子进程因写阻塞而无法退出（匿名管道经典死锁）。 */
static void
drain_pipe (HANDLE hPipe, char *out, DWORD max_store, DWORD *stored)
{
    DWORD total = 0;
    while (TRUE)
    {
        DWORD n = 0;
        if (total < max_store)
        {
            if (!ReadFile(hPipe, out + total, max_store - total, &n, NULL) || n == 0)
                break;
            total += n;
        }
        else
        {
            char sink[4096];
            if (!ReadFile(hPipe, sink, sizeof(sink), &n, NULL) || n == 0)
                break;
        }
    }
    out[total < max_store ? total : max_store] = '\0';
    if (stored)
        *stored = total;
}

/* 启动游戏进程并等待其退出。
 * capture_output 为 TRUE 时把 Java 输出收集到 output_buf（非控制台模式）。
 * 返回进程退出码；启动失败时弹窗并终止启动器。 */
static DWORD
run_java (LPWSTR cmd_line, LPCWSTR work_dir, BOOL capture_output,
          char *output_buf, DWORD output_size)
{
    HANDLE hPipeRead = NULL, hPipeWrite = NULL;
    BOOL pipe_ok = FALSE;
    if (capture_output)
    {
        SECURITY_ATTRIBUTES sa = { sizeof(sa), NULL, TRUE };
        pipe_ok = CreatePipe(&hPipeRead, &hPipeWrite, &sa, 65536);
        if (pipe_ok)
            SetHandleInformation(hPipeRead, HANDLE_FLAG_INHERIT, HANDLE_FLAG_INHERIT);
    }

    STARTUPINFOW si = { .cb = sizeof(si) };
    PROCESS_INFORMATION pi = {0};
    if (pipe_ok)
    {
        si.dwFlags = STARTF_USESTDHANDLES;
        si.hStdOutput = hPipeWrite;
        si.hStdError  = hPipeWrite;
    }

    if (!CreateProcessW(NULL, cmd_line, NULL, NULL,
                        pipe_ok ? TRUE : FALSE,
                        capture_output ? CREATE_NO_WINDOW : 0,
                        NULL, work_dir, &si, &pi))
    {
        DWORD err = GetLastError();
        if (pipe_ok)
        {
            CloseHandle(hPipeWrite);
            CloseHandle(hPipeRead);
        }
        debug_log("[启动] CreateProcessW 失败，错误码: %lu", err);
        WCHAR msg[MAX_PATH + 128];
        swprintf_s(msg, ARRAY_LEN(msg),
                   L"启动 Java 失败（错误码：%lu）。\n\n"
                   L"这可能是因为当前系统环境不满足 Java 21 的运行要求。\n"
                   L"建议安装所有系统更新，若问题依旧请升级至 Windows 10 或更高版本。",
                   err);
        show_error(msg);
    }
    debug_log("[启动] CreateProcessW: 成功（PID: %lu）", pi.dwProcessId);

    if (pipe_ok)
        CloseHandle(hPipeWrite);

    /* 边运行边读管道，等 Java 退出后写端关闭、读返回 EOF */
    DWORD total = 0;
    if (pipe_ok && output_buf && output_size > 0)
        drain_pipe(hPipeRead, output_buf, output_size - 1, &total);

    WaitForSingleObject(pi.hProcess, INFINITE);

    DWORD exit_code = 0;
    GetExitCodeProcess(pi.hProcess, &exit_code);

    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    if (pipe_ok)
        CloseHandle(hPipeRead);

    debug_log("[退出] Java 退出码: %lu", exit_code);
    debug_log("[退出] 管道输出字节数: %lu", total);
    return exit_code;
}

/* ================================================== 主函数 ================================================== */

int WINAPI
WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance,
         LPSTR lpCmdLine, int nCmdShow)
{
    (void)hInstance; (void)hPrevInstance; (void)lpCmdLine; (void)nCmdShow;

    /* 0. 单实例检测：已有氢风在运行则提示并退出 */
    if (check_single_instance())
        return 1;

    /* 1. 获取程序基础目录并切换工作目录 */
    WCHAR base_dir[MAX_PATH];
    get_base_dir(base_dir, ARRAY_LEN(base_dir));
    SetCurrentDirectoryW(base_dir);

    /* 2. 初始化调试日志 */
    {
        SYSTEMTIME st;
        GetLocalTime(&st);
        debug_log("");
        debug_log("=== 氢风启动器调试日志 ===");
        debug_log("时间: %04d-%02d-%02d %02d:%02d:%02d",
                  st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
    }
    debug_logW("[路径] 程序目录", base_dir);

    /* 3. 读取配置（console 显隐、jre 路径）；set.json 缺失/解析失败时安全回退默认值 */
    WCHAR config_path[MAX_PATH];
    PathCombineW(config_path, base_dir, CONFIG_FILE);

    BOOL console_mode = FALSE;
    char *json = read_file_to_heap(config_path);
    if (json)
    {
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
        char cfg_utf8[1024];
        WideCharToMultiByte(CP_UTF8, 0, config_path, -1,
                            cfg_utf8, sizeof(cfg_utf8), NULL, NULL);
        debug_log("[配置] set.json: 不存在或无法读取（路径: %s）", cfg_utf8);
        debug_log("[配置] console: false（默认值，因 set.json 缺失）");
    }

    /* 4. 控制台管理 */
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

    /* 5. Windows 版本检测 */
    check_windows_version();

    /* 6. 查找 Java：优先内置 JRE，不可用则回退系统 Java */
    WCHAR java_exe[MAX_PATH];
    BOOL found = find_bundled_jre(base_dir, java_exe, ARRAY_LEN(java_exe));

    if (found && check_java_version(java_exe))
    {
        debug_log("[Java] 来源: 内置 JRE");
        debug_logW("[Java] 路径", java_exe);
        debug_log("[Java] 版本检查: 通过");
    }
    else
    {
        if (found)
            debug_logW("[Java] 内置 JRE 版本检查失败", java_exe);
        else
            debug_log("[Java] 内置 JRE: 未找到");

        if (find_system_java(java_exe, ARRAY_LEN(java_exe)))
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
                swprintf_s(msg, ARRAY_LEN(msg),
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

    /* 7. 查找游戏 JAR */
    WCHAR jar_file[MAX_PATH];
    find_jar(base_dir, jar_file, ARRAY_LEN(jar_file));
    debug_logW("[JAR] 文件", jar_file);

    /* 8. 构建命令行并启动 Java（run_java 内含等待退出与输出捕获） */
    LPCWSTR extra = get_extra_args();
    WCHAR cmd_line[CMD_BUF];
    if (*extra)
        swprintf_s(cmd_line, ARRAY_LEN(cmd_line),
                   L"\"%s\" -jar \"%s\" %s", java_exe, jar_file, extra);
    else
        swprintf_s(cmd_line, ARRAY_LEN(cmd_line),
                   L"\"%s\" -jar \"%s\"", java_exe, jar_file);

    {
        char cmd_utf8[8192], dir_utf8[1024];
        WideCharToMultiByte(CP_UTF8, 0, cmd_line, -1, cmd_utf8, sizeof(cmd_utf8), NULL, NULL);
        WideCharToMultiByte(CP_UTF8, 0, base_dir, -1, dir_utf8, sizeof(dir_utf8), NULL, NULL);
        debug_log("[启动] 命令行: %s", cmd_utf8);
        debug_log("[启动] 工作目录: %s", dir_utf8);
    }
    debug_log("[启动] 创建标志: %s", console_mode ? "0（继承控制台）" : "CREATE_NO_WINDOW");
    debug_log("[启动] 管道: %s", console_mode ? "未使用" : "已创建");

    char output_buf[CAPTURE_BUF] = {0};
    DWORD exit_code = run_java(cmd_line, base_dir, !console_mode,
                               output_buf, sizeof(output_buf));

    /* 9. 若 Java 异常退出，弹窗显示错误输出和退出码 */
    if (exit_code != 0)
    {
        WCHAR detail[REPORT_BUF];
        if (output_buf[0])
            MultiByteToWideChar(CP_UTF8, 0, output_buf, -1, detail, ARRAY_LEN(detail));
        else
            wcscpy_s(detail, ARRAY_LEN(detail), L"（无详细错误信息）");

        WCHAR msg[REPORT_BUF];
        swprintf_s(msg, ARRAY_LEN(msg),
                   L"Java 运行时遇到问题（退出码：%lu）。\n\n"
                   L"错误信息：\n%s\n\n"
                   L"这可能是当前系统环境不满足运行条件，\n"
                   L"建议安装所有系统更新后重试，或升级至 Windows 10 或更高版本。",
                   exit_code, detail);
        show_error(msg);
    }

    return 0;
}
