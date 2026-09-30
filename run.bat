@echo off
setlocal EnableExtensions
cd /d "%~dp0"
set "PROJECT_ROOT=%CD%"
set "TOOLS_INSTALLED=0"

call :ensure_java
if errorlevel 1 goto failed
call :ensure_maven
if errorlevel 1 goto failed
call :ensure_docker
if errorlevel 1 goto failed

if "%TOOLS_INSTALLED%"=="1" goto installed_restart

docker compose version >nul 2>&1
if errorlevel 1 goto docker_compose_missing

docker info >nul 2>&1
if errorlevel 1 goto start_docker
goto docker_ready

:start_docker
if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
    start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
) else if exist "%LOCALAPPDATA%\Programs\Docker\Docker\Docker Desktop.exe" (
    start "" "%LOCALAPPDATA%\Programs\Docker\Docker\Docker Desktop.exe"
) else (
    goto docker_not_running
)
for /l %%I in (1,1,60) do (
    docker info >nul 2>&1
    if not errorlevel 1 goto docker_ready
    timeout /t 2 /nobreak >nul
)
goto docker_not_running

:docker_ready

echo Compiling the Java project and resolving dependencies...
mvn -q -f "%PROJECT_ROOT%\pet_hotel\pom.xml" -DskipTests compile dependency:build-classpath "-Dmdep.outputFile=%PROJECT_ROOT%\pet_hotel\target\runtime-classpath.txt"
if errorlevel 1 goto build_failed

echo Starting PostgreSQL...
docker compose up -d
if errorlevel 1 goto compose_failed

echo Waiting for PostgreSQL to become ready...
for /l %%I in (1,1,30) do (
    docker compose exec -T postgres pg_isready -U postgres -d pet_hotel >nul 2>&1
    if not errorlevel 1 goto database_ready
    timeout /t 2 /nobreak >nul
)
goto database_timeout

:database_ready
echo.
echo Select what to run:
echo 1. Java demo application
echo 2. Create an Excel database snapshot
set /p "RUN_OPTION=Enter 1 or 2: "

set "MAIN_CLASS=ru.mirea.project.util.DatabaseExcelDump"
if "%RUN_OPTION%"=="1" set "MAIN_CLASS=ru.mirea.project.Main"
if not "%RUN_OPTION%"=="1" if not "%RUN_OPTION%"=="2" goto invalid_option

set "CLASSPATH_FILE=%PROJECT_ROOT%\pet_hotel\target\runtime-classpath.txt"
if not exist "%CLASSPATH_FILE%" goto classpath_missing
set /p "DEPENDENCY_CLASSPATH=" < "%CLASSPATH_FILE%"
if not defined DEPENDENCY_CLASSPATH goto classpath_missing

pushd "%PROJECT_ROOT%\pet_hotel"
java -cp "target\classes;%DEPENDENCY_CLASSPATH%" %MAIN_CLASS%
set "JAVA_EXIT_CODE=%ERRORLEVEL%"
popd
if not "%JAVA_EXIT_CODE%"=="0" goto java_failed

echo.
echo Finished successfully.
pause
exit /b 0

:ensure_java
where java >nul 2>&1
if errorlevel 1 set "NEED_JDK=1"
where javac >nul 2>&1
if errorlevel 1 set "NEED_JDK=1"
javac --release 23 -version >nul 2>&1
if errorlevel 1 set "NEED_JDK=1"
if not defined NEED_JDK exit /b 0
echo Installing Eclipse Temurin JDK 25...
call :install_package EclipseAdoptium.Temurin.25.JDK
exit /b %ERRORLEVEL%

:ensure_maven
where mvn >nul 2>&1
if not errorlevel 1 exit /b 0
echo Installing Apache Maven...
call :install_package Apache.Maven
exit /b %ERRORLEVEL%

:ensure_docker
where docker >nul 2>&1
if errorlevel 1 goto install_docker
docker compose version >nul 2>&1
if not errorlevel 1 exit /b 0
:install_docker
echo Installing Docker Desktop with Docker Compose...
call :install_package Docker.DockerDesktop
exit /b %ERRORLEVEL%

:install_package
where winget >nul 2>&1
if errorlevel 1 goto winget_missing
winget install --exact --id %~1 --silent --accept-package-agreements --accept-source-agreements
if errorlevel 1 exit /b 1
set "TOOLS_INSTALLED=1"
exit /b 0

:installed_restart
echo.
echo Required tools were installed. Close this window, then run run.bat again.
echo Docker Desktop may also require a sign-out or system restart after installation.
pause
exit /b 0

:winget_missing
echo Windows Package Manager winget was not found.
echo Install Java JDK 23 or newer, Apache Maven, and Docker Desktop manually.
echo Then run this file again.
pause
exit /b 1

:docker_compose_missing
echo Docker Compose is unavailable. Install or update Docker Desktop, then retry.
goto failed

:docker_not_running
echo Docker is installed but its engine is not running.
echo Start Docker Desktop, wait until it is ready, then run this file again.
goto failed

:build_failed
echo Maven could not compile the project. Check that JDK 23 or newer is active.
goto failed

:compose_failed
echo Docker Compose could not start PostgreSQL.
goto failed

:database_timeout
echo PostgreSQL did not become ready within 60 seconds.
goto failed

:invalid_option
echo Choose 1 or 2 the next time you run this file.
goto failed

:classpath_missing
echo Maven did not create the runtime classpath file.
goto failed

:java_failed
echo The selected Java program returned an error.
goto failed

:failed
echo.
pause
exit /b 1
