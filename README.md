
## 1. 简介
本项目主要演示Bytebuddy字节码增强技术和遇到的ClassNotFound问题解决方案。

相关博客文章见[Java字节码增强技术Bytebuddy探路篇二](https://www.hyhblog.cn/2022/08/09/bytebuddy_no_classdef_found_error/)

## 2. 项目结构

整个工程结构如下，

```
+ phantom-core 演示基础类。
+ phantom-demo 演示web应用，这是一个简单的sprint boot应用，运行在端口18080。
+ phantom-agent 一个Java agent，里面通过ByteBuddy对指定类以无侵入方式切面增强，在演示工程中主要对Tomcat web的请求进行切面处理。
+ phantom-agent-plugin 一个增强类插件，这个是为了解决NoClassDefFoundError问题而提供的一个插件解决方案，应用于phantom-agent的TransformerV3.tranform()中，项目构建后需要复制构建包到路径/tmp/phantom-agent-plugin.jar上。
```

## 3. 项目构建和运行

构建和运行环境为，
- Java 8
- Maven 3

本项目使用maven 3+进行构建，命令为，
```bash
mvn clean package
```

启动和测试命令，
```bash
# 启动演示web应用，端口在18080
java -jar ./phantom-demo/target/phantom-demo.jar

# 测试web请求命令，若正常将收到hello,world的响应消息
curl -X POST http://127.0.0.1:18080/api/hello

# 启动命令（加载agent, transformer = v1）
java \
-javaagent:./phantom-agent/target/phantom-agent.jar \
-Dagent.transformer.version=v1 \
-jar ./phantom-demo/target/phantom-demo.jar

# 启动命令（加载agent, transformer = v2）
java \
-javaagent:./phantom-agent/target/phantom-agent.jar \
-Dagent.transformer.version=v2 \
-classpath /tmp/ \
-jar ./phantom-demo/target/phantom-demo.jar

# 在启动transformer = v3前，复制文件到/tmp/phantom-agent-plugin.jar
cp ./phantom-agent-plugin/target/phantom-agent-plugin.jar /tmp/

# 启动命令（加载agent, transformer = v3）
java \
-javaagent:./phantom-agent/target/phantom-agent.jar \
-Dagent.transformer.version=v3 \
-jar ./phantom-demo/target/phantom-demo.jar

# 启动命令（加载agent，打开远程调试端口在28080）
java \
-javaagent:./phantom-agent/target/phantom-agent.jar \
-Dagent.transformer.version=v2 \
-classpath ".;/tmp/phantom-agent-plugin.jar" \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=28080 \
-jar ./phantom-demo/target/phantom-demo.jar

```


## 4. 演示

见[Java字节码增强技术Bytebuddy探路篇二](https://www.hyhblog.cn/2022/08/09/bytebuddy_no_classdef_found_error/)

## 5. 联系 Contact
邮箱地址：peipeihh@qq.com，欢迎来信联系。

更多的信息，可以访问博客地址：[hyhblog.cn](https://hyhblog.cn)。

## 6. 开源许可协议 License
Apache License 2.0