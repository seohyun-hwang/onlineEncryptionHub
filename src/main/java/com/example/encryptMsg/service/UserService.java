package com.example.encryptMsg.service;

import com.example.encryptMsg.model.*;
import com.example.encryptMsg.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class UserService {
    private final AccountRepo accountRepo;
    private final MessageRepo messageRepo;
    private final EncryptionService encryptionService;

    public UserService(AccountRepo accountRepo, MessageRepo messageRepo, EncryptionService encryptionService) {
        this.accountRepo = accountRepo;
        this.messageRepo = messageRepo;
        this.encryptionService = encryptionService;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public void createAccount(String username, String password) {
        byte[] passwordSalt = new byte[32];
        secureRandom.nextBytes(passwordSalt);
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);
        byte[] passwordBytes = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        Account newAccount = new Account(
                username,
                encryptionService.keySaltedStretch(passwordBytes, passwordBytes.length, passwordSalt),
                passwordSalt,
                expansionSalt
        );
        accountRepo.save(newAccount);
    }
    @Transactional
    public boolean createMessage(String username, String messagePlaintext, String password) {
        Optional<Account> accountOptional = findByUsername(username);
        if (accountOptional.isEmpty()) {
            return false;
        }
        Account account = accountOptional.get();

        if (!passwordMatch(account, password)) return false;
        byte[] initializationVector = new byte[16];
        secureRandom.nextBytes(initializationVector);

        byte[] masterKey = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] messageCiphertext = encryptionService.aes256encryptionCBC(
                messagePlaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                encryptionService.rijndael256expansion(encryptionService.keySaltedStretch(masterKey, masterKey.length, account.getExpansionSaltSHA256())),
                initializationVector
        );
        Message newMessage = new Message(account, messageCiphertext, initializationVector);
        messageRepo.save(newMessage);
        account.addToAssociatedMessagesList(newMessage);
        return true;
    }
    @Transactional
    public boolean deleteAccount(String username, String password) {
        Optional<Account> accountOptional = findByUsername(username);
        if (accountOptional.isEmpty()) {
            return false;
        }
        Account account = accountOptional.get();
        if (!passwordMatch(account, password)) return false;
        accountRepo.delete(account);
        return true;
    }
    public boolean passwordMatch(Account account, String password) {
        byte[] passwordSalt = account.getSaltSHA256();
        byte[] passwordBytes = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // array-comparison with timing-attack prevention
        return java.security.MessageDigest.isEqual(
                encryptionService.keySaltedStretch(passwordBytes, passwordBytes.length, passwordSalt),
                account.getPasswordHash()
        );
    }
    public Map<Integer, String> getAllStoredPlaintext_byAccount(Account account, String password) {
        Map<Integer, String> map = new HashMap<>();
        byte[] masterKey = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int[] roundKeys = encryptionService.rijndael256expansion(encryptionService.keySaltedStretch(masterKey, masterKey.length, account.getExpansionSaltSHA256()));
        for (Message message : messageRepo.findAllByAccount(account)) {
            map.put(message.getMessageId(), encryptionService.aes256decryptionCBC(message.getMessageCiphertext(), roundKeys, message.getInitializationVector()));
        }
        return map;
    }
    public boolean deleteMessage(String username, int messageId, String password) {
        Optional<Account> accountOptional = findByUsername(username);
        if (accountOptional.isEmpty()) {
            return false;
        }
        Account account = accountOptional.get();
        if (!passwordMatch(account, password)) return false;
        Message message = getMessageById(messageId);
        if (!messageRepo.findAllByAccount(account).contains(message)) return false;
        messageRepo.delete(message);
        return true;
    }
    public Optional<Account> findByUsername(String username) {
        return accountRepo.findByUsername(username);
    }


    public Message getMessageById(int messageId) {
        Optional<Message> messageStoredOptional = messageRepo.findById(messageId);
        return messageStoredOptional
                .orElseThrow(() -> new NoSuchElementException("Message not found with ID " + messageId + "."));
    }
    /*
    public Account getAccountById(int accountId) {
        Optional<Account> accountOptional = accountRepo.findById(accountId);
        return accountOptional
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID " + accountId + "."));
    }
    */
}
