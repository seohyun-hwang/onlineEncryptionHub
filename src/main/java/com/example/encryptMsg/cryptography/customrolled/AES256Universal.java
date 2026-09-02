package com.example.encryptMsg.cryptography.customrolled;

import com.example.encryptMsg.cryptography.customrolled.sha.SHA256;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@Primary
public class AES256Universal {
    // salted PBKDF2 key-stretching (public), salted Rijndael key-expansion (public)
    // Rijndael encryption (protected), Rijndael decryption (protected)
    // constant-time array comparison (public)

    private final SHA256 sha256;

    public AES256Universal(SHA256 sha256) {
        this.sha256 = sha256;
    }

    private final short[] rijndaelSBOX = new short[]{
            0x63,0x7c,0x77,0x7b,0xf2,0x6b,0x6f,0xc5,0x30,0x01,0x67,0x2b,0xfe,0xd7,0xab,0x76,
            0xca,0x82,0xc9,0x7d,0xfa,0x59,0x47,0xf0,0xad,0xd4,0xa2,0xaf,0x9c,0xa4,0x72,0xc0,
            0xb7,0xfd,0x93,0x26,0x36,0x3f,0xf7,0xcc,0x34,0xa5,0xe5,0xf1,0x71,0xd8,0x31,0x15,
            0x04,0xc7,0x23,0xc3,0x18,0x96,0x05,0x9a,0x07,0x12,0x80,0xe2,0xeb,0x27,0xb2,0x75,
            0x09,0x83,0x2c,0x1a,0x1b,0x6e,0x5a,0xa0,0x52,0x3b,0xd6,0xb3,0x29,0xe3,0x2f,0x84,
            0x53,0xd1,0x00,0xed,0x20,0xfc,0xb1,0x5b,0x6a,0xcb,0xbe,0x39,0x4a,0x4c,0x58,0xcf,
            0xd0,0xef,0xaa,0xfb,0x43,0x4d,0x33,0x85,0x45,0xf9,0x02,0x7f,0x50,0x3c,0x9f,0xa8,
            0x51,0xa3,0x40,0x8f,0x92,0x9d,0x38,0xf5,0xbc,0xb6,0xda,0x21,0x10,0xff,0xf3,0xd2,
            0xcd,0x0c,0x13,0xec,0x5f,0x97,0x44,0x17,0xc4,0xa7,0x7e,0x3d,0x64,0x5d,0x19,0x73,
            0x60,0x81,0x4f,0xdc,0x22,0x2a,0x90,0x88,0x46,0xee,0xb8,0x14,0xde,0x5e,0x0b,0xdb,
            0xe0,0x32,0x3a,0x0a,0x49,0x06,0x24,0x5c,0xc2,0xd3,0xac,0x62,0x91,0x95,0xe4,0x79,
            0xe7,0xc8,0x37,0x6d,0x8d,0xd5,0x4e,0xa9,0x6c,0x56,0xf4,0xea,0x65,0x7a,0xae,0x08,
            0xba,0x78,0x25,0x2e,0x1c,0xa6,0xb4,0xc6,0xe8,0xdd,0x74,0x1f,0x4b,0xbd,0x8b,0x8a,
            0x70,0x3e,0xb5,0x66,0x48,0x03,0xf6,0x0e,0x61,0x35,0x57,0xb9,0x86,0xc1,0x1d,0x9e,
            0xe1,0xf8,0x98,0x11,0x69,0xd9,0x8e,0x94,0x9b,0x1e,0x87,0xe9,0xce,0x55,0x28,0xdf,
            0x8c,0xa1,0x89,0x0d,0xbf,0xe6,0x42,0x68,0x41,0x99,0x2d,0x0f,0xb0,0x54,0xbb,0x16
    };

    // Inverse operations are only needed in CBC mode.
    private final short[] rijndaelInverseSBOX = new short[]{
            0x52,0x09,0x6a,0xd5,0x30,0x36,0xa5,0x38,0xbf,0x40,0xa3,0x9e,0x81,0xf3,0xd7,0xfb,
            0x7c,0xe3,0x39,0x82,0x9b,0x2f,0xff,0x87,0x34,0x8e,0x43,0x44,0xc4,0xde,0xe9,0xcb,
            0x54,0x7b,0x94,0x32,0xa6,0xc2,0x23,0x3d,0xee,0x4c,0x95,0x0b,0x42,0xfa,0xc3,0x4e,
            0x08,0x2e,0xa1,0x66,0x28,0xd9,0x24,0xb2,0x76,0x5b,0xa2,0x49,0x6d,0x8b,0xd1,0x25,
            0x72,0xf8,0xf6,0x64,0x86,0x68,0x98,0x16,0xd4,0xa4,0x5c,0xcc,0x5d,0x65,0xb6,0x92,
            0x6c,0x70,0x48,0x50,0xfd,0xed,0xb9,0xda,0x5e,0x15,0x46,0x57,0xa7,0x8d,0x9d,0x84,
            0x90,0xd8,0xab,0x00,0x8c,0xbc,0xd3,0x0a,0xf7,0xe4,0x58,0x05,0xb8,0xb3,0x45,0x06,
            0xd0,0x2c,0x1e,0x8f,0xca,0x3f,0x0f,0x02,0xc1,0xaf,0xbd,0x03,0x01,0x13,0x8a,0x6b,
            0x3a,0x91,0x11,0x41,0x4f,0x67,0xdc,0xea,0x97,0xf2,0xcf,0xce,0xf0,0xb4,0xe6,0x73,
            0x96,0xac,0x74,0x22,0xe7,0xad,0x35,0x85,0xe2,0xf9,0x37,0xe8,0x1c,0x75,0xdf,0x6e,
            0x47,0xf1,0x1a,0x71,0x1d,0x29,0xc5,0x89,0x6f,0xb7,0x62,0x0e,0xaa,0x18,0xbe,0x1b,
            0xfc,0x56,0x3e,0x4b,0xc6,0xd2,0x79,0x20,0x9a,0xdb,0xc0,0xfe,0x78,0xcd,0x5a,0xf4,
            0x1f,0xdd,0xa8,0x33,0x88,0x07,0xc7,0x31,0xb1,0x12,0x10,0x59,0x27,0x80,0xec,0x5f,
            0x60,0x51,0x7f,0xa9,0x19,0xb5,0x4a,0x0d,0x2d,0xe5,0x7a,0x9f,0x93,0xc9,0x9c,0xef,
            0xa0,0xe0,0x3b,0x4d,0xae,0x2a,0xf5,0xb0,0xc8,0xeb,0xbb,0x3c,0x83,0x53,0x99,0x61,
            0x17,0x2b,0x04,0x7e,0xba,0x77,0xd6,0x26,0xe1,0x69,0x14,0x63,0x55,0x21,0x0c,0x7d
    };

    private final int[] rijndaelRoundConstants = {
            0x00000000,
            0x01000000, 0x02000000, 0x04000000, 0x08000000, 0x10000000,
            0x20000000, 0x40000000, 0x80000000, 0x1B000000, 0x36000000
    };

    // Constant-time array comparison for timing-attack prevention
    public boolean compareByteArrays_constantTime(byte[] arr1, byte[] arr2) {
        if (arr1.length != arr2.length) return false;
        int concat = 0;
        for (int i = 0; i < arr1.length; i++) {
            concat |= (arr1[i] ^ arr2[i]);
        }
        return concat == 0;
    }

    // HMAC-SHA256 PBKDF2 salted masterKey stretching (iteration count: 600,000)
    public byte[] pbkdf2_hmacSha256(byte[] keyBytes, int keyLength, byte[] salt) {
        byte[] normalizedKey = new byte[64];
        if (keyLength > 64) System.arraycopy(sha256.sha256(keyBytes), 0, normalizedKey, 0, 64);
        else System.arraycopy(keyBytes, 0, normalizedKey, 0, keyLength);

        byte[] innerKeyPadding = new byte[64];
        byte[] outerKeyPadding = new byte[64];
        for (int i = 0; i < 64; i++) {
            innerKeyPadding[i] = (byte) (normalizedKey[i] ^ 0x36);
            outerKeyPadding[i] = (byte) (normalizedKey[i] ^ 0x5C);
        }

        byte[] hmacProcessed = sha256.hmacSha256(innerKeyPadding, outerKeyPadding, salt);
        byte[] accumulatorToReturn = hmacProcessed.clone();

        for (int iteration = 0; iteration < 600000; iteration++) {
            hmacProcessed = sha256.hmacSha256(innerKeyPadding, outerKeyPadding, hmacProcessed);
            for (int byteIndex = 0; byteIndex < 32; byteIndex++) {
                accumulatorToReturn[byteIndex] ^= hmacProcessed[byteIndex];
            }
        }
        return accumulatorToReturn;
    }
    public byte[] charToByteArr(char[] input) {
        if (input == null) return new byte[0];
        byte[] splitToReturn = new byte[input.length * 2];
        for (int i = 0; i < input.length; i++) {
            splitToReturn[i*2] = (byte)(input[i] >>> 8);
            splitToReturn[i*2 + 1] = (byte)(input[i]);
        }
        return splitToReturn;
    }
    public char[] byteToCharArr(byte[] input) {
        if (input == null) return new char[0];
        char[] concatToReturn = new char[input.length / 2];
        for (int i = 0; i < concatToReturn.length; i++) {
            concatToReturn[i] = (char) (((input[i*2] & 0xFF) << 8) | (input[i*2 + 1] & 0xFF));
        }
        return concatToReturn;
    }

    // finite-field multiplication in GF(2^8)
    private int galoisMultiplyGF2_8(int operand1, int operand2) {
        int productAccumulator = 0;
        for (int i = 0; i < 8; i++) {
            if ((operand2 & 1) != 0) {
                productAccumulator ^= operand1;
            }
            boolean willOverflow = (operand1 & 0x80) != 0;
            operand1 <<= 1;
            if (willOverflow) {
                // multiply operand1 by GF(2^8) polynomial 0x11b until it is certainly within the Galois field.
                operand1 ^= 0x11b;
            }
            operand2 >>= 1;
        }
        return productAccumulator & 0xFF;
    }

    private int subByte(int input, boolean inverse) {
        int concatToReturn = 0;
        short[] sboxChosen = inverse ? rijndaelInverseSBOX : rijndaelSBOX;

        for (int i = 0; i < 4; i++) {
            int bite = (input >>> (i * 8)) & 0xFF;
            concatToReturn |= sboxChosen[bite] << (i * 8);
        }
        return concatToReturn; // input 4 bytes; output 4 bytes
    }
    private void shiftRows(int[] operand, boolean inverse) {
        int column0 = operand[0];
        int column1 = operand[1];
        int column2 = operand[2];
        int column3 = operand[3];

        if (!inverse) {
            operand[0] = (column0 & 0xFF000000) | (column1 & 0x00FF0000) | (column2 & 0x0000FF00) | (column3 & 0x000000FF);
            operand[1] = (column1 & 0xFF000000) | (column2 & 0x00FF0000) | (column3 & 0x0000FF00) | (column0 & 0x000000FF);
            operand[2] = (column2 & 0xFF000000) | (column3 & 0x00FF0000) | (column0 & 0x0000FF00) | (column1 & 0x000000FF);
            operand[3] = (column3 & 0xFF000000) | (column0 & 0x00FF0000) | (column1 & 0x0000FF00) | (column2 & 0x000000FF);
        } else {
            operand[0] = (column0 & 0xFF000000) | (column3 & 0x00FF0000) | (column2 & 0x0000FF00) | (column1 & 0x000000FF);
            operand[1] = (column1 & 0xFF000000) | (column0 & 0x00FF0000) | (column3 & 0x0000FF00) | (column2 & 0x000000FF);
            operand[2] = (column2 & 0xFF000000) | (column1 & 0x00FF0000) | (column0 & 0x0000FF00) | (column3 & 0x000000FF);
            operand[3] = (column3 & 0xFF000000) | (column2 & 0x00FF0000) | (column1 & 0x0000FF00) | (column0 & 0x000000FF);
        }
    }
    private void mixColumns(int[] operand, boolean inverse) {
        for (int i = 0; i < 4; i++) {
            int column = operand[i];

            int byte0 = (column >>> 24) & 0xFF;
            int byte1 = (column >>> 16) & 0xFF;
            int byte2 = (column >>> 8) & 0xFF;
            int byte3 = column & 0xFF;

            int[] output = new int[4];
            if (!inverse) {
                output[0] = galoisMultiplyGF2_8(byte0, 2) ^ galoisMultiplyGF2_8(byte1, 3) ^ byte2 ^ byte3;
                output[1] = byte0 ^ galoisMultiplyGF2_8(byte1, 2) ^ galoisMultiplyGF2_8(byte2, 3) ^ byte3;
                output[2] = byte0 ^ byte1 ^ galoisMultiplyGF2_8(byte2, 2) ^ galoisMultiplyGF2_8(byte3, 3);
                output[3] = galoisMultiplyGF2_8(byte0, 3) ^ byte1 ^ byte2 ^ galoisMultiplyGF2_8(byte3, 2);
            } else {
                output[0] = galoisMultiplyGF2_8(byte0, 14) ^ galoisMultiplyGF2_8(byte1, 11) ^ galoisMultiplyGF2_8(byte2, 13) ^ galoisMultiplyGF2_8(byte3, 9);
                output[1] = galoisMultiplyGF2_8(byte0, 9) ^ galoisMultiplyGF2_8(byte1, 14) ^ galoisMultiplyGF2_8(byte2, 11) ^ galoisMultiplyGF2_8(byte3, 13);
                output[2] = galoisMultiplyGF2_8(byte0, 13) ^ galoisMultiplyGF2_8(byte1, 9) ^ galoisMultiplyGF2_8(byte2, 14) ^ galoisMultiplyGF2_8(byte3, 11);
                output[3] = galoisMultiplyGF2_8(byte0, 11) ^ galoisMultiplyGF2_8(byte1, 13) ^ galoisMultiplyGF2_8(byte2, 9) ^ galoisMultiplyGF2_8(byte3, 14);
            }
            operand[i] = (output[0] << 24) | (output[1] << 16) | (output[2] << 8) | output[3];
        }
    }

    // AES-256: 15 round-keys, 14 rounds, 32-byte initial key
    public int[] rijndael256expansion(byte[] masterKey) {

        int[] expansionArr = new int[60];
        // filling up the first 8 words with bytes of the master-key
        for (int i = 0; i < 8; i++) {
            expansionArr[i] =
                    ((masterKey[i*4] & 0xFF) << 24) |
                            ((masterKey[i*4 + 1] & 0xFF) << 16) |
                            ((masterKey[i*4 + 2] & 0xFF) << 8) |
                            (masterKey[i*4 + 3] & 0xFF);
        }
        // key expansion
        for (int i = 8; i < 60; i++) {
            int firstWord_ofRow = expansionArr[i - 1];
            if (i % 8 == 0) {
                // G function with RCON applied every 8th word in AES-256
                firstWord_ofRow =
                        subByte((firstWord_ofRow << 8) | (firstWord_ofRow >>> 24), false)
                                ^ rijndaelRoundConstants[i / 8];
            } else if (i % 8 == 4) {
                // G function applied every 4th word regardless of RCON
                firstWord_ofRow = subByte(firstWord_ofRow, false);
            }
            expansionArr[i] = expansionArr[i - 8] ^ firstWord_ofRow;
        }
        return expansionArr;
    }

    // returns a 16-byte ciphertext-block per 16-byte plaintext-block
    protected byte[] rijndael256encrypt(byte[] plaintextBytesPadded, int[] expansionArr) {
        // matching to int-size (32 bits) round-key array-elements
        int[] plaintextIntArr = new int[plaintextBytesPadded.length / 4];
        for (int i = 0; i < plaintextIntArr.length; i++) {
            plaintextIntArr[i] =
                    ((plaintextBytesPadded[i*4    ] & 0xFF) << 24)
                            | ((plaintextBytesPadded[i*4 + 1] & 0xFF) << 16)
                            | ((plaintextBytesPadded[i*4 + 2] & 0xFF) << 8)
                            | ( plaintextBytesPadded[i*4 + 3] & 0xFF);
        }
        // round 0: XOR(plaintext, round 0 key)
        for (int i = 0; i < plaintextIntArr.length; i += 4) {
            for (int k = 0; k < 4; k++) {
                plaintextIntArr[i+k] ^= expansionArr[k];
            }
        }
        // rounds 1-13
        for (int round = 1; round < 14; round++) {
            for (int i = 0; i < plaintextIntArr.length; i++) {
                plaintextIntArr[i] = subByte(plaintextIntArr[i], false);
            }
            for (int i = 0; i < plaintextIntArr.length; i += 4) {
                shiftRows(plaintextIntArr, false);
                mixColumns(plaintextIntArr, false);
            }

            // XOR(text, round x key)
            for (int i = 0; i < plaintextIntArr.length; i += 4) {
                for (int k = 0; k < 4; k++) {
                    plaintextIntArr[i+k] ^= expansionArr[round*4 + k];
                }
            }
        }
        // round 14
        for (int i = 0; i < plaintextIntArr.length; i++) {
            plaintextIntArr[i] = subByte(plaintextIntArr[i], false);
        }
        for (int i = 0; i < plaintextIntArr.length; i += 4) {
            shiftRows(plaintextIntArr, false);
        }
        // XOR(text, round 14 key)
        for (int i = 0; i < plaintextIntArr.length; i += 4) {
            for (int k = 0; k < 4; k++) {
                plaintextIntArr[i+k] ^= expansionArr[14*4 + k];
            }
        }


        byte[] ciphertextBlock = new byte[16];
        for (int i = 0; i < ciphertextBlock.length; i += 4) {
            int word = plaintextIntArr[i / 4];
            ciphertextBlock[i] = (byte) ((word >>> 24) & 0xFF);
            ciphertextBlock[i + 1] = (byte) ((word >>> 16) & 0xFF);
            ciphertextBlock[i + 2] = (byte) ((word >>> 8) & 0xFF);
            ciphertextBlock[i + 3] = (byte) (word & 0xFF);
        }

        return ciphertextBlock;
    }

    protected byte[] rijndael256decrypt(byte[] ciphertextInput, int[] expansionArr) {
        byte[] ciphertextBytes = ciphertextInput.clone();

        int[] ciphertext = new int[ciphertextBytes.length / 4];
        for (int i = 0; i < ciphertext.length; i++) {
            ciphertext[i] =
                    ((ciphertextBytes[i*4    ] & 0xFF) << 24)
                            | ((ciphertextBytes[i*4 + 1] & 0xFF) << 16)
                            | ((ciphertextBytes[i*4 + 2] & 0xFF) << 8)
                            | ( ciphertextBytes[i*4 + 3] & 0xFF);
        }

        // round 0: XOR(ciphertext, round 14 key)
        for (int i = 0; i < ciphertext.length; i += 4) {
            for (int k = 0; k < 4; k++) {
                ciphertext[i+k] ^= expansionArr[14*4 + k];
            }
        }
        // rounds 1-13
        for (int round = 13; round > 0; round--) {
            for (int i = 0; i < ciphertext.length; i += 4) {
                shiftRows(ciphertext, true);
            }
            for (int i = 0; i < ciphertext.length; i++) {
                ciphertext[i] = subByte(ciphertext[i], true);
            }
            // XOR(text, round x key)
            for (int i = 0; i < ciphertext.length; i += 4) {
                for (int k = 0; k < 4; k++) {
                    ciphertext[i+k] ^= expansionArr[round*4 + k];
                }
            }
            for (int i = 0; i < ciphertext.length; i += 4) {
                mixColumns(ciphertext, true);
            }
        }
        // round 14
        for (int i = 0; i < ciphertext.length; i += 4) {
            shiftRows(ciphertext, true);
        }
        for (int i = 0; i < ciphertext.length; i++) {
            ciphertext[i] = subByte(ciphertext[i], true);
        }
        // XOR(text, round 0 key)
        for (int i = 0; i < ciphertext.length; i += 4) {
            for (int k = 0; k < 4; k++) {
                ciphertext[i+k] ^= expansionArr[k];
            }
        }


        for (int i = 0; i < ciphertextBytes.length; i += 4) {
            int word = ciphertext[i / 4];
            ciphertextBytes[i] = (byte) ((word >>> 24) & 0xFF);
            ciphertextBytes[i + 1] = (byte) ((word >>> 16) & 0xFF);
            ciphertextBytes[i + 2] = (byte) ((word >>> 8) & 0xFF);
            ciphertextBytes[i + 3] = (byte) (word & 0xFF);
        }

        return ciphertextBytes;
    }
}
