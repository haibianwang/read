package com.video.read;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DY03 {

    public static void main(String[] args) throws Exception {
        //●抖音链接(使用手机分享功能,复制链接)
        String url = "http://v.douyin.com/2MKBC6/";
        String url1 = "#在抖音，记录美好生活#在最好的年纪，做最疯狂的事 http://v.douyin.com/xB7gDq/ 复制此链接，打开【抖音短视频】，直接观看视频！";
        //过滤链接，获取http连接地址
        String finalUrl = decodeHttpUrl(url1);
        // HtmlUnit 模拟浏览器
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);              // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false);                    // 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false);   // js运行错误时，是否抛出异常
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setTimeout(10 * 1000);                   // 设置连接超时时间

        webClient.waitForBackgroundJavaScript(30 * 1000);               // 等待js后台执行30秒
        HtmlPage page = webClient.getPage(finalUrl);
        String pageAsXml = page.asXml();
        // Jsoup解析处理
        Document doc = Jsoup.parse(pageAsXml, finalUrl);
        Elements pngs = doc.select("img[src$=.png]");
        //1.利用Jsoup抓取抖音链接
        //抓取抖音网页
        //OkHttpClient client=new OkHttpClient();
        //Request request=new Request.Builder().url(finalUrl)
                //.get()
                //.build();
        //Response response = client.newCall(request).execute();
        //String htmls = Jsoup.connect(finalUrl).ignoreContentType(true).execute().body();
       //response.body().string();
        //String htmls = response.body().string();
        //System.out.println(htmls);
        //2.利用正则匹配可以抖音下载链接
        //具体匹配内容格式：「https://aweme.snssdk.com/aweme/...line=0」
        Pattern patternCompile = Pattern.compile("(?<=playAddr: \")https?://.+(?=\",)");
        //利用Pattern.compile("正则条件").matcher("匹配的字符串对象")方法可以将需要匹配的字段进行匹配封装 返回一个封装了匹配的字符串Matcher对象
        //3.匹配后封装成Matcher对象
        Matcher m = patternCompile.matcher(pageAsXml);
        //4.①利用Matcher中的group方法获取匹配的特定字符串 ②利用String的replace方法替换特定字符,得到抖音的去水印链接
        String matchUrl = "";
        while (m.find()) {
            matchUrl = m.group(0).replaceAll("playwm", "play");
        }
        if (!matchUrl.isEmpty()) {
            //5.将链接封装成流
            //注:由于抖音对请求头有限制,只能设置一个伪装手机浏览器请求头才可实现去水印下载
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Connection", "keep-alive");
            headers.put("Host", "aweme.snssdk.com");
            headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 Version/12.0 Safari/604.1");
            //6.利用Joup获取视频对象'
            BufferedInputStream in = Jsoup.connect(matchUrl).headers(headers).timeout(10000).ignoreContentType(true).execute().bodyStream();
            Long timetmp = new Date().getTime();
            String fileAddress = "video/" + timetmp + ".mp4";
            //7.封装一个保存文件的路径对象
            File fileSavePath = new File(fileAddress);
            //注:如果保存文件夹不存在,那么则创建该文件夹
            File fileParent = fileSavePath.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            //8.新建一个输出流对象
            OutputStream out =
                    new BufferedOutputStream(
                            new FileOutputStream(fileSavePath));
            //9.遍历输出文件
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            out.close();//关闭输出流
            in.close(); //关闭输入流
            //注:打印获取的链接
            System.out.println("-----抖音去水印链接-----\n" + matchUrl);
            System.out.println("\n-----视频保存路径-----\n" + fileSavePath.getAbsolutePath());
        }
    }

    public static String decodeHttpUrl(String url) {
        int start = url.indexOf("http");
        int end = url.lastIndexOf("/");
        String decodeurl = url.substring(start, end);
        return decodeurl;
    }
}
