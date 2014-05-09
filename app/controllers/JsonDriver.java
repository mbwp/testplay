package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import play.mvc.Controller;

import com.google.gson.Gson;

public class JsonDriver extends Controller {
	public class User {
	    String id;
	    String text;
	}
	
	public static void index() {
        render();
    }

    public static void getjsondata() throws Exception{
    	User user = new Gson().fromJson(inputStreemToString(request.body), User.class);
    	String retJsonString = new Gson().toJson(user);
        renderJSON(retJsonString);
    }
    
    private static String inputStreemToString(InputStream in) throws IOException{
        
        BufferedReader reader = 
            new BufferedReader(new InputStreamReader(in, "UTF-8"/* 文字コード指定 */));
        StringBuffer buf = new StringBuffer();
        String str;
        while ((str = reader.readLine()) != null) {
                buf.append(str);
                buf.append("\n");
        }
        return buf.toString();
    }
}
