package com.example.encryptMsg.cryptography.customrolled.aes;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorOperators;
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
        int plaintextRemainder = 16 - (plaintextLength & 15);
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
        for (int i = 0; i < plaintext16ByteBlocks.length; i++) {
            ByteVector.fromArray(vectorSpecies_block128bit, plaintextBytesPadded, i * 16)
                    .intoArray(plaintext16ByteBlocks[i], 0);
        }

        byte[] cipherblockPrev = iv;
        for (int i = 0; i < plaintext16ByteBlocks.length; i++) {
            ByteVector.fromArray(
                            vectorSpecies_block128bit,
                            plaintext16ByteBlocks[i],
                            0
                    )
                    .lanewise( // parallel XOR
                            VectorOperators.XOR,
                            ByteVector.fromArray(vectorSpecies_block128bit, cipherblockPrev, 0)
                    )
                    .intoArray(plaintext16ByteBlocks[i], 0);

            plaintext16ByteBlocks[i] = rijndael256encrypt(plaintext16ByteBlocks[i], expansionArr);
            cipherblockPrev = plaintext16ByteBlocks[i];
        }

        for (int i = 0; i < plaintext16ByteBlocks.length; i++) {
            ByteVector.fromArray(vectorSpecies_block128bit, plaintext16ByteBlocks[i], 0)
                    .intoArray(plaintextBytesPadded, 16*i);
        }

        return plaintextBytesPadded;
    }
    public char[] aes256decryptionCBC(byte[] ciphertextInput, int[] expansionArr, byte[] iv) {
        byte[][] ciphertext16ByteBlocks = new byte[ciphertextInput.length / 16][16];
        for (int i = 0, blockIndex = 0; i < ciphertextInput.length; i += 16, blockIndex++) {
            ByteVector.fromArray(vectorSpecies_block128bit, ciphertextInput, i)
                    .intoArray(ciphertext16ByteBlocks[blockIndex], 0);
        }

        byte[] cipherblockPrev = iv;
        for (int i = 0; i < ciphertext16ByteBlocks.length; i++) {
            byte[] cache_cipherblockPresent = ciphertext16ByteBlocks[i].clone();
            ByteVector.fromArray(
                            vectorSpecies_block128bit,
                            rijndael256decrypt(ciphertext16ByteBlocks[i], expansionArr),
                            0
                    )
                    .lanewise( // parallel XOR
                            VectorOperators.XOR,
                            ByteVector.fromArray(vectorSpecies_block128bit, cipherblockPrev, 0)
                    )
                    .intoArray(ciphertext16ByteBlocks[i], 0);
            cipherblockPrev = cache_cipherblockPresent;
        }

        byte[] returnedPlaintext = ciphertextInput.clone();
        for (int i = 0; i < ciphertext16ByteBlocks.length; i++) {
            ByteVector.fromArray(vectorSpecies_block128bit, ciphertext16ByteBlocks[i], 0)
                    .intoArray(returnedPlaintext, 16 * i);
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
