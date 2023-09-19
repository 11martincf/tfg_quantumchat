package org.qkdlab.nfc;

import javax.smartcardio.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class NfcTunnelManager {
    protected final Card card;
    protected final CardChannel channel;

    public NfcTunnelManager(Card card) {
        this.card = card;
        this.channel = card.getBasicChannel();
    }

    protected byte[] sendApdu(byte[] apduBytes) throws CardException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(258);

        int received = sendApdu(apduBytes, responseBuffer);
        //System.out.println(received);

        byte[] result = Arrays.copyOf(responseBuffer.array(), received);

        if(result.length < 2) {
            throw new CardException("Received empty message.");
        }

        byte[] flags = Arrays.copyOfRange(result, result.length - 2, result.length);

        if(Arrays.equals(flags, APDUMessages.RESPONSE_NOK)) {
            throw new CardException("No info to send");
        }
        else if(Arrays.equals(flags, APDUMessages.INTERNAL_ERROR_RESPONSE)) {
            throw new CardException("Internal error in client.");
        }
        else if(!Arrays.equals(flags, APDUMessages.RESPONSE_OK)) {
            System.out.println(Arrays.toString(result));
            throw new CardException("Unknown message received");
        }

        return result;
    }

    protected int sendApdu(byte[] apduBytes, ByteBuffer responseBuffer) throws CardException {
        ByteBuffer commandBuffer = ByteBuffer.wrap(apduBytes);

        return channel.transmit(commandBuffer, responseBuffer);
    }
    
    protected byte[] parseResponseApdu(byte[] responseApdu) {

        if((responseApdu[responseApdu.length - 2] == (byte)0x90) && (responseApdu[responseApdu.length - 1] == 0)) {
            return Arrays.copyOfRange(responseApdu, 0, responseApdu.length - 2);
        }

        return responseApdu;
    }

    public abstract void establishConnection() throws CardException;

    public abstract byte[] receiveData() throws CardException;

    public abstract void sendError() throws CardException;
    public abstract void sendSuccess(byte[] key) throws CardException;

    public void disconnect() throws CardException {
        card.disconnect(false);
    }

    public String getCardName() {
        return card.toString();
    }
}
