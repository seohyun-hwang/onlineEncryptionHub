package com.example.encryptMsg.cryptography.customrolled.aes;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AES256CBC extends AES256Universal {
    // Cipher Block Chain mode for AES-256
    public AES256CBC(SHA256 sha256) {
        super(sha256);
    }

    private byte[] paddingPKCS7(byte[] unpaddedBytes) {
        byte[] paddedBytes;
        int plaintextLength = unpaddedBytes.length;
        int plaintextRemainder = 16 - (plaintextLength % 16);
        paddedBytes = new byte[plaintextLength + plaintextRemainder];
        System.arraycopy(unpaddedBytes, 0, paddedBytes, 0, unpaddedBytes.length);
        for (int byteIndex = unpaddedBytes.length; byteIndex < paddedBytes.length; byteIndex++) {
            paddedBytes[byteIndex] = (byte) plaintextRemainder;
        }
        return paddedBytes;
    }
    public byte[] aes256encryptionCBC(byte[] plaintextBytesUnpadded, int[] expansionArr, byte[] iv) {
        byte[] plaintextBytesPadded = paddingPKCS7(plaintextBytesUnpadded);

        byte[][] plaintext16ByteBlocks = new byte[plaintextBytesPadded.length / 16][16];
        for (int i = 0; i < plaintextBytesPadded.length; i++) {
            plaintext16ByteBlocks[i/16][i%16] = plaintextBytesPadded[i];
        }

        byte[] cipherblockPrev = iv;
        for (int i = 0; i < plaintext16ByteBlocks.length; i++) {
            for (int k = 0; k < 16; k++) {
                plaintext16ByteBlocks[i][k] ^= cipherblockPrev[k];
            }

            plaintext16ByteBlocks[i] = rijndael256encrypt(plaintext16ByteBlocks[i], expansionArr);
            cipherblockPrev = plaintext16ByteBlocks[i];
        }

        for (int i = 0; i < plaintext16ByteBlocks.length; i++) {
            for (int k = 0; k < 16; k++) {
                plaintextBytesPadded[16*i + k] = plaintext16ByteBlocks[i][k];
            }
        }

        return plaintextBytesPadded;
    }
    public char[] aes256decryptionCBC(byte[] ciphertextInput, int[] expansionArr, byte[] iv) {

        byte[][] ciphertext16ByteBlocks = new byte[ciphertextInput.length / 16][16];
        for (int i = 0; i < ciphertextInput.length; i++) {
            ciphertext16ByteBlocks[i/16][i%16] = ciphertextInput[i];
        }

        byte[] cipherblockPrev = iv;
        for (int i = 0; i < ciphertext16ByteBlocks.length; i++) {
            byte[] cipherblockPresent = ciphertext16ByteBlocks[i].clone();
            byte[] plaintextBlock = rijndael256decrypt(ciphertext16ByteBlocks[i], expansionArr);
            for (int k = 0; k < 16; k++) {
                ciphertext16ByteBlocks[i][k] = (byte) (plaintextBlock[k] ^ cipherblockPrev[k]);
            }
            cipherblockPrev = cipherblockPresent;
        }

        byte[] returnedPlaintext = ciphertextInput.clone();
        for (int i = 0; i < ciphertext16ByteBlocks.length; i++) {
            for (int k = 0; k < 16; k++) {
                returnedPlaintext[16*i + k] = ciphertext16ByteBlocks[i][k];
            }
        }


        // Removing any PKCS#7 padding that was done during encryption
        int padValue = returnedPlaintext[returnedPlaintext.length - 1] & 0xFF;
        if (padValue < 1 || padValue > 16) throw new IllegalArgumentException("Invalid padding"); // this exception causes a generic fallback in the frontend fetchMessages() decryption-call.
        for (int i = 1; i <= padValue; i++) {
            if (returnedPlaintext[returnedPlaintext.length - i] != padValue) throw new IllegalArgumentException("Invalid padding");
        }
        return byteToCharArr(
                Arrays.copyOfRange(
                        returnedPlaintext,
                        0, returnedPlaintext.length - (returnedPlaintext[returnedPlaintext.length - 1] & 0xFF)
                )
        );
    }
}
