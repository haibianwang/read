package com.video.read.controller;


import com.alibaba.fastjson.JSONObject;
import com.video.read.util.Util;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class VideoController {
    @RequestMapping("/get")
    @ResponseBody
    public String get(String url,int type,String m){
        String urls="";
        if (url!=null&&!url.isEmpty()) {


            String real = Util.getrealUrl(url);
            String id=Util.getID(real);
            if (id!=null&&!id.isEmpty()){
                String vurl=Util.getvideo(id);
                JSONObject json=JSONObject.parseObject(vurl);
                List<Map<String,Object>> maprest = (List<Map<String, Object>>) json.get("item_list");
                //List<Object> b=(List<Object>)json.get("item_list");
                Map<String,Object> re = maprest.get(0);
                if (type==0) {
                    Map<String, Object> video = (Map<String, Object>) re.get("video");
                    Map<String, Object> play_addr = (Map<String, Object>) video.get("play_addr");
                    urls = ((List<String>) play_addr.get("url_list")).get(0);
                    if (m!=null&&m.equals("0")){
                        urls=urls.replace("playwm","play");
                    }
                }else if (type==1){
                    Map<String, Object> music = (Map<String, Object>) re.get("music");
                    Map<String, Object> play_url = (Map<String, Object>) music.get("play_url");
                    urls = ((List<String>) play_url.get("url_list")).get(0);

                }
                //System.out.println(urls);
                //System.out.println(vurl);
            }


        }
        return urls;
    }

    @RequestMapping("/")
    public String index(){
        return "index";
    }


}
