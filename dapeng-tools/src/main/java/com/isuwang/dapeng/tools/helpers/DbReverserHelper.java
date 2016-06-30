package com.isuwang.dapeng.tools.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

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
  private static Map<String, Map<String, String>> tablesToReverseMeta = new HashMap();

  public static final String arraySeperator = ">";

  public static Connection conn ;

  static{
    conn = JdbcUtils.getConnection();
  }
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
    checkArg(args);

    readConf(args[1]);
    readMode();
    intReverseTables();
    intMetaInfos();

    if (tablesToReverseMeta.size() == 0) {
      System.out.println("No new created table has been detected");
    }
    for (Map.Entry<String, Map<String, String>> entry : tablesToReverseMeta.entrySet()) {
       reverse((String) entry.getKey(), (Map) entry.getValue());
    }
  }

  public static void reverse(String key, Map value){

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
  public static void intMetaInfos(){
    for (String newAddedTable : tablesToReverse) {
      try {
        ResultSet rs = conn.getMetaData().getColumns(null, "%", newAddedTable, "%");
        LinkedHashMap<String, String> map = new LinkedHashMap();
        tablesToReverseMeta.putIfAbsent(newAddedTable, map);
        String colname;
        String typeName;
        String remark;
        int nullAble;
        boolean nullable = true;
        while (rs.next()) {
           colname = rs.getString("COLUMN_NAME");
           typeName = rs.getString("TYPE_NAME");
           remark = rs.getString("REMARKS");
           nullAble = rs.getInt("NULLABLE");
          switch (nullAble) {
            case 0:
              nullable = false;
              break;
            case 1:
              nullable = true;
              break;
            default:
              nullable = true;
          }
          String colmunSize = rs.getString("COLUMN_SIZE");
          if (colmunSize != null) {
            map.putIfAbsent(colname, typeName + arraySeperator + nullable + arraySeperator + remark + arraySeperator + colmunSize);
          } else {
            map.putIfAbsent(colname, typeName + arraySeperator + nullable + arraySeperator + remark);
          }
        }
      }catch (Exception e){
        e.printStackTrace();
      }
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

  /**
   * JdbcUtils
   */
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
  /**
   * 工具
   */
  public static class Utils {

    public static final char UNDERLINE = '_';

    public static String underlineToCamel(boolean transferFiled, String param, boolean firstLetterToUpper) {
      if (transferFiled && param.equals("type")) {
        return "`type`";
      }
      if ((param == null) || ("".equals(param.trim()))) {
        return "";
      }
      int len = param.length();
      StringBuilder sb = new StringBuilder(len);
      for (int i = 0; i < len; i++) {
        char c = param.charAt(i);
        if (i == 0) {
          if ((firstLetterToUpper) && (c >= 'a') && (c <= 'z')) {
            sb.append((char) (c - 32));
          } else if ((!firstLetterToUpper) && (c >= 'A') && (c <= 'Z')) {
            sb.append((char) (c + 32));
          } else {
            sb.append(c);
          }
        } else if (c == UNDERLINE) {
          i++;
          if (i < len) {
            sb.append(Character.toUpperCase(param.charAt(i)));
          }
        } else {
          sb.append(c);
        }
      }

      return sb.toString();
    }

    public static String underlineToCamel(String param) {
      if ((param == null) || ("".equals(param.trim()))) {
        return "";
      }
      int len = param.length();
      StringBuilder sb = new StringBuilder(len);
      char c;
      for (int i = 0; i < len; i++) {
        c = param.charAt(i);
        if ((c >= 'A') && (c <= 'Z')) {
          sb.append("_").append((char) (c + 32));
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }
    public static String toScalaType(String type) {
      if (type.equalsIgnoreCase("CHAR")
              || type.equalsIgnoreCase("VARCHAR")
              || type.equalsIgnoreCase("TINYBLOB")
              || type.equalsIgnoreCase("TINYTEXT")
              || type.equalsIgnoreCase("BLOB")
              || type.equalsIgnoreCase("TEXT")
              || type.equalsIgnoreCase("MEDIUMBLOB")
              || type.equalsIgnoreCase("MEDIUMTEXT")
              || type.equalsIgnoreCase("LOGNGBLOB")
              || type.equalsIgnoreCase("LONGTEXT")) {
        return "String";
      }
      if (type.equalsIgnoreCase("DOUBLE")) {
        return "Double";
      }
      if (type.equalsIgnoreCase("ENUM")) {
        return "String";
      }
      if (type.equalsIgnoreCase("DECIMAL")) {
        return "BigDecimal";
      }
      if (type.equalsIgnoreCase("TINYINT")
              || type.equalsIgnoreCase("SMALLINT")
              || type.equalsIgnoreCase("MEDIUMINT")
              || type.equalsIgnoreCase("INT")
              || type.equalsIgnoreCase("BIT")
              || type.equalsIgnoreCase("BIGINT")) {
        return "Int";
      }

      if (type.equalsIgnoreCase("TIME") || type.equalsIgnoreCase("YEAR")) {
        return "String";
      }
      if (type.equalsIgnoreCase("DATE")) {
        return "java.sql.Date";
      }
      if (type.equalsIgnoreCase("DATETIME")) {
        return "java.sql.Timestamp";
      }
      if (type.equalsIgnoreCase("TIMESTAMP")) {
        return "java.sql.Timestamp";
      }
      if (type.equalsIgnoreCase("LONGBLOB")) {
        return "Array[Byte]";
      }

      return type;
    }


    public static String toThriftType(String type) {
      if (type.equalsIgnoreCase("CHAR")
              || type.equalsIgnoreCase("VARCHAR")
              || type.equalsIgnoreCase("TINYBLOB")
              || type.equalsIgnoreCase("TINYTEXT")
              || type.equalsIgnoreCase("BLOB")
              || type.equalsIgnoreCase("TEXT")
              || type.equalsIgnoreCase("MEDIUMBLOB")
              || type.equalsIgnoreCase("MEDIUMTEXT")
              || type.equalsIgnoreCase("LOGNGBLOB")
              || type.equalsIgnoreCase("LONGTEXT")) {
        return "string";
      }
      if (type.equalsIgnoreCase("DOUBLE")) {
        return "double";
      }
      if (type.equalsIgnoreCase("DECIMAL")) {
        return "double";
      }
      if (type.equalsIgnoreCase("TINYINT")
              || type.equalsIgnoreCase("BIT")
              || type.equalsIgnoreCase("SMALLINT")
              || type.equalsIgnoreCase("MEDIUMINT")
              || type.equalsIgnoreCase("INT")
              || type.equalsIgnoreCase("BIGINT")) {
        return "i32";
      }

      if (type.equalsIgnoreCase("TIME") || type.equalsIgnoreCase("YEAR")) {
        return "string";
      }
      if (type.equalsIgnoreCase("DATE")) {
        return "i64";
      }
      if (type.equalsIgnoreCase("DATETIME")) {
        return "i64";
      }
      if (type.equalsIgnoreCase("Timestamp")) {
        return "i64";
      }
      if (type.equalsIgnoreCase("java.sql.Timestamp")) {
        return "i64";
      }
      if (type.equalsIgnoreCase("ENUM")) {
        return "string";
      }
      if (type.equalsIgnoreCase("LONGBLOB")) {
        return "binary";
      }
      return type;
    }
  }
}
