package com.example.encryptMsg.payload;

import jakarta.validation.constraints.NotBlank;

public class MessageCreateInfo {
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotBlank(message = "Message plaintext must not be blank")
    private String messagePlaintext;
    @NotBlank(message = "Password must not be blank")
    private String password;

    MessageCreateInfo() {

    }


    // GETTERS
    public String getUsername() { return username; }
    public String getMessagePlaintext() { return messagePlaintext; }
    public String getPassword() { return password; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setMessagePlaintext(String messagePlaintext) { this.messagePlaintext = messagePlaintext; }
    public void setPassword(String password) { this.password = password; }
}
