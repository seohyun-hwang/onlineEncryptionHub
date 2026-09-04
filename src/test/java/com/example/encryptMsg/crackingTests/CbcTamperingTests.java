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
        char[] plaintext = "AAAAAHHHHHHHHH".toCharArray();
        char[] password = "password0123456789".toCharArray();
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
        char[] plaintextLong = "This text is long enough to exceed 16 bytes.".toCharArray();
        char[] plaintextShort = "<16bytes".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedDataLong = encryptionCustom.encryptionAES(plaintextLong, password, expansionSalt, "CBC");
        IV_and_Ciphertext encryptedDataShort = encryptionCustom.encryptionAES(plaintextShort, password, expansionSalt, "CBC");
        byte[] tamperedCiphertextLong = encryptedDataLong.ciphertext().clone();
        byte[] tamperedCiphertextShort = encryptedDataShort.ciphertext().clone();

        // tampering with the first byte
        tamperedCiphertextLong[0] ^= 0x01;
        tamperedCiphertextShort[0] ^= 0x01;

        // padding is intact if initial plaintext was 16+ bytes; decryption succeeds (technically)
        char[] decryptedCharsLong = encryptionCustom.decryptionAES(
                tamperedCiphertextLong, encryptedDataLong.iv(), password, expansionSalt, "CBC"
        );
        assertFalse(java.util.Arrays.equals(plaintextLong, decryptedCharsLong));

        // padding is destroyed if initial plaintext was under 16 bytes (therefore being the last block)
        assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustom.decryptionAES(tamperedCiphertextShort, encryptedDataLong.iv(), password, expansionSalt, "CBC");
        });
    }

    @Test
    void compliant_AES256CBC_TamperLastBlock_ThrowsBadPadding() throws Exception {
        char[] plaintext = "This text is long enough to exceed 16 bytes.".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCompliant.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0x01; // tampering with the first byte

        // The native Java library throws a BadPaddingException
        assertThrows(javax.crypto.BadPaddingException.class, () -> {
            encryptionCompliant.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC");
        });
    }

    @Test
    void compliant_AES256CBC_TamperFirstBlock_SilentCorruption() throws Exception {
        char[] plaintextLong = "This text is long enough to exceed 16 bytes.".toCharArray();
        char[] plaintextShort = "<16bytes".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedDataLong = encryptionCompliant.encryptionAES(plaintextLong, password, expansionSalt, "CBC");
        IV_and_Ciphertext encryptedDataShort = encryptionCompliant.encryptionAES(plaintextShort, password, expansionSalt, "CBC");
        byte[] tamperedCiphertextLong = encryptedDataLong.ciphertext().clone();
        byte[] tamperedCiphertextShort = encryptedDataShort.ciphertext().clone();

        // tampering with the first byte
        tamperedCiphertextLong[0] ^= 0x01;
        tamperedCiphertextShort[0] ^= 0x01;

        // padding is intact if initial plaintext was 16+ bytes; decryption succeeds (technically)
        char[] decryptedCharsLong = encryptionCompliant.decryptionAES(
                tamperedCiphertextLong, encryptedDataLong.iv(), password, expansionSalt, "CBC"
        );
        assertFalse(java.util.Arrays.equals(plaintextLong, decryptedCharsLong));

        // padding is destroyed if initial plaintext was under 16 bytes (therefore being the last block)
        assertThrows(javax.crypto.BadPaddingException.class, () -> {
            encryptionCompliant.decryptionAES(tamperedCiphertextShort, encryptedDataLong.iv(), password, expansionSalt, "CBC");
        });
    }
}
