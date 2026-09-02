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

import static org.junit.jupiter.api.Assertions.*;

public class CbcTamperingTests {
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
    void custom_AES256CBC_TamperLastBlock_ThrowsInvalidPadding() throws Exception {
        // Message must be long enough to span multiple blocks
        char[] plaintext = "This is a long message designed to test CBC padding.".toCharArray();
        char[] password = "SecurePassword123!".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        // Tamper with the very last byte of the ciphertext
        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0x01;

        // Because the last block is scrambled, PKCS#7 padding validation fails
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustom.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC");
        });
        assertEquals("Invalid padding", exception.getMessage());
    }

    @Test
    void custom_AES256CBC_TamperFirstBlock_SilentCorruption() throws Exception {
        char[] plaintext = "This is a long message designed to test CBC padding.".toCharArray();
        char[] password = "SecurePassword123!".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustom.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        // Tamper with the FIRST byte of the ciphertext
        tamperedCiphertext[0] ^= 0x01;

        // Decryption SUCCEEDS because the padding at the end is perfectly intact!
        char[] decryptedChars = encryptionCustom.decryptionAES(
                tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC"
        );

        // However, the resulting plaintext is corrupted and no longer matches the original
        assertFalse(java.util.Arrays.equals(plaintext, decryptedChars), "CBC failed to protect data integrity!");
    }

    @Test
    void compliant_AES256CBC_TamperLastBlock_ThrowsBadPadding() throws Exception {
        char[] plaintext = "This is a long message designed to test CBC padding.".toCharArray();
        char[] password = "SecurePassword123!".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCompliant.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        // Tamper with the very last byte
        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0x01;

        // The native Java library throws a BadPaddingException
        assertThrows(javax.crypto.BadPaddingException.class, () -> {
            encryptionCompliant.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC");
        });
    }

    @Test
    void compliant_AES256CBC_TamperFirstBlock_SilentCorruption() throws Exception {
        char[] plaintext = "This is a long message designed to test CBC padding.".toCharArray();
        char[] password = "SecurePassword123!".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCompliant.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        // Tamper with the FIRST byte
        tamperedCiphertext[0] ^= 0x01;

        // Native Java library silently succeeds
        char[] decryptedChars = encryptionCompliant.decryptionAES(
                tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC"
        );

        // The data is corrupted
        assertFalse(java.util.Arrays.equals(plaintext, decryptedChars));
    }
}
