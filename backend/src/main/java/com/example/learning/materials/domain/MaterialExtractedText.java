package com.example.learning.materials.domain;

import com.example.learning.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "material_extracted_texts")
public class MaterialExtractedText extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "material_id", nullable = false, unique = true)
    private UUID materialId;

    @Column(name = "text_content", nullable = false, columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "character_count", nullable = false)
    private long characterCount;

    protected MaterialExtractedText() {
    }

    public MaterialExtractedText(UUID materialId, String textContent, Integer pageCount) {
        this.id = UUID.randomUUID();
        this.materialId = materialId;
        this.textContent = textContent;
        this.pageCount = pageCount;
        this.characterCount = textContent.length();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public String getTextContent() {
        return textContent;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public long getCharacterCount() {
        return characterCount;
    }

    public void replaceText(String textContent, Integer pageCount) {
        this.textContent = textContent;
        this.pageCount = pageCount;
        this.characterCount = textContent.length();
    }
}
