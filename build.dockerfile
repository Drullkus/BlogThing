# https://codefresh.io/docs/docs/learn-by-example/java/gradle/
FROM gradle:4.7.0-jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:17-jdk-alpine
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/blog_app.jar
ENTRYPOINT ["java","-jar","/app/blog_app.jar"]
