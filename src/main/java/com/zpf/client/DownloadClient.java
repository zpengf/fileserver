package com.zpf.client;


import com.zpf.dto.FileInfo;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URLDecoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 文件下载客户端
 *
 * 前端不好做分片合并
 */
@RestController
public class DownloadClient {

    //定义每个分片有多大 5M
    private final static long per_page = 1024l * 1024l * 50l;

    //定义临时目录 存放文件
    private final static String down_path = "/Users/pengfeizhang/Desktop/javaProject";

    //定义线程池 多线程去下载
    //给10个多线程
    ExecutorService pool = Executors.newFixedThreadPool(10);

    //需要拿到一些参数 文件大小 文件名字
    // 根据文件大小知道多少个分块 知道文件名字 然后存储
    // 要做分块  知道多少个分块 这样就知道下载多少次 往服务端发送多少次请求




    // 使用多线程分块 下载  最后当最后分块下载完做文件合并
    @RequestMapping("/Client-download")
    public String downloadFile() throws Exception {

        //做个一个探测  下载小部分获取文件信息
        FileInfo fileInfo = download(0,10,-1,null);

        if(fileInfo != null){
            long pages = fileInfo.getFileSize() / per_page;
            //算出多少个分块 开多少个线程
            for(int i = 0;i <= pages;i++){
                pool.submit(new downThred(i * per_page,(i + 1) * per_page - 1,i,fileInfo.getFileName()));
            }



        }
        return "success";
    }

    /**
     * 下载线程
     */
    class downThred implements Runnable{
        long start;
        long end;
        long page;
        String fileName;
        public void run(){
            try {
                download(start,end,page,fileName);
            }catch (Exception e){
                e.printStackTrace();
            }


        }

        public downThred(long start, long end, long page, String fileName) {
            this.start = start;
            this.end = end;
            this.page = page;
            this.fileName = fileName;
        }
    }



    //告诉服务端 开始位置 结束位置  相剪就是分块大小
    //临时存储分块文件

    /**
     *
     * @param start 分块开始
     * @param end  分块结束
     * @param page 第几个分块
     * @param fileName  文件名字
     * @return
     */
    private FileInfo download(long start, long end, long page, String fileName) throws Exception {
        // 断点继续下载
        File file = new File(down_path,page+"-" + fileName);
        //文件存在就不需要下载
        if(file.exists() && page != -1 && file.length() == per_page){
            return null;
        }

        //创建一个客户端 去发请求
        HttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:8080/download");
        //告诉服务端 我要做分块下载
        httpGet.setHeader("Range","bytes=" + start+ "-" + end);

        HttpResponse response = httpClient.execute(httpGet);

        String fileSize = response.getFirstHeader("fileSize").getValue();

        fileName = URLDecoder.decode(response.getFirstHeader("fileName").getValue(),"utf-8");

        HttpEntity entity = response.getEntity();

        InputStream is =  entity.getContent();

        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int ch;
        while ((ch = is.read(buffer)) != -1){
            fos.write(buffer,0,ch);
        }

        is.close();
        fos.flush();
        fos.close();


        //如果最后一个块 下载完就开始合并文件
        if(end - Long.valueOf(fileSize) > 0){
            mergeFile(fileName,page);
        }

        return new FileInfo(Long.parseLong(fileSize),fileName);
    }

    private void mergeFile(String fileName,long page) throws Exception {
        File file = new File(down_path,fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        for(int i = 0;i <= page;i++){
            File tempFile = new File(down_path,i+"-"+fileName);
            //有可能分块没下载完
            while (!file.exists() || (i != page && tempFile.length() < per_page)){
                Thread.sleep(100);
            }
            //临时文件 传进去 开始合并
            byte[] bytes = FileUtils.readFileToByteArray(tempFile);
            bos.write(bytes);
            bos.flush();
            tempFile.delete();
        }

        File tanceFile = new File(down_path,-1+"-null");
        tanceFile.delete();
        bos.flush();
        bos.close();
    }

}
