package controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import play.mvc.Controller;
import play.test.Fixtures;

public class db2yml extends Controller {

    public static void index() {
        render();
    }
    
    public static void download()
    {
        System.out.println("【download Start】");

        // 使用変数
        List<String> tableList = new ArrayList();
        ArrayList<Hashtable<String,String>> valueList = new ArrayList();

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
                // TODO:ysml形式で出力
                outputYml ( valueList );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("【download End】");
        }
        
        String txt = "ダウンロードいたしました";
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

    private static ArrayList<Hashtable<String,String>> getValueList (String sql) throws Exception {
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
            ArrayList<Hashtable<String, String>> list
               = new ArrayList<Hashtable<String, String>>();

            while(rs.next()){
               //1件分のデータ(連想配列)
               Hashtable<String, String> hdata
                  = new Hashtable<String, String>();
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

    private static void outputYml (ArrayList<Hashtable<String, String>> data) throws Exception {
        System.out.println("---------- outputYml Start ----------");
        //格納したデータをすべて表示する
        for (int i = 0; i < data.size(); i++) {
           //データi件目のフィールド名リストを取得
           Enumeration<String> keyList = data.get(i).keys();
           System.out.println("---------- " + (i+1) + "件目データ ----------");
           while(keyList.hasMoreElements()) {
              //フィールド名取得
              String key = (String)keyList.nextElement();
              //データ出力
              System.out.println(key + ":" + data.get(i).get(key));
           }
        }
        System.out.println("---------- outputYml End ----------");
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
