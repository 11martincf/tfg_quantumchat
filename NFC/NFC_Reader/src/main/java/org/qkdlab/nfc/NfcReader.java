package org.qkdlab.nfc;

import javax.smartcardio.*;
import java.io.IOException;
import java.util.List;

public class NfcReader {
    private CardTerminal terminal;

    public void init() throws IOException {
        // show the list of available terminals
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = null;
        try {
            terminals = factory.terminals().list();
        } catch (CardException e) {
            throw new IOException(e);
        }
        System.out.println("Terminals: " + terminals);
        terminal = terminals.get(0);
    }

    public byte[] receiveData() throws CardException {
        byte[] result = new byte[0];
        Card card = connect();
        NfcTunnelManager tunnelManager = new NfcHceTunnelManager(card);

        tunnelManager.establishConnection();
        result = tunnelManager.receiveData();
        tunnelManager.disconnect();


        return result;
    }

    public NfcTunnelManager createConnection() throws IOException {
        NfcTunnelManager tunnelManager;
        try {
            Card card = connect();
            tunnelManager = new NfcHceTunnelManager(card);
            tunnelManager.establishConnection();
        } catch (CardException e) {
            throw new IOException(e);
        }

        return tunnelManager;
    }

    protected Card connect() throws CardException {
        terminal.waitForCardPresent(0);
        // establish a connection with the card
        Card card = terminal.connect("*");
        //System.out.println("card: " + card);

        byte buzzerOn = (byte)0xFF;
        byte buzzerOff = (byte)0x00;
        byte clazz = (byte)0xFF;
        byte ins = (byte)0x00;
        byte p1 = (byte)0x52;
        byte p2 = buzzerOff;
        byte le = (byte)0x00;

        byte[] apdu = new byte[]{clazz,ins,p1,p2,le};
        ResponseAPDU answer = card.getBasicChannel().transmit( new CommandAPDU(apdu));

        return card;
    }

    public String getNfcTerminalName() {
        return terminal.getName();
    }
}
