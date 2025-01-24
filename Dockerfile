
FROM gradle:7.6.2-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN chmod +x ./gradlew
RUN ./gradlew server:dist --build-cache --stacktrace

FROM eclipse-temurin:17-jre-alpine


COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/server.jar


ENTRYPOINT ["java -jar /app/server.jar"]
