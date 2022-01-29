package uz.app.OptiFin.heroku;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


@WebServlet("/getdbcredentials")
public class DBCredentials extends HttpServlet {
    // ***** DATA *********
    private static String DB_JDBC_URL = "";
    private static String DB_LOGIN = "";
    private static String DB_PASSWORD = "";

    // ***** ACCESS *******
    private static String LOGIN = "opti-fin-client";
    private static String PASSWORD = "";
    //private static HashMap<String, String> connectionUsers;

    // *******************
    static GsonBuilder gsonBuilder;
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        gsonBuilder = new GsonBuilder();
        refreshCredentials();
        refreshConnectionUser();
    }
    // *******************************************************************

    private static HashMap<String, Object> getConnectionUser(String login) throws SQLException {
        HashMap<String, Object> user = null;

        Connection conn = DriverManager.getConnection(DB_JDBC_URL, DB_LOGIN, DB_PASSWORD);
        PreparedStatement pStatement = conn.prepareStatement("select u.* from service.sc_connection_users u where u.login = ?");
        pStatement.setString(1, login);
        ResultSet rs = pStatement.executeQuery();
        if(rs.next()) {
            user = new HashMap<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                user.put(rsmd.getColumnName(i), rs.getObject(i));
            }
        }

        return user;
    }

    private static String sha256Hash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return "";
        }
        
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) 
              hexString.append('0');
            hexString.append(hex);
        }

        String hashStr = hexString.toString();
        return hashStr;
    }

    private static void refreshCredentials()
    {
        try{
            String uriStr = System.getenv("DATABASE_URL");//"postgres://bekmyzbubqr:71a1186ef3295b424924be0f9165dff4e30f86f7fae4f4f405026c0e76ccd889@ec2-52-49-68-244.eu-west-1.compute.amazonaws.com:5432/ddu1rikpfr5jq6";
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

    private static void refreshConnectionUser()
    {
        HashMap<String, Object> user = null;
        try {
            user = getConnectionUser(LOGIN);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        if(user == null) 
            throw new RuntimeException("NO_SUCH_USER_FOUND");

        PASSWORD = user.get("password_hash").toString();
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
        Map requestJson = null;
        try {
            requestJson = gson.fromJson(requestRawData, Map.class);
        } catch(JsonSyntaxException ex) {
            res.sendError(400);
            out.flush();
            return;
        }
        
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
            if("Y".equals(refresh.toString())) {
                refreshConnectionUser();
            }

            String input_login = loginObj.toString();
            String input_password = passwordObj.toString();

            if(LOGIN.equals(input_login) && PASSWORD.equals(sha256Hash(input_password)))
            {
                if("Y".equals(refresh.toString())) {
                    refreshCredentials();
                }

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
