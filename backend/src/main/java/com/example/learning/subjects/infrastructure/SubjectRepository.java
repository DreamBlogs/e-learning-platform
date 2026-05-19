package com.example.learning.subjects.infrastructure;

import com.example.learning.subjects.domain.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Subject> findByIdAndUserId(UUID id, UUID userId);
}
