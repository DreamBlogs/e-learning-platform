package com.example.learning.tasks.infrastructure;

import com.example.learning.tasks.domain.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Task> findAllByUserIdAndCompletedOrderByCreatedAtDesc(UUID userId, boolean completed);
    Optional<Task> findByIdAndUserId(UUID id, UUID userId);
    long countByUserIdAndCompleted(UUID userId, boolean completed);
}
