package com.isuwang.dapeng.tools;

import com.isuwang.dapeng.tools.helpers.*;

/**
 * 命令行工具入口
 */
public class SoaInfos {
    private static final String RUNNING_INFO = "runningInfo";
    private static final String META_DATA = "metadata";
    private static final String REQUEST = "request";
    private static final String JSON = "json";
    private static final String ROUT_INFO = "routInfo";

    private static String help = "-----------------------------------------------------------------------\n" +
            " |commands: runningInfo | metadata | request | json  \n" +
            " | 通过指定服务名，或服务名+版本号，获取对应的服务的容器ip和端口: \n" +
            " |    java -jar dapeng.jar runningInfo com.isuwang.soa.hello.service.HelloService\n" +
            " |    java -jar dapeng.jar runningInfo com.isuwang.soa.hello.service.HelloService 1.0.1\n" +
            " | 通过服务名和版本号，获取元信息: \n" +
            " |    java -jar dapeng.jar metadata com.isuwang.soa.hello.service.HelloService 1.0.1\n" +
            " | 通过json文件，请求对应服务，并打印结果: \n" +
            " |    java -jar dapeng.jar request request.json\n" +
            " | 通过系统参数，json文件，调用指定服务器的服务并打印结果: \n" +
            " |    java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.json\n" +
            " | 通过服务名/版本号/方法名，获取请求json的示例: \n" +
            " |    java -jar dapeng.jar json com.isuwang.soa.hello.service.HelloService 1.0.0 sayHello\n" +
            "-----------------------------------------------------------------------";

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            System.out.println(help);
            System.exit(0);
        }

        switch (args[0]) {
            case RUNNING_INFO:
                ZookeeperSerachHelper.getInfos(args);
                break;
            case META_DATA:
                MetaInfoHelper.getService(args);
                break;
            case REQUEST:
                JsonRequestHelper.postJson(args);
                break;
            case JSON:
                JsonRequestExampleHelper.getRequestJson(args);
                break;
            case ROUT_INFO:
                RouteInfoHelper.routeInfo(args);
                break;
            default:
                System.out.println(help);
        }

        System.exit(0);
    }
}
