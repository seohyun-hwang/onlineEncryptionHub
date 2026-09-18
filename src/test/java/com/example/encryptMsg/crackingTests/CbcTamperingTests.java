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

import static org.junit.jupiter.api.Assertions.*;

public class CbcTamperingTests {
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
    void customRolled_AES256CBC_TamperLastBlock_ThrowsInvalidPadding() throws Exception {
        char[] plaintext = "AAAAAHHHHHHHHH".toCharArray(); // should be bigger than 16 bytes somehow
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionCustomRolled.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0x01; // Tampering with the last ciphertext-byte

        // PKCS#7 padding validation fails here
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustomRolled.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC");
        });
        assertEquals("Invalid padding", exception.getMessage());
    }

    @Test
    void customRolled_AES256CBC_TamperFirstBlock_SilentCorruption() throws Exception {
        char[] plaintextLong = "This text is long enough to exceed 16 bytes.".toCharArray();
        char[] plaintextShort = "<16bytes".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedDataLong = encryptionCustomRolled.encryptionAES(plaintextLong, password, expansionSalt, "CBC");
        IV_and_Ciphertext encryptedDataShort = encryptionCustomRolled.encryptionAES(plaintextShort, password, expansionSalt, "CBC");
        byte[] tamperedCiphertextLong = encryptedDataLong.ciphertext().clone();
        byte[] tamperedCiphertextShort = encryptedDataShort.ciphertext().clone();

        // tampering with the first byte
        tamperedCiphertextLong[0] ^= 0x01;
        tamperedCiphertextShort[0] ^= 0x01;

        // padding is intact if initial plaintext was 16+ bytes; decryption succeeds (technically)
        char[] decryptedCharsLong = encryptionCustomRolled.decryptionAES(
                tamperedCiphertextLong, encryptedDataLong.iv(), password, expansionSalt, "CBC"
        );
        assertFalse(java.util.Arrays.equals(plaintextLong, decryptedCharsLong));

        // padding is destroyed if initial plaintext was under 16 bytes (therefore being the last block)
        assertThrows(IllegalArgumentException.class, () -> {
            encryptionCustomRolled.decryptionAES(tamperedCiphertextShort, encryptedDataLong.iv(), password, expansionSalt, "CBC");
        });
    }

    @Test
    void libraryBased_AES256CBC_TamperLastBlock_ThrowsBadPadding() throws Exception {
        char[] plaintext = "This text is long enough to exceed 16 bytes.".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedData = encryptionLibraryReliant.encryptionAES(plaintext, password, expansionSalt, "CBC");
        byte[] tamperedCiphertext = encryptedData.ciphertext().clone();

        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0x01; // tampering with the first byte

        // The native Java library throws a BadPaddingException
        assertThrows(javax.crypto.BadPaddingException.class, () -> {
            encryptionLibraryReliant.decryptionAES(tamperedCiphertext, encryptedData.iv(), password, expansionSalt, "CBC");
        });
    }

    @Test
    void libraryBased_AES256CBC_TamperFirstBlock_SilentCorruption() throws Exception {
        char[] plaintextLong = "This text is long enough to exceed 16 bytes.".toCharArray();
        char[] plaintextShort = "<16bytes".toCharArray();
        char[] password = "password0123456789".toCharArray();
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(expansionSalt);

        IV_and_Ciphertext encryptedDataLong = encryptionLibraryReliant.encryptionAES(plaintextLong, password, expansionSalt, "CBC");
        IV_and_Ciphertext encryptedDataShort = encryptionLibraryReliant.encryptionAES(plaintextShort, password, expansionSalt, "CBC");
        byte[] tamperedCiphertextLong = encryptedDataLong.ciphertext().clone();
        byte[] tamperedCiphertextShort = encryptedDataShort.ciphertext().clone();

        // tampering with the first byte
        tamperedCiphertextLong[0] ^= 0x01;
        tamperedCiphertextShort[0] ^= 0x01;

        // padding is intact if initial plaintext was 16+ bytes; decryption succeeds (technically)
        char[] decryptedCharsLong = encryptionLibraryReliant.decryptionAES(
                tamperedCiphertextLong, encryptedDataLong.iv(), password, expansionSalt, "CBC"
        );
        assertFalse(java.util.Arrays.equals(plaintextLong, decryptedCharsLong));

        // padding is destroyed if initial plaintext was under 16 bytes (therefore being the last block)
        assertThrows(javax.crypto.BadPaddingException.class, () -> {
            encryptionLibraryReliant.decryptionAES(tamperedCiphertextShort, encryptedDataLong.iv(), password, expansionSalt, "CBC");
        });
    }
}
