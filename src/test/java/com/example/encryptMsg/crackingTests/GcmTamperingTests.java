package com.example.encryptMsg.crackingTests;

import com.example.encryptMsg.cryptography.EncryptionCompliant;
import com.example.encryptMsg.cryptography.EncryptionCustom;
import com.example.encryptMsg.cryptography.IV_and_Ciphertext;
import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.aes.AES256CBC;
import com.example.encryptMsg.cryptography.customrolled.aes.AES256GCM;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GcmTamperingTests {
    private EncryptionCompliant encryptionCompliant;
    private EncryptionCustom encryptionCustom;
    private SecureRandom secureRandom;

    @BeforeEach
    void setUp() {
        secureRandom = new SecureRandom();
        SHA256 sha256 = new SHA256();
        AES256Universal aesUniversal = new AES256Universal(sha256);
        AES256GCM aesGcm = new AES256GCM(sha256);
        AES256CBC aesCbc = new AES256CBC(sha256);

        encryptionCompliant = new EncryptionCompliant(aesUniversal);
        encryptionCustom = new EncryptionCustom(aesUniversal, aesGcm, aesCbc);
    }

    @Test
    void AES256GCM_customTamper() throws Exception { // should throw exception
        char[] plaintext = "Top Secret Financial Data".toCharArray();
        char[] password = "SecurePassword123!".toCharArray();
        byte[] expansionSalt = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(plaintext, password, expansionSalt, "GCM");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();
        tamperedCiphertext[0] ^= 0x01; // Flip the first bit

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustom.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "GCM");
        });

        assertEquals("Message tampered or wrong password!", exception.getMessage());
    }

    @Test
    void AES256GCM_compliantTamper() throws Exception { // should throw exception
        char[] plaintext = "Top Secret Financial Data".toCharArray();
        char[] password = "SecurePassword123!".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCompliant.encryptionAES(plaintext, password, expansionSalt, "GCM");

        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();
        tamperedCiphertext[0] ^= 0x01; // Flipping the first bit

        assertThrows(javax.crypto.AEADBadTagException.class, () -> {
            encryptionCompliant.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "GCM");
        });
    }
}
