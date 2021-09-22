package com.zpf.mapper;

import com.zpf.dto.FileInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileInfoMapper {

    List<FileInfo> selectAllFile();

    FileInfo selectFileById(Integer id);

    FileInfo selectFileByCode(String fileCode);

    void addFile(FileInfo file);


    void deleteFile(Integer id);

}
