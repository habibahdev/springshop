FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home spring
COPY --from=build /workspace/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]