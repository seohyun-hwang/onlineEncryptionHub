package com.example.encryptMsg.controllerTests;

import com.example.encryptMsg.controller.DistributedTaskController;
import com.example.encryptMsg.service.TaskScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DistributedTaskController.class)
class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskScheduleService scheduler;

    @Test
    @DisplayName("POST /api/tasks/submit returns 202 Accepted and enqueues task")
    void submitTask_ReturnsAccepted() throws Exception {
        mockMvc.perform(post("/api/tasks/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "payload": "bulk-encryption-batch-001"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.taskId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("QUEUED"));

        verify(scheduler).enqueue(anyString(), eq("bulk-encryption-batch-001"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/status returns 200 with metadata when present")
    void getStatus_Found() throws Exception {
        String taskId = "task-xyz";
        when(scheduler.getTaskStatus(taskId)).thenReturn(Optional.of(Map.of(
                "id", taskId,
                "status", "COMPLETED",
                "result", "cipher_12345"
        )));

        mockMvc.perform(get("/api/tasks/{taskId}/status", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result").value("cipher_12345"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/status returns 404 when task missing")
    void getStatus_NotFound() throws Exception {
        when(scheduler.getTaskStatus("missing-task")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/{taskId}/status", "missing-task"))
                .andExpect(status().isNotFound());
    }
}