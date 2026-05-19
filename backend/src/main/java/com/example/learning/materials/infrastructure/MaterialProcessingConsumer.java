package com.example.learning.materials.infrastructure;

import com.example.learning.materials.application.MaterialIngestionProcessor;
import com.example.learning.materials.application.MaterialProcessingRequestedEvent;
import com.example.learning.processing.infrastructure.RabbitMqTopologyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MaterialProcessingConsumer {

    private static final Logger log = LoggerFactory.getLogger(MaterialProcessingConsumer.class);

    private final MaterialIngestionProcessor processor;

    public MaterialProcessingConsumer(MaterialIngestionProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = RabbitMqTopologyConfig.MATERIAL_PROCESSING_QUEUE)
    public void handle(MaterialProcessingRequestedEvent event) {
        log.info("Received material processing request. materialId={}", event.materialId());
        processor.process(event.materialId());
    }
}
