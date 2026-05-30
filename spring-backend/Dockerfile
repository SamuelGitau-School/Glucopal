FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
ARG CACHEBUST=1
COPY src ./src
RUN mvn clean package -DskipTests -B
RUN jar tf target/diabetes-backend-1.0.0.jar | grep application.properties
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/diabetes-backend-1.0.0.jar"]

