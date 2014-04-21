package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.mvc.Controller;
import play.test.Fixtures;

public class db2yml extends Controller {

    private final static String OUTPUTPATH = "test";
    
    private final static List<String> EXCLUSIONKEY= Arrays.asList("id", "hogehoge");
    
    public static void index() {
        render();
    }
    
    public static void output()
    {
        System.out.println("【output Start】");

        // 使用変数
        List<String> tableList = new ArrayList();
        ArrayList<HashMap<String,String>> valueList = new ArrayList();

        // DBコネクション取得
        // DB一覧取得
        try {
            tableList = getTableList( "select * from pg_tables where not tablename like 'pg%' and schemaname = 'public' order by tablename;" );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("gettablelist END");
        }
        
        // TODO:除外テーブル
        
        // 項目名取得
        // テーブルの値取得
        try {
            for ( String table : tableList ) {
                System.out.println("getValueList Start");
                valueList = getValueList ( "SELECT * FROM " + table + ";" );
                System.out.println("getValueList END");
                // yml形式で出力
                outputYml ( table, valueList );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("【output End】");
        }
        
        String txt = "出力いたしました";
        render("db2yml/index.html", txt);
    }
    
    private static List<String> getTableList (String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        // JDBCドライバの登録
        String driver = "org.postgresql.Driver";
        // データベースの指定
        String server   = "localhost";   // PostgreSQL サーバ ( IP または ホスト名 )
        String dbname   = "pfm_db";         // データベース名
        String url = "jdbc:postgresql://" + server + "/" + dbname;
        String user     = "postgres";         //データベース作成ユーザ名
        String password = "postgres";     //データベース作成ユーザパスワード
        // 返却値
        List<String> ret = new ArrayList<>();
        try {
            Class.forName(driver);

            conn = DriverManager.getConnection( url, user, password );
            stmt = conn.createStatement();

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ret.add(rs.getString("tablename"));
                System.out.println(rs.getString("tablename"));
            }
            return ret;
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
    }

    private static ArrayList<HashMap<String,String>> getValueList (String sql) throws Exception {
//        System.out.println("sql :" + sql);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        // JDBCドライバの登録
        String driver = "org.postgresql.Driver";
        // データベースの指定
        String server   = "localhost";   // PostgreSQL サーバ ( IP または ホスト名 )
        String dbname   = "pfm_db";         // データベース名
        String url = "jdbc:postgresql://" + server + "/" + dbname;
        String user     = "postgres";         //データベース作成ユーザ名
        String password = "postgres";     //データベース作成ユーザパスワード
        try {
            Class.forName(driver);

            conn = DriverManager.getConnection( url, user, password );
            stmt = conn.createStatement();

            rs = stmt.executeQuery(sql);

            //フィールド名取得
            ResultSetMetaData rsmd= rs.getMetaData();
            //データ格納
            ArrayList<HashMap<String, String>> list
               = new ArrayList<HashMap<String, String>>();

            while(rs.next()){
               //1件分のデータ(連想配列)
               HashMap<String, String> hdata = new HashMap<String, String>();
               for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                  //フィールド名
                  String field = rsmd.getColumnName(i);
                  //フィールド名に対するデータ
                  String getdata = rs.getString(field);
                  if (getdata == null) { getdata = ""; }
                  //データ格納(フィールド名, データ)
                  hdata.put(field, getdata);
               }
               //1件分のデータを格納
               list.add(hdata);
            }
            return list;
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
    }

    private static void outputYml ( String table, ArrayList<HashMap<String, String>> data ) throws Exception {
        System.out.println("---------- outputYml Start ----------");
        // 出力する文字列
        String ymlStr = "";
        //格納したデータをすべて表示する
        for (int i = 0; i < data.size(); i++) {
            ymlStr += table + "(" + table + i + "):\n";
            System.out.println("---------- " + (i+1) + "件目データ Start ----------");
            for ( String key : data.get(i).keySet() ) {
                if ( ! EXCLUSIONKEY.contains(key) ) {
                    ymlStr += "    " + key + ":    " + data.get(i).get(key) + "\n";
                    //データ出力
                    System.out.println(key + ":" + data.get(i).get(key));
                }
            }
            ymlStr += "\n";
            System.out.println("---------- " + (i+1) + "件目データ End ----------");
        }
        writeYml(table, ymlStr);
        System.out.println("---------- outputYml End ----------");
    }
    
    private static void writeYml(String table, String outputStr) throws IOException {
        File file = new File(OUTPUTPATH + "/" + table + ".yml");
        FileOutputStream fop = new FileOutputStream(file);
        fop.write("# Created by db2yml.\n\n".getBytes());
        System.out.println("outputStr : " + outputStr);
        fop.write(outputStr.getBytes());
        fop.flush();
        fop.close();
    }
//    private static List<String> getColumnList (String sql) throws Exception {
////        System.out.println("sql :" + sql);
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        // JDBCドライバの登録
//        String driver = "org.postgresql.Driver";
//        // データベースの指定
//        String server   = "localhost";   // PostgreSQL サーバ ( IP または ホスト名 )
//        String dbname   = "pfm_db";         // データベース名
//        String url = "jdbc:postgresql://" + server + "/" + dbname;
//        String user     = "postgres";         //データベース作成ユーザ名
//        String password = "postgres";     //データベース作成ユーザパスワード
//        // 返却値
//        List<String> ret = new ArrayList<>();
//        try {
//            Class.forName(driver);
//
//            conn = DriverManager.getConnection( url, user, password );
//            stmt = conn.createStatement();
//
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                ret.add(rs.getString("attname"));
//                System.out.println(rs.getString("attname"));
//            }
//            return ret;
//        } finally {
//            if (rs != null)
//                rs.close();
//            if (stmt != null)
//                stmt.close();
//            if (conn != null)
//                conn.close();
//        }
//    }
//
//    private static List<Map> getValueList (String sql, int num) throws Exception {
////        System.out.println("sql :" + sql);
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        // JDBCドライバの登録
//        String driver = "org.postgresql.Driver";
//        // データベースの指定
//        String server   = "localhost";   // PostgreSQL サーバ ( IP または ホスト名 )
//        String dbname   = "pfm_db";         // データベース名
//        String url = "jdbc:postgresql://" + server + "/" + dbname;
//        String user     = "postgres";         //データベース作成ユーザ名
//        String password = "postgres";     //データベース作成ユーザパスワード
//        // 返却値
//        HashMap map = new HashMap();
//        List<Map> ret = new ArrayList<>();
//        try {
//            Class.forName(driver);
//
//            conn = DriverManager.getConnection( url, user, password );
//            stmt = conn.createStatement();
//
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                map.clear();
//                for (int i = 0; i < num; i++ ) {
//                    map.put("項目1", rs.getString("項目1"));
//                }
//                map.put("項目2", rs.getString("項目2"));
//                map.put("項目3", rs.getString("項目3"));
//                ret.add(rs.getString("attname"));
//                System.out.println(rs.getString("attname"));
//            }
//            return ret;
//        } finally {
//            if (rs != null)
//                rs.close();
//            if (stmt != null)
//                stmt.close();
//            if (conn != null)
//                conn.close();
//        }
//    }
}
