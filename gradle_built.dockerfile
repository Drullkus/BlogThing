# https://codefresh.io/docs/docs/learn-by-example/java/gradle/
FROM gradle:7.3.3-jdk17-alpine AS build
COPY --chown=gradle:gradle . ./appbuild/
WORKDIR ./appbuild/
RUN gradle build --no-daemon --stacktrace

# FROM openjdk:17-jdk-alpine
# # Requires there to be one and only one *.jar FIXME make into a variable from lines above or in docker_compose.yml
# COPY --from=build ./build/libs/*.jar /blog_app.jar
# ENTRYPOINT ["java","-jar","/blog_app.jar"]

FROM openjdk:17-jdk-alpine
#VOLUME /tmp
ARG JAR_FILE=./appbuild/*.jar
COPY ${JAR_FILE} ./target/app.jar
ENTRYPOINT java -jar ./target/app.jar