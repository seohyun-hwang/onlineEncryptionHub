package com.example.encryptMsg.cryptography;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.aes.AES256CBC;
import com.example.encryptMsg.cryptography.customrolled.aes.AES256GCM;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Arrays;


// in this class, every cryptographic algorithm is custom-rolled with no reliance on cryptographic libraries.
@Service("custom")
public class EncryptionCustom implements CryptographyToggle {
    private final AES256Universal aesUniversal;
    private final AES256GCM aesGcm;
    private final AES256CBC aesCbc;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionCustom(AES256Universal aesUniversal, AES256GCM aesGcm, AES256CBC aesCbc) {
        this.aesUniversal = aesUniversal;
        this.aesGcm = aesGcm;
        this.aesCbc = aesCbc;
    }

    @Override
    public byte[] passwordHashingSHA256(char[] password, byte[] salt) {
        byte[] passwordBytes = aesUniversal.charToByteArr(password);
        try {
            return aesUniversal.pbkdf2_hmacSha256(passwordBytes, passwordBytes.length, salt);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    @Override
    public boolean passwordCheck(char[] password, byte[] salt, byte[] expectedHash) {
        return aesUniversal.compareByteArrays_constantTime(passwordHashingSHA256(password, salt), expectedHash);
    }

    @Override
    public IV_and_Ciphertext encryptionAES(char[] plaintextChars, char[] password, byte[] expansionSalt, String cipherMode) {
        byte[] passwordBytes = aesUniversal.charToByteArr(password);
        int[] roundKeys = aesUniversal.rijndael256expansion(
                aesUniversal.pbkdf2_hmacSha256(passwordBytes, passwordBytes.length, expansionSalt)
        );

        return switch (cipherMode) {
            case "GCM" -> {
                byte[] iv = new byte[12];
                secureRandom.nextBytes(iv);
                yield new IV_and_Ciphertext(
                        iv,
                        aesGcm.aes256encryptionGCM(aesUniversal.charToByteArr(plaintextChars), roundKeys, iv)
                );
            }
            case "CBC" -> {
                byte[] iv = new byte[16];
                secureRandom.nextBytes(iv);
                yield new IV_and_Ciphertext(
                        iv,
                        aesCbc.aes256encryptionCBC(aesUniversal.charToByteArr(plaintextChars), roundKeys, iv)
                );
            }
            default -> throw new IllegalStateException("Unexpected value: " + cipherMode);
        };
    }

    @Override
    public char[] decryptionAES(byte[] ciphertext, byte[] iv, char[] password, byte[] expansionSalt, String cipherMode) {
        byte[] passwordBytes = aesUniversal.charToByteArr(password);
        int[] roundKeys = aesUniversal.rijndael256expansion(aesUniversal.pbkdf2_hmacSha256(passwordBytes, passwordBytes.length, expansionSalt));
        return switch (cipherMode) {
            case "GCM" -> aesGcm.aes256decryptionGCM(ciphertext, roundKeys, iv);
            case "CBC" -> aesCbc.aes256decryptionCBC(ciphertext, roundKeys, iv);
            default -> throw new IllegalStateException("Unexpected value: " + cipherMode);
        };
    }
}