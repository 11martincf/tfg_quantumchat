package org.qkdlab.zksnark.zkvalidator.dao;

import org.qkdlab.zksnark.model.ZKProof;

import java.io.IOException;
import java.util.List;

/**
 * ValidatorDAO
 *
 * Interfaz que encapsula la obtención de información del servidor
 */
public interface ValidatorDAO {

    List<byte[]> getTreeRoots() throws IOException;

    List<byte[]> getNullifiers() throws IOException;

    void writeProof(ZKProof proof, String filename) throws IOException;

    void addNullifier(byte[] nullifier) throws IOException;
}
