package org.qkdlab.nfc_app.util;


public class HexEncoder {

    public static byte[] convertHexStringToByteArray(String s) {
        int len = (s.length() / 2) * 2;
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String convertByteArrayToHexString(byte[] value) {
        final StringBuilder stringBuilder = new StringBuilder(value.length);
        for (byte byteChar : value) {
            stringBuilder.append(String.format("%02x", byteChar));
        }
        return stringBuilder.toString();
    }
}
