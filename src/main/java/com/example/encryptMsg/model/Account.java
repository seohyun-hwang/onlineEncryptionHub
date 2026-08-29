package com.example.encryptMsg.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountId;
    @Column(unique = true, nullable = false) // protection against username-claim race-condition
    private String username;
    private byte[] passwordHash; // one-way salted SHA-256 encryption
    private byte[] saltSHA256;
    private byte[] expansionSaltSHA256;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Message> associatedMessagesList = new ArrayList<>();

    public Account() {
    }

    public Account(String username, byte[] passwordHash, byte[] saltSHA256, byte[] expansionSaltSHA256) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.saltSHA256 = saltSHA256;
        this.expansionSaltSHA256 = expansionSaltSHA256;
    }



    // GETTERS
    public int getAccountId() { return accountId; }
    public String getUsername() {
        return username;
    }
    public byte[] getPasswordHash() {
        return passwordHash;
    }
    public byte[] getSaltSHA256() { return saltSHA256; }
    public byte[] getExpansionSaltSHA256() { return expansionSaltSHA256; }

    // SETTERS
    public void addToAssociatedMessagesList(Message message) {
        associatedMessagesList.add(message);
    }
}