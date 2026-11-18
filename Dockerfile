FROM gradle:9.1.0-jdk25 AS build
WORKDIR /home/gradle
COPY build.gradle settings.gradle ./
RUN gradle --no-daemon dependencies
COPY src ./src
RUN gradle --no-daemon bootJar -x test

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /home/gradle/build/libs/funds-api-1.0.0.jar app.jar
VOLUME /app/data
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
