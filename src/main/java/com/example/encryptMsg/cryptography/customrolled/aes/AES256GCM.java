package com.example.encryptMsg.cryptography.customrolled.aes;

import com.example.encryptMsg.cryptography.customrolled.AES256Universal;
import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.springframework.stereotype.Service;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

@Service
public class AES256GCM extends AES256Universal {
    // Galois Counter Mode for AES-256 (tag length 128 bits)
    public AES256GCM(SHA256 sha256) {
        super(sha256);
    }


    private long[] byteToLongArr(byte[] block16) {
        MemorySegment segment = MemorySegment.ofArray(block16);
        return new long[]{
                segment.get(longLayout_bigEndian, 0L),
                segment.get(longLayout_bigEndian, 8L)
        };
    }
    private byte[] longToByteArr(long[] long2) {
        byte[] segmentToReturn = new byte[16];
        MemorySegment segment = MemorySegment.ofArray(segmentToReturn);
        segment.set(longLayout_bigEndian, 0L, long2[0]);
        segment.set(longLayout_bigEndian, 8L, long2[1]);
        return segmentToReturn;
    }


    // GHASH
    private byte[] galoisHash(byte[] H, byte[] ciphertext, Arena arena) {
        long[] hLong = byteToLongArr(H);
        long[] accumulatorToReturn = new long[2];

        MemorySegment ciphertextSegment = MemorySegment.ofArray(ciphertext);

        // hashing through non-padded blocks
        int blockCount_totalInCiphertext = ciphertext.length / 16;
        for (int i = 0; i < blockCount_totalInCiphertext; i++) {
            long long0 = ciphertextSegment.get(longLayout_bigEndian, i * 16);
            long long1 = ciphertextSegment.get(longLayout_bigEndian, i * 16 + 8);

            accumulatorToReturn[0] ^= long0;
            accumulatorToReturn[1] ^= long1;
            accumulatorToReturn = galoisMultiplyGF2_128(accumulatorToReturn, hLong);
        }

        // hashing through padded block
        int remainder = ciphertext.length & 15;
        if (remainder != 0) {
            MemorySegment remainderSegment_offHeap = arena.allocate(16);
            MemorySegment.copy(
                    ciphertextSegment, blockCount_totalInCiphertext * 16,
                    remainderSegment_offHeap, 0,
                    remainder
            );

            long long0 = remainderSegment_offHeap.get(longLayout_bigEndian, 0L);
            long long1 = remainderSegment_offHeap.get(longLayout_bigEndian, 8L);

            accumulatorToReturn[0] ^= long0;
            accumulatorToReturn[1] ^= long1;
            accumulatorToReturn = galoisMultiplyGF2_128(accumulatorToReturn, hLong);
        }

        long ciphertextBitlength = (long) ciphertext.length * 8;
        accumulatorToReturn[0] ^= 0L; // AAD value (set to 0)
        accumulatorToReturn[1] ^= ciphertextBitlength;
        accumulatorToReturn = galoisMultiplyGF2_128(accumulatorToReturn, hLong);

        return longToByteArr(accumulatorToReturn);
    }

    // branchless constant-time finite-field multiplication in GF(2^128)
    public long[] galoisMultiplyGF2_128(long[] operand1, long[] operand2) {
        long[] productAccumulator = new long[2];
        long low1 = operand1[0], high1 = operand1[1];

        for (int i = 0; i < 128; i++) {
            int arrayIndex_operand2 = i >> 6; // 63 is a bitstream of six 1's. This field equals 0 if i < 64 and 1 if i >= 64.
            int presentBitModulo64_operand2 = i & 63; // functions as i % 64
            long presentBit_operand2 = (operand2[arrayIndex_operand2] >>> presentBitModulo64_operand2) & 1L; // least-significant-byte after right-shift

            // every time a 1-bit is found in operand2, XORing the product-array with the proper shift of operand1 will function as addition.
            long maskBinary_allSame = -presentBit_operand2; // all 1s as two's complement of -1 or all 0s as two's complement of 0
            productAccumulator[0] ^= low1 & maskBinary_allSame;
            productAccumulator[1] ^= high1 & maskBinary_allSame;

            // If the least-significant-bit of operand1 is 1, operand1 will overflow during right shift.
            long maskBinary_all1ifOverflow = -(high1 & 1L);

            // Multiplying operand1 by 2, which translates to right shift of operand1 by 1 bit
            high1 = (high1 >>> 1) | (low1 << 63);
            low1 = (low1 >>> 1);

            // subtract operand1 by GF(2^128) reversed polynomial 0xE1 until it is certainly within the Galois field.
            low1 ^= 0xE100000000000000L & maskBinary_all1ifOverflow;
        }
        return productAccumulator;
    }


    public byte[] aes256encryptionGCM(byte[] plaintextBytes, int[] expansionArr, byte[] nonce96Bit) {
        try (Arena arena = Arena.ofConfined()) {
            byte[] J0 = new byte[16]; // nonce || counter=1
            byte[] H = rijndael256encrypt(J0, expansionArr); // passing an all-0 byte-array
            System.arraycopy(nonce96Bit, 0, J0, 0, 12);
            J0[15] = 1;
            byte[] J0incremented = J0.clone();

            int counter = 1;

            byte[] ciphertextBytes = new byte[plaintextBytes.length];

            for (int i = 0; i < plaintextBytes.length; i += 16) {
                counter++;
                J0incremented[12] = (byte) (counter >>> 24);
                J0incremented[13] = (byte) (counter >>> 16);
                J0incremented[14] = (byte) (counter >>> 8);
                J0incremented[15] = (byte) counter;

                byte[] J0incrementedCiphertext = rijndael256encrypt(J0incremented, expansionArr);

                int lengthOf_presentBlock = Math.min(16, plaintextBytes.length - i);
                for (int k = 0; k < lengthOf_presentBlock; k++) {
                    ciphertextBytes[i + k] = (byte) (plaintextBytes[i + k] ^ J0incrementedCiphertext[k]);
                }
            }

            byte[] ghashBytes = galoisHash(H, ciphertextBytes, arena);
            byte[] encryptedJ0 = rijndael256encrypt(J0, expansionArr);
            byte[] tag = new byte[16];

            for (int i = 0; i < 16; i++) {
                tag[i] = (byte) (ghashBytes[i] ^ encryptedJ0[i]);
            }
            byte[] finalOutput = new byte[ciphertextBytes.length + 16];
            System.arraycopy(ciphertextBytes, 0, finalOutput, 0, ciphertextBytes.length);
            System.arraycopy(tag, 0, finalOutput, ciphertextBytes.length, 16);
            return finalOutput;
        }
    }
    public char[] aes256decryptionGCM(byte[] ciphertextInput, int[] expansionArr, byte[] nonce96Bit) {
        try (Arena arena = Arena.ofConfined()) {
            if (ciphertextInput.length < 16) throw new IllegalArgumentException("There are too few ciphertext bytes for a GCM tag to possibly exist.");

            int ciphertextByteCount = ciphertextInput.length - 16;
            byte[] ciphertextBytes = new byte[ciphertextByteCount];
            byte[] inputTagGCM = new byte[16];
            System.arraycopy(ciphertextInput, 0, ciphertextBytes, 0, ciphertextByteCount);
            System.arraycopy(ciphertextInput, ciphertextByteCount, inputTagGCM, 0, 16);

            byte[] H = rijndael256encrypt(new byte[16], expansionArr);

            byte[] J0 = new byte[16]; // nonce || counter=1
            System.arraycopy(nonce96Bit, 0, J0, 0, 12);
            J0[15] = 1;

            byte[] ghashResult = galoisHash(H, ciphertextBytes, arena);
            byte[] encryptedJ0 = rijndael256encrypt(J0, expansionArr);
            byte[] expectedTagGCM = new byte[16];
            for (int i = 0; i < 16; i++) {
                expectedTagGCM[i] = (byte) (ghashResult[i] ^ encryptedJ0[i]);
            }

            if (!compareByteArrays_constantTime(expectedTagGCM, inputTagGCM)) throw new IllegalArgumentException("Message tampered or wrong password!");

            byte[] J0incremented = J0.clone();
            int counter = 1;
            byte[] plaintextBytes = new byte[ciphertextByteCount];

            for (int i = 0; i < ciphertextByteCount; i += 16) {
                counter++;
                J0incremented[12] = (byte) (counter >>> 24);
                J0incremented[13] = (byte) (counter >>> 16);
                J0incremented[14] = (byte) (counter >>> 8);
                J0incremented[15] = (byte) counter;

                byte[] J0incrementedCiphertext = rijndael256encrypt(J0incremented, expansionArr);

                int lengthOf_presentBlock = Math.min(16, ciphertextBytes.length - i);
                for (int k = 0; k < lengthOf_presentBlock; k++) {
                    plaintextBytes[i + k] = (byte) (ciphertextBytes[i + k] ^ J0incrementedCiphertext[k]);
                }
            }

            return byteToCharArr(plaintextBytes);
        }
    }
}
