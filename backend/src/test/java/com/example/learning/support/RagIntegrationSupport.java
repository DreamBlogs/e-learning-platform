package com.example.learning.support;

import com.example.learning.auth.domain.User;
import com.example.learning.auth.infrastructure.UserRepository;
import com.example.learning.materials.domain.Material;
import com.example.learning.materials.infrastructure.MaterialRepository;
import com.example.learning.subjects.domain.Subject;
import com.example.learning.subjects.infrastructure.SubjectRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RagIntegrationSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private MaterialRepository materialRepository;

    protected RagTestIds persistRagFixture() {
        return persistRagFixture(UUID.randomUUID());
    }

    protected RagTestIds persistRagFixture(UUID materialId) {
        User user = new User(
                "rag-test-" + UUID.randomUUID() + "@example.com",
                "hash",
                "RAG Test User"
        );
        userRepository.save(user);

        Subject subject = new Subject(user.getId(), "RAG Test Subject", "Integration test", null);
        subjectRepository.save(subject);

        Material material = new Material(
                materialId,
                subject.getId(),
                user.getId(),
                "test.pdf",
                "application/pdf",
                "test/" + materialId + ".pdf",
                1024
        );
        materialRepository.save(material);

        return new RagTestIds(user.getId(), subject.getId(), material.getId());
    }
}
