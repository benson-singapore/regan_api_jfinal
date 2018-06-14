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
4. 项目中注释基础配置，请参考[文档](https://regan_jeff.gitee.io/jfinal-api/)。

5.启动服务访问本地地址：http://***/api

#### 码云特技

1. 使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2. 码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3. 你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4. [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5. 码云官方提供的使用手册 [http://git.mydoc.io/](http://git.mydoc.io/)
6. 码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)