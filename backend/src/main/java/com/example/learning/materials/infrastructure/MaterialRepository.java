package com.example.learning.materials.infrastructure;

import com.example.learning.materials.domain.Material;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, UUID> {

    List<Material> findAllBySubjectIdAndUserIdOrderByCreatedAtDesc(UUID subjectId, UUID userId);

    Optional<Material> findByIdAndUserId(UUID id, UUID userId);
}
