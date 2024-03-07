@ECHO OFF

rem ------ ENVIRONMENT --------------------------------------------------------
rem The script depends on various environment variables to exist in order to
rem run properly. The java version we want to use, the location of the java
rem binaries (java home), and the project version as defined inside the pom.xml
rem file, e.g. 1.0-SNAPSHOT.
rem
rem PROJECT_VERSION: version used in pom.xml, e.g. 1.0-SNAPSHOT
rem APP_VERSION: the application version, e.g. 1.0.0, shown in "about" dialog

set JAVA_VERSION=17
set PROJECT_VERSION=2.2.0
set FINAL_NAME=AMAPVox-%PROJECT_VERSION%
set MAIN_JAR=%FINAL_NAME%.jar

echo java home: %JAVA_HOME%
echo java version: %JAVA_VERSION%
echo project version: %PROJECT_VERSION%
echo main JAR file: %MAIN_JAR%

rem Set desired installer type: "app-image" "msi" "exe".
set INSTALLER_TYPE=app-image

rem ------ SETUP DIRECTORIES AND FILES ----------------------------------------
rem Remove previously generated java runtime and installers. Copy all required
rem jar files into the input/libs folder.

IF EXIST target\jre rmdir /S /Q  .\target\jre
IF EXIST target\installer rmdir /S /Q target\installer
IF EXIST target\AMAPVox-%PROJECT_VERSION%-windows rmdir /S /Q target\%FINAL_NAME%-windows
IF EXIST target\%FINAL_NAME%-windows.zip DEL target\%FINAL_NAME%-windows.zip

xcopy /S /I /Q target\dependency\* target\installer\input\dependency

rem ------ REQUIRED MODULES ---------------------------------------------------
rem Use jlink to detect all modules that are required to run the application.
rem Starting point for the jdep analysis is the set of jars being used by the
rem application.

echo detecting required modules

"%JAVA_HOME%\bin\jdeps" ^
  -q ^
  --multi-release %JAVA_VERSION% ^
  --recursive ^
  --ignore-missing-deps ^
  --class-path "target/dependency/*" ^
  --print-module-deps target/%MAIN_JAR% > temp.txt

set /p detected_modules=<temp.txt
DEL temp.txt

echo detected modules: %detected_modules%

rem ------ MANUAL MODULES -----------------------------------------------------
rem jdk.crypto.ec has to be added manually bound via --bind-services or
rem otherwise HTTPS does not work.
rem
rem See: https://bugs.openjdk.java.net/browse/JDK-8221674
rem
rem In addition we need jdk.localedata if the application is localized.
rem This can be reduced to the actually needed locales via a jlink parameter,
rem e.g., --include-locales=en,de.
rem
rem Don't forget the leading ','!

rem set manual_modules=,jdk.crypto.ec,jdk.localedata
set manual_modules=
echo manual modules: %manual_modules%

rem ------ RUNTIME IMAGE ------------------------------------------------------
rem Use the jlink tool to create a runtime image for our application. We are
rem doing this in a separate step instead of letting jlink do the work as part
rem of the jpackage tool. This approach allows for finer configuration and also
rem works with dependencies that are not fully modularized, yet.

echo creating java runtime image

rem  --strip-native-commands
call "%JAVA_HOME%\bin\jlink" ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2 ^
  --strip-debug ^
  --add-modules %detected_modules%%manual_modules% ^
  --output target/jre
  

rem ------ PACKAGING ----------------------------------------------------------
rem In the end we will find the package inside the target/installer directory.

echo creating installer of type %INSTALLER_TYPE%

copy target\%MAIN_JAR% target\installer\input\

call "%JAVA_HOME%\bin\jpackage" ^
  --type %INSTALLER_TYPE% ^
  --dest target/installer ^
  --input target/installer/input ^
  --name AMAPVox ^
  --main-class org.amapvox.Main ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xms2048m ^
  --java-options "--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED" ^
  --runtime-image target/jre ^
  --icon src/main/resources/amapvox.ico ^
  --app-version %PROJECT_VERSION% ^
  --vendor "IRD" ^
  --copyright "Copyright © 2023 ird.fr"
rem  --win-dir-chooser ^
rem  --win-shortcut ^
rem  --win-per-user-install ^
rem  --win-menu

echo creating zip target\%FINAL_NAME%-windows.zip

MOVE target\installer\AMAPVox target/%FINAL_NAME%-windows
cd target
jar -cfM %FINAL_NAME%-windows.zip %FINAL_NAME%-windows

