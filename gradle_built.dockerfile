# https://codefresh.io/docs/docs/learn-by-example/java/gradle/
# FROM gradle:7.3.3-jdk17-alpine AS build
# # COPY --chown=gradle:gradle . .
# RUN gradle build --no-daemon --stacktrace

# FROM openjdk:17-jdk-alpine
# # Requires there to be one and only one *.jar FIXME make into a variable from lines above or in docker_compose.yml
# COPY --from=build ./build/libs/*.jar /blog_app.jar
# ENTRYPOINT ["java","-jar","/blog_app.jar"]

FROM openjdk:17-jdk-alpine
#VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]