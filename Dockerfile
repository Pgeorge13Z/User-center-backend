# Docker 镜像构建
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>
FROM maven:3.5-jdk-8-alpine as builder
#安装依赖的java包
#RUN apk update && apk add --no-cache openjfx
#RUN apt-get update && apt-get install -y --no-install-recommends openjfx && rm -rf /var/lib/apt/lists/*
# Copy local code to the container image.
WORKDIR /app
#COPY pom.xml .
#COPY src ./src

# Build a release artifact.
#RUN mvn package -DskipTests
COPY user-center-0.0.1-SNAPSHOT.jar .

# Run the web service on container startup.
#CMD ["java","-jar","/app/target/user-center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
CMD ["java","-jar","/app/user-center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
