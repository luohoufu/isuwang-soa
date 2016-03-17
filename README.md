### 容器部署

#### 运行脚本

```
cd isuwang-soa-container

sh dev.sh
```

#### 输出目录

```
isuwang-soa-container/target/isuwang-soa-container
```

#### 目录说明

```
|-- isuwang-soa-container                  
|   |-- bin                                
|   |   |-- lib                            平台jar包目录
|   |   |   |-- isuwang-soa-container.jar  
|   |   |   |-- ...                   
|   |   |-- startup.sh                     
|   |   |-- shutdown.sh                    
|   |   |-- isuwang-soa-bootstrap.jar      
|   |-- lib                                公共依赖jar包目录
|   |   |-- isuwang-soa-core.jar           
|   |   |-- ...
|   |-- conf                               配置文件目录
|   |   |-- server-conf.xml                
|   |   |-- logback.xml                    
|   |-- apps                               服务目录
|   |   |-- service-a/*.jar                
|   |   |-- service-b.jar                  
|   |   |-- service-c_d_f.jar              
|   |   |-- service-e/classes              
|   |-- logs                               日志目录
-------------------------------------------------------
```

### 工程目录说明

```
|-- isuwang-soa                           
|   |-- isuwang-soa-api-doc                 服务api站点工程
|   |-- isuwang-soa-bootstrap               启动模块工程
|   |-- isuwang-soa-code-generator          服务idl代码生成工程
|   |-- isuwang-soa-container               容器工程
|   |-- isuwang-soa-core                    核心工程
|   |-- isuwang-soa-maven-plugin            Maven开发插件工程
|   |-- isuwang-soa-monitor
|   |   |-- isuwang-soa-monitor-api         监控模块api工程
|   |   |-- isuwang-soa-monitor-druid       druid的监控工具
|   |   |-- isuwang-soa-monitor-influxdb    监控模块api实现工程(influxdb版本)
|   |-- isuwang-soa-registry
|   |   |-- isuwang-soa-registry-api        注册模块api工程
|   |   |-- isuwang-soa-registry-zookeeper  注册模块api实现工程(zookeeper版本)
|   |-- isuwang-soa-remoting
|   |   |-- isuwang-soa-remoting-api        客户端通讯模块api工程
|   |   |-- isuwang-soa-remoting-netty      客户端通讯模块api实现工程(netty版本)
|   |   |-- isuwang-soa-remoting-socket     客户端通讯模块api实现工程(socket版本)
|   |-- isuwang-soa-spring                  spring扩展模块工程             
```

### 服务开发简易说明

#### 例子工程

```
git clone https://github.com/isuwang/isuwang-soa-hello.git
```

#### thrift idl 定义服务接口

* hello_domain.thrift:

```
namespace java com.isuwang.soa.hello.domain

struct Hello {

    1: string name,

    2: optional string message

}
```

* hello_service.thrift：

```
include "hello_domain.thrift"

namespace java com.isuwang.soa.hello.service

/**
* Hello Service
**/
service HelloService {

    /**
    * say hello
    **/
    string sayHello(1:string name),

    string sayHello2(1:hello_domain.Hello hello)
    
}
```

#### 服务接口代码生成：

> 打包服务接口代码工程(`isuwang-soa-code-generator`): `mvn clean package` 
>
> 输出的可执行jar包目录: `isuwang-soa-code-generator/target/isuwang-soa-code-generator-1.0-SNAPSHOT-jar-with-dependencies.jar`

打印帮助命令

```
java -jar isuwang-soa-code-generator-1.0-SNAPSHOT-jar-with-dependencies.jar

-----------------------------------------------------------------------
 args: -gen metadata,js,json file
 Scrooge [options] file
 Options:
   -out dir    Set the output location for generated files.
   -gen STR    Generate code with a dynamically-registered generator.
               STR has the form language[val1,val2,val3].
               Keys and values are options passed to the generator.

 Available generators (and options):
   metadata
   js
   json
   java
-----------------------------------------------------------------------
```

生成thrift idl 定义服务接口代码

```
java -jar isuwang-soa-code-generator-1.0-SNAPSHOT-jar-with-dependencies.jar -gen java -out F:\hello F:\hello\hello_domain.thrift,F:\hello\hello_service.thrift

# 说明：
# 1. `-gen java` 表示生成java代码； 
# 2. `-out F:\hello`表示生成代码到`F:\hello`文件夹；
# 3. 多个thrift文件使用`,`分隔； 
# 4. 生成的xml文件在`F:\hello`文件夹，java类在`F:\hello\java-gen`文件夹。
```

#### 创建API工程

`hello-api`工程会被服务端和客户端依赖。

新建已经maven工程，即`hello-api`工程，依赖于`isuwang-soa-remoting-api`:
```
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-remoting-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
优先从公司私服获取依赖包：
```
<repositories>
    <repository>
        <id>maven-isuwang-com</id>
        <url>http://nexus.oa.isuwang.com/content/groups/public/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
    <repository>
        <id>maven-net-cn</id>
        <name>Maven China Mirror</name>
        <url>http://repo1.maven.org/maven2/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

将上一步中生成的java代码，拷贝入对应的package。将上一步中生成的xml文件，拷贝入`resources`文件夹。示例如图：

![api结构示例](http://7xnl6z.com1.z0.glb.clouddn.com/com.isuwang.soaapi_sturct_demo.png)

最后`mvn clean install`此项目。

#### 创建Service工程

`hello-service`工程依赖于`hello-api`工程和`isuwang-soa-spring`包，在工程中实现api中的接口类，在方法中实现具体的业务逻辑。

* 依赖：

```
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-hello-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-spring</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* 实现类：

```
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) throws SoaException {
        return "hello, " + name;
    }

    @Override
    public String sayHello2(Hello hello) throws SoaException {
        if (hello.getName().equals("bad")) {
            throw new SoaException("hello-001", "so bad");
        } else {
            String message;
            if (!hello.getMessage().isPresent())
                message = "you message is emtpy";
            else
                message = "you message is '" + hello.getMessage().get() + "'";

            return "hello, " + hello.getName() + ", " + message;
        }
    }
}
```

* 声明服务，使得容器启动时加载和注册该服务：

在`resources/META-INF/spring/`文件夹下新建services.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:soa="http://soa-springtag.isuwang.com/schema/service"
   xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://soa-springtag.isuwang.com/schema/service
    http://soa-springtag.isuwang.com/schema/service/service.xsd">

    <bean id="helloService" class="com.isuwang.soa.hello.HelloServiceImpl"/>
    <soa:service ref="helloService"/>
</beans>
```
    
#### 开发模式启动服务

> 前提:需要把`isuwang-soa-maven-plugin`安装到本地maven仓库

##### 安装Maven插件

安装`isuwang-soa-maven-plugin`工程

* 源码手动安装

```
cd isuwang-soa/isuwang-soa-maven-plugin
mvn clean install
```

* pom.xml依赖自动安装

`hello-service`添加maven插件：

```
<pluginRepositories>
    <pluginRepository>
        <id>maven-isuwang-com</id>
        <url>http://nexus.oa.isuwang.com/content/groups/public/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </pluginRepository>

    <pluginRepository>
        <id>maven-net-cn</id>
        <name>Maven China Mirror</name>
        <url>http://repo1.maven.org/maven2/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </pluginRepository>
</pluginRepositories>
...
<plugin>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwangsoa-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```

##### Maven启动服务容器

> 启动服务容器在开发模式下可以选择`本地模式`或`远程模式`
>
> 可在无开发ide环境下或在ide开发环境下运行
> 
> 可以使用`isuwangsoa`的插件简称,需要在本地maven进行配置

* isuwangsoa插件简称配置(不使用不用配置)

修改maven的主配置文件（${MAVEN_HOME}/conf/settings.xml文件或者 ~/.m2/settings.xml文件）

```
<pluginGroups>
    <pluginGroup>com.isuwang</pluginGroup>
  </pluginGroups>
```

* 本地模式(无需启动zookeeper)

> 默认启动端口:9090

启动命令:
```
# 第一种(简称)
cd hello-service
compile isuwangsoa:run -Dsoa.remoting.mode=local

# 第二种
cd hello-service
compile com.isuwang:isuwangsoa-maven-plugin:1.0-SNAPSHOT:run -Dsoa.remoting.mode=local
```

* 远程模式(需要启动zookeeper)

> 默认启动端口:9090

启动命令:
```
# 第一种(简称)
cd hello-service
compile isuwangsoa:run

# 第二种
cd hello-service
compile com.isuwang:isuwangsoa-maven-plugin:1.0-SNAPSHOT:run
```

* 启动可选参数

```
# -Dsoa.zookeeper.host=127.0.0.1:2181
# -Dsoa.container.port=9090
# -Dsoa.monitor.enable=false
```

#### 客户端调用服务

##### 依赖配置

客户端要依赖`hello-api`,`isuwang-soa-registry-zookeeper`和`isuwang-soa-remoting-netty`
```
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-hello-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-registry-zookeeper</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-remoting-netty</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```


##### 本地模式

> 非本地模式不用配置

启动参数:

```
# -Dsoa.remoting.mode=local
```

可选参数:

```
-Dsoa.service.port=9091
```

##### 远程模式

> 无必选参数

可选参数:

```
-Dsoa.zookeeper.host=127.0.0.1:2181
-Dsoa.service.port=9091
```

##### 调用服务测试

测试代码：

```
HelloServiceClient client = new HelloServiceClient();
System.out.println(client.sayHello("LiLei"));
```

#### 文档站点和在线测试

* 启动服务后，在浏览器访问地址：[http://localhost:8080/index.htm](http://localhost:8080/index.htm),点击`api`标签，即可看到当前运行的服务信息：

![API站点页面](http://7xnl6z.com1.z0.glb.clouddn.com/com.isuwang.soa.api_website.png)
    
* 点击对应服务，可以查看该服务相关信息，包括全名称、版本号、方法列表、结构体和枚举类型列表等，点击对应项目可查看详情。
* 从方法详情页面点击在线测试，进入在线测试页面：

![在线测试页面](http://7xnl6z.com1.z0.glb.clouddn.com/com.isuwang.soa.api_test_page.png)

* 输入必填项参数，点击提交请求，即可请求本机当前运行的服务，并获得返回数据：

![请求数据](http://7xnl6z.com1.z0.glb.clouddn.com/com.isuwang.soa.api_test_req.png)

![返回数据](http://7xnl6z.com1.z0.glb.clouddn.com/com.isuwang.soa.api_test_rsp.png)

* 控制台可以看到相应的请求信息：

其中soa-threadPool-1是后台服务打印日志

![控制台打印信息](http://7xnl6z.com1.z0.glb.clouddn.com/com.isuwang.soa.api_test_info.png)












