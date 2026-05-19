package com.example.learning.storage.infrastructure;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    public MinioHealthIndicator(MinioClient minioClient, StorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.bucket())
                    .build());

            if (!bucketExists) {
                return Health.down()
                        .withDetail("bucket", properties.bucket())
                        .withDetail("reason", "Bucket does not exist")
                        .build();
            }

            return Health.up()
                    .withDetail("bucket", properties.bucket())
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("bucket", properties.bucket())
                    .build();
        }
    }
}
