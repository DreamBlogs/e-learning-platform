package com.example.learning.knowledge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID subjectId;
    
    private String name;
    
    private Instant createdAt = Instant.now();

    protected Topic() {}

    public Topic(UUID subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
    }

    public UUID getId() { return id; }
    public UUID getSubjectId() { return subjectId; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
