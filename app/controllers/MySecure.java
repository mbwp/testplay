package controllers;
/**
 * 
 * もとのSecureクラスの場合、checkAccess()、login()をそのまま使用できないため、
 * 新しいMySecureクラスとして作成
 * 
 */
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.utils.Java;

import com.google.gson.Gson;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

public class MySecure extends Controller {
	public class ReqeustData {
	    public String pfmToken;
	}
	public class ReturnData {
		public boolean status;
	    public String token;
	}

    @Before(unless={"login", "authenticate", "logout"})
    static void checkAccess(String body) throws Throwable {
    	ReturnData resData = null;
    	// body にTokenがあるか確認
    	ReqeustData reqData = new Gson().fromJson(body, ReqeustData.class);
    	
    	// 認証基盤にTOKEN連携(TOKENが存在し認証基盤もOKであれば、何も行わない)
    	if ( reqData != null && !"".equals(reqData.pfmToken) ) 
    	{
    		// 存在する場合、認証基盤に問い合わせ
    		resData = (ReturnData)Security.invoke("authenticate", new Gson().toJson(reqData));
    		if ( resData != null && resData.status != false ) {
        		// 認証基板OK
    		} else {
        		// 認証基盤NG
        		renderJSON(resData);
    		}
    	}
    	else
    	{
    		// 存在しない場合、認証失敗のJsonを返却
    		resData = new MySecure().new ReturnData();
    		resData.status = false;
    		resData.token = "";
    		renderJSON(resData);
    	}
    }

    // ~~~ Login

    /**
     * 
     * アプリからの連携のため不要だが、画面からの確認のため、残しておく。
     * 
     * @throws Throwable
     */
    public static void login() throws Throwable {
        Http.Cookie remember = request.cookies.get("rememberme");
        if(remember != null) {
            int firstIndex = remember.value.indexOf("-");
            int lastIndex = remember.value.lastIndexOf("-");
            if (lastIndex > firstIndex) {
                String sign = remember.value.substring(0, firstIndex);
                String restOfCookie = remember.value.substring(firstIndex + 1);
                String username = remember.value.substring(firstIndex + 1, lastIndex);
                String time = remember.value.substring(lastIndex + 1);
                Date expirationDate = new Date(Long.parseLong(time)); // surround with try/catch?
                Date now = new Date();
                if (expirationDate == null || expirationDate.before(now)) {
                    logout();
                }
                if(Crypto.sign(restOfCookie).equals(sign)) {
                    session.put("username", username);
//                    redirectToOriginalURL();
                }
            }
        }
        flash.keep("url");
        render();
    }
    
    public static void authenticate(String body) throws Throwable {
        // TODO: Check tokens

    	// 認証連携
    	ReturnData resData = (ReturnData)Security.invoke("authenticate", new Gson().toJson(body));
    	String retJsonString = new Gson().toJson(resData);
        renderJSON(retJsonString);
    }

    public static void logout() throws Throwable {
    	// 認証基盤にログアウト情報を連携する
    	
    }

    // ~~~ Utils

    public static class Security extends Controller {

        /**
         * @Deprecated
         * 
         * @param username
         * @param password
         * @return
         */
        static boolean authentify(String username, String password) {
            throw new UnsupportedOperationException();
        }

        /**
         * This method is called during the authentication process. This is where you check if
         * the user is allowed to log in into the system. This is the actual authentication process
         * against a third party system (most of the time a DB).
         *
         * @param username
         * @param password
         * @return true if the authentication process succeeded
         */
        static boolean authenticate(String username, String password) {
            return true;
        }

        /**
         * This method checks that a profile is allowed to view this page/method. This method is called prior
         * to the method's controller annotated with the @Check method. 
         *
         * @param profile
         * @return true if you are allowed to execute this controller method.
         */
        static boolean check(String profile) {
            return true;
        }

        /**
         * This method returns the current connected username
         * @return
         */
        static String connected() {
            return session.get("username");
        }

        /**
         * Indicate if a user is currently connected
         * @return  true if the user is connected
         */
        static boolean isConnected() {
            return session.contains("username");
        }

        /**
         * This method is called after a successful authentication.
         * You need to override this method if you with to perform specific actions (eg. Record the time the user signed in)
         */
        static void onAuthenticated() {
        }

         /**
         * This method is called before a user tries to sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the name of the user who signed off)
         */
        static void onDisconnect() {
        }

         /**
         * This method is called after a successful sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
         */
        static void onDisconnected() {
        }

        /**
         * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
         * @param profile
         */
        static void onCheckFailed(String profile) {
            forbidden();
        }

        private static Object invoke(String m, Object... args) throws Throwable {

            try {
                return Java.invokeChildOrStatic(Security.class, m, args);       
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

}
