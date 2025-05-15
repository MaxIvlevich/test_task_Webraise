FROM maven:3.9-eclipse-temurin-17  AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
ARG JAR_FILE_PATH=target/*.jar
COPY --from=build /app/${JAR_FILE_PATH} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]