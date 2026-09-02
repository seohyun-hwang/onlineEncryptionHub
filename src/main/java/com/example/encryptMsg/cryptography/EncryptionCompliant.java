package com.example.encryptMsg.cryptography;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import org.springframework.stereotype.Service;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.MessageDigest;
import java.security.SecureRandom;

// in this class, every cryptographic algorithm relies on cryptographic libraries.
@Service("compliant")
public class EncryptionCompliant implements CryptographyToggle {
    private final SecureRandom secureRandom = new SecureRandom();
    private final AES256Universal aes256Universal;

    public EncryptionCompliant(AES256Universal aes256Universal) {
        this.aes256Universal = aes256Universal;
    }

    private SecretKey deriveKeyAES(char[] password, byte[] salt)
            throws Exception
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, 600000, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    @Override
    public byte[] passwordHashingSHA256(char[] password, byte[] salt) throws Exception {
        return deriveKeyAES(password, salt).getEncoded();
    }

    @Override
    public boolean passwordCheck(char[] password, byte[] salt, byte[] expectedHash)
            throws Exception
    {
        return MessageDigest.isEqual(passwordHashingSHA256(password, salt), expectedHash);
    }

    @Override
    public IV_and_Ciphertext encryptionAES(char[] plaintextChars, char[] password, byte[] expansionSalt, String cipherMode)
            throws Exception
    {
        SecretKey key = deriveKeyAES(password, expansionSalt);

        return switch (cipherMode) {
            case "GCM" -> {
                byte[] iv = new byte[12];
                secureRandom.nextBytes(iv);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
                yield new IV_and_Ciphertext(iv, cipher.doFinal(aes256Universal.charToByteArr(plaintextChars)));
            }
            case "CBC" -> {
                byte[] iv = new byte[16];
                secureRandom.nextBytes(iv);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
                yield new IV_and_Ciphertext(iv, cipher.doFinal(aes256Universal.charToByteArr(plaintextChars)));
            }
            default -> throw new IllegalStateException("Unexpected value: " + cipherMode);
        };
    }

    @Override
    public char[] decryptionAES(byte[] ciphertext, byte[] iv, char[] password, byte[] expansionSalt, String cipherMode)
            throws Exception
    {
        SecretKey key = deriveKeyAES(password, expansionSalt);
        Cipher cipher;

        return switch (cipherMode) {
            case "GCM" -> {
                cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
                yield aes256Universal.byteToCharArr( cipher.doFinal(ciphertext));
            }
            case "CBC" -> {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                yield aes256Universal.byteToCharArr( cipher.doFinal(ciphertext));
            }
            default -> throw new IllegalStateException("Unexpected value: " + cipherMode);
        };
    }
}