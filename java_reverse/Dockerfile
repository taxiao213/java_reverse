### 基础镜像，使用alpine操作系统，openjkd使用8u201
#FROM hirokimatsumoto/alpine-openjdk-11:11-ea-25
#FROM openjdk:8u201-jdk-alpine3.9
FROM openjdk:11.0.15-jdk
#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
#作者
MAINTAINER taxiao <yin13753884368@163.com>
VOLUME /tmp

#在容器的根目录中创建spring-cloud文件夹
RUN mkdir -p /spring-cloud \
    && apt-get update \
    && apt-get install -y vim

#将本地项目jar包拷贝到Docker容器中的位置
COPY target/java_reverse-1.0-SNAPSHOT.jar /spring-cloud/app.jar
#将本地的可执行文件拷贝到Docker容器中的根目录下
COPY ./app-entrypoint.sh /spring-cloud/
#给app-entrypoint.sh赋予可执行权限
RUN chmod +x /spring-cloud/app-entrypoint.sh

#对外端口号
EXPOSE 8088
#开机启动
ENTRYPOINT ["/spring-cloud/app-entrypoint.sh"]
