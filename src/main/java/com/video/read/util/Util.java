package com.video.read.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Util {

private static final Logger LOGGER= LoggerFactory.getLogger(Util.class);


    public static String decodeHttpUrl(String url) {
        int start = url.indexOf("http");
        int end = url.lastIndexOf("/");
        String decodeurl ="";
        if (start!=-1&&end!=-1){
        decodeurl = url.substring(start, end);
        }

        return decodeurl;
    }
    public static String getvideo(String id) {
        String rceurl = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids="+id;
        String res="";
        Connection c=getcon(rceurl);
        try {
           res  = c.ignoreContentType(true).timeout(10000).execute().body();
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.error(e.getStackTrace().toString());
        }
        return res;
    }
    public static Connection getcon(String url){
        //return  Jsoup.connect(url).userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1").ignoreContentType(true);
        return  Jsoup.connect(url).userAgent("Mozilla/5.0 (Android5.1.1) AppleWebKit/537. 36 (KHTML, like Gecko) Chrome/41. 0.2225.0 Safari/537. 36").ignoreContentType(true);

    }
    public static String getID(String realurl){
        String[] rest = realurl.split("video/");
        String id="";
        if (rest.length>1){
            String[] mid =  rest[1].split("/");
             id=mid[0];
        }else {
            //System.out.println("not found");
            LOGGER.info("videoid not found");
        }

       return id;
    }
    public static String getrealUrl(String url){

        Connection c=getcon(url);
        String r="";
        try {
            r=c.followRedirects(false).timeout(10000).execute().header("location");
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.error(e.getStackTrace().toString());
        }
        return  r;
    }
    public static String getzhibo(String room) throws IOException {
        String rceurl = "https://webcast-hl.amemv.com/webcast/room/reflow/info/";
        Connection c=getcon(rceurl);
        //添加参数
        Map<String, String> data = new HashMap<String, String>();

        data.put("room_id",room);
        data.put("live_id","1");
        //获取响应
        Connection.Response response = c.data(data).method(Connection.Method.POST).ignoreContentType(true).execute();
        String a=response.body();

        return "";
    }

}
