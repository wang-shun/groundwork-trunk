#!/bin/bash
NMS_RELEASE=$1
uname -p | grep x86_64
if [ $? == 1 ]; then
        NMS_CORE_SPEC=groundwork-nms-core-$NMS_RELEASE.spec
else
        NMS_CORE_SPEC=groundwork-nms-core-$NMS_RELEASE-x86_64.spec
fi
if   [ -f /etc/redhat-release ]; then
    PLATFORM_RPMRC_FILES=/usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc
elif [ -f /etc/SuSE-release   ]; then
    PLATFORM_RPMRC_FILES=/usr/lib/rpm/rpmrc
else
    echo "This platform is not yet supported."
    exit 1
fi
rpmbuild --rcfile $PLATFORM_RPMRC_FILES:$PWD/rpmsetup/groundwork-nms-core.rpmrc --dbpath $PWD/rpmsetup --quiet -bb --short-circuit rpmsetup/$NMS_CORE_SPEC 2>&1

