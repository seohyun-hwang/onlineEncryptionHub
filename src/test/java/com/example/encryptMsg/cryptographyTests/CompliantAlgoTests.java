package com.example.encryptMsg.cryptographyTests;

import com.example.encryptMsg.cryptography.EncryptionCompliant;
import com.example.encryptMsg.cryptography.IV_and_Ciphertext;
import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class CompliantAlgoTests {
    private EncryptionCompliant encryptionCompliant;
    private SecureRandom secureRandom;

    @BeforeEach
    void setUp() {
        secureRandom = new SecureRandom();
        SHA256 sha256 = new SHA256();
        AES256Universal aesUniversal = new AES256Universal(sha256);
        encryptionCompliant = new EncryptionCompliant(aesUniversal);
    }

    @Test
    void compliant_AES256GCM_encryptDecryptCycle() throws Exception {
        char[] plaintext = "Hiiiiiiiiiiii".toCharArray();
        char[] password = "pazzworr".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);
        IV_and_Ciphertext encryptedData = encryptionCompliant.encryptionAES(plaintext, password, expansionSalt, "GCM");

        assertNotNull(encryptedData);
        assertNotNull(encryptedData.iv());
        assertTrue(encryptedData.ciphertext().length > 0);
        assertEquals(12, encryptedData.iv().length, "IV not 12 bytes for GCM???");

        char[] decryptedChars = encryptionCompliant.decryptionAES(
                encryptedData.ciphertext(), encryptedData.iv(), password, expansionSalt, "GCM"
        );
        assertArrayEquals(plaintext, decryptedChars, "Can't get decrypted plaintext from compliant GCM");
    }

    @Test
    void compliant_AES256CBC_encryptDecryptCycle() throws Exception {
        char[] plaintext = "Hiihhiiihiihaaaaaaaaa".toCharArray();
        char[] password = "wazzup".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);
        IV_and_Ciphertext encryptedData = encryptionCompliant.encryptionAES(plaintext, password, expansionSalt, "CBC");

        assertNotNull(encryptedData);
        assertNotNull(encryptedData.iv());
        assertTrue(encryptedData.ciphertext().length > 0);
        assertEquals(16, encryptedData.iv().length, "IV not 16 bytes for CBC???");

        char[] decryptedChars = encryptionCompliant.decryptionAES(
                encryptedData.ciphertext(), encryptedData.iv(), password, expansionSalt, "CBC"
        );
        assertArrayEquals(plaintext, decryptedChars, "Can't get decrypted plaintext from compliant CBC");
    }
}
