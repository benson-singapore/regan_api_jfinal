# jfinal-api

#### 项目介绍
jfianl api文档项目
***Regan API*** 项目是基于注释自动生成api文档，很大缩短了开始与后期维护API接口文档的时间。***Regan API*** 利用jdk提供的Doclet
类读取文档注释，可手动配置需要读取的文件，同时增加了读取过滤指定方法过滤等功能。

#### 文档：[https://regan_jeff.gitee.io/jfinal-api/](https://regan_jeff.gitee.io/jfinal-api)

#### 软件架构
软件架构说明
***Regan API*** 基础项目基于[jfinal](http://www.jfinal.com/) 开发，前端基于[飞冰](https://alibaba.github.io/ice)开发的API接口文档管理目录，项目需要引用只需要加入 ***src下api包下的文件，以及webapp下api目录里的页面*** 。

#### 安装教程

1. 下载demo项目，执行 ApiConfig 下main方法即可。

#### 使用说明

1. _引入依赖_

```xml
<dependency>
  <groupId>cn.hutool</groupId>
  <artifactId>hutool-all</artifactId>
  <version>4.0.12</version>
</dependency>
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>18.0</version>
</dependency>
<dependency>
  <groupId>com.alibaba</groupId>
  <artifactId>fastjson</artifactId>
  <version>1.2.9</version>
</dependency>
```

2. 加入 ***src下api包下的文件，以及webapp下api目录里的页面***

3. jfinal 配置加入api路由

```java
/**
 * 配置路由
 */
@Override
public void configRoute(Routes me) {
    //配置api路由
    me.add("/api", ApiController.class);
}
```
4.启动服务访问本地地址：http://***/api

**2.配置文件说明**
* 在项目resources 加入 ***api.properties*** 文件，指定解析的包文件。
```properties
#解析的controller包 多个用逗号间隔
packages=com.jfinal.api.controller

#需要过滤的controller 多个用逗号间隔
filters=UserController 
```
**3.controller注释配置**

```java
/**
 * 用户管理
 * @action /user
 * @author zhangby
 * @date 2018/6/12 下午3:26
 */
public class UserController extends Controller{
    
}
```
**4.method注释配置**

```java
    /**
     * 用户登录功
     * @title 登录接口
     * @param username|用户名|string|必填
     * @param password|密码|string|必填
     * @resqParam code|用户名|String|必填
     * @resqParam data|数据|object|非必填
     * @resqParam msg|消息信息|String|必填
     * @respBody {"code":"000","data":"","msg":"success"}
     * @author zhangby
     * @date 2018/6/12 下午4:23
     */
    public void login() {
        renderJson(Kv.create().set("code","000"));
    }
```
> 注：如果需要过滤controller中的方法，可在方法上添加 **@ApiIgnore**注解。