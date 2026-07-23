@REM 编译原生 C 启动器（替换 PyInstaller，消除 Win7 兼容问题）
@REM 自动检测:
@REM   1. PATH 中的 x86_64-w64-mingw32-gcc
@REM   2. w64devkit（C:\tools\w64devkit\w64devkit\bin\）
@REM   3. CLion 捆绑的 MinGW
@REM   4. 若未安装，运行: winget install BrechtSanders.WinLibs.POSIX.MSVCRT

@echo off
setlocal enabledelayedexpansion

set "GCC="
set "GCCDIR="

rem 1. 检查 PATH 中的 x86_64-w64-mingw32-gcc
where x86_64-w64-mingw32-gcc >nul 2>&1
if %errorlevel% equ 0 (
    set "GCC=x86_64-w64-mingw32-gcc"
    goto :compile
)

rem 2. 检查 w64devkit
if exist "C:\tools\w64devkit\w64devkit\bin\gcc.exe" (
    set "GCC=C:\tools\w64devkit\w64devkit\bin\gcc.exe"
    set "GCCDIR=C:\tools\w64devkit\w64devkit\bin"
    goto :compile
)

rem 3. 检查 CLion 捆绑的 MinGW
for /d %%d in ("%ProgramFiles%\JetBrains\CLion*") do (
    if exist "%%d\bin\mingw\bin\gcc.exe" (
        set "GCC=%%d\bin\mingw\bin\gcc.exe"
        set "GCCDIR=%%d\bin\mingw\bin"
        goto :compile
    )
)

rem 4. 检查 PATH 中的 gcc
where gcc >nul 2>&1
if %errorlevel% equ 0 (
    set "GCC=gcc"
    goto :compile
)

echo [错误] 未找到 MinGW-w64 gcc 编译器。
echo.
echo 快速安装（推荐 w64devkit，仅 50MB）:
echo   1. 下载 https://github.com/skeeto/w64devkit/releases
echo   2. 解压到 C:\tools\w64devkit
echo.
echo 或使用 winget（262MB 完整版）:
echo   winget install BrechtSanders.WinLibs.POSIX.MSVCRT
pause
exit /b 1

:compile
rem gcc 需要 bin 目录在 PATH 中才能找到 as/ld 等子工具
if not "%GCCDIR%"=="" (
    set "PATH=%GCCDIR%;%PATH%"
)

echo [信息] 使用 GCC: %GCC%

rem 编译 .rc 资源（嵌入 console.ico）
set "RC_OBJ="
if exist "launcher.rc" if not "%GCCDIR%"=="" (
    "%GCCDIR%\windres.exe" -O coff launcher.rc launcher_res.o
    if errorlevel 1 (
        echo [警告] 图标资源编译失败，继续不带图标
    ) else (
        set "RC_OBJ=launcher_res.o"
        echo [信息] 图标资源已编译
    )
)

"%GCC%" -O2 -s -static -mwindows -D_WIN32_WINNT=0x0601 -D_WIN32_IE=0x0601 -o dist\launcher\launcher.exe launcher.c %RC_OBJ% -lshlwapi
if %errorlevel% neq 0 (
    echo [错误] 编译失败
    pause
    exit /b 1
)
echo [成功] launcher.exe 编译完成（已嵌入图标）
pause
