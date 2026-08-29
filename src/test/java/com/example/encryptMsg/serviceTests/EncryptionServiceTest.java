package com.example.encryptMsg.serviceTests;

import com.example.encryptMsg.service.EncryptionService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();
    private final SecureRandom secureRandom = new SecureRandom();

    @Test
    void cipher_fullCycle() { // stretching, expansion, encryption, decryption, CBC
        String plaintext = "This is the plaintext.";
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] masterKey = "password0123456789".getBytes(StandardCharsets.UTF_8);
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);

        byte[] stretchedKey = encryptionService.keySaltedStretch(masterKey, masterKey.length, salt);
        int[] roundKeys = encryptionService.rijndael256expansion(stretchedKey);
        byte[] ciphertext = encryptionService.aes256encryptionCBC(plaintextBytes, roundKeys, iv);

        String decrypted = encryptionService.aes256decryptionCBC(ciphertext, roundKeys, iv);
        assertNotNull(ciphertext);
        assertNotEquals(plaintext, new String(ciphertext));
        assertEquals(plaintext, decrypted);
    }
}