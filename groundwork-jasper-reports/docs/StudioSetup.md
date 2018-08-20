### Setup JasperSoft Studio for GroundWork Reporting

#### DataSource Adapter Creation
Two Database JDBC Connection Adapters are to be created for your DataSource.  I.e., Right-click on <i>Data Adapters</i> node in <i>Repository Explorer</i> view to get context menu.
- archive_gwcollagedb  

  Name: archive_gwcollagedb  
  JDBC Driver: org.postgresql.Driver  
  Username: reporter  
  Password: reporter

- dashboard

  Name: dashboard  
  JDBC Driver: org.postgresql.Driver  
  Username: reporter  
  Password: reporter

  #### Import this project
  It is recommended to checkout this project from subversion under <i>$HOME/JaspersoftWorkspace</i> folder.  

  You can also import an <i>Existing Projects into Workspace</i> from <i>File->Import...->General</i> if you have checkout the project to a different folder.
  You can also refer to this [link](https://community.jaspersoft.com/wiki/import-projects-and-settings-previous-version-tibco-jaspersoft-studio) for details.
