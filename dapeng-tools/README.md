### dapeng工具介紹

#### 功能說明

```
1. 通过服务名获取运行信息
2. 通过服务名和版本号，获取元信息
3. 通过json文件，请求对应服务，并打印json格式的结果
4. 通过xml文件，请求对应服务，并打印xml格式的结果
5. 通过系统参数，json文件，调用指定服务器的服务并打印结果
6. 通过系统参数，xml文件，调用指定服务器的服务并打印结果
7. 通过服务名/版本号/方法名，获取请求json的示例
8. 通过服务名/版本号/方法名，获取请求xml的示例
9. 获取当前zookeeper中的服务路由信息
10. 指定配置文件，设置路由信息
```

#### 使用示例

```

1. 通过服务名获取运行信息
   命令：java -jar dapeng.jar runningInfo com.isuwang.soa.hello.service.HelloService
   
   打印结果：
       com.isuwang.soa.hello.service.HelloService    1.0.0   192.168.0.1 9090
       com.isuwang.soa.hello.service.HelloService    1.0.0   192.168.0.1 9091
       com.isuwang.soa.hello.service.HelloService    1.0.1   192.168.0.2 9090
   
   命令： java -jar dapeng.jar runningInfo com.isuwang.soa.hello.service.HelloService 1.0.1
   
   打印结果：
   com.isuwang.soa.hello.service.HelloService    1.0.1   192.168.0.2 9090
   
2. 通过服务名和版本号，获取元信息
   命令：java -jar dapeng.jar metadata com.isuwang.soa.hello.service.HelloService 1.0.1
   
   打印结果：
   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   <service namespace="com.isuwang.soa.hello.service" name="HelloService">
       <doc>
    Hello Service
   </doc>
       <meta>
           <version>1.0.0</version>
           <timeout>30000</timeout>
       </meta>
       <methods>
           <method name="sayHello">
               <doc>
               ...
               
3. 通过json文件，请求对应服务，并打印结果
    命令：java -jar dapeng.jar request request.json
    request.json
    {
      "serviceName": "com.isuwang.soa.hello.service.HelloService",
      "version": "1.0.0",
      "methodName": "sayHello",
      "params": {
        "name":"Tom"
      }
    }
    
4. 通过json文件，请求对应服务，并打印XML格式的结果
    命令：java -jar dapeng.jar request request.xml
    request.xml
    
    <?xml version="1.0" encoding="UTF-8"?>
    <service>
    	<version>1.0.0</version>
    	<methodName>sayHello</methodName>
    	<serviceName>com.isuwang.soa.hello.service.HelloService</serviceName>
    	<params>
    	   "name":"Tom"
    	</params>
    </service>
        
5. 通过系统参数，json文件，调用指定服务器的服务并打印结果
    命令：java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.json
    以上命令会调用运行在192.168.0.1：9091的服务

6. 通过系统参数，xml文件，调用指定服务器的服务并打印结果
    命令：java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.xml
    以上命令会调用运行在192.168.0.1：9091的服务
    
7. 通过服务名/版本号/方法名，获取请求json的示例
    java -jar dapeng.jar json com.isuwang.soa.hello.service.HelloService 1.0.0 sayHello
    打印结果：
    {
      "serviceName": "com.isuwang.soa.hello.service.HelloService",
      "version": "1.0.0",
      "methodName": "sayHello",
      "params": {
        "name":"demoString"
      }
    }
    
8. 通过服务名/版本号/方法名，获取请求xml的示例
    java -jar dapeng.jar json com.isuwang.soa.hello.service.HelloService 1.0.0 sayHello
    打印结果：
    {
      "serviceName": "com.isuwang.soa.hello.service.HelloService",
      "version": "1.0.0",
      "methodName": "sayHello",
      "params": {
        "name":"demoString"
      }
    }
    ```
9. 获取当前zookeeper中的服务路由信息
   java -jar dapeng.jar routInfo 
   
10. 指定配置文件，设置路由信息
   java -jar dapeng.jar routInfo route.cfg
#### 目录说明





