FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 hirelog
COPY --from=build /src/target/hirelog-*.jar app.jar
USER hirelog
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
