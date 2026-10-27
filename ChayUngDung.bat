@echo off
rem Chay ung dung Vintage Books (Java Swing + SQL Server): bam dup file nay.
rem Can: Java JDK (co javac) va SQL Server dang chay; thong tin ket noi nam trong db.properties.
setlocal
cd /d "%~dp0"

rem ---- tim JDK: JAVA_HOME, roi java trong PATH, roi JDK di kem Apache NetBeans
set "JAVA_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVA_BIN=%JAVA_HOME%\bin"
if not defined JAVA_BIN (
    for /f "delims=" %%I in ('where javac 2^>nul') do if not defined JAVA_BIN set "JAVA_BIN=%%~dpI"
)
if not defined JAVA_BIN if exist "C:\Program Files\Apache NetBeans\jdk\bin\javac.exe" set "JAVA_BIN=C:\Program Files\Apache NetBeans\jdk\bin"
if not defined JAVA_BIN (
    echo Khong tim thay JDK. Hay cai JDK hoac mo du an bang Apache NetBeans.
    pause
    exit /b 1
)

rem ---- thu vien: cac jar trong lib va driver SQL Server (mssql-jdbc)
set "JDBC="
for %%F in (lib\mssql-jdbc*.jar) do set "JDBC=%%F"
if not defined JDBC if exist "C:\sqljdbc_13.4\enu\jars\mssql-jdbc-13.4.0.jre11.jar" set "JDBC=C:\sqljdbc_13.4\enu\jars\mssql-jdbc-13.4.0.jre11.jar"
if not defined JDBC (
    echo Khong tim thay driver SQL Server mssql-jdbc. Hay copy file mssql-jdbc-*.jar vao thu muc lib.
    pause
    exit /b 1
)
set "CP=build\run;lib\*;%JDBC%"

if not exist db.properties (
    echo Chua co file db.properties. Hay copy db.properties.example thanh db.properties roi dien thong tin SQL Server.
    pause
    exit /b 1
)

rem ---- bien dich ma nguon moi nhat vao build\run (khong dung toi build\classes cua NetBeans)
if not exist build mkdir build
if not exist build\run mkdir build\run
dir /s /b src\*.java > build\sources_run.txt
echo Dang bien dich...
"%JAVA_BIN%\javac.exe" -encoding UTF-8 -nowarn -cp "lib\*;%JDBC%" -d build\run @build\sources_run.txt
if errorlevel 1 (
    echo Bien dich that bai, xem loi o tren.
    pause
    exit /b 1
)
xcopy /e /i /y /q src\fonts build\run\fonts >nul

echo Dang mo ung dung...
start "" "%JAVA_BIN%\javaw.exe" -cp "%CP%" view.customer.CustomerHomeFrm
endlocal
