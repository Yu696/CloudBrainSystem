package com.cloudbrain.service.image.impl;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.service.image.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements StorageService {

    @Value("${storage.base-path:./data/cloudbrain/images}")
    private String basePath;

    @Override
    public String store(MultipartFile file, String imageId) {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = imageId + ext;
            Path dir = Paths.get(basePath, dateDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return dateDir + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("文件存储失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] retrieve(String filePath) {
        try {
            Path path = Paths.get(basePath, filePath);
            if (!Files.exists(path)) {
                throw new BusinessException("文件不存在");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieveAsStream(String filePath) {
        try {
            Path path = Paths.get(basePath, filePath);
            if (!Files.exists(path)) {
                throw new BusinessException("文件不存在");
            }
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(basePath, filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String filePath) {
        return filePath;
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(basePath, filePath));
        } catch (IOException e) {
            return 0;
        }
    }
}
