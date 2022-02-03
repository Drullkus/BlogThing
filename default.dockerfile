FROM openjdk:17-jdk-alpine
#VOLUME /tmp
RUN mkdir /app
COPY build/libs/*.jar /app/blog_app.jar
ENTRYPOINT ["java","-jar","/app/blog_app.jar"]