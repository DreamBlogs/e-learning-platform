package com.example.learning.materials.application;

import com.example.learning.common.exception.ResourceNotFoundException;
import com.example.learning.materials.api.CreateMaterialRequest;
import com.example.learning.materials.api.MaterialExtractedTextResponse;
import com.example.learning.materials.api.MaterialResponse;
import com.example.learning.materials.domain.Material;
import com.example.learning.materials.infrastructure.MaterialExtractedTextRepository;
import com.example.learning.materials.infrastructure.MaterialRepository;
import com.example.learning.processing.application.MessagePublisher;
import com.example.learning.processing.infrastructure.RabbitMqTopologyConfig;
import com.example.learning.storage.application.ObjectStorageService;
import com.example.learning.subjects.application.SubjectService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaterialService {

    private static final Logger log = LoggerFactory.getLogger(MaterialService.class);

    private final MaterialRepository materialRepository;
    private final MaterialExtractedTextRepository extractedTextRepository;
    private final SubjectService subjectService;
    private final ObjectStorageService objectStorageService;
    private final MessagePublisher messagePublisher;
    private final MaterialStorageKeyFactory storageKeyFactory;
    private final MaterialFileValidator fileValidator;

    public MaterialService(
            MaterialRepository materialRepository,
            MaterialExtractedTextRepository extractedTextRepository,
            SubjectService subjectService,
            ObjectStorageService objectStorageService,
            MessagePublisher messagePublisher,
            MaterialStorageKeyFactory storageKeyFactory,
            MaterialFileValidator fileValidator
    ) {
        this.materialRepository = materialRepository;
        this.extractedTextRepository = extractedTextRepository;
        this.subjectService = subjectService;
        this.objectStorageService = objectStorageService;
        this.messagePublisher = messagePublisher;
        this.storageKeyFactory = storageKeyFactory;
        this.fileValidator = fileValidator;
    }

    @Transactional
    public MaterialResponse create(UUID userId, UUID subjectId, CreateMaterialRequest request) {
        subjectService.getOwnedSubject(userId, subjectId);

        Material material = new Material(
                subjectId,
                userId,
                request.fileName(),
                request.fileType(),
                request.storageKey(),
                request.sizeBytes()
        );

        return MaterialResponse.from(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponse upload(UUID userId, UUID subjectId, MultipartFile file) {
        subjectService.getOwnedSubject(userId, subjectId);
        fileValidator.validatePdf(file);

        UUID materialId = UUID.randomUUID();
        String fileName = file.getOriginalFilename() == null ? "upload.pdf" : file.getOriginalFilename();
        String fileType = file.getContentType() == null ? "application/pdf" : file.getContentType();
        String storageKey = storageKeyFactory.create(userId, subjectId, materialId, fileName);

        try {
            objectStorageService.putObject(storageKey, file.getInputStream(), file.getSize(), fileType);
        } catch (IOException exception) {
            throw new com.example.learning.common.exception.BusinessException(
                    "FILE_READ_FAILED",
                    "Failed to read uploaded file"
            );
        }

        Material material = new Material(
                materialId,
                subjectId,
                userId,
                fileName,
                fileType,
                storageKey,
                file.getSize()
        );

        Material saved = materialRepository.save(material);
        publishProcessingRequestedAfterCommit(saved.getId());

        log.info("Material uploaded and processing requested. materialId={}, subjectId={}", saved.getId(), subjectId);
        return MaterialResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> list(UUID userId, UUID subjectId) {
        subjectService.getOwnedSubject(userId, subjectId);
        return materialRepository.findAllBySubjectIdAndUserIdOrderByCreatedAtDesc(subjectId, userId)
                .stream()
                .map(MaterialResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse get(UUID userId, UUID materialId) {
        return MaterialResponse.from(getOwnedMaterial(userId, materialId));
    }

    @Transactional(readOnly = true)
    public MaterialExtractedTextResponse getExtractedText(UUID userId, UUID materialId) {
        getOwnedMaterial(userId, materialId);
        return extractedTextRepository.findByMaterialId(materialId)
                .map(MaterialExtractedTextResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Material extracted text", materialId));
    }

    public Material getOwnedMaterial(UUID userId, UUID materialId) {
        return materialRepository.findByIdAndUserId(materialId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));
    }

    private void publishProcessingRequestedAfterCommit(UUID materialId) {
        MaterialProcessingRequestedEvent event = new MaterialProcessingRequestedEvent(materialId);
        Runnable publisher = () -> messagePublisher.publish(RabbitMqTopologyConfig.MATERIAL_PROCESSING_QUEUE, event);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.run();
                }
            });
            return;
        }

        publisher.run();
    }
}
