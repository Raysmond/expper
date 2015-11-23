#!/bin/bash
# ------------------------------------------------------------------
# [Author] Raysmond
#
#          This script automatically deploy the Spring Boot
#          application to remote server.
#
# Dependency:
#     python
# ------------------------------------------------------------------

# Get application version from build.gradle
version=$(cat build.gradle |  grep "version = \"[0-9\.]")
tmp=${version#*\"}
version=${tmp%\-*}

APP_NAME="expper"
DIST_DIR="build/libs"
REMOTE_SERVER="47.89.25.26"
REMOTE_DIR="/root/apps/expper/releases"
REMOTE_USER="root"

# Automatically increment application version
# For example: 0.1.6 -> 0.1.7
increment_version ()
{
  declare -a part=( ${1//\./ } )
  declare    new
  declare -i carry=1

  for (( CNTR=${#part[@]}-1; CNTR>=0; CNTR-=1 )); do
    len=${#part[CNTR]}
    new=$((part[CNTR]+carry))
    [ ${#new} -gt $len ] && carry=1 || carry=0
    [ $CNTR -gt 0 ] && part[CNTR]=${new: -len} || part[CNTR]=${new}
  done
  new="${part[*]}"
  version="${new// /.}"
}

increment_version $version
sed -i -e "s/version\ =\ .*/version\ =\ \"$version\-SNAPSHOT\"/g" build.gradle

gradle -Pprod clean bootRepackage

# Upload asset files to qiniu cdn storage
qrsync scripts/qiniu.conf

# Upload war
scp $DIST_DIR/$APP_NAME-$version-SNAPSHOT.war $REMOTE_USER@$REMOTE_SERVER:$REMOTE_DIR

# Restart application on remote server
python scripts/restart_hk_server.py $APP_NAME-$version-SNAPSHOT.war
