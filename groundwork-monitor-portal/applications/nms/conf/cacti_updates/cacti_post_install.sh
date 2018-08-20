#!/bin/sh
gw_home=/usr/local/groundwork
source /usr/local/groundwork/scripts/setenv.sh

#patch cacti subdirectory
dir=`/bin/pwd`
cd /usr/local/groundwork/cacti
/usr/bin/patch -p1 -i $dir/cacti_gw_updates.patch
cd $dir

#change the poller to spine
cat cacti.poller.sql |mysql cacti

#update path information
cat cacti.paths.sql |mysql cacti

#add schema changes for plugin architecture
cat cacti.pluginarch.sql | mysql cacti

#authorize admin user for plugin management
cat cacti.userauth.sql | mysql cacti

#install cacti settings plugin
#cat cacti_settings_plugin.sql | mysql cacti

#add cacti automation schema to monarch
cat cacti.import_schema.sql | mysql monarch

#remove plugins that dont belong
rm -rf /usr/local/groundwork/cacti/htdocs/plugins/weathermap/
rm -rf /usr/local/groundwork/cacti/htdocs/plugins/nedi/
rm -rf /usr/local/groundwork/cacti/htdocs/plugins/ntop/

#add cacti script called by cron
cp cacti_cron.sh /usr/local/groundwork/common/bin/cacti_cron.sh
chown nagios.nagios /usr/local/groundwork/common/bin/cacti_cron.sh
chmod +x /usr/local/groundwork/common/bin/cacti_cron.sh

#add find_cacti_graphs
cp find_cacti_graphs /usr/local/groundwork/foundation/feeder/find_cacti_graphs
chown nagios.nagios /usr/local/groundwork/foundation/feeder/find_cacti_graphs
chmod +x /usr/local/groundwork/foundation/feeder/find_cacti_graphs

#add monarch automation script
cp extract_cacti.pl /usr/local/groundwork/cacti/extract_cacti.pl
chown nagios.nagios /usr/local/groundwork/cacti/extract_cacti.pl
chmod +x /usr/local/groundwork/cacti/extract_cacti.pl

#add cacti properties file to be read by portlet and find_cacti_graphs
cp cacti.properties /usr/local/groundwork/config/cacti.properties
chown nagios.nagios /usr/local/groundwork/config/cacti.properties

#add cron entries for cacti
/usr/local/groundwork/perl/bin/perl ./crontab_cacti.pl
/usr/local/groundwork/perl/bin/perl ./crontab_find_cacti.pl

#clear the poller cache this should not be neccesary but the db is starting out with junk in it
/usr/local/groundwork/php/bin/php -q /usr/local/groundwork/cacti/htdocs/cli/rebuild_poller_cache.php -d

#copy over the cacti automation template
cp *.xml $gw_home/core/monarch/automation/templates
chown nagios.nagios $gw_home/core/monarch/automation/templates/*.xml

#add check_cacti.pl script
cp check_cacti.pl $gw_home/cacti/scripts/check_cacti.pl
chmod +x $gw_home/cacti/scripts/check_cacti.pl
chown nagios.nagios $gw_home/cacti/scripts/check_cacti.pl
cp check_cacti.conf $gw_home/common/etc/
chown nagios.nagios $gw_home/common/etc/check_cacti.conf
chmod 600 $gw_home/common/etc/check_cacti.conf

#set everything back to owner nagios
chown -R nagios.nagios $gw_home/cacti

#add updated php.ini
cp php.ini $gw_home/php/etc/
chown nagios.nagios $gw_home/php/etc/

