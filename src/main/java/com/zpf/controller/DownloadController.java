package com.zpf.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.AbstractDocument;
import java.io.*;
import java.net.URLEncoder;

@Controller
public class DownloadController {
    private final static String utf8 = "utf-8";

    @RequestMapping("/download")
    public void downLoadFile(HttpServletRequest request, HttpServletResponse response) throws Exception{

        File file = new File("");
        response.setCharacterEncoding(utf8);
        InputStream is = null;
        OutputStream os = null;

        try {
            //分块下载  http在分块这里有约定俗成的 accept range
            long fileSize = file.length();
            //设置头 告诉前端 要下载 下载类型
            response.setContentType("application/x-download");
            //告诉前端  打开一个对话框 存在哪里的对话框
            String fileName = URLEncoder.encode(file.getName(),utf8);
            response.addHeader("Content-Dispostion","attachment;filename=" + fileName);
            //前端 支持不支持分片下载
            response.setHeader("Accept-Range","bytes");

            //文件多大
            response.setHeader("fileSize",String.valueOf(fileSize));
            //文件名字
            response.setHeader("fileName",fileName);


            //开始分块 一块一块的读  起始位置 结束位置
            long pos = 0;
            long last = fileSize - 1;
            //读取了多少
            long sum = 0;

            //取一下 看看前端需不需要做分块下载
            if(null != request.getHeader("Range")){
                //告诉前端 我做的是分块下载
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

               String numRange = request.getHeader("Range").replaceAll("bytes=","");

               String[] strRange = numRange.split("-");

               if(strRange.length == 2){
                   pos = Long.parseLong(strRange[0].trim());
                   last = Long.parseLong(strRange[1].trim());

                   //如果 结束超出了文件大小 直接取文件大小
                   if(last > fileSize - 1){
                       last = fileSize - 1;
                   }

               }else{
                   pos = Long.parseLong(numRange.replaceAll("-","").trim());
               }
            }

            long rangeLength = last - pos + 1;

            //http 规定设置断点下载约定俗称的一些参数
            String contentRange = new StringBuffer("bytes").append(pos).
                    append("-").append(last).append("/").append(fileSize).toString();

            //设置告诉前端 我当前读取哪些内容  哪一个分块
            response.setHeader("Content-Range",contentRange);

            //当前分块大小
            response.setHeader("Content-length",String.valueOf(rangeLength));

            //---------------------------初始化 输入 输出流
            os = new BufferedOutputStream(response.getOutputStream());
            is = new BufferedInputStream(new FileInputStream(file));
            //从pos 开始读 之前的都跳过去
            is.skip(pos);

            //开始读文件  流操作
            byte[] buffer = new byte[1024];
            int length = 0;
            // 如果加到sum = rangeLength当前长度 表示读完
            while (sum < rangeLength){
                 length = is.read(buffer,0,(rangeLength - sum) <= buffer.length?
                         ((int)(rangeLength - sum)) : buffer.length);

                 sum = sum + length;
                 os.write(buffer,0,length);
            }
            System.out.println("下载完成");


        }finally {
            if(is != null){
                is.close();
            }
            if(os != null){
                os.close();
            }
        }

    }
}
