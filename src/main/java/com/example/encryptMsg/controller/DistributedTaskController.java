package com.example.encryptMsg.controller;

import com.example.encryptMsg.service.TaskScheduleService;
import com.example.encryptMsg.service.TaskScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class DistributedTaskController {

    private final TaskScheduleService scheduler;

    public DistributedTaskController(TaskScheduleService scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitTask(@RequestBody Map<String, String> body) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        String payload = body.getOrDefault("payload", "");

        scheduler.enqueue(taskId, payload);
        return ResponseEntity.accepted().body(Map.of("taskId", taskId, "status", "QUEUED"));
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<?> getStatus(@PathVariable String taskId) {
        return scheduler.getTaskStatus(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}