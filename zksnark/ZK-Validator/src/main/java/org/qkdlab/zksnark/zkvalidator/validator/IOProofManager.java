package org.qkdlab.zksnark.zkvalidator.validator;

import org.qkdlab.zksnark.model.ZKProof;
import org.qkdlab.zksnark.model.message.ProofMessage;

import java.io.IOException;

/**
 * IOProofManager
 *
 * Interfaz que encapsula la interacción entre el validador y un cliente, por el medio que sea
 */
public interface IOProofManager {
    void init() throws IOException;
    String getIoManagerName();
    IOProofConnection listen() throws IOException;
}
