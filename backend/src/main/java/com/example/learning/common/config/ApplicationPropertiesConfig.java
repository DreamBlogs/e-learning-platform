package com.example.learning.common.config;

import com.example.learning.auth.infrastructure.JwtProperties;
import com.example.learning.storage.infrastructure.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        CorsProperties.class,
        JwtProperties.class,
        StorageProperties.class
})
public class ApplicationPropertiesConfig {
}
