#!/bin/sh

/bin/mkdir -m 0755 /usr/local/groundwork/apache2/cgi-bin/exposed
/bin/chown nagios:nagios /usr/local/groundwork/apache2/cgi-bin/exposed
/bin/cp -p /usr/local/groundwork/apache2/cgi-bin/nagios/statuswml.cgi /usr/local/groundwork/apache2/cgi-bin/exposed

/bin/echo -n 'ScriptAlias /exposed/cgi-bin "/usr/local/groundwork/apache2/cgi-bin/exposed" 
<Directory "/usr/local/groundwork/apache2/cgi-bin/exposed"> 
  AllowOverride AuthConfig 
  Options ExecCGI 
  Order allow,deny 
  Allow from all 
  PassEnv LD_LIBRARY_PATH 
</Directory>' > /usr/local/groundwork/apache2/conf/groundwork/wap.conf

