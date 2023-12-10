# 伙伴匹配系统+用户中心系统作后台管理 by Pgeorge

## 🐯 平台简介

采用Springboot+React框架搭建的用户中心管理系统，作为基础平台，完成了用户的基本管理功能，如注册、登录、认证、修改等多个功能。后续本人会接入本人完成的各种系统，并进一步改进本管理系统。

采用Springboot+Vue框架搭建的伙伴匹配系统，移动端H5网站（APP风格），包括用户登录，更新个人信息，按标签搜索用户、建房组队、推荐相似用户的功能。

## 🐶 在线地址

* 后台管理系统在线地址:http://user.code-club.fun/user/login
* 伙伴匹配系统在线地址: http://code-club.fun:81/ (请使用火狐浏览器登录，账号需要在后台管理系统中注册)
* 后端源码【springboot+Mysql+Mybatis-plus】:https://github.com/Pgeorge13Z/User-center-backend
* 前端源码【React+Ant Design PRO】:https://github.com/Pgeorge13Z/user-center-frontend

## 🐼技术选型

前端：

* 管理系统
  * React 开发框架（管理系统）
  * Ant Design Pro（管理系统）

- 伙伴匹配系统
  * Vue3
  * Vant UI组件库
  * Axios请求库


后端：

- mybatis-plus（对 mybatis 的增强，不用写 sql 也能实现增删改查）
- springboot（**快速启动** / 快速集成项目。不用自己管理 spring 配置，不用自己整合各种框架）
- junit 单元测试库
- Redis缓存高频数据
- Redis分布式登录
- Redission分布式锁
- Spring Scheduler 定时任务
- Swagger + Knife4j 接口文档
- 相似度匹配算法

## 🐨 内置功能

| 功能         | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| 登录/注册    | 密码加密存储，分布式session登录，redis存储session            |
| 后台用户管理 | 包括增添、删除、修改用户，包含**权限检验**逻辑，和Oss在线管理头像等图片信息。 |
| 容器化部署   | 提供了Docker**容器化部署**的方案，并解决了**跨域**等问题     |
| 搜索用户     | 允许用户根据标签搜索用户                                     |
| 组队         | 创建、加入、标签查询、邀请、退出队伍等队伍相关功能           |
| 推荐         | 首页根据相似度匹配算法，推荐用户。                           |
| 缓存         | 使用分页的方法展示用户，使用Scheduler的定时任务将重要用户（或者频繁用户）的查询结果做缓存。 |

## 🐷演示图

| 模块               | 演示                                                         | 演示                                                         |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 登录 & 注册        | ![image-20231103175854550](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031758623.png) | ![image-20231103175907969](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031759004.png) |
| 首页               | ![image-20231103175956592](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031759240.png) |                                                              |
| 用户管理（管理员） | ![image-20231103180024015](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031800129.png) | ![image-20231103180047378](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031800463.png) |
| 个人信息管理       | ![image-20231103180133200](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311031801369.png) |                                                              |
| 推荐               | ![image-20231210170608964](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101706026.png) | ![image-20231210170631453](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101706484.png) |
| 队伍               | ![image-20231210170651357](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101706397.png) | ![image-20231210170700351](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101707385.png) |
| 信息管理           | ![image-20231210170727760](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101707806.png) | ![image-20231210170741772](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101707814.png) |



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

**添加跨域头**

让服务器告诉浏览器：允许跨域（返回 cross-origin-allow 响应头）

**1. 网关支持（Nginx）**

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

**2. 修改后端服务**

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



### 7. 服务器（arch）上安装并使用redis，springboot项目

**安装redis**

```shell
sudo pacman -S redis
```

**启动redis服务**

```shell
sudo systemctl start redis
```

**开机启动redis服务器**

```shell
sudo systemctl enable redis
```

**查看服务状态**

```shell
systemctl status redis
```

**允许其他ip访问**

默认只允许127.0.0.1访问，若需要其他IP访问

```shell
sudo vim /etc/redis/redis.conf
```

注释掉`bind 127.0.0.1 -::1`即可，或在后面增加ip

```shell
bind 127.0.0.1 **.**.**.**
```

**重启redis服务**

```shell
sudo systemctl restart redis
```

**Springboot配置**

* 使用docker部署注意事项，不能使用localhost，否则使用的是容器内的redis，若要使用服务器的，请配置为服务器ip

```yml
spring:
      #redis配置
  redis:
    port: 6379
    host: **.**.**.**
    database: 0
```



### 8.axios无法携带cookie到后端，session可存cookie但不可取

登录逻辑采用session存储，并采用分布式redis缓存。

**登录代码**

```java
request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
```

**验证代码**

```java
if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN, "未登录");
        }
```

**session配置**

```yml
 spring:
  session:
    timeout: 86400
    store-type: redis
```



**上线时出现的问题**

![image-20231210153722055](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202312101537152.png)

就是出现了跨域请求，浏览器默认在页面跳转时无法携带cookie



**解决方案**

```yml
server:
  port: 8081
  servlet:
    context-path: /api
    session:
      cookie:
        domain: code-club.fun
        same-site: lax
        secure: false
```

* 使用一级域名，因为二级域名也可以被解析为一级域名
* 此方案必须在firefox下使用，chrome目前版本仍然被禁止



**方案二**

```yml
server:
  port: 8081
  servlet:
    context-path: /api
    session:
      cookie:
        domain: code-club.fun
        same-site: none
        secure: true
```



* same-site设置为none，secure必须为true。此时需要SSL证书，使用https协议，方可成功



**原理探究：**

chrome开始支持新属性SameSite，解决接口Cookie被滥用以及CSRF的问题。

**SameSite属性简介**

ameSite是Cookie的一个属性，其和path，domain一样，是一个普通的Cookie属性。其作用是限制该Cookie的在请求的时候是否被传递到Cookie所属服务的场景。

它一共有三个值，其基本含义分别如下：

**Strict**

Strict最为严格，完全禁止第三方 Cookie。即在跨站点时，任何情况下都不会发送 Cookie。换言之，只有当前网页的 URL 与请求目标一致，才会带上 Cookie。

设置方式为：Set-Cookie: CookieName=CookieValue; SameSite=Strict;

**Lax**

Lax规则稍稍放宽，大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外( top-level navigations)。

**所有浏览器的Cookie默认SameSite属性都为Lax**，即SameSite设置为空（未设置），则表示为Lax。

设置方式为：Set-Cookie: CookieName=CookieValue; SameSite=Lax;

设置了Strict或Lax以后，基本就杜绝了 CSRF 攻击。当然，前提是用户浏览器支持 SameSite 属性。

**None**

如果设置为None（**前提是必须同时设置Secure属性**（Cookie 只能通过 HTTPS 协议发送），否则无效）。此时就表示Cookie不受约束在任何跨域场景下，只要设置了withCredentials=true都会发送到三方服务端。

设置方式为：Set-Cookie: CookieName=CookieValue; SameSite=None; Secure;



参考文章：

https://juejin.cn/post/6999262693715050533