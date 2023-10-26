package org.qkdlab.zksnark.zkvalidator.validator;

import org.qkdlab.zksnark.model.message.ProofMessage;

import java.io.IOException;

/**
 * IOProofConnection
 *
 * Interfaz que encapsula la conexión con un dispositivo, y permite enviar y recibir información
 */
public interface IOProofConnection {
    ProofMessage receiveData() throws IOException;
    void sendError() throws IOException;
    void sendSuccess(byte[] key) throws IOException;

    String getConnectionName() throws IOException;
}
