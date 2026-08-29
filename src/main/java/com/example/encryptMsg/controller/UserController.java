package com.example.encryptMsg.controller;

import com.example.encryptMsg.payload.*;
import com.example.encryptMsg.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    // Account components
    @PostMapping("/accounts/create")
    public ResponseEntity<Void> createAccount(@Valid @RequestBody UserInfo info) {
        userService.createAccount(info.getUsername(), info.getPassword());
        return ResponseEntity.ok().build();
    }
    @PostMapping("/accounts/delete")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody UserInfo info) {
        if (!userService.deleteAccount(info.getUsername(), info.getPassword())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else return ResponseEntity.ok().build();
    }


    // Message components
    @PostMapping("/messages/create")
    public ResponseEntity<Void> createMessage(@Valid @RequestBody MessageCreateInfo info)
    {
        if (!userService.createMessage(info.getUsername(), info.getMessagePlaintext(), info.getPassword())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else return ResponseEntity.ok().build();
    }
    @PostMapping("/messages/search")
    public ResponseEntity<Map<Integer, String>> getAllStoredMessages_byUsername(@Valid @RequestBody UserInfo info) {
        return userService.findByUsername(info.getUsername())
                .filter(account -> userService.passwordMatch(account, info.getPassword()))
                .map(account -> ResponseEntity.ok(userService.getAllStoredPlaintext_byAccount(account, info.getPassword())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    @PostMapping("/messages/delete")
    public ResponseEntity<Void> deleteMessage(@Valid @RequestBody MessageDeleteInfo info) {
        if (!userService.deleteMessage(info.getUsername(), info.getMessageId(), info.getPassword())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else return ResponseEntity.ok().build();
    }
}
