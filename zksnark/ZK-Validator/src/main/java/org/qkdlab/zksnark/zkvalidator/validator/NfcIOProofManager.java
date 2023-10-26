package org.qkdlab.zksnark.zkvalidator.validator;

import org.qkdlab.nfc.NfcReader;
import org.qkdlab.nfc.NfcTunnelManager;

import java.io.IOException;

/**
 * NfcIOProofManager
 *
 * Se comunica con los clientes a través de NFC
 */
public class NfcIOProofManager implements IOProofManager {

    private NfcReader nfcReader;

    public NfcIOProofManager() {
        nfcReader = new NfcReader();
    }


    @Override
    public void init() throws IOException {
        nfcReader.init();
    }

    @Override
    public String getIoManagerName() {
        return nfcReader.getNfcTerminalName();
    }

    /**
     * Intenta establecer una conexión con otro dispositivo
     * @return Conexión con dispositivo
     * @throws IOException
     */
    @Override
    public IOProofConnection listen() throws IOException {
        NfcTunnelManager tunnelManager;
        try {
            tunnelManager = nfcReader.createConnection();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new NfcIOProofConnection(tunnelManager);
    }

}
