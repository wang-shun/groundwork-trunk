GroundWork OpenTSDB Datasource
------------------------------------------------------

Files copied from Grafana public/app/plugins/datasource/opentsdb:

datasource.js
queryCtrl.js
partials/config.html
partials/query.editor.html

Make patches after copy:

> cat opentsdb.patch | patch -Np0 --merge

Install:

> rm -rf public/app/plugins/datasource/groundwork
> mkdir public/app/plugins/datasource/groundwork
> cp -r *.js *.json partials public/app/plugins/datasource/groundwork
