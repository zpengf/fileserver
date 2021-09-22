package com.zpf.controller;


import com.zpf.dto.FileInfo;
import com.zpf.mapper.FileInfoMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Controller
public class UploadController {


    private final static String utf8 = "utf-8";

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @RequestMapping("/upload")
    @ResponseBody
    public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //分块
        response.setCharacterEncoding(utf8);

        List<FileInfo> list = fileInfoMapper.selectAllFile();
        
        System.out.println(list);

        Integer schunk = null;//当前分块 数字
        Integer schunks = null;//总的分块数量
        String name = null;
        String uploadPath = "/Users/pengfeizhang/Desktop/javaProject/filetest";
        BufferedOutputStream os = null; //文件合并 用流合并
        try {

            DiskFileItemFactory itemFactory = new DiskFileItemFactory();//
            itemFactory.setSizeThreshold(1024); //设置缓存区  文件不是直接写在硬盘中 先写到内存里
            itemFactory.setRepository(new File(uploadPath));//存的地址

            //通过servletFileUpload 去设置 文件大小 解析 request
            ServletFileUpload upload = new ServletFileUpload(itemFactory);
            upload.setFileSizeMax(5l * 1024l * 1024l * 1024l);
            upload.setSizeMax(10l * 1024l * 1024l * 1024l);

            List<FileItem> fileItems =  upload.parseRequest(request);//直接解析 request
            for(FileItem fileItem:fileItems){
                if(fileItem.isFormField()){
                    if("chunk".equals(fileItem.getFieldName())){
                        schunk =  Integer.parseInt(fileItem.getString(utf8));
                    }
                    if("chunks".equals(fileItem.getFieldName())){
                        schunks =  Integer.parseInt(fileItem.getString(utf8));
                    }
                    if("name".equals(fileItem.getFieldName())){
                        name =  fileItem.getString(utf8);
                    }
                }

            }

            for(FileItem fileItem:fileItems){
                if(!fileItem.isFormField()){
                    String temFileName = name;// 临时文件名字目录
                    if(name != null){
                        if(schunk != null){
                            temFileName = schunk + "_" + name;

                        }
                        File temFile = new File(uploadPath,temFileName);
                        if(!temFile.exists()){
                            //断点续传  如果上传过程断开 再次上传 总共20个分片 已经传了10个分片
                            // 重新上传 还是来20个分片 在这里做出判断如果已经有了 就不需要写入了 不存在才写
                            fileItem.write(temFile);// 文件全部写入
                        }
                    }
                }

            }
            //合并分块 先判断是否有分块 没有不需要合并
            // 当前分块==总的分块数 表示文件都来了 进行合并

            if(schunk != null && schunk.intValue() == schunks.intValue() - 1){
                File tempFile = new File(uploadPath,name);
                os = new BufferedOutputStream(new FileOutputStream(tempFile));

                for(int i = 0;i < schunks;i++){
                    File file = new File(uploadPath,i+"_"+name);
                    // 并发上传 有可能最后一个到了 其他的还没到
                    while (!file.exists()){
                        Thread.sleep(100);
                    }
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    os.write(bytes);
                    os.flush();
                    file.delete();
                }
                os.flush();
            }
            response.getWriter().write("上传成功！");

        } finally {
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
