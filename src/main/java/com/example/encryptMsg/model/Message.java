package com.example.encryptMsg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int messageId;
    @ManyToOne
    @JoinColumn(name = "account_id") // the "account" field is stored as its reference-identifier "account_id".
    @JsonIgnore
    private Account account; // the account that sent this message
    // @Lob and BLOB only required for H2-database
    //@Lob
    //@Column(columnDefinition = "BLOB")
    private byte[] messageCiphertext; // [AES, masterKey: password] for storage
    private byte[] initializationVector; // AES-GCM or AES-CBC; either way, the iv/nonce is unique to each message.
    //private List<Integer> receiverID = new ArrayList<>(); // IDs of receiver-accounts; each ephemeral-key is mapped to one receiver-ID.
    //private List<byte[]> ephemeralKeys = new ArrayList<>(); // stored in case the message is sent to another account and needs to be AES-encrypted by a random bytestream


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
