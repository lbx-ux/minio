package com.fjq.service;

import com.fjq.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /*
      上传文件（私有）
      @return 存储在 MinIO 中的唯一文件名（Object Key）
     */
    public String uploadFile(MultipartFile file) {
        try{
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            // 使用 UUID 生成唯一文件名，防止重名冲突
            String objectName = UUID.randomUUID().toString().replace("-", "") + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType()) // 必须指定类型，否则浏览器访问时一律变成下载而非预览
                                .build()
                );
            }
            log.info("文件上传成功: {}", objectName);
            return objectName;
        }catch(Exception e){
            log.error("MinIO 上传文件失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /*
      生成有有效期的预签名访问链接（供前端直接预览/下载）
      @param objectName 文件名
     * @param expiry     有效时间
     * @param timeUnit   时间单位
     */
    public String getPresignedUrl(String objectName, int expiry, TimeUnit timeUnit){
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .expiry(expiry, timeUnit)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名链接失败", e);
            throw new RuntimeException("获取文件访问链接失败");
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("MinIO 删除文件失败", e);
            throw new RuntimeException("文件删除失败");
        }
    }

}
