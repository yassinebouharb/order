FROM openjdk:22-jdk
VOLUME /tmp
COPY target/Order-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app.jar"]

