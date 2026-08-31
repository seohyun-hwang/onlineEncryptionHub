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
    private byte[] passwordSalt; // SHA-256 salt to store account password
    private byte[] expansionSalt; // SHA-256 salt for key-expansion in AES-256 message cipher
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> associatedMessagesList = new ArrayList<>();

    public Account() {
    }

    public Account(String username, byte[] passwordHash, byte[] passwordSalt, byte[] expansionSalt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.expansionSalt = expansionSalt;
    }



    // GETTERS
    public int getAccountId() { return accountId; }
    public String getUsername() {
        return username;
    }
    public byte[] getPasswordHash() {
        return passwordHash;
    }
    public byte[] getSaltSHA256() { return passwordSalt; }
    public byte[] getExpansionSaltSHA256() { return expansionSalt; }

    // SETTERS
    public void addToAssociatedMessagesList(Message message) {
        associatedMessagesList.add(message);
        message.setAccount(this);
    }
}