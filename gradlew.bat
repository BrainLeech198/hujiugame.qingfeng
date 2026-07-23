@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem === Auto-provision JDK 21+ from TUNA mirror (清华镜像) ===
set "JDK_DIR=%APP_HOME%\.jdk"
set "HAS_JAVA="

@rem Jump over helper subroutine
goto :start_provision

@rem Helper: check if a java executable is version 21+
:check_java_version
@rem %1 = path to java.exe, sets HAS_JAVA=1 if >= 21
set "JAVA_MAJOR=0"
set "JAVA_VER="
for /f "tokens=3" %%v in ('"%~1" -version 2^>^&1 ^| findstr /i version') do set "JAVA_VER=%%v"
if not defined JAVA_VER goto :eof
set "JAVA_VER=%JAVA_VER:"=%"
if not defined JAVA_VER goto :eof
for /f "tokens=1 delims=." %%v in ("%JAVA_VER%") do set "JAVA_MAJOR=%%v"
if %JAVA_MAJOR% GEQ 21 set "HAS_JAVA=1"
goto :eof

:start_provision
@rem 1) Check JAVA_HOME
if defined JAVA_HOME (
    call :check_java_version "%JAVA_HOME%/bin/java"
)
@rem 2) Check java on PATH
if not defined HAS_JAVA call :check_java_version "java"
@rem 3) Check .jdk directory (JDK extracted in a subfolder like jdk-17.0.19+10)
if not defined HAS_JAVA (
    for /d %%d in ("%JDK_DIR%\jdk-*") do (
        call :check_java_version "%%d\bin\java"
        if defined HAS_JAVA set "JAVA_HOME=%%d"
    )
)
@rem 4) Auto-download JDK 21 from TUNA mirror if still not found
if defined HAS_JAVA goto :after_provision
echo.
echo [qingfeng] JDK 21+ not found, downloading from TUNA mirror (清华镜像)...
mkdir "%JDK_DIR%" 2>nul
echo This may take a while (~180MB).
powershell -NoProfile -ExecutionPolicy Bypass -Command "$url='https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/windows/OpenJDK21U-jdk_x64_windows_hotspot_21.0.11_10.zip'; $zip='%JDK_DIR%\jdk.zip'; Write-Host 'Downloading...'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing -TimeoutSec 300 } catch { Write-Host 'Failed: '+$_.Exception.Message; exit 1 }; Write-Host 'Extracting...'; Expand-Archive -Path $zip -DestinationPath '%JDK_DIR%' -Force; Remove-Item $zip"
if errorlevel 1 (
    echo [qingfeng] Failed to download JDK automatically.
    echo Please install JDK 21+ manually and set JAVA_HOME.
    pause
    exit /b 1
)
@rem Find extracted JDK folder (e.g. jdk-21.0.11+10)
for /d %%d in ("%JDK_DIR%\jdk-*") do set "JAVA_HOME=%%d"
echo JDK 21 ready.

:after_provision

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega