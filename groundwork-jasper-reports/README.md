### Jasper Reports

#### JasperSoft Information
Refer to [JasperReportInfo.md](./JasperReportInfo.md) for information that may be helpful to you.

#### Environment Setup
Refer to [StudioSetup.md](./docs/StudioSetup.md) to setup JasperSoft Studio.

#### Exporting from Jasper Server 
We have done our development on local Jasper Studios with the source code here, and published to a remote Jasper Server 
on the terra server. Jasper Server does not change the compiled reports that are create here in Jasper Studio. 
Jasper Server does store lots of configurations in its local database. All configurations can be exported, and then
imported into another database. This process will be useful when installing on customer systems, although the 
data sources will need to be updated or somehow merged (not sure if that is possible) during installation.

What is exported?

* DataAdaptors and DataSources
* Reports
* Resources including images and plugin jar files
* Input Controls
* Parameters
* Queries.

# Exporting on Terra
````
cd /usr/local/groundwork/reporting/jasperreports-server/buildomatic
 ./js-export.sh --everything --output-zip /home/dtaylor/zip-exports/2018-04-19-terra.zip
````
NOTE: You may need to change the database username in master.properties if the js-export command fails
````
vi build_conf/default/master.properties 
````

# Importing
````
cd $JASPER_SERVER_HOME/buildomatic
./js-import.sh --input-zip ../exports/2018-04-19-terra.zip --update
````
 
[ImportExport Docs](https://community.jaspersoft.com/documentation/jasperreports-server-install-guide/v56/locating-and-changing-buildomatic-configuration)
 

[Buildomatic Docs](https://community.jaspersoft.com/documentation/jasperreports-server-install-guide/v56/locating-and-changing-buildomatic-configuration)
