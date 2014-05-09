package controllers;
/**
 * 
 * もとのSecureクラスの場合、checkAccess()、login()をそのまま使用できないため、
 * 新しいMySecureクラスとして作成
 * 
 */
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import play.Play;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.utils.Java;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class MySecure extends Controller {
    @Before(unless={"login", "authenticate", "logout"})
    static void checkAccess(String body) throws Throwable {
        JSONRPC2Request reqData = null;
        JSONRPC2Response respData = null;
        // confのmysecure.confirmの値がtrueの場合のみチェックを行う。
        if ( "true".equals(Play.configuration.get("mysecure.confirm")) )
        {
            // body にTokenがあるか確認
            try {
                reqData = JSONRPC2Request.parse(body);
            } catch (JSONRPC2ParseException e) {
                System.out.println(e.getMessage());
                // Handle exception...
            }
            
            // 認証基盤にTOKEN連携(TOKENが存在し認証基盤もOKであれば、何も行わない)
            if ( reqData != null && !"".equals(reqData.getNamedParams().get("pfmToken")) ) 
            {
                // 存在する場合、認証基盤に問い合わせ
                // localhost の場合、TimeOutExceptionでエラーとなってしまうため、別のサーバとしてPort9001で立ち上げ動くようにする。
                String checkUrl = Play.configuration.getProperty("mysecure.check.url", "http://localhost:9001/AuthenticationStub/login");
                respData = (JSONRPC2Response)Security.invoke("authenticate", checkUrl, reqData);
            	JSONObject obj = (JSONObject)respData.getResult();
            	boolean status = ((Boolean)obj.get("status")).booleanValue();
                if ( respData != null && respData.indicatesSuccess() && status ) {
                    // 認証基板OK
                } else {
                    // TODO: 認証基盤NG
                    respData = new JSONRPC2Response("token check error",reqData.getID());
                    renderJSON(respData.toJSONString());
                }
            }
            else
            {
                // TODO: 存在しない場合、認証失敗のJsonを返却
                respData = new JSONRPC2Response("token check error",reqData.getID());
                renderJSON(respData.toJSONString());
            }
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
        JSONRPC2Request reqData = null;
        try {
            reqData  = JSONRPC2Request.parse(body);
        } catch (JSONRPC2ParseException e) {
            System.out.println(e.getMessage());
            // Handle exception...
        }

        // TODO: Check tokens

        // 認証連携
        // localhost の場合、TimeOutExceptionでエラーとなってしまうため、別のサーバとしてPort9001で立ち上げ動くようにする。
        String authorizeUrl = Play.configuration.getProperty("mysecure.authenticate.url", "http://localhost:9001/AuthenticationStub/login");
        JSONRPC2Response resData = (JSONRPC2Response)Security.invoke("authenticate", authorizeUrl, reqData);
        String retJsonString = resData.toJSONString();
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
