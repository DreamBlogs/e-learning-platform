package com.example.learning.materials.application;

import com.example.learning.common.exception.BusinessException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfTextExtractionServiceTest {

    private final PdfTextExtractionService service = new PdfTextExtractionService();

    @Test
    void shouldExtractTextFromPdf() throws IOException {
        // Given
        byte[] pdfBytes = createTestPdf("Hello World");
        InputStream inputStream = new ByteArrayInputStream(pdfBytes);

        // When
        ExtractedPdfText result = service.extract(inputStream);

        // Then
        assertThat(result.text()).isEqualTo("Hello World");
        assertThat(result.pageCount()).isEqualTo(1);
    }

    @Test
    void shouldHandleEmptyPdf() throws IOException {
        // Given
        byte[] pdfBytes = createTestPdf("");
        InputStream inputStream = new ByteArrayInputStream(pdfBytes);

        // When
        ExtractedPdfText result = service.extract(inputStream);

        // Then
        assertThat(result.text()).isEmpty();
        assertThat(result.pageCount()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionForInvalidPdf() {
        // Given
        InputStream inputStream = new ByteArrayInputStream("not a pdf".getBytes());

        // When & Then
        assertThatThrownBy(() -> service.extract(inputStream))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to extract text from PDF");
    }

    private byte[] createTestPdf(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            if (!text.isEmpty()) {
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    contentStream.newLineAtOffset(100, 700);
                    contentStream.showText(text);
                    contentStream.endText();
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
