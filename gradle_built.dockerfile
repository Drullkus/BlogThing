# https://codefresh.io/docs/docs/learn-by-example/java/gradle/
FROM gradle:7.3.3-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:17-jdk-alpine
RUN mkdir /app
# Requires there to be one and only one *.jar FIXME make into a variable from lines above or in docker_compose.yml
COPY --from=build /home/gradle/src/build/libs/*.jar /app/blog_app.jar
ENTRYPOINT ["java","-jar","/app/blog_app.jar"]
