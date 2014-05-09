package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import play.mvc.Controller;

public class AuthenticationStub extends Controller {
	public class ReturnData {
		public boolean status;
	    public String token;
	}

    public static void login() {
    	// 受け取った値で認証（スタブのため、処理割愛）
    	
    	// 返却値を作成
    	AuthenticationStub.ReturnData ret = new AuthenticationStub().new ReturnData();
    	ret.status = true;
    	ret.token = "test";
        renderJSON(ret);
    }
}
