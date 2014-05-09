package controllers;

import java.io.IOException;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import com.google.gson.Gson;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class Security extends MySecure.Security {
    static JSONRPC2Response authenticate(String url, JSONRPC2Request reqData) {
        // JSON形式で認証を行う
        JSONRPC2Response respData = null;
        try {
            respData = wsrequestJson(url, reqData);
        } catch (JSONRPC2ParseException e) {
            e.printStackTrace();
        }
        return respData;
    }

    static JSONRPC2Response wsrequestJson(String url, JSONRPC2Request reqData) throws JSONRPC2ParseException {

        WSRequest wsrequest = WS.url(url).timeout("5s");
        wsrequest.setHeader("Content-Type", "application/json");
        wsrequest.body = reqData.toJSONString();
        HttpResponse retAuth = wsrequest.post();
        return JSONRPC2Response.parse(retAuth.getString());
    }
}
