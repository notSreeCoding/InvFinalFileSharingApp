FROM maven:3.8.4-openjdk-25 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:25-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]