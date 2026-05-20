package com.example.learning.materials.application;

import com.example.learning.common.exception.BusinessException;
import com.example.learning.common.exception.ResourceNotFoundException;
import com.example.learning.materials.domain.Material;
import com.example.learning.materials.domain.MaterialExtractedText;
import com.example.learning.materials.domain.MaterialStatus;
import com.example.learning.materials.infrastructure.MaterialExtractedTextRepository;
import com.example.learning.materials.infrastructure.MaterialRepository;
import com.example.learning.rag.application.VectorStorageService;
import com.example.learning.storage.application.ObjectStorageService;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialIngestionProcessor {

    private static final Logger log = LoggerFactory.getLogger(MaterialIngestionProcessor.class);

    private final MaterialRepository materialRepository;
    private final MaterialExtractedTextRepository extractedTextRepository;
    private final ObjectStorageService objectStorageService;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final VectorStorageService vectorStorageService;

    public MaterialIngestionProcessor(
            MaterialRepository materialRepository,
            MaterialExtractedTextRepository extractedTextRepository,
            ObjectStorageService objectStorageService,
            PdfTextExtractionService pdfTextExtractionService,
            VectorStorageService vectorStorageService
    ) {
        this.materialRepository = materialRepository;
        this.extractedTextRepository = extractedTextRepository;
        this.objectStorageService = objectStorageService;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.vectorStorageService = vectorStorageService;
    }

    @Transactional
    public void process(UUID materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        if (material.isProcessed() && extractedTextRepository.existsByMaterialId(materialId)) {
            log.info("Skipping already processed material. materialId={}", materialId);
            return;
        }

        if (!isPdf(material)) {
            material.markFailed("Only PDF processing is supported in this version");
            log.warn("Material processing failed due to unsupported file type. materialId={}, fileType={}",
                    materialId, material.getFileType());
            return;
        }

        try {
            material.markProcessing();
            log.info("Processing material. materialId={}, storageKey={}", materialId, material.getStorageKey());

            ExtractedPdfText extractedText;
            try (InputStream inputStream = objectStorageService.getObject(material.getStorageKey())) {
                extractedText = pdfTextExtractionService.extract(inputStream);
            }

            upsertExtractedText(materialId, extractedText);
            material.markTextExtracted();

            try {
                vectorStorageService.processAndStoreChunks(materialId, extractedText.text());
                material.markProcessed();
                log.info("Material processed with RAG pipeline. materialId={}, pages={}, characters={}, chunks={}",
                        materialId, extractedText.pageCount(), extractedText.text().length(),
                        vectorStorageService.getChunkCount(materialId));
            } catch (Exception ragException) {
                log.error("RAG pipeline failed. materialId={}, error={}",
                        materialId, ragException.getMessage(), ragException);
                material.markFailed("RAG pipeline error: " + ragException.getMessage());
                log.warn("Material marked as failed due to RAG failure. materialId={}", materialId);
            }
        } catch (BusinessException exception) {
            material.markFailed(exception.getMessage());
            log.warn("Material processing failed. materialId={}, code={}, message={}",
                    materialId, exception.getCode(), exception.getMessage());
        } catch (RuntimeException exception) {
            material.markFailed(exception.getMessage());
            log.error("Material processing failed with runtime exception. materialId={}, error={}",
                    materialId, exception.getMessage(), exception);
        } catch (Exception exception) {
            log.error("Material processing failed with unexpected exception. materialId={}, error={}",
                    materialId, exception.getMessage(), exception);
            throw new com.example.learning.common.exception.InfrastructureException(
                    "UNEXPECTED_PROCESSING_ERROR", "Unexpected error during material processing", exception);
        }
    }

    private boolean isPdf(Material material) {
        return "application/pdf".equalsIgnoreCase(material.getFileType())
                || material.getFileName().toLowerCase().endsWith(".pdf");
    }

    private void upsertExtractedText(UUID materialId, ExtractedPdfText extractedText) {
        extractedTextRepository.findByMaterialId(materialId)
                .ifPresentOrElse(
                        existing -> existing.replaceText(extractedText.text(), extractedText.pageCount()),
                        () -> extractedTextRepository.save(new MaterialExtractedText(
                                materialId,
                                extractedText.text(),
                                extractedText.pageCount()
                        ))
                );
    }
}
