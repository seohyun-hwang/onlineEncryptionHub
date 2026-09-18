package com.example.encryptMsg.crackingTests;

import com.example.encryptMsg.cryptography.Encryption_LibraryReliant;
import com.example.encryptMsg.cryptography.Encryption_CustomRolled;
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
    private Encryption_LibraryReliant encryptionLibraryReliant;
    private Encryption_CustomRolled encryptionCustomRolled;
    private SecureRandom secureRandom;

    @BeforeEach
    void setUp() {
        secureRandom = new SecureRandom();
        SHA256 sha256 = new SHA256();
        AES256Universal aesUniversal = new AES256Universal(sha256);
        AES256GCM aesGcm = new AES256GCM(sha256);
        AES256CBC aesCbc = new AES256CBC(sha256);

        encryptionLibraryReliant = new Encryption_LibraryReliant(aesUniversal);
        encryptionCustomRolled = new Encryption_CustomRolled(aesUniversal, aesGcm, aesCbc);
    }

    @Test
    void AES256GCM_customTamper() throws Exception { // should throw exception
        char[] plaintext = "Top Secret Financial Data".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustomRolled.encryptionAES(plaintext, password, expansionSalt, "GCM");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();
        tamperedCiphertext[0] ^= 0x01; // Flipping the first bit

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustomRolled.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "GCM");
        });

        assertEquals("Message tampered or wrong password!", exception.getMessage());
    }

    @Test
    void AES256GCM_libraryTamper() throws Exception { // should throw exception
        char[] plaintext = "Top Secret Financial Data".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionLibraryReliant.encryptionAES(plaintext, password, expansionSalt, "GCM");

        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();
        tamperedCiphertext[0] ^= 0x01; // Flipping the first bit

        assertThrows(javax.crypto.AEADBadTagException.class, () -> {
            encryptionLibraryReliant.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "GCM");
        });
    }
}
