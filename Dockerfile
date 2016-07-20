FROM java:8
VOLUME /tmp
ADD build/libs/expper-1.6.0.war app.war
RUN bash -c 'touch /app.war'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.war"]

