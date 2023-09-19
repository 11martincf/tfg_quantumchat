package org.qkdlab.nfc;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NfcHceTunnelManager extends NfcTunnelManager {
    private int numOfBytes;

    public NfcHceTunnelManager(Card card) {
        super(card);
    }



    @Override
    public void establishConnection() throws CardException {
        sendApdu(APDUMessages.SELECT_AID_APDU);

        byte[] responseSize = parseResponseApdu(sendApdu(APDUMessages.REQUEST_SIZE_APDU));
        //responseSize = parseResponseApdu(responseSize);

        numOfBytes = new BigInteger(responseSize).intValue();
    }

    public byte[] receiveData() throws CardException {
        ByteBuffer buffer = ByteBuffer.allocate(numOfBytes + 258);
        int remainingBytes = numOfBytes;
        int seq = 0;
        while(remainingBytes > 0) {
            //System.out.println(Arrays.toString(buffer.array()));
            int receivedBytes = sendApdu(APDUMessages.apduRequestWithSeq(seq), buffer);
            buffer.position(buffer.position() - 2);

            //System.out.println(receivedBytes);
            remainingBytes -= receivedBytes;
            seq++;
        }

        //System.out.println(Arrays.toString(buffer.array()));

        return Arrays.copyOf(buffer.array(), numOfBytes);
    }

    @Override
    public void sendError() throws CardException {
        sendApdu(APDUMessages.AUTH_ERROR_APDU);

        sendApdu(APDUMessages.END_CONNECTION_APDU);
    }

    @Override
    public void sendSuccess(byte[] key) throws CardException {
        for (int i = 0; i < key.length; i = i + 256) {
            byte[] roundBytes = Arrays.copyOfRange(key, i, Math.min(key.length, i + 256));
            sendApdu(APDUMessages.apduWithKey(roundBytes));
        }

        sendApdu(APDUMessages.END_CONNECTION_APDU);

    }
}
