package com.example.rfidnohttp;


import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    ExecutorService cachedThreadPool;
    Runnable runnable;


    public HttpServer(int port) {
        super(port);

    }

    public HttpServer(String hostname, int port) {
        super(hostname, port);
        Log.e("weifang","hostName:"+ hostname + " port:" + port);

    }




    @Override
    public Response serve(IHTTPSession session) {

        //获取请求uri
        String uri = session.getUri();
        cachedThreadPool = Executors.newCachedThreadPool();
        Method method = session.getMethod();
        if (method.equals(Method.POST)){
            Map<String, String> files = new HashMap<String, String>();
            //Map<String, String> header = session.getHeaders();
            try {
                session.parseBody(files);
                String param=files.get("postData");
                MyLOg.v("param : ", param);
                //String s = "{\"reader_name\":\"FX7500FBAF44 FX7500 RFID Reader\", \"mac_address\":\"84:24:8D:FB:AF:44\", \"tag_reads\":[{\"epc\":\"010203040506070809101112\"},{\"epc\":\"BBBB0002\"},{\"epc\":\"3333444455556666\"},{\"epc\":\"800388888888\"}]}";
                JSONObject jsonObject = JSONObject.parseObject(param);
               // JSONObject jsonObject = JSONObject.parseObject(s);
                String encodeData = jsonObject.getString("tag_reads");
                List<ParamEntity>list = JSONArray.parseArray(encodeData, ParamEntity.class);
                for (final ParamEntity ui:list) {
                    Log.d("RFIDInfo", ui.getEpc());
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                String op = httpGet(ui.getEpc());
                                MyLOg.d("State:",op+","+"rfidID:"+(ui.getEpc()));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    cachedThreadPool.execute(runnable);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
               // e.printStackTrace();
                return newFixedLengthResponse( "失败！");
            }
        }
       return newFixedLengthResponse( "成功！");
    }

    /**
     * get请求
     * @param httpUrl
     * @return
     * @throws
     */
    public String httpGet( String httpUrl ){
        String result = "" ;
        try {
            BufferedReader reader = null;
            StringBuffer sbf = new StringBuffer() ;

            URL url  = new URL( "http://192.168.10.100:9081/notify_RFID?RFID="+httpUrl) ;
            //URL url  = new URL( "http://192.168.0.128:9988?"+httpUrl) ;

            HttpURLConnection connection = (HttpURLConnection) url.openConnection() ;
            //设置超时时间 3s
            connection.setConnectTimeout(3000);
            //设置请求方式
            connection.setRequestMethod( "GET" ) ;
            connection.connect();
            InputStream is = connection.getInputStream() ;
            reader = new BufferedReader(new InputStreamReader( is , "UTF-8" )) ;
            String strRead = null ;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            return e.getMessage();
            //e.printStackTrace();
        }
        return result;
    }
}
