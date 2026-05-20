package com.example.learning.tasks.api;

import com.example.learning.tasks.application.CreateTaskRequest;
import com.example.learning.tasks.application.TaskResponse;
import com.example.learning.tasks.application.TaskService;
import com.example.learning.tasks.application.UpdateTaskRequest;
import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;

    public TaskController(TaskService taskService, CurrentUserProvider currentUserProvider) {
        this.taskService = taskService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> create(@RequestBody CreateTaskRequest request) {
        return ApiResponse.ok(taskService.create(currentUserProvider.get().id(), request));
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> list(@RequestParam(required = false) Boolean completed) {
        return ApiResponse.ok(taskService.list(currentUserProvider.get().id(), completed));
    }

    @PatchMapping("/{taskId}/toggle")
    public ApiResponse<TaskResponse> toggle(@PathVariable UUID taskId) {
        return ApiResponse.ok(taskService.toggle(currentUserProvider.get().id(), taskId));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID taskId) {
        taskService.delete(currentUserProvider.get().id(), taskId);
    }

    @PatchMapping("/{taskId}")
    public ApiResponse<TaskResponse> update(@PathVariable UUID taskId, @Valid @RequestBody UpdateTaskRequest request) {
        return ApiResponse.ok(taskService.update(currentUserProvider.get().id(), taskId, request));
    }
}
