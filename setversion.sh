#!/bin/bash

# go to maven directory
cd java

# current version
CURRENT_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
# current version without snapshot
CURRENT_VERSION_NONSNAPSHOT=${CURRENT_VERSION/-SNAPSHOT/}
echo "Current version $CURRENT_VERSION_NONSNAPSHOT"
# update maven version
mvn versions:set -q -DgenerateBackupPoms=false
# updated version
UPDATED_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
# version without snaphot
UPDATED_VERSION_NONSNAPSHOT=${UPDATED_VERSION/-SNAPSHOT/}
echo "Updated version $UPDATED_VERSION_NONSNAPSHOT"

# update windows build script
sed -i "s/$CURRENT_VERSION_NONSNAPSHOT/$UPDATED_VERSION_NONSNAPSHOT/" amapvox.main/build_app_windows.bat

cd ..
# update DESCRIPTION file
sed -i "s/$CURRENT_VERSION_NONSNAPSHOT/$UPDATED_VERSION_NONSNAPSHOT/" DESCRIPTION

# update NEWS.md
RELEASE="# AMAPVox $UPDATED_VERSION_NONSNAPSHOT

*yyyy-MM-dd*

Released/Not release on CRAN

"
echo "$(echo -n "$RELEASE"; cat NEWS.md)" > NEWS.md
# EOF
