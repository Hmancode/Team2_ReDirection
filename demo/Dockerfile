# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven config and source
COPY pom.xml .
COPY src ./src

# Build the jar (skip tests for speed)
RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Spring will use PORT if set, otherwise default you defined in application.properties
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
