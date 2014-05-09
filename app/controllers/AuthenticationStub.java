package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import play.mvc.Controller;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class AuthenticationStub extends Controller {

    public static void login(String body) {
        // 受け取った値で認証（スタブのため、処理割愛）
        JSONRPC2Request reqData = null;
        try {
            reqData = JSONRPC2Request.parse(body);
        } catch (JSONRPC2ParseException e) {
            System.out.println(e.getMessage());
            // Handle exception...
        }
        
        System.out.println("Parsed request with properties :");
        System.out.println("\tmethod     : " + reqData.getMethod());
        System.out.println("\tparameters : " + reqData.getNamedParams());
        System.out.println("\tid         : " + reqData.getID() + "\n\n");

        // 返却値を作成
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("status", true);
        result.put("token", "test");
        JSONRPC2Response respData = new JSONRPC2Response(result, reqData.getID());

        System.out.println("The request succeeded :");
        System.out.println("\tresult : " + respData.getResult());
        System.out.println("\tid     : " + respData.getID());
        System.out.println("\ttoJSONString     : " + respData.toJSONString());
        
        renderJSON(respData.toJSONString());
    }
}
