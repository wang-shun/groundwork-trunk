FROM tomcat:7.0.84-jre7-alpine

# VOLUME /tmp
VOLUME /usr/local/groundwork
RUN mkdir -p /usr/local/groundwork/config/cloudhub/deploy
RUN mkdir -p /usr/local/groundwork/config/cloudhub/profiles
RUN mkdir -p /usr/local/groundwork/config/cloudhub/profile-templates
RUN mkdir -p /usr/local/groundwork/config/cloudhub/statistics
ADD ./src/profiles/ /usr/local/groundwork/config/cloudhub/profile-templates/
COPY ./src/docker/ws_client.properties /usr/local/groundwork/config/
COPY ./target/cloudhub-tc.war /usr/local/tomcat/webapps/cloudhub.war

# RUN sh -c 'touch /app.jar'
# ENV JAVA_OPTS=""
# ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS" ]
CMD ["catalina.sh", "run"]
