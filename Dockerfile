FROM openjdk:21-jdk-slim
COPY target/examine-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env
ENTRYPOINT ["java", "-jar", "/app.jar"]

