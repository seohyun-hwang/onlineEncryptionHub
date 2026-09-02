package com.example.encryptMsg.payload;

import com.example.encryptMsg.config.CharArrDeserialization;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateAccountRequest {
    @NotEmpty(message = "Username must not be empty")
    private String username;
    @NotNull(message = "Password must not be null")
    @Size(min = 1, message = "Password must not be empty")
    @JsonDeserialize(using = CharArrDeserialization.class)
    private char[] password;
    @NotBlank
    private String ciphermode;

    public CreateAccountRequest() {

    }


    // GETTERS
    public String getUsername() { return username; }
    public char[] getPassword() { return password; }
    public String getCiphermode() { return ciphermode; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setPassword(char[] password) { this.password = password; }
    public void setCiphermode(String ciphermode) { this.ciphermode = ciphermode; }
}
