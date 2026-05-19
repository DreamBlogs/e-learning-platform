package com.example.learning.materials.infrastructure;

import com.example.learning.materials.domain.MaterialExtractedText;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialExtractedTextRepository extends JpaRepository<MaterialExtractedText, UUID> {

    Optional<MaterialExtractedText> findByMaterialId(UUID materialId);

    boolean existsByMaterialId(UUID materialId);
}
