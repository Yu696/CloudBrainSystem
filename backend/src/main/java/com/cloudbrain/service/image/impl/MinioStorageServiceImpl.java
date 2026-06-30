package com.cloudbrain.service.image.impl;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.StorageProperties;
import com.cloudbrain.service.image.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    @Override
    public String store(MultipartFile file, String imageId) {
        String bucket = getBucket();
        ensureBucketExists(bucket);

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = dateDir + "/" + imageId + ext;

        try (InputStream stream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(stream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new BusinessException("MinIO 文件存储失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] retrieve(String filePath) {
        try (InputStream stream = retrieveAsStream(filePath)) {
            return stream.readAllBytes();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("MinIO 文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieveAsStream(String filePath) {
        String bucket = getBucket();
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(filePath).build());
        } catch (Exception e) {
            throw new BusinessException("MinIO 文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String filePath) {
        String bucket = getBucket();
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket).object(filePath).build());
        } catch (Exception e) {
            throw new BusinessException("MinIO 文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String filePath) {
        String bucket = getBucket();
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new BusinessException("MinIO 获取访问 URL 失败: " + e.getMessage());
        }
    }

    @Override
    public long getFileSize(String filePath) {
        String bucket = getBucket();
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(filePath).build());
            return stat.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getBucket() {
        return storageProperties.getMinio().getBucket();
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new BusinessException("MinIO Bucket 初始化失败: " + e.getMessage());
        }
    }
}
