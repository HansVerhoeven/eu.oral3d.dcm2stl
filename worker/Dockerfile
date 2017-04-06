FROM java:8

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

ADD target/dcm2stl-worker-1.0-SNAPSHOT.jar app.jar

RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=production", "-Xmx8g", "-jar","app.jar"]