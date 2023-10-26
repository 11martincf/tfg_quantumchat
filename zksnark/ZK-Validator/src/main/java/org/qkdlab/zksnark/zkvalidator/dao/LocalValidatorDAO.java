package org.qkdlab.zksnark.zkvalidator.dao;

import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.NullifierList;
import org.qkdlab.zksnark.model.ZKProof;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * LocalValidatorDAO
 *
 * Obtiene la información del servidor a través de los ficheros que almacena
 * Contiene todos los FileAccessor e información sobre la carpeta
 */
public class LocalValidatorDAO implements ValidatorDAO {

    private String folder;
    private String proofsFolder;
    private final FileAccessor<MerkleTree> merkleTreeAccessor;
    private final FileAccessor<NullifierList> nullifiersAccessor;
    private final FileAccessor<ZKProof> zkProofAccessor;

    public LocalValidatorDAO(String folder, String proofsFolder, String treeFile, String nullifierFile) {
        this.folder = folder;
        this.proofsFolder = proofsFolder;
        merkleTreeAccessor = new FileAccessor<>(buildFilePath(treeFile), MerkleTree.class);
        nullifiersAccessor = new FileAccessor<>(buildFilePath(nullifierFile), NullifierList.class);
        zkProofAccessor = new FileAccessor<>(folder + File.separator + proofsFolder, ZKProof.class);
    }

    @Override
    public List<byte[]> getTreeRoots() throws IOException {
        MerkleTree merkleTree = merkleTreeAccessor.readRawFile();
        return merkleTree.getMerkleRoots();
    }

    @Override
    public List<byte[]> getNullifiers() throws IOException {
        NullifierList nullifierList = nullifiersAccessor.readFile();
        return nullifierList.getNullifiers();
    }

    @Override
    public void writeProof(ZKProof proof, String filename) throws IOException {
        zkProofAccessor.saveFile(proof, buildProofFilePath(filename));
    }

    @Override
    public void addNullifier(byte[] nullifier) throws IOException {
        NullifierList nullifierList = nullifiersAccessor.readFile();
        nullifierList.addNullifier(nullifier);
        nullifiersAccessor.saveFile(nullifierList);
    }

    private String buildFilePath(String filename) {
        return folder + File.separator + filename;
    }

    private String buildProofFilePath(String filename) {
        return folder + File.separator + proofsFolder + File.separator + filename + ".json";
    }
}
