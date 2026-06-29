package com.cloudbrain.service.image;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /** 存储文件，返回存储路径 */
    String store(MultipartFile file, String imageId);

    /** 读取文件为字节数组 */
    byte[] retrieve(String filePath);

    /** 删除文件 */
    void delete(String filePath);

    /** 获取文件访问 URL（本地存储返回相对路径） */
    String getAccessUrl(String filePath);

    /** 获取文件大小 */
    long getFileSize(String filePath);
}
