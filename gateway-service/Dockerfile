FROM ubuntu:latest
LABEL authors="PengXinyang"

# 使用 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将服务的 JAR 文件复制到镜像中
COPY target/gateway-service-0.0.1-SNAPSHOT.jar gateway-service.jar

# 暴露应用运行的端口
EXPOSE 9090

# 启动服务
ENTRYPOINT ["java", "-jar", "gateway-service.jar"]