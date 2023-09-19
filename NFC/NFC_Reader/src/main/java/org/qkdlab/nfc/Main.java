package org.qkdlab.nfc;

import javax.smartcardio.CardException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        NfcReader nfcReader = null;
        try {
            nfcReader = new NfcReader();
            nfcReader.init();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        while(true) {
            try {

                byte[] data = nfcReader.receiveData();
                System.out.println(new String(data));

            } catch (CardException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}