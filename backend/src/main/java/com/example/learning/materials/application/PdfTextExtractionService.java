package com.example.learning.materials.application;

import com.example.learning.common.exception.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfTextExtractionService {

    public ExtractedPdfText extract(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).trim();
            return new ExtractedPdfText(text, document.getNumberOfPages());
        } catch (IOException exception) {
            throw new BusinessException("PDF_TEXT_EXTRACTION_FAILED", "Failed to extract text from PDF");
        }
    }
}
