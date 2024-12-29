FROM ubuntu:latest
LABEL authors="PengXinyang"

# 使用 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将服务的 JAR 文件复制到镜像中
COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

# 暴露应用运行的端口
EXPOSE 8091

# 启动服务
ENTRYPOINT ["java", "-jar", "user-service.jar"]