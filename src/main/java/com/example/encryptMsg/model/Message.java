package com.example.encryptMsg.model;

import jakarta.persistence.*;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int messageId;
    @ManyToOne
    @JoinColumn(name = "account_id") // the "account" field is stored as its reference-identifier "account_id".
    private Account account; // the account that sent this message
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] messageCiphertext; // [AES, masterKey: password] for storage
    private byte[] initializationVector; // AES-GCM or AES-CBC; either way, the iv/nonce is unique to each message.

    public Message() {
    }
    public Message(Account account, byte[] messageCiphertext, byte[] iv)
    {
        this.account = account;
        this.messageCiphertext = messageCiphertext;
        this.initializationVector = iv;
    }


    // GETTERS
    public int getMessageId() { return messageId; }
    public Account getAccount() {
        return account;
    }
    public byte[] getMessageCiphertext() { return messageCiphertext; }
    public byte[] getInitializationVector() { return initializationVector; }

    // SETTERS
    public void setAccount(Account account) {
        this.account = account;
    }
}
