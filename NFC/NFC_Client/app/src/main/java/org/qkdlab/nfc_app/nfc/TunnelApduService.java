package org.qkdlab.nfc_app.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.qkdlab.nfc_app.util.Constants;
import org.qkdlab.nfc_app.util.HexEncoder;
import org.qkdlab.nfc_app.util.InfoTransferBroker;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * TunnelApduService
 *
 * Servicio que recibe las APDUs.
 */
public class TunnelApduService extends HostApduService {

    public static final String BROADCAST_INTENT_LINK_ESTABLISHED = "LINK_ESTABLISHED";
    public static final String BROADCAST_INTENT_PROGRESS_UPDATED = "PROGRESS_UPDATED";
    public static final String BROADCAST_INTENT_AUTH_SUCCESS = "AUTH_SUCCESS";
    public static final String BROADCAST_INTENT_AUTH_ERROR = "AUTH_ERROR";
    public static final String BROADCAST_INTENT_LINK_DEACTIVATED = "LINK_DEACTIVATED";
    private static final String TAG = Constants.LOG_TAG;

    public static final int BYTES_IN_RESPONSE = 255;

    // the SELECT AID APDU issued by the terminal
    // our AID is 0xF0010203040506
    private static final byte[] SELECT_AID_COMMAND = {
            (byte) 0x00, // Class
            (byte) 0xA4, // Instruction
            (byte) 0x04, // Parameter 1
            (byte) 0x00, // Parameter 2
            (byte) 0x07, // length
            (byte) 0xF0, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06
    };

    // OK status sent in response to SELECT AID command (0x9000)

    private static final byte DATA_INSTRUCTION = (byte) 0x02;

    // Custom protocol responses by phone
    /*private static final byte READ_URL_RESPONSE = (byte) 0x00;
    private static final byte DATA_APDUMessages.RESPONSE_OK = (byte) 0x00;
    private static final byte DATA_RESPONSE_NOK = (byte) 0x01;*/

    private String url;
    private byte[] transferMessage;
    private boolean linkEstablished;

    @Override
    public void onCreate() {
        super.onCreate();
        //Debug.waitForDebugger();
    }

    private byte[] getUrlBytes() {
        try {
            return TunnelSettings.getUrl(this).getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e); // never happens
        }
    }

    /**
     * Procesa APDU recibida y genera una respuesta
     * @param commandApdu APDU recibida
     * @return APDU de respuesta
     */
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

        //Log.i(TAG, "APDU Received: " + Arrays.toString(commandApdu));
        try {
            // Si ha recibido una APDU dirigida a establecer comunicacion
            if (Arrays.equals(SELECT_AID_COMMAND, commandApdu)) {
                Log.i(TAG, "Link established");
                transferMessage = InfoTransferBroker.getInstance().getInfoToTransfer();
                if (transferMessage == null) return APDUMessages.SELECT_RESPONSE_NOK;
                notifyLinkEstablished(transferMessage.length);
                return APDUMessages.RESPONSE_OK;
            } else if (commandApdu[0] == APDUMessages.TRANSFER_FILE_CLASS) {
                if (!linkEstablished) return APDUMessages.SELECT_RESPONSE_NOK;
                // El byte [1] indica la instruccion
                switch (commandApdu[1]) {

                    // Enviar tamaño
                    case APDUMessages.GET_SIZE_INSTRUCTION:
                        byte[] responseSize = APDUMessages.buildSizeMessage(transferMessage.length);
                        //Log.i(TAG, "size sent: " + Arrays.toString(responseSize));
                        return responseSize;

                    // Enviar segmento
                    case APDUMessages.READ_FILE_INSTRUCTION:
                        int seq = commandApdu[3];
                        notifyProgressUpdate(Math.min((seq + 1) * BYTES_IN_RESPONSE, transferMessage.length), transferMessage.length);
                        return APDUMessages.buildTransferMessage(transferMessage, seq);

                    // Notificar exito
                    case APDUMessages.AUTH_SUCCESS_INSTRUCTION:
                        byte[] key = APDUMessages.parseSuccessApdu(commandApdu);
                        notifyAuthSuccess(key);
                        return APDUMessages.RESPONSE_OK;

                    // Notificar error
                    case APDUMessages.AUTH_ERROR_INSTRUCTION:
                        notifyAuthError();
                        return APDUMessages.RESPONSE_OK;

                    // Fin de la comunicacion
                    case APDUMessages.END_TRANSFER_INSTRUCTION:
                        onDeactivated(1);
                        return APDUMessages.RESPONSE_OK;
                    default:
                        return APDUMessages.UNKNOWN_COMMAND_RESPONSE;
                }
            } else {
                Log.e(TAG, "Terminal sent unknown command: " + HexEncoder.convertByteArrayToHexString(commandApdu));
                return APDUMessages.UNKNOWN_COMMAND_RESPONSE;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return APDUMessages.INTERNAL_ERROR_RESPONSE;
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Link deactivated: " + reason);

        notifyLinkDeactivated(reason);
    }


    /**
     * Envia broadcast indicando que se ha establecido conexion
     * @param msgSize tamaño del mensaje a enviar
     */
    private void notifyLinkEstablished(int msgSize) {
        linkEstablished = true;
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(200);

        Intent intent = new Intent(BROADCAST_INTENT_LINK_ESTABLISHED);
        intent.putExtra("msgSize", msgSize);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Envia broadcast indicando que se esta enviando la prueba
     * @param numSent bytes enviados
     * @param numTotal bytes totales
     */
    private void notifyProgressUpdate(int numSent, int numTotal) {
        Intent intent = new Intent(BROADCAST_INTENT_PROGRESS_UPDATED);
        intent.putExtra("numSent", numSent);
        intent.putExtra("numTotal", numTotal);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Envia broadcast informando del exito de la transmision. Incluye la clave QRNG
     * @param key clave QRNG recibida
     */
    private void notifyAuthSuccess(byte[] key) {
        transferMessage = null;
        Intent intent = new Intent(BROADCAST_INTENT_AUTH_SUCCESS);
        intent.putExtra("key", key);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Envia broadcast indicando un error en la comunicacion
     */
    private void notifyAuthError() {
        Intent intent = new Intent(BROADCAST_INTENT_AUTH_ERROR);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Envia broadcast indicando fin de la comunicacion
     * @param reason motivo del fin de la comunicacion
     */
    private void notifyLinkDeactivated(int reason) {
        linkEstablished = false;
        Intent intent = new Intent(BROADCAST_INTENT_LINK_DEACTIVATED);
        intent.putExtra("reason", reason);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}
