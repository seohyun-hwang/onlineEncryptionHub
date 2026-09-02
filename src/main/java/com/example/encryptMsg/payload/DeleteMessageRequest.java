package com.example.encryptMsg.payload;

import com.example.encryptMsg.config.CharArrDeserialization;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeleteMessageRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotNull(message = "Message ID must not be null")
    private int messageId;
    @NotNull(message = "Password must not be null")
    @Size(min = 1, message = "Password must not be empty")
    @JsonDeserialize(using = CharArrDeserialization.class)
    private char[] password;

    DeleteMessageRequest() {

    }


    // GETTERS
    public String getUsername() { return username; }
    public int getMessageId() { return messageId; }
    public char[] getPassword() { return password; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setMessagePlaintext(int messageId) { this.messageId = messageId; }
    public void setPassword(char[] password) { this.password = password; }
}

