package com.example.encryptMsg.controllerTests;

import com.example.encryptMsg.controller.UserController;
import com.example.encryptMsg.payload.CreateMessageResponse;
import com.example.encryptMsg.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createAccount_validData() throws Exception {
        when(userService.createAccount(anyString(), any(char[].class), anyString())).thenReturn(1);
        String jsonPayload = "{\"username\":\"Darth Mater\", \"password\":\"password0123456789\", \"ciphermode\":\"GCM\"}";

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void createAccount_blankUsername() throws Exception {
        String jsonPayload = "{\"username\":\"\", \"password\":\"password0123456789\", \"ciphermode\":\"GCM\"}";

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username must not be empty"));
    }

    @Test
    void createMessage_validData() throws Exception {
        CreateMessageResponse mockResponse = new CreateMessageResponse("GCM", 42);
        when(userService.createMessage(anyString(), any(char[].class), any(char[].class))).thenReturn(mockResponse);

        String jsonPayload = "{\"username\":\"Darth Mater\", \"messagePlaintext\":\"Hello World\", \"password\":\"password0123456789\"}";

        mockMvc.perform(post("/api/messages/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cipherMode").value("GCM"))
                .andExpect(jsonPath("$.messageId").value(42));
    }
}