package com.video.read.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.video.read.util.Util;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Controller
public class VideoController {
    @RequestMapping("/get")
    @ResponseBody
    public JSON get(String url, int type, String m){
        String urls="";
        JSONObject r=new JSONObject();
        if (url!=null&&!url.isEmpty()) {


            String real = Util.getrealUrl(Util.decodeHttpUrl(url));
            String id=Util.getID(real);
            if (id!=null&&!id.isEmpty()){
                String vurl=Util.getvideo(id);
                JSONObject json=JSONObject.parseObject(vurl);
                List<Map<String,Object>> maprest = (List<Map<String, Object>>) json.get("item_list");
                //List<Object> b=(List<Object>)json.get("item_list");
                Map<String,Object> re = maprest.get(0);
                String desc=re.get("desc").toString();
                r.put("desc",desc);
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
                r.put("url",urls);
                //System.out.println(urls);
                //System.out.println(vurl);
            }


        }
        return r;
    }
    @RequestMapping("/getvideo")
    @ResponseBody
    public void getvideo(String url,String desc,HttpServletResponse res) throws IOException {
        res.setContentType("application/force-download");// 设置强制下载不打开
        res.addHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(desc+".mp4", "UTF-8"));
        //String msg="";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //创建接收文件的流
       // File file = new File( "./a.mp4");
        //OutputStream outputStream = new FileOutputStream(file);

        OutputStream outputStream = null;
        try {
            outputStream = res.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将responseBody截取并写入到指定文件路径下
        outputStream.write(response.body().bytes());
        outputStream.flush();
        outputStream.close();
        //msg="success";
        //return msg;
        //return "index";
    }
    @RequestMapping("/")
    public String index(){
        return "index";
    }


}
