package com.example.encryptMsg.controllerTests;

import com.example.encryptMsg.controller.*;
import com.example.encryptMsg.payload.UserInfo;
import com.example.encryptMsg.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @BeforeEach
    void preparationMockMVC() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createAccount_validData() throws Exception { // createAccount should return '200 OK'.
        UserInfo info = new UserInfo();
        info.setUsername("Darth Mater");
        info.setPassword("password0123456789");

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(info)))
                .andExpect(status().isOk());
    }

    @Test
    void createAccount_blankUsername() throws Exception { // createAccount should return '400 Bad Request'.
        UserInfo info = new UserInfo();
        info.setUsername("");
        info.setPassword("password0123456789");

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(info)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("must not be blank"));
    }
}