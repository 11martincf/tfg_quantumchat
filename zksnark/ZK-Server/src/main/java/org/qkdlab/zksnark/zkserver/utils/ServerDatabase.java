package org.qkdlab.zksnark.zkserver.utils;

import org.qkdlab.zksnark.model.NullifierList;
import org.qkdlab.zksnark.model.ZKProof;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ServerDatabase {
    void init() throws IOException;
    File getTreeFile() throws IOException;

    void addTreeLeaf(byte[] leaf) throws IOException;

    boolean isTreeRootValid(byte[] root) throws IOException;

    String saveProof(ZKProof proof, String name) throws IOException;
    String saveRawProof(byte[] proof, String name) throws IOException;

    boolean addNullifier(byte[] nullifier) throws IOException;

    NullifierList getNullifiers() throws IOException;

    List<byte[]> getMerkleRoots() throws IOException;

}
