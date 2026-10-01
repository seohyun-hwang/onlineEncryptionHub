package com.example.encryptMsg.cryptography;

// provides flexibility for toggling between library-reliant and custom-rolled cryptography processes
// 2 implementors: Encryption_LibraryReliant and Encryption_CustomRolled
public interface CryptographyToggleInterface {
    byte[] passwordHashingSHA256(char[] password, byte[] salt) throws Exception;

    boolean passwordCheck(char[] password, byte[] salt, byte[] expectedHash) throws Exception;

    // returns ciphertext byte-array and initialization-vector byte-array; bundled in a DTO
    IV_and_Ciphertext encryptionAES(char[] plaintext, char[] password, byte[] expansionSalt, String cipherMode) throws Exception;

    // returns plaintext char-array
    char[] decryptionAES(byte[] ciphertext, byte[] iv, char[] password, byte[] expansionSalt, String cipherMode) throws Exception;
}
