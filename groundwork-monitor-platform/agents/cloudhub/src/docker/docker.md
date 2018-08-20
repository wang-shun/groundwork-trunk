# Running CloudHub on Docker

There is a docker file in the root of the cloudhub source, Dockerfile. 
The container is built upon:

* Alpine Linux
* Java 7
* Tomcat 7.0.84
* Current version of Cloudhub

Tomcat is installed into this directory on the container:
```` 
/usr/local/tomcat 
````
Steps to build the container and run are documented here:

1. Build CloudHub with the Tomcat profile
2. Build the Docker image
3. Run CloudHub in Docker
4. Verify its running

## Building CloudHub

The Tomcat profile must be used when building CloudHub:

````
cd cloudhub
mvn clean install -P tomcat
````

## Building Docker Image

Build a docker image, tag it as groundwork/cloudhub:

````
docker build -t groundwork/cloudhub .
````

## Run CloudHub in Docker

To run CloudHub, exposed on port 8090 not to conflict with JBoss on port 8080:

````
docker run -p 8090:8080 --name cloudhub --tty groundwork/cloudhub
````

## Verify its Running

Navigate your browser to 

````
http://localhost:8090/cloudhub/mvc/
````

and create a connector. If connecting to Groundwork running on localhost, you may want to use your host's IP address or a DNS name


To verify the Cloudhub configuration files are saving, you can create a bash session in the container:
````
docker exec -it cloudhub bash
# example commands
cd /usr/local/groundwork/config/cloudhub
ls /usr/local/tomcat/webapps/cloudhub 
````

## Known Issues

1. all configuration files and profiles are lost on container destroy
   We may want to consider mounting an external directory
   
2. a ws_client.properties is unfortunately required to determine how to 
   encrypt credentials sent to the Groundwork server. We should look into
   removing this requirement
  
## Mounting a Volume

To address the known issues, mounting a volume could help. A volume
can permanently persist the configuration and profile files across the container's life cycle.

First, remove these commands from the Dockerfile, then rebuild your image:

````
RUN mkdir -p /usr/local/groundwork/config/cloudhub/deploy
RUN mkdir -p /usr/local/groundwork/config/cloudhub/profiles
RUN mkdir -p /usr/local/groundwork/config/cloudhub/profile-templates
RUN mkdir -p /usr/local/groundwork/config/cloudhub/statistics
ADD ./src/profiles/ /usr/local/groundwork/config/cloudhub/profile-templates/
COPY ./src/docker/ws_client.properties /usr/local/groundwork/config/
````

And then run CloudHub in docker with an attached volume:

````
docker run -p 8090:8080 --name cloudhub --tty -v /usr/local/groundwork/config:/usr/local/groundwork/config  groundwork/cloudhub
````
 
In the future we should consider storing configurations and profiles in a configuration service like Consul.
  
