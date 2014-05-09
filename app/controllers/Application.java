package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import models.*;

@With(MySecure.class)
public class Application extends Controller {

    public static void index() {
        renderJSON("test");
    }

    public static void test(String body) {
    	JSONRPC2Request reqData = null;
        try {
            reqData = JSONRPC2Request.parse(body);
        } catch (JSONRPC2ParseException e) {
            System.out.println(e.getMessage());
            // Handle exception...
        }

        renderJSON(reqData.toJSONString());
    }
}