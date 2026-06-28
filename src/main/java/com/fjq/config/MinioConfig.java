package com.fjq.config;

import com.fjq.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        MinioClient minioClient=MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .build();

        try {
            boolean found=minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build()
            );
            if(!found){
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO 自动初始化 Bucket 失败，请检查网络或配置！",e);
        }
        return minioClient;
    }
}
