package com.example.encryptMsg.serviceTests;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ArrayConversionTests {

    @Test
    void testCharAndByteArrayConversions() {
        SHA256 sha256 = new SHA256();
        AES256Universal aes256Universal = new AES256Universal(sha256);

        char[] originalChars = "arrrrrrrrrr".toCharArray();

        byte[] convertedBytes = aes256Universal.charToByteArr(originalChars);
        char[] reconstructedChars = aes256Universal.byteToCharArr(convertedBytes);

        assertArrayEquals(originalChars, reconstructedChars);
    }

    @Test
    void testCharToByteArr_NullInput() {
        SHA256 sha256 = new SHA256();
        AES256Universal aes256Universal = new AES256Universal(sha256);

        byte[] result = aes256Universal.charToByteArr(null);
        assertArrayEquals(new byte[0], result);
    }
}