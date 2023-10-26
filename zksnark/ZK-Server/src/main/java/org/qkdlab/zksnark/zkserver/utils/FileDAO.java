package org.qkdlab.zksnark.zkserver.utils;

import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.NullifierList;
import org.qkdlab.zksnark.model.ZKProof;

import java.io.*;

/**
 * FileDAO
 *
 * Clase que traduce necesidades de información del programa a accesos a ficheros
 * Contiene todos los FileAccessor e información sobre la carpeta
 */
public class FileDAO {
    private String folder;
    private String proofsFolder;
    private final FileAccessor<MerkleTree> merkleTreeAccessor;
    private final FileAccessor<NullifierList> nullifiersAccessor;
    private final FileAccessor<ZKProof> zkProofAccessor;

    public FileDAO(String folder, String proofsFolder) {
        this.folder = folder;
        this.proofsFolder = proofsFolder;

        merkleTreeAccessor = new FileAccessor<>(folder, MerkleTree.class);
        nullifiersAccessor = new FileAccessor<>(folder, NullifierList.class);
        zkProofAccessor = new FileAccessor<>(folder + File.separator + proofsFolder, ZKProof.class);

    }

    /**
     * Inicializar la estructura de carpetas
     * @throws IOException
     */
    public void init() throws IOException {
        File folderFile = new File(folder);
        if(!folderFile.exists()) {
            folderFile.mkdir();
        }
        File proofsFolderFile = new File(folder + File.separator + proofsFolder);
        if(!proofsFolderFile.exists()) {
            proofsFolderFile.mkdir();
        }
    }

    public MerkleTree loadMerkleTree(String filename) throws IOException {
        return merkleTreeAccessor.readRawFile(filename);

    }

    public void saveMerkleTree(MerkleTree merkleTree, String filename) throws IOException {
        merkleTreeAccessor.saveRawFile(merkleTree, filename);
    }

    public NullifierList loadNullifiers(String filename) throws IOException {
        return nullifiersAccessor.readFile(filename);
    }

    public void saveNullifiers(NullifierList nullifiers, String filename) throws IOException {
        nullifiersAccessor.saveFile(nullifiers, filename);
    }

    public File getFile(String filename) {
        return new File(merkleTreeAccessor.getAbsolutePath(filename));
    }

    public void saveProof(ZKProof proof, String filename) throws IOException {
        zkProofAccessor.saveFile(proof, filename);
    }

    public void saveRawProof(byte[] proof, String filename) throws IOException{
        File outputFile = new File(folder + File.separator + proofsFolder + File.separator + filename);
        outputFile.createNewFile();
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(proof);
        }
    }

    public String getFolderName() {
        return folder;
    }

}
