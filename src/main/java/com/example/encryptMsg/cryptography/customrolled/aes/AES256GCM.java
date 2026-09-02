package com.example.encryptMsg.cryptography.customrolled.aes;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AES256GCM extends AES256Universal {
    // Galois Counter Mode for AES-256 (tag length 128 bits)
    public AES256GCM(SHA256 sha256) {
        super(sha256);
    }

    private long[] byteToLongArr(byte[] block16) {
        long[] toReturn = new long[2];
        for (int i = 0; i < 8; i++) {
            toReturn[0] = (toReturn[0] << 8) | (block16[i] & 0xFFL);
            toReturn[1] = (toReturn[1] << 8) | (block16[i + 8] & 0xFFL);
        }
        return toReturn;
    }
    private byte[] longToByteArr(long[] long2) {
        byte[] concatToReturn = new byte[16];
        for (int i = 0; i <= 1; i++) {
            concatToReturn[8*i]     = (byte)(long2[i] >>> 56);
            concatToReturn[8*i + 1] = (byte)(long2[i] >>> 48);
            concatToReturn[8*i + 2] = (byte)(long2[i] >>> 40);
            concatToReturn[8*i + 3] = (byte)(long2[i] >>> 32);
            concatToReturn[8*i + 4] = (byte)(long2[i] >>> 24);
            concatToReturn[8*i + 5] = (byte)(long2[i] >>> 16);
            concatToReturn[8*i + 6] = (byte)(long2[i] >>>  8);
            concatToReturn[8*i + 7] = (byte)(long2[i]);
        }
        return concatToReturn;
    }


    // GHASH
    private byte[] galoisHash(byte[] H, byte[] ciphertext) {
        long[] hLong = byteToLongArr(H);
        long[] accumulatorToReturn = new long[2];

        // hashing through non-padded blocks
        int blockCount_totalInCiphertext = ciphertext.length / 16;
        for (int i = 0; i < blockCount_totalInCiphertext; i++) {
            long[] cipherBlockLong =
                    byteToLongArr(
                            Arrays.copyOfRange(ciphertext, i * 16, i * 16 + 16)
                    );
            accumulatorToReturn[0] ^= cipherBlockLong[0];
            accumulatorToReturn[1] ^= cipherBlockLong[1];
            accumulatorToReturn = galoisMultiplyGF2_128(accumulatorToReturn, hLong);
        }

        // hashing through padded block
        int remainder = ciphertext.length % 16;
        if (remainder > 0) {
            byte[] cipherBlockBytes = new byte[16];
            System.arraycopy(ciphertext, blockCount_totalInCiphertext * 16, cipherBlockBytes, 0, remainder);
            long[] cipherBlockLong = byteToLongArr(cipherBlockBytes);
            accumulatorToReturn[0] ^= cipherBlockLong[0];
            accumulatorToReturn[1] ^= cipherBlockLong[1];
            accumulatorToReturn = galoisMultiplyGF2_128(accumulatorToReturn, hLong);
        }

        long ciphertextBitlength = (long) ciphertext.length * 8;
        accumulatorToReturn[0] ^= 0L; // AAD value (set to 0)
        accumulatorToReturn[1] ^= ciphertextBitlength;
        accumulatorToReturn = galoisMultiplyGF2_128(accumulatorToReturn, hLong);

        return longToByteArr(accumulatorToReturn);
    }
    // finite-field multiplication in GF(2^128)
    public long[] galoisMultiplyGF2_128(long[] operand1, long[] operand2) {
        long[] productAccumulator = new long[2];
        long low1 = operand1[0], high1 = operand1[1];

        for (int i = 0; i < 128; i++) {
            long presentBit_operand2 = (i < 64) ? ((operand2[0] >>> i) & 1L) : ((operand2[1] >>> (i - 64)) & 1L);

            // every time a 1-bit is found in operand2, XORing the product-array with the proper rotation of operand1 will function as addition.
            if (presentBit_operand2 == 1L) {
                productAccumulator[0] ^= low1;
                productAccumulator[1] ^= high1;
            }

            // If the least-significant-bit of operand1 is 1, operand1 will overflow during right rotation.
            boolean willOverflow = ((high1 & 1L) == 1L);

            // Multiplying operand1 by 2, which translates to right rotation of operand1 by 1 bit
            high1 = (high1 >>> 1) | (low1 << 63);
            low1 = (low1 >>> 1);

            if (willOverflow) {
                // multiply operand1 by GF(2^128) reversed polynomial 0xE1 until it is certainly within the Galois field.
                low1 ^= 0xE100000000000000L;
            }
        }
        return productAccumulator;
    }


    public byte[] aes256encryptionGCM(byte[] plaintextBytes, int[] expansionArr, byte[] nonce96Bit) {
        byte[] H = rijndael256encrypt(new byte[16], expansionArr);

        byte[] J0 = new byte[16]; // nonce + counter
        System.arraycopy(nonce96Bit, 0, J0, 0, 12);
        J0[15] = 1;
        byte[] J0incremented = J0.clone();

        byte[] ciphertextBytes = new byte[plaintextBytes.length];
        for (int i = 0; i < plaintextBytes.length; i += 16) {
            for (int k = 15; k >= 12; k--) if (++J0incremented[k] != 0) break;
            byte[] J0incrementedCiphertext = rijndael256encrypt(J0incremented, expansionArr);

            int lengthOf_presentBlock = Math.min(16, plaintextBytes.length - i);
            for (int k = 0; k < lengthOf_presentBlock; k++) {
                ciphertextBytes[i + k] = (byte) (plaintextBytes[i + k] ^ J0incrementedCiphertext[k]);
            }
        }

        byte[] tag = new byte[16];
        byte[] J0Ciphertext = rijndael256encrypt(J0, expansionArr);
        for (int i = 0; i < 16; i++) {
            tag[i] = (byte) (galoisHash(H, ciphertextBytes)[i] ^ J0Ciphertext[i]);
        }
        byte[] finalOutput = new byte[ciphertextBytes.length + 16];
        System.arraycopy(ciphertextBytes, 0, finalOutput, 0, ciphertextBytes.length);
        System.arraycopy(tag, 0, finalOutput, ciphertextBytes.length, 16);
        return finalOutput;
    }
    public char[] aes256decryptionGCM(byte[] ciphertextInput, int[] expansionArr, byte[] nonce96Bit) {
        if (ciphertextInput.length < 16) throw new IllegalArgumentException("There are too few ciphertext bytes for a GCM tag to possibly exist.");
        int ciphertextByteCount = ciphertextInput.length - 16;
        byte[] ciphertextBytes = new byte[ciphertextByteCount];
        byte[] inputTagGCM = new byte[16];
        System.arraycopy(ciphertextInput, 0, ciphertextBytes, 0, ciphertextByteCount);
        System.arraycopy(ciphertextInput, ciphertextByteCount, inputTagGCM, 0, 16);

        byte[] H = rijndael256encrypt(new byte[16], expansionArr);
        byte[] J0 = new byte[16]; // nonce + counter
        System.arraycopy(nonce96Bit, 0, J0, 0, 12);
        J0[15] = 1;

        byte[] expectedTagGCM = new byte[16];
        byte[] J0Ciphertext = rijndael256encrypt(J0, expansionArr);
        for (int i = 0; i < 16; i++) {
            expectedTagGCM[i] = (byte) (galoisHash(H, ciphertextBytes)[i] ^ J0Ciphertext[i]);
        }

        if (!compareByteArrays_constantTime(expectedTagGCM, inputTagGCM)) throw new IllegalArgumentException("Message tampered or wrong password!");

        byte[] J0incremented = J0.clone();
        byte[] plaintextBytes = new byte[ciphertextByteCount];

        for (int i = 0; i < ciphertextByteCount; i += 16) {
            for (int k = 15; k >= 12; k--) if (++J0incremented[k] != 0) break;
            byte[] J0incrementedCiphertext = rijndael256encrypt(J0incremented, expansionArr);

            int lengthOf_presentBlock = Math.min(16, ciphertextBytes.length - i);
            for (int k = 0; k < lengthOf_presentBlock; k++) {
                plaintextBytes[i + k] = (byte) (ciphertextBytes[i + k] ^ J0incrementedCiphertext[k]);
            }
        }
        return byteToCharArr(plaintextBytes);
    }
}
