package com.example.encryptMsg.controller;

import com.example.encryptMsg.payload.*;
import com.example.encryptMsg.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<Integer> createAccount(@Valid @RequestBody CreateAccountRequest info) throws Exception {
        return ResponseEntity.ok(
                userService.createAccount(info.getUsername(), info.getPassword(), info.getCiphermode())
        );
    }
    @PostMapping("/accounts/delete")
    public ResponseEntity<Integer> deleteAccount(@Valid @RequestBody FetchMessagesRequest info) throws Exception {
        int accountId = userService.deleteAccount(info.getUsername(), info.getPassword());
        if (accountId == 0) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else return ResponseEntity.ok(accountId);
    }


    // Message components
    @PostMapping("/messages/create")
    public ResponseEntity<CreateMessageResponse> createMessage(@Valid @RequestBody CreateMessageRequest info) throws Exception {
        CreateMessageResponse response = userService.createMessage(info.getUsername(), info.getMessagePlaintext(), info.getPassword());
        if (response == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/messages/search")
    public ResponseEntity<Map<Integer, char[]>> getAllStoredMessages_byUsername(@Valid @RequestBody FetchMessagesRequest info) throws Exception {
        Map<Integer, char[]> mapToReturn = userService.getAllStoredPlaintext_byAccount(info.getUsername(), info.getPassword());
        if (mapToReturn != null) return ResponseEntity.ok(mapToReturn);
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @PostMapping("/messages/delete")
    public ResponseEntity<Integer> deleteMessage(@Valid @RequestBody DeleteMessageRequest info) throws Exception {
        if (!userService.deleteMessage(info.getUsername(), info.getMessageId(), info.getPassword())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else return ResponseEntity.ok(info.getMessageId());
    }
}
