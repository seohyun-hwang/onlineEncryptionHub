package com.example.encryptMsg.payload;

import com.example.encryptMsg.config.CharArrDeserialization;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotNull(message = "Message must not be null")
    @Size(min = 1, message = "Message must not be empty")
    private char[] messagePlaintext;
    @NotNull(message = "Password must not be null")
    @Size(min = 1, message = "Password must not be empty")
    @JsonDeserialize(using = CharArrDeserialization.class)
    private char[] password;

    CreateMessageRequest() {

    }


    // GETTERS
    public String getUsername() { return username; }
    public char[] getMessagePlaintext() { return messagePlaintext; }
    public char[] getPassword() { return password; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setMessagePlaintext(char[] messagePlaintext) { this.messagePlaintext = messagePlaintext; }
    public void setPassword(char[] password) { this.password = password; }
}
