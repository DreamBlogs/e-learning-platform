package com.example.learning.processing.infrastructure;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqTopologyConfig {

    public static final String MATERIAL_PROCESSING_QUEUE = "material.processing";
    public static final String EMBEDDING_GENERATION_QUEUE = "embedding.generation";
    public static final String CONTENT_ANALYSIS_QUEUE = "content.analysis";

    @Bean
    Queue materialProcessingQueue() {
        return new Queue(MATERIAL_PROCESSING_QUEUE, true);
    }

    @Bean
    Queue embeddingGenerationQueue() {
        return new Queue(EMBEDDING_GENERATION_QUEUE, true);
    }

    @Bean
    Queue contentAnalysisQueue() {
        return new Queue(CONTENT_ANALYSIS_QUEUE, true);
    }
}
