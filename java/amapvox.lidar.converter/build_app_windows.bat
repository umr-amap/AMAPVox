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
set PROJECT_VERSION=2.0.0
set MAIN_JAR=LidarConverter-%PROJECT_VERSION%.jar

echo %JAVA_HOME% %JAVA_VERSION% %PROJECT_VERSION% %MAIN_JAR%

rem Set desired installer type: "app-image" "msi" "exe".
set INSTALLER_TYPE=msi

rem ------ SETUP DIRECTORIES AND FILES ----------------------------------------
rem Remove previously generated java runtime and installers. Copy all required
rem jar files into the input/libs folder.

IF EXIST target\jre rmdir /S /Q  .\target\jre
IF EXIST target\installer rmdir /S /Q target\installer

xcopy /S /Q target\release\* target\installer\input\

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
  --class-path "target/release/lib/*" ^
  --print-module-deps target\release\%MAIN_JAR% > temp.txt

set /p detected_modules=<temp.txt
rm temp.txt

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
  --strip-native-commands ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2 ^
  --strip-debug ^
  --add-modules %detected_modules%%manual_modules% ^
  --output target/jre
  

rem ------ PACKAGING ----------------------------------------------------------
rem In the end we will find the package inside the target/installer directory.

call "%JAVA_HOME%\bin\jpackage" ^
  --type %INSTALLER_TYPE% ^
  --dest target/installer ^
  --input target/installer/input ^
  --name LidarConverter ^
  --main-class fr.umramap.lidar.converter.MainApp ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xmx2048m ^
  --runtime-image target/jre ^
  --icon src/main/resources/fr/umramap/lidar/converter/icons/lidar-converter.ico ^
  --app-version %PROJECT_VERSION% ^
  --vendor "IRD" ^
  --copyright "Copyright © 2022 ird.fr"
  --win-dir-chooser ^
  --win-shortcut ^
  --win-per-user-install ^
  --win-menu