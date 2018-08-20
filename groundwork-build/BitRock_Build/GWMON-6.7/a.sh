#!/bin/sh
#

gw_home=/usr/local/groundwork

echo "Patching NMS Apache configuration"
(echo "g/TKTAuthLoginURL/d"; echo 'wq') | ex -s /usr/local/groundwork/nms/tools/httpd/conf/httpd.conf

#patch -d $gw_home/nms/tools/httpd/conf < ./conf/httpd-2.1.2.patch

