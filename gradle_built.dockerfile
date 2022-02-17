# https://sairamkrish.medium.com/docker-for-spring-boot-gradle-java-micro-service-done-the-right-way-2f46231dbc06
FROM openjdk:17 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
COPY . $APP_HOME
RUN ./gradlew build --no-watch-fs

FROM openjdk:17
# FIXME Can we possibly pull the "blog" name from settings.gradle then add the "0.0.1-SNAPSHOT" from version string in build.gradle?
ENV ARTIFACT_NAME="blog-0.0.1-SNAPSHOT.jar"
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
ENTRYPOINT java -jar $ARTIFACT_NAME