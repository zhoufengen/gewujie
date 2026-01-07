# Build Stage
FROM registry.cn-shenzhen.aliyuncs.com/sync_dockerhub/maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Skip tests to speed up build
RUN mvn clean package -DskipTests

# Run Stage
FROM registry.cn-shenzhen.aliyuncs.com/sync_dockerhub/openjdk:17-ea-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
