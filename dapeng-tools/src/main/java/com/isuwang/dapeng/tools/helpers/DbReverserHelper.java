package com.isuwang.dapeng.tools.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 指定数据库以及表,生成相应的scala对象，thrift文件" +
 * @author Eric on 2016-06-30
 */
public class DbReverserHelper {

  public static Properties properties;

  private static String tables;
  private static String db;
  private static String mode;
  private static String packageName;


  private static List<String> tablesToReverse = new ArrayList();;
  /**
   * 檢查參數
   * @param args
   */
  private static void checkArg(String... args){

  }

  /**
   * 生成代碼
   * @param args
   */
  public static void gen(String... args){
    readConf(args[1]);
    readMode();
    intReverseTables();


  }

  /**
   * 读取配置
   * @param file
   */
  public static void readConf(String file){
    try {
      InputStream input = new FileInputStream(new File(file));
      properties = new Properties();
      properties.load(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 读取模式
   */
  public static void readMode(){
    try {
      db= properties.getProperty("db");
      tables= properties.getProperty("tables");
      mode = properties.getProperty("mode");
      packageName = properties.getProperty("package");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void intReverseTables(){

    Connection conn = JdbcUtils.getConnection();
    try{
      if (mode.equalsIgnoreCase("scanAll")) {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = null;
        String[] typeList = {"TABLE"};
        rs = databaseMetaData.getTables(null, null, null, typeList);
        for (boolean more = rs.next(); more; more = rs.next()) {
          String tableName = rs.getString("TABLE_NAME");
          String type = rs.getString("TABLE_TYPE");
          if ((type.equalsIgnoreCase("table")) && (tableName.indexOf("$") == -1)) {
            tablesToReverse.add(tableName);
          }
        }
      } else if (mode.equalsIgnoreCase("specify")) {
        tablesToReverse = Arrays.asList(tables.split(","));
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 生成scala對象
   * @param file
   */
  public static void genPo(String file){

  }
  /**
   * 生成thrift结构体
   * @param file
   */
  public static void genStruct(String file){

  }
  /**
   * 生成thrift枚举
   * @param file
   */
  public static void genEnumes(String file){

  }


  public static class JdbcUtils {

    public static Connection getConnection() {
      try {
        Class.forName(properties.getProperty("dataBaseDriver"));
        String databaseConnectUrl = properties.getProperty("url");
        DriverManager.getConnection(databaseConnectUrl, properties.getProperty("username"), properties.getProperty("password"));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

  }
}
