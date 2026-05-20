package com.example.learning.tasks.application;

import com.example.learning.tasks.domain.Task;
import com.example.learning.tasks.infrastructure.TaskRepository;
import com.example.learning.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse create(UUID userId, CreateTaskRequest request) {
        Task task = new Task(
                userId,
                request.subjectId(),
                request.title(),
                request.description(),
                request.dueDate(),
                Task.Priority.valueOf(request.priority())
        );
        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> list(UUID userId, Boolean completed) {
        List<Task> tasks;
        if (completed == null) {
            tasks = taskRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        } else {
            tasks = taskRepository.findAllByUserIdAndCompletedOrderByCreatedAtDesc(userId, completed);
        }
        return tasks.stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse toggle(UUID userId, UUID taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        if (task.getCompleted()) {
            task.markIncomplete();
        } else {
            task.markComplete();
        }
        return toResponse(task);
    }

    @Transactional
    public void delete(UUID userId, UUID taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        taskRepository.delete(task);
    }

    public long countPending(UUID userId) {
        return taskRepository.countByUserIdAndCompleted(userId, false);
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getSubjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority().name(),
                task.getCompleted(),
                task.getCreatedAt()
        );
    }
}
