package com.example.encryptMsg.cryptographyTests;

import com.example.encryptMsg.cryptography.EncryptionCustom;
import com.example.encryptMsg.cryptography.IV_and_Ciphertext;
import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.aes.AES256CBC;
import com.example.encryptMsg.cryptography.customrolled.aes.AES256GCM;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class CustomAlgoTests {
    private EncryptionCustom encryptionCustom;
    private SecureRandom secureRandom;

    @BeforeEach
    void setUp() {
        secureRandom = new SecureRandom();
        SHA256 sha256 = new SHA256();
        AES256Universal aesUniversal = new AES256Universal(sha256);
        AES256GCM aesGcm = new AES256GCM(sha256);
        AES256CBC aesCbc = new AES256CBC(sha256);
        encryptionCustom = new EncryptionCustom(aesUniversal, aesGcm, aesCbc);
    }

    @Test
    void custom_AES256GCM_encryptDecryptCycle() throws Exception {
        char[] plaintext = "Urrrr".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(plaintext, password, expansionSalt, "GCM");
        assertNotNull(encryptedData);
        assertNotNull(encryptedData.iv());
        assertTrue(encryptedData.ciphertext().length > 0);
        assertEquals(12, encryptedData.iv().length, "IV better be 12 bytes only for GCM mhhmmm???");

        char[] decryptedChars = encryptionCustom.decryptionAES(
                encryptedData.ciphertext(), encryptedData.iv(), password, expansionSalt, "GCM"
        );
        assertArrayEquals(plaintext, decryptedChars, "Can't get decrypted plaintext from custom GCM");
    }

    @Test
    void custom_AES256CBC_encryptDecryptCycle() throws Exception {
        char[] plaintext = "Yesss!".toCharArray();
        char[] password = "pazzworrr0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(plaintext, password, expansionSalt, "CBC");
        assertNotNull(encryptedData);
        assertNotNull(encryptedData.iv());
        assertTrue(encryptedData.ciphertext().length > 0);
        assertEquals(16, encryptedData.iv().length, "IV better be 16 bytes for CBC ehh???");


        char[] decryptedChars = encryptionCustom.decryptionAES(
                encryptedData.ciphertext(), encryptedData.iv(), password, expansionSalt, "CBC"
        );
        assertArrayEquals(plaintext, decryptedChars, "Can't get decrypted plaintext from custom CBC");
    }
    @Test
    void custom_AES256CBC_WrongPassword_ThrowsInvalidPadding() throws Exception {
        // decrypting with the wrong password won't fail the decryption, but will churn out garbage plaintext, which messes up the padding.
        char[] plaintext = "Ayyyyy".toCharArray();
        char[] correctPassword = "CorrectPassword".toCharArray();
        char[] hackerPassword = "HackerPassword".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(plaintext, correctPassword, expansionSalt, "CBC");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustom.decryptionAES(encryptedData.ciphertext(), encryptedData.iv(), hackerPassword, expansionSalt, "CBC");
        });

        assertEquals("Invalid padding", exception.getMessage());
    }

    @Test
    void custom_PasswordVerification_RejectsWrongPassword() throws Exception {
        char[] password = "MyIncrediblePassword".toCharArray();
        char[] wrongPassword = "MyIncredibleWROOONNGPassword".toCharArray();
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);

        byte[] expectedHash = encryptionCustom.passwordHashingSHA256(password, salt);

        assertTrue(encryptionCustom.passwordCheck(password, salt, expectedHash), "Correct password was not verified as expected");
        assertFalse(encryptionCustom.passwordCheck(wrongPassword, salt, expectedHash), "Incorrect password was not rejected as expected");
    }

    @Test
    void custom_EmptyPlaintext_HandledCorrectly() throws Exception {
        char[] emptyPlaintext = new char[0];
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(emptyPlaintext, password, expansionSalt, "GCM");
        char[] decryptedChars = encryptionCustom.decryptionAES(encryptedData.ciphertext(), encryptedData.iv(), password, expansionSalt, "GCM");

        assertArrayEquals(emptyPlaintext, decryptedChars, "Empty plaintext didn't decrypt back to an empty array as expected");
    }
}
