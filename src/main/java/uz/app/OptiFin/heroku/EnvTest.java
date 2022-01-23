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

/**
 * EnvTest
 */
@WebServlet("/envtest")
public class EnvTest extends HttpServlet  {
    static String DB_URI = "";
    // *******************
    static GsonBuilder gsonBuilder;

    private static void refreshCredentials()
    {
        DB_URI = System.getenv("DATABASE_URL");//"postgres://bekmyzbubqr:71a1186ef3295b424924be0f9165dff4e30f86f7fae4f4f405026c0e76ccd889@ec2-52-49-68-244.eu-west-1.compute.amazonaws.com:5432/ddu1rikpfr5jq6";
        if(DB_URI == null)
            DB_URI = "";
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
        Object refresh = requestJson.get("refresh");
        if(refresh == null)
            refresh = "N";

        res.setContentType("application/json");
        
        if("Y".equals(refresh.toString()))
            refreshCredentials();

        responseJsonMap.put("url", DB_URI);

        String responseStr = gson.toJson(responseJsonMap);
        out.print(responseStr);
        out.flush();
    }
    
}