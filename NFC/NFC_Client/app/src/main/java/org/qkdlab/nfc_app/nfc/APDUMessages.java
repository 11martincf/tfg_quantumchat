package org.qkdlab.nfc_app.nfc;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * APDUMessages
 *
 * Clase estática con utilidades para crear y parsear APDUs
 */
public class APDUMessages {

    //Mensajes completos que se envían en situaciones concretas
    public static final byte[] RESPONSE_OK = {(byte) 0x90, (byte) 0x00};
    public static final byte[] SELECT_RESPONSE_NOK = {(byte) 0x6A, (byte) 0x82};
    public static final byte[] INTERNAL_ERROR_RESPONSE = {(byte)0x6F, 0x00};
    public static final byte[] UNKNOWN_COMMAND_RESPONSE = {(byte) 0xff, (byte) 0x00};

    // Instrucciones que indican el tipo de mensaje
    public static final byte TRANSFER_FILE_CLASS = (byte) 0xC1;
    public static final byte GET_SIZE_INSTRUCTION = (byte) 0xC0;
    public static final byte READ_FILE_INSTRUCTION = (byte) 0xB0;
    public static final byte END_TRANSFER_INSTRUCTION = (byte) 0xFC;
    public static final byte AUTH_SUCCESS_INSTRUCTION = (byte) 0xA0;
    public static final byte AUTH_ERROR_INSTRUCTION = (byte) 0x6F;

    /**
     * Construye mensaje indicando el tamaño. Si el tamaño cabe en 1 byte, añade 0x00
     * @param size tamaño de la prueba a enviar
     * @return APDU lista para enviar al lector
     */
    public static byte[] buildSizeMessage(int size) {
        byte[] result = null;
        if (size > 255) {
            result = BigInteger.valueOf(size & 0xFFFFFFFL).toByteArray();
        }
        else {
            byte[] sizeArraySmall = new byte[2];
            sizeArraySmall[1] = (byte)size;

            result = sizeArraySmall;
        }
        return addOkFlag(result);
    }

    public static byte[] buildTransferMessage(byte[] message, int seq) {
        int from = TunnelApduService.BYTES_IN_RESPONSE * seq;
        int to = Math.min(TunnelApduService.BYTES_IN_RESPONSE * (seq + 1), message.length);
        byte[] responseRead = Arrays.copyOfRange(message, from, to);
        return addOkFlag(responseRead);
    }

    /**
     * Función auxiliar para añadir la cabecera de "exito" (0x90 0x00) a una APDU
     * @param message mensaje al que añadir cabecera
     * @return APDU con cabecera
     */
    private static byte[] addOkFlag(byte[] message) {
        byte[] padding = {(byte)0x90, 0};
        ByteBuffer buf = ByteBuffer.allocate(message.length + padding.length);
        buf.put(message);
        buf.put(padding);
        return buf.array();
    }

    /**
     * Extrae la cabecera de un mensaje recibido
     * @param commandApdu APDU recibida del lector
     * @return contenido de la APDU sin cabecera
     */
    public static byte[] parseSuccessApdu(byte[] commandApdu) {
        return Arrays.copyOfRange(commandApdu, 2, commandApdu.length);
    }
}
