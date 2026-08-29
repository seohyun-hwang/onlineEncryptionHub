package com.example.encryptMsg.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageDeleteInfo {
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotNull(message = "Message ID must not be null")
    private int messageId;
    @NotBlank(message = "Password must not be blank")
    private String password;

    MessageDeleteInfo() {

    }


    // GETTERS
    public String getUsername() { return username; }
    public int getMessageId() { return messageId; }
    public String getPassword() { return password; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setMessagePlaintext(int messageId) { this.messageId = messageId; }
    public void setPassword(String password) { this.password = password; }
}

