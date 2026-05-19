package com.example.learning.subjects.application;

import com.example.learning.common.exception.ResourceNotFoundException;
import com.example.learning.subjects.api.CreateSubjectRequest;
import com.example.learning.subjects.api.SubjectResponse;
import com.example.learning.subjects.api.UpdateSubjectRequest;
import com.example.learning.subjects.domain.Subject;
import com.example.learning.subjects.infrastructure.SubjectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public SubjectResponse create(UUID userId, CreateSubjectRequest request) {
        Subject subject = new Subject(
                userId,
                request.name().trim(),
                request.description(),
                request.color()
        );
        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> list(UUID userId) {
        return subjectRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(SubjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse get(UUID userId, UUID subjectId) {
        return SubjectResponse.from(getOwnedSubject(userId, subjectId));
    }

    @Transactional
    public SubjectResponse update(UUID userId, UUID subjectId, UpdateSubjectRequest request) {
        Subject subject = getOwnedSubject(userId, subjectId);
        subject.update(request.name().trim(), request.description(), request.color());
        return SubjectResponse.from(subject);
    }

    @Transactional
    public void delete(UUID userId, UUID subjectId) {
        Subject subject = getOwnedSubject(userId, subjectId);
        subjectRepository.delete(subject);
    }

    public Subject getOwnedSubject(UUID userId, UUID subjectId) {
        return subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
    }
}
