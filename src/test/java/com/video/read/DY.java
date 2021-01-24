package com.video.read;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DY {
    public static void main(String[] args) throws IOException {
        String url = "http://v.douyin.com/2MKBC6/";
        String url1 = "#在抖音，记录美好生活#在最好的年纪，做最疯狂的事 http://v.douyin.com/xB7gDq/ 复制此链接，打开【抖音短视频】，直接观看视频！";
        int start=url.indexOf("http");
        int end=url.lastIndexOf("/");
        String furl=url.substring(start,end);
        //System.out.println(furl);
        Connection connection= getcon(furl);
        String reu=connection.followRedirects(false).timeout(10000).execute().header("location");
        //System.out.println(reu);
        String[] rest = reu.split("video/");

        String[] mid =  rest[1].split("/");
        String rceurl = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids="+mid[0];
        Connection c=getcon(rceurl);
        String res  = c.ignoreContentType(true).timeout(10000).execute().body();

        System.out.println(res);
        JSONObject json=JSONObject.parseObject(res);
        List<Map<String,Object>> maprest = (List<Map<String, Object>>) json.get("item_list");
        //List<Object> b=(List<Object>)json.get("item_list");
        Map<String,Object> re = maprest.get(0);
        Map<String,Object> video = (Map<String, Object>) re.get("video");
        Map<String,Object> play_addr = (Map<String, Object>) video.get("play_addr");
        String urls = ((List<String>)play_addr.get("url_list")).get(0);
        System.out.println(urls);
    }
    private static Connection getcon(String url){
        return  Jsoup.connect(url).userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1").ignoreContentType(true);

    }
}
