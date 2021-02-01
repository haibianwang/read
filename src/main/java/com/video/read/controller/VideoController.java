package com.video.read.controller;


import ch.qos.logback.classic.util.LoggerNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.video.read.util.Util;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class VideoController {
    private static final Logger LOGGER= LoggerFactory.getLogger(VideoController.class);

    @RequestMapping("/get")
    @ResponseBody
    public JSON get(@RequestParam(name = "url",defaultValue = "")String url, @RequestParam(name = "type",defaultValue = "0") String type, String m){
        LOGGER.info("抖音url:"+url);
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
                Map<String, Object> video = (Map<String, Object>) re.get("video");
                String vid=video.get("vid").toString().trim();
                r.put("desc",vid);
                if (type.equals("0")) {

                    Map<String, Object> play_addr = (Map<String, Object>) video.get("play_addr");
                    urls = ((List<String>) play_addr.get("url_list")).get(0);
                    if (m!=null&&m.equals("0")){
                        urls=urls.replace("playwm","play");
                    }
                }else if (type.equals("1")){
                    Map<String, Object> music = (Map<String, Object>) re.get("music");
                    Map<String, Object> play_url = (Map<String, Object>) music.get("play_url");
                    List<String> object=(List<String>)play_url.get("url_list");
                    if (object.size()>0) {
                        urls = ((List<String>) play_url.get("url_list")).get(0);
                    }

                }
                r.put("url",urls);
                //System.out.println(urls);
                //System.out.println(vurl);
            }


        }else{
            LOGGER.info("url is empty");
        }
        return r;
    }
    @RequestMapping("/getvideo")
    @ResponseBody
    public void getvideo(String url,String desc,HttpServletResponse res,String type) throws IOException {
        //if (url!=null&&url.isEmpty())
        //Pattern pattern = Pattern.compile("//(//d{3}//)//s//d{3}-//d{4}");
        //String regx="https?:\\/\\/[A-Za-z0-9]+\\.[A-Za-z0-9]+[\\/=\\?%\\-&_~`@[\\]\\':+!]*([^<>\\\"\\\"])*$";
        //boolean a=url.matches(regx);
        if (desc==null||desc.isEmpty()){
            desc="dy";
        }
        String regx="((https?):\\/\\/|)[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        if (url!=null&&url.matches(regx)) {
            String pre = "";
            if (type != null) {
                pre = ".mp3";
            } else {
                pre = ".mp4";
            }
            res.addHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(desc + pre, "UTF-8"));
            //res.setContentType("application/force-download");// 设置强制下载不打开
            //res.addHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(desc+pre, "UTF-8"));
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
                //e.printStackTrace();
                LOGGER.error(e.getStackTrace().toString());
            }
            //创建接收文件的流
            //File file = new File( "./a.mp4");
            //OutputStream outputStream = new FileOutputStream(file);

            OutputStream outputStream = null;
            if (type != null && type.equals("t")) {

                //创建接收文件的流
                File file = new File(desc + ".mp4");
                outputStream = new FileOutputStream(file);
                outputStream.write(response.body().bytes());
                outputStream.flush();
                outputStream.close();
                //File source = new File(desc+pre);
                File target = new File(desc + pre);
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("libmp3lame");
                audio.setBitRate(new Integer(128000));
                audio.setChannels(new Integer(2));
                audio.setSamplingRate(new Integer(44100));
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp3");
                attrs.setAudioAttributes(audio);
                Encoder encoder = new Encoder();
                try {
                    encoder.encode(file, target, attrs);
                    outputStream = res.getOutputStream();
                    byte[] buff = new byte[1024];
                    BufferedInputStream bis = null;
                    // 读取filename
                    bis = new BufferedInputStream(new FileInputStream(target));
                    int i = bis.read(buff);

                    while (i != -1) {
                        outputStream.write(buff, 0, buff.length);
                        outputStream.flush();
                        i = bis.read(buff);
                    }
                } catch (EncoderException e) {
                    //e.printStackTrace();
                    LOGGER.error(e.getStackTrace().toString());
                }

            } else {
                try {

                    res.setContentType("application/force-download");// 设置强制下载不打开
                    //res.addHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(desc+pre, "UTF-8"));
                    outputStream = res.getOutputStream();
                } catch (IOException e) {

                    //e.printStackTrace();
                    LOGGER.error(e.getStackTrace().toString());
                }
                //将responseBody截取并写入到指定文件路径下
                outputStream.write(response.body().bytes());
                outputStream.flush();
                outputStream.close();
            }
            //msg="success";
            //return msg;
            //return "index";
        }
    }
    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("/getvideobynio")
    @ResponseBody
    public void getvideoByNio(String url,String desc,HttpServletResponse res,String type) throws IOException {

        if (desc==null||desc.isEmpty()){
            desc="dy";
        }
        String regx="((https?):\\/\\/|)[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        if (url!=null&&url.matches(regx)) {
            String pre = "";
            if (type != null) {
                pre = ".mp3";
            } else {
                pre = ".mp4";
            }
            res.addHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(desc + pre, "UTF-8"));

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = null;
            WritableByteChannel writableByteChannel =null;
            ReadableByteChannel readableByteChannel=null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                //e.printStackTrace();
                LOGGER.error(e.getStackTrace().toString());
            }
            ByteBuffer buf = ByteBuffer.allocate(1024);

            OutputStream outputStream = null;
            if (type != null && type.equals("t")) {

                //创建接收文件的流
                File file = new File(desc + ".mp4");
                //FileChannel fileChannel=file.get
                outputStream = new FileOutputStream(file);
                 writableByteChannel = Channels.newChannel(outputStream);
                 readableByteChannel=Channels.newChannel(response.body().byteStream());
                while (readableByteChannel.read(buf)!=-1){
                    buf.flip();
                    while (buf.hasRemaining()){
                        writableByteChannel.write(buf);
                    }

                    buf.clear();
                }
                outputStream.flush();
                readableByteChannel.close();
                writableByteChannel.close();
                //File source = new File(desc+pre);
                File target = new File(desc + pre);
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("libmp3lame");
                audio.setBitRate(new Integer(128000));
                audio.setChannels(new Integer(2));
                audio.setSamplingRate(new Integer(44100));
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp3");
                attrs.setAudioAttributes(audio);
                Encoder encoder = new Encoder();
                try {
                    encoder.encode(file, target, attrs);
                    outputStream = res.getOutputStream();
                    writableByteChannel = Channels.newChannel(outputStream);
                    readableByteChannel=Channels.newChannel(new FileInputStream(target));
                    while (readableByteChannel.read(buf)!=-1){
                        buf.flip();
                        while (buf.hasRemaining()){
                            writableByteChannel.write(buf);
                        }

                        buf.clear();
                    }
                    //buf.put(bytes);
                    //buf.flip();
                    //writableByteChannel.write(buf);
                    outputStream.flush();
                    readableByteChannel.close();
                    writableByteChannel.close();

                } catch (EncoderException e) {

                    LOGGER.error(e.getStackTrace().toString());
                }

            } else {
                try {

                    res.setContentType("application/force-download");// 设置强制下载不打开
                    //byte[] bytes=response.body().bytes();


                    //for (int offset=0;offset<bytes.length;offset++){

                    //}

                    outputStream = res.getOutputStream();
                   // ServletOutputStream outputStream1=res.getOutputStream();
//FileOutputStream fileOutputStream=new FileOutputStream(outputStream)
                    //FileChannel fileChannel=outputStream.
                     writableByteChannel = Channels.newChannel(outputStream);
                     readableByteChannel=Channels.newChannel(response.body().byteStream());
                    //int len=readableByteChannel.read(buf);
                    while (readableByteChannel.read(buf)!=-1){
                        buf.flip();
                        while (buf.hasRemaining()){
                            writableByteChannel.write(buf);
                        }

                        buf.clear();
                    }
                    //buf.put(bytes);
                    //buf.flip();
                    //writableByteChannel.write(buf);
                    outputStream.flush();
                    readableByteChannel.close();
                    writableByteChannel.close();

                } catch (IOException e) {


                    LOGGER.error(e.getStackTrace().toString());
                }

                //outputStream.write(response.body().bytes());


            }

        }
    }
}
