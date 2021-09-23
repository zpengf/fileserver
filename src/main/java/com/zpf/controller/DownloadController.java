package com.zpf.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * 文件下载服务端
 */
@Controller
public class DownloadController {
    private final static String utf8 = "utf-8";

    @RequestMapping("/download")
    public void downLoadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {

        File file = new File("/Users/pengfeizhang/Desktop/javaProject/filetest/1-1课程整体介绍.mp4");
        response.setCharacterEncoding(utf8);
        InputStream is = null;
        OutputStream os = null;

        try {
            //分块下载
            long fileSize = file.length();
            //设置头 告诉前端 要下载 下载类型
            response.setContentType("application/x-download");

            //汉字处理一下
            String fileName = URLEncoder.encode(file.getName(), utf8);

//            String fileName = new String(file.getName().
//                    getBytes("utf-8"), "iso-8859-1");
            //告诉前端  打开一个对话框 存在哪里的对话框 默认的名字
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);


            //前端如果想要分块下载 会传来Range
            // 后端支持不支持分片下载 给他个响应accept range
            // http在分块这里有约定俗成的
            //分块的单位为byte
            response.setHeader("Accept-Range", "bytes");



            //这里如果需要用java客户端来下载 需要获得文件一些信息
            //因为java客户端需要调用这个api 这个api前边只是在浏览器认识的参数设置了名字 java客户端不会认识
            // 这里header 自定义的  这样java客户端调用 能直接拿到文件信息
            //文件大小
            response.setHeader("fileSize", String.valueOf(fileSize));
            //文件名字
            response.setHeader("fileName", fileName);


            //开始分块 一块一块的读  起始位置 结束位置
            long pos = 0;
            //文件的大小 - 1
            long last = fileSize - 1;
            //读取了多少
            long sum = 0;

            //取一下 看看前端需不需要做分块下载
            if (null != request.getHeader("Range")) {
                //如果前端支持分块 设置一下响应  告诉前端 我做分块下载
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); //206


                //取他的分块信息 格式 Range bytes=100-1000 或者 bytes=100-   直接从100读到了最后

                //把bytes换成空字符串 这样就取到分块100-1000
                String numRange = request.getHeader("Range").replaceAll("bytes=", "");

                //换成了数组 方便获取起始位置 最终位置
                String[] strRange = numRange.split("-");

                if (strRange.length == 2) {
                    pos = Long.parseLong(strRange[0].trim());
                    last = Long.parseLong(strRange[1].trim());

                    //如果 结束超出了文件大小 直接取文件大小
                    if (last > fileSize - 1) {
                        last = fileSize - 1;
                    }

                } else {
                    //如果是bytes=100-  只取到起始位置 last就不需要改了 就是默认值文件的大小 就是末尾
                    pos = Long.parseLong(numRange.replaceAll("-", "").trim());
                }
            }

            //总共需要读取的字节数
            long rangeLength = last - pos + 1;

            //http 规定设置断点下载约定俗称的一些参数

            String contentRange = new StringBuffer("bytes").append(pos).
                    append("-").append(last).append("/").append(fileSize).toString();

            //设置告诉前端 我当前读取哪些内容  哪一个分块
            response.setHeader("Content-Range", contentRange);

            //当前分块大小
            response.setHeader("Content-length", String.valueOf(rangeLength));

            //---------------------------初始化 输入 输出流
            //获取到输出流 reponse的输出流 往浏览器端 或者 客户端去写
            os = new BufferedOutputStream(response.getOutputStream());

            //获取到 file
            is = new BufferedInputStream(new FileInputStream(file));

            //从pos 开始读 之前的都跳过去  也可以用RandomAccessFile这个类操作
            is.skip(pos);

            //开始读文件  流操作
            byte[] buffer = new byte[1024];
            int length = 0;
            // 如果加到sum = rangeLength当前长度 表示读完
            while (sum < rangeLength) {
                //读取
                length = is.read(buffer, 0, (rangeLength - sum) <= buffer.length ? ((int) (rangeLength - sum)) : buffer.length);

                sum = sum + length;
                //写
                os.write(buffer, 0, length);
            }
            System.out.println("下载完成");
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }

    }
}
