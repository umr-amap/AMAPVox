# AMAPVox Java code

This subfolder contains AMAPVox java code. 

## Compilation

Requirements:
- [maven](https://maven.apache.org/)
- [Liberica Full JDK 25](https://bell-sw.com/pages/downloads/#jdk-25-lts) (JDK + LibericaFX, their own implementation of OpenJFX)

From `./java` folder: 
- Compile with tests (default): `mvn clean install`
- Compile without tests (faster): `mvn clean install -DskipTests`

## Run

From `./java` folder:

`mvn -f amapvox.gui javafx:run` (the 3d viewer is not expected to work when running AMAPVox with the maven javafx plugin)

## Deploy

Generate package:
- linux: `mvn clean install -P build-linux` (from linux system only)
- windows: `mvn clean install -P build-windows` (from windows system only)

Generated packages will be found in `java/amapvox.main/target/AMAPVox-x.y.z-{linux|windows}.zip`

Upload packages to [Package registry](https://forge.ird.fr/amap/amapvox/AMAPVox/-/packages): 
```bash
curl --header "PRIVATE-TOKEN: *************" \
     --upload-file /path/to/AMAPVox-x.y.z-linux.zip \
     "https://forge.ird.fr/api/v4/projects/819/packages/generic/amapvox/x.y.z/AMAPVox-x.y.z-{linux|windows}.zip"
```

## Versions

Project version: use dedicated script in main folder `./setversion.sh`

Update dependencies: `mvn versions:display-dependency-updates`

Update plugins: `mvn versions:display-plugin-updates`

