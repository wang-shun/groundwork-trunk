# Azure

Additional instructions for using Azure and Azure CLI

## Installing CLI

Install the CLI
````
brew update && brew install azure-cli
````

## Creating an Azure Auth File

An Auth file is generated using azure-cli

We default to 
````
/usr/local/groundwork/config/cloudhub/azure/cloudhub.azureauth
````

To create an auth file, first login and then run the create-for-rbac command:
```` 
az login -u (username)
az ad sp create-for-rbac --sdk-auth > cloudhub.azureauth
````

Place this file in /usr/local/groundwork/cloudhub/azure/ directory

https://github.com/Azure/azure-libraries-for-net/blob/master/AUTH.md

## Creating Function App and Web App


https://azure.microsoft.com/en-us/blog/deploy-java-web-apps-to-azure-using-eclipse/
https://docs.microsoft.com/en-us/azure/azure-functions/functions-create-first-azure-function
https://gw-functiion-app1.azurewebsites.net/api/HttpTriggerCSharp1?code=CyHPzjC7crPaMnJZxTdBGFLfafnOfIaCRvNWgPS3jwjosWEa11QlDA==

