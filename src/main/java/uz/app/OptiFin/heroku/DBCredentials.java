package uz.app.OptiFin.heroku;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@WebServlet("/getdbcredentials")
public class DBCredentials extends HttpServlet {
    // ***** DATA *********
    private static String DB_JDBC_URL = "";
    private static String DB_LOGIN = "";
    private static String DB_PASSWORD = "";

    // ***** ACCESS *******
    private String LOGIN = "opti-fin-client";
    private String PASSWORD = "heroku-445566";

    // *******************
    static GsonBuilder gsonBuilder;

    private static void refreshCredentials()
    {
        try{
            String uriStr = "postgres://bekmyzbubqr:71a1186ef3295b424924be0f9165dff4e30f86f7fae4f4f405026c0e76ccd889@ec2-52-49-68-244.eu-west-1.compute.amazonaws.com:5432/ddu1rikpfr5jq6";//System.getenv("DATABASE_URL");
            if(uriStr == null)
                return;
            URI dbUri = new URI(uriStr);
            String[] uriSplit = dbUri.getUserInfo().split(":");

            if(uriSplit.length < 2)
                return;

            DB_LOGIN = uriSplit[0];
            DB_PASSWORD = uriSplit[1];
            DB_JDBC_URL = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
        } catch(URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    static {
        gsonBuilder = new GsonBuilder();
        refreshCredentials();
    }

    protected String getRequestRawData(HttpServletRequest req) throws IOException
    {
        Reader bodyReader = req.getReader();
        StringBuilder body = new StringBuilder();
        int c;
        while( (c = bodyReader.read()) != -1)
            body.append((char)c);

        return body.toString();
    }


    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();
        Gson gson = gsonBuilder.create();
        String requestRawData = getRequestRawData(req);
        Map requestJson = gson.fromJson(requestRawData, Map.class);
        Map responseJsonMap = new HashMap<String, String>();
        Object loginObj = requestJson.get("login");
        Object passwordObj = requestJson.get("password");
        Object refresh = requestJson.get("refresh");
        if(refresh == null)
            refresh = "N";

        res.setContentType("application/json");
        if(loginObj == null || passwordObj == null)
        {
            responseJsonMap.put("errcode", "1");
            responseJsonMap.put("errmessage", "INVALID_ARGUMENT");
            String responseStr = gson.toJson(responseJsonMap);
            out.print(responseStr);
            out.flush();
        } else 
        {
            String input_login = loginObj.toString();
            String input_password = passwordObj.toString();

            if(LOGIN.equals(input_login) && PASSWORD.equals(input_password))
            {
                if("Y".equals(refresh.toString()))
                    refreshCredentials();

                responseJsonMap.put("errcode", "0");
                responseJsonMap.put("errmessage", "ACCESS_GRANTED");
                responseJsonMap.put("db_jdbc_url", DB_JDBC_URL);
                responseJsonMap.put("db_login", DB_LOGIN);
                responseJsonMap.put("db_password", DB_PASSWORD);

                String responseStr = gson.toJson(responseJsonMap);
                out.print(responseStr);
                out.flush();
            }
            else
            {
                responseJsonMap.put("errcode", "2");
                responseJsonMap.put("errmessage", "ACCESS_DENIED");
                String responseStr = gson.toJson(responseJsonMap);
                out.print(responseStr);
                out.flush();
            }
        }
    }
}
