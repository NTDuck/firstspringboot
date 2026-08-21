# Build stage
FROM eclipse-temurin:11-jdk AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /workspace/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
