#!/bin/bash -x
source /usr/local/groundwork/scripts/setenv.sh

/usr/local/groundwork/nms/tools/php/bin/php /usr/local/groundwork/nms/applications/cacti/poller.php ; /usr/local/groundwork/nms/tools/automation/scripts/extract_cacti.pl

