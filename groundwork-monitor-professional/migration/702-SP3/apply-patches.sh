#!/bin/bash
#
# Applies patch file and scripted patches. Returns 0 status on success
# and a non-zero status if a patch has failed and needs to be manually
# performed or reviewed.
#
# Usage: apply-patches.sh [ <install directory> ]
#
# Where install directory defaults to /usr/local/groundwork.
#

INSTALL_DIR=$1
if [ -z "$INSTALL_DIR" ] ; then
    INSTALL_DIR=/usr/local/groundwork
elif [ "${INSTALL_DIR##/*}" != "" ] ; then
    INSTALL_DIR=$PWD/$INSTALL_DIR
fi
cd $(dirname $0)
PATCHES=$PWD
cd $INSTALL_DIR

STATUS=0
DUAL=$([ -d foundation/container/jpp2 ])$?
echo "Applying patches..."
for PATCH in $PATCHES/*.patch ; do
    # patch target
    TARGETS=($(grep "^[+][+][+] " $PATCH | sed 's/^+++ //;s/\t.*$//'))
    # add/modify additional patch target or skip if dual
    if [ $DUAL -eq 0 ] ; then
        if [ "${TARGETS[0]}" = "foundation/container/jpp/dual-jboss-installer/installed/standalone.xml" ] ; then
            TARGETS[0]=foundation/container/jpp/standalone/configuration/standalone.xml
        elif [ "${TARGETS[0]}" = "foundation/container/jpp/dual-jboss-installer/installed/standalone2.xml" ] ; then
            TARGETS[0]=foundation/container/jpp2/standalone/configuration/standalone.xml
        elif [ "${TARGETS[0]}" = "foundation/container/jpp/standalone/configuration/standalone.xml" ] ; then
            continue
        elif [ "${TARGETS[0]}" = "foundation/container/jpp/standalone/configuration/application-roles.properties" ] ; then
            TARGETS[1]=foundation/container/jpp2/standalone/configuration/application-roles.properties
        elif [ "${TARGETS[0]}" = "foundation/container/jpp/standalone/configuration/application-users.properties" ] ; then
            TARGETS[1]=foundation/container/jpp2/standalone/configuration/application-users.properties
        fi
    fi
    # patch targets
    echo "Applying patch: ${PATCH}..."
    for TARGET in "${TARGETS[@]}" ; do
        # validate patch target dir
        TARGET_DIR=$(dirname $TARGET)
        if [ ! -d $TARGET_DIR ] ; then
            continue
        fi
        # apply patch to target
        echo "Patching file: ${TARGET}..."
        if [ -f $TARGET ] ; then
            chmod +w $TARGET
        fi
        if ! cat $PATCH | patch -Np0 --merge $TARGET > /dev/null 2>&1 ; then
            echo "Unable to patch ${TARGET} cleanly. Manual review and merge of file required, (look for <<<<<<< and >>>>>>> lines in file)."
            STATUS=1
        fi
        chown nagios.nagios $TARGET
    done
done
echo "Applying patch scripts..."
for PATCH in $PATCHES/*.sh ; do
    if [ "${PATCH##*/}" != "${0##*/}" ] ; then
        if ! $PATCH $INSTALL_DIR ; then
            echo "Unable to run $PATCH script cleanly. Please check related file(s)."
            STATUS=1
        fi
    fi
done
echo "Patches applied."
exit $STATUS
