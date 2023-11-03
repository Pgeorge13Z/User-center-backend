# 用户中心系统 by Pgeorge

## 🐯 平台简介

采用Springboot+React框架搭建的用户中心管理系统，作为基础平台，完成了用户的基本管理功能，如注册、登录、认证、修改等多个功能。后续本人会接入本人完成的各种系统，并进一步改进本管理系统。



## 🐶 在线地址

* 在线地址:http://user.code-club.fun/user/login
* 后端源码【springboot+Mysql+Mybatis-plus】:https://github.com/Pgeorge13Z/User-center-backend
* 前端源码【React+Ant Design PRO】:https://github.com/Pgeorge13Z/user-center-frontend

## 🐼技术选型

前端：

- React 开发框架
- Ant Design Pro（现成的管理系统）


后端：

- mybatis-plus（对 mybatis 的增强，不用写 sql 也能实现增删改查）
- springboot（**快速启动** / 快速集成项目。不用自己管理 spring 配置，不用自己整合各种框架）
- junit 单元测试库

## 🐨 内置功能

| 功能       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| 登录/注册  | 密码加密存储，todo：后续增加分布式session的方法改进          |
| 用户管理   | 包括增添、删除、修改用户，包含**权限检验**逻辑，和Oss在线管理头像等图片信息。 |
| 容器化部署 | 提供了Docker**容器化部署**的方案，并解决了**跨域**等问题     |

## 🐷演示图

| 模块               | 演示                                                         | 演示                                                         |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 登录 & 注册        | ![image-20231103175854550](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031758623.png) | ![image-20231103175907969](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031759004.png) |
| 首页               | ![image-20231103175956592](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031759240.png) |                                                              |
| 用户管理（管理员） | ![image-20231103180024015](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031800129.png) | ![image-20231103180047378](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031800463.png) |
| 个人信息管理       | ![image-20231103180133200](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031801369.png) |                                                              |



## 杂记

在做这个项目的过程中，学会的一些小tips。

### 1. IDEA插件：

1. GenerateAllSetter : alt+insert 可以自动生成一个对象的所有set方法。
2. GenerateSerialVersionUID 在implements Serializable接口时alt+insert 可以自动生成序列化ID。
3. Auto Filling Java Call Arguments 在调用对象方法时，alt+insert 可以自动填充方法参数。



### 2.调试方法：

* F7： Step into 进入方法
* F8： Step Over 以行为单位，不进入方法
* F9： 跳到下一个断点，没有的话就结束



因此 比如可以在controller的每一行打上断点，然后给调用的重要业务，如Service里的方法打上断点，通过F9 进入Service中。

（需要先打开Application主文件的debug，打开服务器）



### 3.逻辑删除：

mybatis-plus的功能（学会查官方文档，找配置逻辑删除），

1. 在domain的user中给需要逻辑删除的变量加上@TableLogic

2. 在`application.yml`中配置如下内容：

```yam
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: false

  global-config:
    db-config:
      logic-delete-field: isDelete # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
```



第一个配置是取消mybatis-plus的自动加粗功能；

第二个配置是设置逻辑删除，即数据库中设置isDelete为1时，便查询不到此值。



### 4. 快捷键：

 shift+F6 重构（批量更改一个变量）

ctrl shift  -  全部折叠



### 5. 跨域问题解决

浏览器为了用户的安全，仅允许向 **同域名、同端口** 的服务器发送请求。

如何解决跨域？

最直接的方式：把域名、端口改成相同的

### 添加跨域头

让服务器告诉浏览器：允许跨域（返回 cross-origin-allow 响应头）

#### 1. 网关支持（Nginx）

```nginx
# 跨域配置
location ^~ /api/ {
    proxy_pass http://127.0.0.1:8081/api/;
    add_header 'Access-Control-Allow-Origin' $http_origin;
    add_header 'Access-Control-Allow-Credentials' 'true';
    add_header Access-Control-Allow-Methods 'GET, POST, OPTIONS';
    add_header Access-Control-Allow-Headers '*';
    if ($request_method = 'OPTIONS') {
        add_header 'Access-Control-Allow-Credentials' 'true';
        add_header 'Access-Control-Allow-Origin' $http_origin;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS';
        add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range';
        add_header 'Access-Control-Max-Age' 1728000;
        add_header 'Content-Type' 'text/plain; charset=utf-8';
        add_header 'Content-Length' 0;
        return 204;
    }
}
```

#### 2. 修改后端服务

1. 配置 @CrossOrigin 注解

2. 添加 web 全局请求拦截器

   ```java
   @Configuration
   public class WebMvcConfg implements WebMvcConfigurer {
    
       @Override
       public void addCorsMappings(CorsRegistry registry) {
           //设置允许跨域的路径
           registry.addMapping("/**")
                   //设置允许跨域请求的域名
                   //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                   .allowedOrigins("http://localhost:9527", "http://127.0.0.1:9527", "http://127.0.0.1:8082", "http://127.0.0.1:8083")
                   //是否允许证书 不再默认开启
                   .allowCredentials(true)
                   //设置允许的方法
                   .allowedMethods("*")
                   //跨域允许时间
                   .maxAge(3600);
       }
   }
   ```

3. 定义新的 corsFilter Bean，参考：https://www.jianshu.com/p/b02099a435bd



### 6.Docker常用命令

根据 Dockerfile 构建镜像：

```bash
# 后端
docker build -t user-center-backend:v0.0.1 .

# 前端
docker build -t user-center-frontend:v0.0.1 .
```

Docker 构建优化：减少尺寸、减少构建时间（比如多阶段构建，可以丢弃之前阶段不需要的内容）

docker run 启动：

```bash
# 前端
docker run -p 80:80 --ulimit nofile=65535:65535 -d user-center-frontend:v0.0.1

# 后端
docker run -p 8081:8081 --ulimit nofile=65535:65535 -d user-center-backend:v0.0.1
```

虚拟化

1. 端口映射：把本机的资源（实际访问地址）和容器内部的资源（应用启动端口）进行关联
2. 目录映射：把本机的端口和容器应用的端口进行关联



进入容器：

```bash
docker exec -i -t  fee2bbb7c9ee /bin/bash
```



查看进程：

```bash
docker ps 
```



查看日志：

```bash
docker logs -f [container-id]
```



杀死容器：

```bash
docker kill
```



删除容器：

```bash
docker rm
```





强制删除镜像：

```bash
docker rmi -f
```

