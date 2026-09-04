package com.example.encryptMsg.cryptography.customrolled.sha;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

// inherited by AES256Universal, which itself has 2 inheritors
@Service
public class SHA256 {
    // SHA-256 (public) and HMAC-SHA-256 (public)

    private static final int[] constants = new int[]{
            0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    };

    private int rightRotation(int operand, int shiftCount) {
        return (operand << (32 - shiftCount)) | (operand >>> shiftCount);
    }
    private int chooseSHA(int operand1, int operand2, int operand3) {
        return (operand1 & operand2) ^ (~operand1 & operand3);
    }
    private int majoritySHA(int operand1, int operand2, int operand3) {
        return (operand1 & operand2) ^ (operand1 & operand3) ^ (operand2 & operand3);
    }

    public byte[] sha256(byte[] plaintextUnpadded) {
        byte[] freeByteArray;

        // SHA padding
        int plaintextLength = plaintextUnpadded.length;
        int plaintextRemainder = 64 - Math.toIntExact(plaintextLength & 63); // 512 bits * (1 byte / 8 bits) = 64 bytes
        if (plaintextRemainder < 9) plaintextRemainder += 64;
        int plaintextPaddedLength = Math.toIntExact(plaintextLength + plaintextRemainder);
        freeByteArray = new byte[plaintextPaddedLength];

        System.arraycopy(plaintextUnpadded, 0, freeByteArray, 0, plaintextLength);

        freeByteArray[plaintextLength] = (byte) 0x80; // bitstream 10000000
        for (int i = plaintextLength + 1; i < freeByteArray.length - 8; i++) {
            freeByteArray[i] = 0; // bitstream 00000000
        }

        ByteBuffer.wrap(freeByteArray, plaintextPaddedLength - 8, 8)
                .order(ByteOrder.BIG_ENDIAN)
                .asLongBuffer()
                .put(plaintextLength * 8L);

        // convert the plaintext into an array of 32-bit words such that there are 16 words per 512-bit block.
        int[] wordArr16 = new int[plaintextPaddedLength / 4]; // 4 bytes (32 bits; 2 chars) per 1 int

        ByteBuffer.wrap(freeByteArray, 0, plaintextPaddedLength)
                .order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer()
                .get(wordArr16);

        // operations on predefined hash-values and round-constants
        final int[] hashValues = new int[]{0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};

        // convert each batch of 16 32-bit word-batch to a batch of 64 32-bit words.
        for (int blockCtr = 0; blockCtr < wordArr16.length; blockCtr += 16) {
            int[] wordArr64 = new int[64];

            // copying the 16-word array into the first 16 words of the 64-word array
            for (int k = 0; k < 16; k++) {
                wordArr64[k] = wordArr16[blockCtr + k];
            }

            // computing the other 48 words
            for (int k = 16; k < 64; k++) {
                wordArr64[k] =
                        (rightRotation(wordArr64[k - 2], 17) ^ rightRotation(wordArr64[k - 2], 19) ^ (wordArr64[k - 2] >>> 10))
                                + wordArr64[k - 7]
                                + (rightRotation(wordArr64[k - 15], 7) ^ rightRotation(wordArr64[k - 15], 18) ^ wordArr64[k - 15] >>> 3)
                                + wordArr64[k - 16];
            }

            int[] variables = hashValues.clone();

            for (int i = 0; i < 64; i++) {
                int value1 =
                        variables[7]
                                + (
                                rightRotation(variables[4], 6)
                                        ^ rightRotation(variables[4], 11)
                                        ^ rightRotation(variables[4], 25)
                        )
                                + chooseSHA(variables[4], variables[5], variables[6])
                                + constants[i]
                                + wordArr64[i];
                int value2 =
                        (rightRotation(variables[0], 2)
                                ^ rightRotation(variables[0], 13)
                                ^ rightRotation(variables[0], 22)
                        ) + majoritySHA(variables[0], variables[1], variables[2]);
                variables[7] = variables[6];
                variables[6] = variables[5];
                variables[5] = variables[4];
                variables[4] = variables[3] + value1;
                variables[3] = variables[2];
                variables[2] = variables[1];
                variables[1] = variables[0];
                variables[0] = value1 + value2;
            }
            for (int i = 0; i < 8; i++) {
                hashValues[i] += variables[i];
            }
        }

        ByteBuffer.wrap(freeByteArray, 0, 32)
                .order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer()
                .put(hashValues);

        return Arrays.copyOfRange(freeByteArray, 0, 32);
    }
    public byte[] hmacSha256(byte[] innerKeyPadding, byte[] outerKeyPadding, byte[] messageBytes) {
        byte[] innerPaddedMessage = new byte[64 + messageBytes.length];
        System.arraycopy(innerKeyPadding, 0, innerPaddedMessage, 0, 64);
        System.arraycopy(messageBytes, 0, innerPaddedMessage, 64, messageBytes.length);

        byte[] outerPaddedInnerHash = new byte[64 + 32];
        System.arraycopy(outerKeyPadding, 0, outerPaddedInnerHash, 0, 64);
        System.arraycopy(sha256(innerPaddedMessage), 0, outerPaddedInnerHash, 64, 32);

        return sha256(outerPaddedInnerHash);
    }
}
