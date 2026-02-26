FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/incident-service-1.0.0.jar app.jar
EXPOSE 6003
ENTRYPOINT ["java", "-jar", "app.jar"]
