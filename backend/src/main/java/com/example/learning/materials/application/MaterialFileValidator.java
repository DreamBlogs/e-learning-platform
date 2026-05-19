package com.example.learning.materials.application;

import com.example.learning.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MaterialFileValidator {

    public void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "Uploaded file is empty");
        }

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean hasPdfName = fileName != null && fileName.toLowerCase().endsWith(".pdf");
        boolean hasPdfContentType = "application/pdf".equalsIgnoreCase(contentType);

        if (!hasPdfName && !hasPdfContentType) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE", "Only PDF uploads are supported in this version");
        }
    }
}
