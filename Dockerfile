FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install -y openjdk-18-jdk maven
COPY . .
RUN mvn clean package

FROM openjdk:18-jdk-slim
EXPOSE 443
COPY --from=build /target/pastebin.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]