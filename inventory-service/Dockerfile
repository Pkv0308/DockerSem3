# ---- Stage 1: build the WAR ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn -q clean package -DskipTests

# ---- Stage 2: run it on Tomcat ----
FROM tomcat:10.1-jre25-temurin-noble
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/inventory-service-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]