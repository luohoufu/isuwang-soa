package com.isuwang.dapeng.tools.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.metadata.Service;
import com.isuwang.soa.remoting.fake.json.JSONPost;
import com.isuwang.soa.remoting.filter.LoadBalanceFilter;
import net.sf.json.xml.XMLSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * @author Eric on 2016/2/15.
 */
public class RequestHelper {

    private static JSONPost jsonPost;

    private static final String HOST = "soa.service.ip";
    private static final String PORT = "soa.service.port";

    public static void post(String... args) {

        String jsonFile = checkArg(args);

        if (jsonFile == null) return;


        String jsonString = null;
        boolean isJson=false;
        if(jsonFile.endsWith(".json")){
            isJson=true;
            jsonString = readFromeFile(jsonFile);
        }else if(jsonFile.endsWith(".xml")){
            jsonString = parseFromXmlToJson(jsonFile);
        }

        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();


        String serviceName = jsonObject.get("serviceName").getAsString();
        String versionName = jsonObject.get("version").getAsString();
        String methodName = jsonObject.get("methodName").getAsString();
        String parameter = jsonObject.get("params").toString();

        SoaHeader header = new SoaHeader();
        header.setServiceName(serviceName);
        header.setVersionName(versionName);
        header.setMethodName(methodName);
        header.setCallerFrom(Optional.of("dapeng-command")) ;

        invokeService(serviceName, versionName, methodName, header, parameter);
    }

    private static String checkArg(String... args) {
        if (args.length != 2) {
            System.out.println("example: java -jar dapeng.jar request request.json");
            System.out.println("         java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.json");
            System.out.println("         java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.xml");
            System.exit(0);
        }
        String jsonFile = args[1];

        File requestFile = new File(jsonFile);
        if (!requestFile.exists()) {
            System.out.println("文件(" + requestFile + ")不存在");
            return null;
        }
        return jsonFile;
    }


    private static void invokeService(String serviceName, String versionName, String methodName, SoaHeader header, String parameter) {

        System.out.println("Getting service from server...");
        Service service = ServiceCache.getService(serviceName, versionName);

        if (service == null) {
            System.out.println("没有找到可用服务");
            return;
        }

        System.out.println("Getting caller Info ...");
        String callerInfo = LoadBalanceFilter.getCallerInfo(serviceName, versionName, methodName);

        if (callerInfo != null) {
            String[] infos = callerInfo.split(":");
            jsonPost = new JSONPost(infos[0], Integer.valueOf(infos[1]), true);

        } else if (SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local")) {
            jsonPost = new JSONPost(SoaSystemEnvProperties.SOA_SERVICE_IP, SoaSystemEnvProperties.SOA_SERVICE_PORT, true);

        } else {
            System.out.println("{\"message\":\"没找到可用服务\"}");
            return;
        }

        try {
            System.out.println("Calling Service ...");
            System.out.println(jsonPost.callServiceMethod(header, parameter, service));
        } catch (Exception e) {

        }
    }

    private static String parseFromXmlToJson(String file) {
        SAXReader sax = new SAXReader();

        return null;
    }

    private static String readFromeFile(String jsonFile) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(jsonFile), StandardCharsets.UTF_8);
            for (String line : lines) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
