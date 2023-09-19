package org.qkdlab.nfc;

public class APDUMessages {

    public static final byte TRANSFER_FILE_CLASS = (byte)0xC1;
    public static final byte REQUEST_SIZE_INSTRUCTION = (byte)0xC0;
    public static final byte REQUEST_FILE_INSTRUCTION = (byte)0xB0;
    public static final byte END_CONNECTION_INSTRUCTION = (byte)0xFC;
    public static final byte AUTH_SUCCESS_INSTRUCTION = (byte)0xA0;
    public static final byte AUTH_ERROR_INSTRUCTION = (byte)0x6F;


    public static final byte[] SELECT_AID_APDU = {0, (byte)0xA4, 4, 0, 7, (byte)0xF0, 1, 2, 3, 4, 5, 6};
    public static final byte[] REQUEST_SIZE_APDU = {TRANSFER_FILE_CLASS, REQUEST_SIZE_INSTRUCTION, 0, 0};
    public static final byte[] END_CONNECTION_APDU = {TRANSFER_FILE_CLASS, END_CONNECTION_INSTRUCTION, 0, 0};
    public static final byte[] AUTH_ERROR_APDU = {TRANSFER_FILE_CLASS, AUTH_ERROR_INSTRUCTION, 0, 0};
    public static final byte[] RESPONSE_OK = {(byte)0x90, 0};
    public static final byte[] RESPONSE_NOK = {(byte)0x6A, (byte)0x82};
    public static final byte[] INTERNAL_ERROR_RESPONSE = {(byte)0x6F, 0x00};

    public static byte[] apduRequestWithSeq(int seq) {
        return new byte[] {TRANSFER_FILE_CLASS, REQUEST_FILE_INSTRUCTION, 0x00, (byte)seq};
    }

    public static byte[] apduWithKey(byte[] key) {
        byte[] result = new byte[key.length + 2];
        result[0] = TRANSFER_FILE_CLASS;
        result[1] = AUTH_SUCCESS_INSTRUCTION;
        System.arraycopy(key, 0, result, 2, key.length);

        return result;
    }
}
