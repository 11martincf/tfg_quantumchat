package org.qkdlab.zksnark.zkvalidator.validator;

import org.qkdlab.nfc.NfcTunnelManager;
import org.qkdlab.zksnark.model.message.ProofMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * NfcIOProofConnection
 *
 * Obtiene y envía información a través de NFC
 */
public class NfcIOProofConnection implements IOProofConnection {

    private NfcTunnelManager nfcTunnelManager;

    public NfcIOProofConnection(NfcTunnelManager nfcTunnelManager) {
        this.nfcTunnelManager = nfcTunnelManager;
    }

    /**
     * Recibe el zk-SNARK a través de NFC
     * @return Prueba zk-SNARK
     * @throws IOException
     */
    @Override
    public ProofMessage receiveData() throws IOException {
        byte[] receivedData;
        try {
            receivedData = nfcTunnelManager.receiveData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(receivedData);
        ObjectInputStream ois = new ObjectInputStream(bais);

        ProofMessage proofMessage;
        try {
            proofMessage = (ProofMessage) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return proofMessage;
    }

    /**
     * Tras una validación con éxito, devuelve la clave QRNG
     * @param key clave QRNG
     * @throws IOException
     */
    @Override
    public void sendSuccess(byte[] key) throws IOException {
        try {
            nfcTunnelManager.sendSuccess(key);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            closeConnection();
        }
    }

    @Override
    public String getConnectionName() throws IOException {
        return nfcTunnelManager.getCardName();
    }

    /**
     * Informa al cliente de un error en la validación
     * @throws IOException
     */
    public void sendError() throws IOException {
        try{
            nfcTunnelManager.sendError();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
        finally {
            closeConnection();
        }
    }

    /**
     * Informa al cliente del cierre de la conexión
     * @throws IOException
     */
    private void closeConnection() throws IOException{
        try {
            nfcTunnelManager.disconnect();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
