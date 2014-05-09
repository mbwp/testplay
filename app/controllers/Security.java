package controllers;

import java.io.IOException;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import com.google.gson.Gson;

public class Security extends MySecure.Security {
    static Object authenticate(final String body) {
    	// JSON形式で認証を行う
    	HttpResponse retAuth = null;
    	MySecure.ReturnData rData = null;
		try {
			retAuth = jsonAuthenticate(body);
			rData = new Gson().fromJson(retAuth.getJson(), MySecure.ReturnData.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rData;
    }

    static HttpResponse jsonAuthenticate(String body) throws IOException {
    	// localhost の場合、TimeOutExceptionでエラーとなってしまうため、別のサーバとしてPort9001で立ち上げ動くようにする。
    	String authorizeUrl = Play.configuration.getProperty("mysecure.login.url", "http://localhost:9001/AuthenticationStub/login");
//    	String authorizeUrl = "http://localhost:9001/AuthenticationStub/login";
//    	String clientId = "loginId";
//    	String clientSecret = "password";
//    	WSRequest wsrequest = WS.url(authorizeUrl).authenticate(clientId, clientSecret).timeout("5s");

//    	HttpResponse res = WS.url("http://localhost:9000/AuthenticationStub/login").post();
//    	String authorizeUrl = "http://weather.livedoor.com/forecast/webservice/json/v1?city=400040";

    	WSRequest wsrequest = WS.url(authorizeUrl).timeout("5s");
    	wsrequest.setHeader("Content-Type", "application/json");
    	wsrequest.body = body;
    	HttpResponse retAuth = wsrequest.post();
    	return retAuth;
    }
}
