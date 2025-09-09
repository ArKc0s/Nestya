# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-Xms128m -Xmx512m"
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
CMD ["bash","-lc","java $JAVA_OPTS -jar /app/app.jar"]