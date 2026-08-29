package com.example.encryptMsg.payload;


import jakarta.validation.constraints.NotBlank;

public class UserInfo {
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotBlank(message = "Password must not be blank")
    private String password;

    public UserInfo() {

    }


    // GETTERS
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // SETTERS
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
