FROM openjdk:8-jdk-alpine 
RUN apk update && apk add wget && rm -rf /var/cache/apk/*
VOLUME ["/app"] 
COPY target/personal-account-1.0.0-RELEASE.jar /app/
ARG DEPENDENCY=target/dependency  
 
EXPOSE 8080 
 
ADD https://raw.githubusercontent.com/SignalMedia/signal-secret-service/master/init.sh /  
RUN chmod +x /init.sh && /init.sh  
RUN wget -O dd-java-agent.jar 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'
 
ENTRYPOINT ["/init.sh","/usr/bin/java", "-javaagent:dd-java-agent.jar", "-jar","/app/personal-account-1.0.0-RELEASE.jar"]
