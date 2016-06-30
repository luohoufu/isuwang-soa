package com.isuwang.dapeng.tools.helpers;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Eric
 */
public class XmlConverUtil {
    private static XMLSerializer xmlserializer = new XMLSerializer();

    private static final String STR_JSON_OBJECT = "{" +
            "\"name\": \"小王\"," +
            "\"age\": \"20\"," +
            "\"add\": \"北京\"," +
            "\"gender\": \"男\"" +
            "}";
    private static final String STR_JSON_ARRAY = "[{" +
            "\"name\": \"小王\"," +
            "\"age\": \"20\"," +
            "\"add\": \"北京\"," +
            "\"gender\": \"男\"" +
            "}," +
            "{" +
            "\"name\": \"小李\"," +
            "\"age\": \"21\"," +
            "\"add\": \"上海\"," +
            "\"gender\": \"女\"" +
            "}]";

    /**
     * xml格式字符串转化成jsonObject或者jsonArray
     *
     * @param xml
     * @return
     */
    public static String xml2json(String xml) {
        String rs = "";
        try {
            rs = xmlserializer.read(xml).toString();
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("xml转化为json异常...");
        }
        return rs;
    }

    /**
     * jsonArray或者jsonObject字符串转化为xml
     *
     * @param json
     * @return
     */
    public static String json2xml(String json) {
        String rs = "";
        try {
            if (json.contains("[") && json.contains("]")) {
                //jsonArray
                JSONArray jobj = JSONArray.fromObject(json);
                rs = xmlserializer.write(jobj);
            } else {
                //jsonObject
                JSONObject jobj = JSONObject.fromObject(json);
                rs = xmlserializer.write(jobj);
            }

        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("jsonArray转化为xml异常...");
        }
        return rs;
    }

    /***
     * json和xml文件互转
     *
     * @param sourcePath json文件的路径
     * @param outPath    xml文件的路径
     * @param enterFlag  转化标识 1表示json转化为xml
     *                   2表示xml转化为json
     * @return
     */
    public static String jfxfTranspose(String sourcePath, String outPath, int enterFlag) {
        FileInputStream in = null;
        BufferedReader br = null;
        FileWriter fw = null;
        String rs = null;
        try {
            File jsonFile = new File(sourcePath);
            in = new FileInputStream(jsonFile);
            StringBuffer sbuf = new StringBuffer();
            br = new BufferedReader(new InputStreamReader(in));
            String temp = null;

            while ((temp = br.readLine()) != null) {
                sbuf.append(temp);
            }
            if (1 == enterFlag) {
                rs = json2xml(sbuf.toString());
            } else {
                rs = xml2json(sbuf.toString());
            }
            File test = new File(outPath);
            if (!test.exists()) {
                test.createNewFile();
            }
            fw = new FileWriter(test);
            fw.write(rs);
        } catch (Exception e) {
            System.err.println("json和xml转化文件异常...");
        } finally {
            try {
                fw.close();
                br.close();
                in.close();
            } catch (Exception e) {
                System.err.println("输入、输出流关闭异常");
                e.printStackTrace();
            }
        }
        return rs;
    }


    public static void main(String[] args) throws IOException {
//        String xml1 = json2xml(STR_JSON_OBJECT);
//        String xml1 = json2xml(STR_JSON_ARRAY);
//        System.out.println("xml ==>"+xml1);
//        String json1 = new XMLSerializer().readFromFile(new File("C:\\Users\\Shadow\\Desktop\\XMLRequest.xml")).toString();
        String json1 = xml2json(readFromeFile("C:\\Users\\Shadow\\Desktop\\XMLRequest.xml"));
        System.out.println("json==>" + json1);
//
//        String spath2 = "F:/WorkSpace/JavaProject/src/com/test/util/gao.json";
//        String opath2 = "F:/WorkSpace/JavaProject/src/com/test/util/testGao.xml";
//        String rs2 =  jfxfTranspose(spath2,opath2,1);
//        System.out.print(rs2);
//
//        String spath1 = "F:/WorkSpace/JavaProject/src/com/test/util/testGao.xml";
//        String opath1 = "F:/WorkSpace/JavaProject/src/com/test/util/test.json";
//        String rs1 =  jfxfTranspose(spath1,opath1,2);
//        System.out.print(rs1);

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
