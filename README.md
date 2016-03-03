### Maven package container

#### 运行脚本

```
cd isuwang-soa-container

sh dev.sh
```

#### 输出目录

```
isuwang-soa-container/target/isuwang-soa-container
```

### isuwang-soa简易使用教程

以下教程使用`hello`作为示例。


#### 使用thrift的IDL定义服务接口

hello_service.thrift：
```
namespace java com.isuwang.soa.hello.service
/**
* Hello Service
**/
service HelloService {
    /**
    * say hello
    **/
    string sayHello(1:string name, 2:string msg);
}
```

#### 生成基础代码：

使用`isuwang-soa-code-generator-1.0-SNAPSHOT.jar`包和上面的idl文件生成基础代码：

1. 命令为：`java -jar isuwang-soa-code-generator-1.0-SNAPSHOT.jar -gen java -out F:\hello F:\hello\hello_service.thrift`；
2. `-out F:\hello`表示生成代码到`F:\hello`文件夹；
3. 多个thrift文件使用`,`分隔； 
4. 生成的xml文件在根目录，java类在`java-gen`文件夹。


#### 创建API工程

`hello-api`工程会被服务端和客户端依赖。

新建已经maven工程，即`hello-api`工程，依赖于`isuwang-soa-core`和`isuang-soa-rpc`:
```
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.isuwang</groupId>
    <artifactId>isuwang-soa-rpc</artifactId>
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

将上一步中生成的java代码，拷贝入对应的package。将上一步中生成的xml文件，拷贝入`resources`文件夹。

最后`install`项目。

#### 创建Service工程

`hello-service`工程依赖于`hello-api`工程和`isuwang-soa-spring`包，在工程中实现api中的接口类，在方法中实现具体的业务逻辑。

1. 依赖：
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

2. 实现类：
    ```
    public class HelloServiceImpl implements HelloService {
        @Override
        public String sayHello(String name, String msg) throws SoaException {
            return "nice to meet you too, " + name;
        }
    }
    ```

3. 声明服务，使得容器启动时加载和注册该服务：

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
    
#### 启动服务

1. 本地启动zookeeper
2. `hello-service`添加maven插件：
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
    
3. 在`hello-service`目录下使用maven命令启动service:
    `compile com.isuwang:isuwangsoa-maven-plugin:1.0-SNAPSHOT:run` or `compile isuwangsoa:run`

到此为止，插件会启动容器，加载服务并向zookeeper注册。

#### 客户端调用服务

客户端要依赖`hello-api`
```
HelloServiceClient client = new HelloServiceClient();
System.out.println(client.sayHello("Xiaoming", "nice to meet you")));
```







