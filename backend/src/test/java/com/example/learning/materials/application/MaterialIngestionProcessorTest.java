package com.example.learning.materials.application;

import com.example.learning.common.exception.BusinessException;
import com.example.learning.materials.domain.Material;
import com.example.learning.materials.domain.MaterialExtractedText;
import com.example.learning.materials.domain.MaterialStatus;
import com.example.learning.materials.infrastructure.MaterialExtractedTextRepository;
import com.example.learning.materials.infrastructure.MaterialRepository;
import com.example.learning.rag.application.VectorStorageService;
import com.example.learning.storage.application.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialIngestionProcessorTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialExtractedTextRepository extractedTextRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private PdfTextExtractionService pdfTextExtractionService;

    @Mock
    private VectorStorageService vectorStorageService;

    private MaterialIngestionProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MaterialIngestionProcessor(
                materialRepository,
                extractedTextRepository,
                objectStorageService,
                pdfTextExtractionService,
                vectorStorageService
        );
    }

    @Test
    void shouldProcessMaterialSuccessfully() throws Exception {
        // Given
        UUID materialId = UUID.randomUUID();
        Material material = createPdfMaterial(materialId);
        InputStream inputStream = new ByteArrayInputStream("pdf content".getBytes());
        ExtractedPdfText extractedText = new ExtractedPdfText("extracted text", 1);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(objectStorageService.getObject(material.getStorageKey())).thenReturn(inputStream);
        when(pdfTextExtractionService.extract(any(InputStream.class))).thenReturn(extractedText);
        when(extractedTextRepository.findByMaterialId(materialId)).thenReturn(Optional.empty());
        when(vectorStorageService.getChunkCount(materialId)).thenReturn(5L);

        // When
        processor.process(materialId);

        // Then
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.PROCESSED);
        assertThat(material.getErrorMessage()).isNull();
        verify(extractedTextRepository).save(any(MaterialExtractedText.class));
        verify(vectorStorageService).processAndStoreChunks(eq(materialId), anyString());
        verify(materialRepository).findById(materialId);
    }

    @Test
    void shouldSkipAlreadyProcessedMaterial() {
        // Given
        UUID materialId = UUID.randomUUID();
        Material material = createPdfMaterial(materialId);
        material.markProcessed();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(extractedTextRepository.existsByMaterialId(materialId)).thenReturn(true);

        // When
        processor.process(materialId);

        // Then
        verifyNoInteractions(objectStorageService);
        verifyNoInteractions(pdfTextExtractionService);
    }

    @Test
    void shouldHandleUnsupportedFileType() {
        // Given
        UUID materialId = UUID.randomUUID();
        Material material = new Material(
                UUID.randomUUID(), UUID.randomUUID(), "test.txt", "text/plain", "key", 100
        );

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        // When
        processor.process(materialId);

        // Then
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.FAILED);
        assertThat(material.getErrorMessage()).contains("Only PDF processing is supported");
    }

    @Test
    void shouldHandleExtractionFailure() throws Exception {
        // Given
        UUID materialId = UUID.randomUUID();
        Material material = createPdfMaterial(materialId);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(objectStorageService.getObject(anyString())).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(pdfTextExtractionService.extract(any())).thenThrow(new BusinessException("ERROR", "Extraction failed"));

        // When
        processor.process(materialId);

        // Then
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.FAILED);
        assertThat(material.getErrorMessage()).isEqualTo("Extraction failed");
    }

    @Test
    void shouldRethrowUnexpectedException() throws Exception {
        // Given
        UUID materialId = UUID.randomUUID();
        Material material = createPdfMaterial(materialId);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(objectStorageService.getObject(anyString())).thenThrow(new RuntimeException("DB down"));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> processor.process(materialId));
        assertThat(material.getStatus()).isNotEqualTo(MaterialStatus.FAILED);
    }

    private Material createPdfMaterial(UUID id) {
        return new Material(
                id, UUID.randomUUID(), UUID.randomUUID(), "test.pdf", "application/pdf", "key", 100
        );
    }
}
