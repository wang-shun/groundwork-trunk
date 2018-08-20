#!/bin/bash
# Copyright 2016 GroundWork, Inc. (GroundWork)
# All rights reserved.  Use is subject to GroundWork commercial license terms.
#
# change_portal_timout.sh -- This is a utility script to update the portal timeout
# in several war files used in the GroundWork application.

#set our environment
source /usr/local/groundwork/scripts/setenv.sh

#usage
USAGE="$0 -t|--timeout [timeout]"

#array of versions supported by this script
declare -a VERSIONS=("7.0.0" "7.0.1" "7.0.2" "7.1.0" "7.1.1" "7.2.0")

OPTS=$(getopt --options t: \
              --longoptions timeout: \
              --name "change_portal_timout.sh" -- "$@")

backup_directory="/usr/local/groundwork/backup/$(date -I)"
deployment_directory="/usr/local/groundwork/foundation/container/jpp/standalone/deployments"
gatein_directory="/usr/local/groundwork/jpp/gatein/gatein.ear"

while [ $# -gt 0 ]; do
  case "$1" in
    -t | --timeout ) timeout="$2"; shift 2 ;;
    (-*) echo "ERROR! unrecognized option $1" 1>&2; exit 1;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

if [[ ! ${timeout} =~ ^[1-9]{1}[0-9]*$ ]]; then
  echo "ERROR! Timeout must be an integer greater than 0"
  echo "$USAGE"
  exit 1
fi

# select which version ...
#"status-viewer-7.0.2.war" #  for 7.0.2
#"status-viewer-7.1.0.war" # for 7.1.0
#"status-viewer-7.1.1.war" # for 7.1.1

version=$(grep version /usr/local/groundwork/Info.txt | cut -d" " -f2)
if [ -n "${version}" ]; then
  sv_war="status-viewer-${version}.war"
  if [ "${version}" = "7.0.2" ]; then
    result=$(grep "TB7.0.2-3" /usr/local/groundwork/Info.txt)
    is7023=$?
    if [ ${is7023} -eq 0 ]; then
      sv_war="status-viewer-7.1.0-SNAPSHOT.war"
    fi
  fi
else
  echo "ERROR! You must run this from a valid Groundwork installation"
  exit 1
fi
if [[ ! " ${VERSIONS[@]} " =~ " ${version} " ]]; then
  echo "ERROR! Version must be one of the following values: ${VERSIONS[@]}"
  exit 1
fi

tmpdir="/tmp/$0-$$"
rm -rf "${tmpdir}"
mkdir -p "${tmpdir}"
cd "${tmpdir}"
mkdir -p "${backup_directory}"
echo "Backing up files to be updated to ${backup_directory}/portal-timeout-backup.tgz"
tar czf ${backup_directory}/portal-timeout-backup.tgz -C / \
        ${deployment_directory:1}/icefaces-push-server-1.8.2-P06-EE.war \
        ${deployment_directory:1}/nagvis.war \
        ${deployment_directory:1}/portal-groundwork-base.war \
        ${deployment_directory:1}/groundwork-enterprise.ear \
        ${gatein_directory:1}/portal.war

declare -a COMMON=("icefaces-push-server-1.8.2-P06-EE.war" "nagvis.war" "portal-groundwork-base.war")

#edit the WEB-INF/web.xml for each war file
for file in ${COMMON[@]}; do
  echo "Setting session timeout for ${file} component to ${timeout} minutes"
  cp -a "${deployment_directory}/${file}" .
  jar xf "${file}" "WEB-INF/web.xml"
  sed -e "s#<session-timeout>.*</session-timeout>#<session-timeout>${timeout}</session-timeout>#" -i "WEB-INF/web.xml"
  jar uf "${file}" "WEB-INF/web.xml"
  mv -f "${file}" "${deployment_directory}/${file}"
  rm -rf "./*"
done

#portal.war is in the gatein.ear directory
file="portal.war"
echo "Setting session timeout for ${file} component to ${timeout} minutes"
cp -a "${gatein_directory}/${file}" .
jar xf "${file}" "WEB-INF/web.xml"
sed -e "s#<session-timeout>.*</session-timeout>#<session-timeout>${timeout}</session-timeout>#" -i "WEB-INF/web.xml"
jar uf "${file}" "WEB-INF/web.xml"
mv -f "${file}" "${gatein_directory}/${file}"
rm -rf "./*"

#statusviewer war is embeded in the groundwork-enterprise.ear which must be exploded first.
file="${sv_war}"
echo "Setting session timeout for ${file} component to ${timeout} minutes"
cp -a "${deployment_directory}/groundwork-enterprise.ear" .
jar xf "groundwork-enterprise.ear"
if [[ "${version}" = "7.1.1" && -e "status-viewer-7.1.1-SNAPSHOT.war" ]]; then
  file="status-viewer-7.1.1-SNAPSHOT.war"
fi
if [[ "${version}" = "7.2.0" && -e "status-viewer-7.2.0-SNAPSHOT.war" ]]; then
  file="status-viewer-7.2.0-SNAPSHOT.war"
fi
wartmp="status-viewer.war-temp"
mkdir -p "${wartmp}"
cp -a "${file}" "${wartmp}"
cd "${wartmp}"
jar xf "${file}" "WEB-INF/web.xml"
sed  -e "s#<session-timeout>.*</session-timeout>#<session-timeout>${timeout}</session-timeout>#" -i "WEB-INF/web.xml"
jar uf "${file}" "WEB-INF/web.xml"
mv -f "${file}" ..
cd ..
jar uf "groundwork-enterprise.ear" "${file}"
mv -f "groundwork-enterprise.ear" "${deployment_directory}/groundwork-enterprise.ear"
cd "/tmp"
rm -rf "${tmpdir}"

echo ""
echo "Restart gwservices to make the new portal timeout active by running:"
echo "    service groundwork restart gwservices"
echo ""
echo "if you need to revert your changes the original files are stored in '${backup_directory}'"
echo "just untar the file and restart gwservices:"
echo "    tar xvf ${backup_directory}/portal-timeout-backp.tgz -C /"
echo "    service groundwork restart gwservices"
