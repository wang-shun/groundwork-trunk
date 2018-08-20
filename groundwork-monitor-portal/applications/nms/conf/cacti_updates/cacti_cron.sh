#!/bin/bash -x
source /usr/local/groundwork/scripts/setenv.sh

/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/poller.php ; touch /usr/local/groundwork/foundation/feeder/run_cacti_feeder ; /usr/local/groundwork/cacti/extract_cacti.pl

