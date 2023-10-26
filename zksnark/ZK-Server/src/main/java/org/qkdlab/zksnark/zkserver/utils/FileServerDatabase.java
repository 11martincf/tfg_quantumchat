package org.qkdlab.zksnark.zkserver.utils;


import org.qkdlab.zksnark.model.Constants;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.NullifierList;
import org.qkdlab.zksnark.model.ZKProof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/**
 * FileServerDatabase
 *
 * Almacena las estructuras de datos del programa e interactúa con FileDAO para leer y escribir los ficheros
 */
@Service
public class FileServerDatabase implements ServerDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(FileServerDatabase.class);
    private MerkleTree merkleTree;
    private NullifierList nullifiers;
    private FileDAO fileDAO;

    private FileServerDatabase() {
        nullifiers = new NullifierList();
        fileDAO = new FileDAO(Constants.DEFAULT_SERVER_FOLDER, Constants.DEFAULT_PROOF_FOLDER);
        try {
            fileDAO.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() throws IOException {
        loadMerkleTree();
        loadNullifiers();
    }

    /**
     * Carga el árbol Merkle al comienzo de la ejecución
     * @throws IOException
     */
    private void loadMerkleTree() throws IOException {
        try {
            merkleTree = fileDAO.loadMerkleTree(Constants.DEFAULT_TREE_FILENAME);
            LOG.info("Merkle Tree loaded.");
        }
        catch (IOException e) {
            LOG.error("Could not find Merkle Tree file. Creating empty tree.");
            merkleTree = new MerkleTree();
            merkleTree.populateEmpty();
        }
    }

    /**
     * Carga los Nullifiers al comienzo de la ejecución
     * @throws IOException
     */
    private void loadNullifiers() throws IOException {
        try {
            nullifiers = fileDAO.loadNullifiers(Constants.DEFAULT_NULLIFIERS_FILENAME);
        }
        catch (IOException e) {
            LOG.error("Could not find Nullifiers. Creating empty set");
            nullifiers = new NullifierList();
        }
    }

    /**
     * Obtiene el fichero del árbol para proporcionarlo a los usuarios
     * @return Fichero del árbol Merkle
     * @throws IOException
     */
    public File getTreeFile() throws IOException {
        return fileDAO.getFile(Constants.DEFAULT_TREE_FILENAME);
    }

    /**
     * Añade nuevo commit al árbol. Después, lo almacena en un fichero
     * @param leaf Commit a añadir
     */
    public void addTreeLeaf(byte[] leaf) {
        merkleTree.addLeaf(leaf);

        try {
            updateTreeFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Guarda el árbol a fichero
     * @throws IOException
     */
    private void updateTreeFile() throws IOException {
        fileDAO.saveMerkleTree(merkleTree, Constants.DEFAULT_TREE_FILENAME);
    }

    /**
     * Comprueba si el nullifier existe. Si no, lo almacena. Después, lo almacena en un fichero
     * @param nullifier
     * @return true si todo correcto, false si ya existía el nullifier
     */
    public boolean addNullifier(byte[] nullifier) {
        if(nullifiers.checkIfNullifierExists(nullifier)) {
            return false;
        }

        nullifiers.addNullifier(nullifier);
        try {
            updateNullifiersFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private void updateNullifiersFile() throws IOException {
        fileDAO.saveNullifiers(nullifiers, Constants.DEFAULT_NULLIFIERS_FILENAME);
    }

    public boolean isTreeRootValid(byte[] root) {
        return merkleTree.isRootValid(root);
    }

    public String saveProof(ZKProof proof, String name) throws IOException {
        String filename = name + ".json";
        fileDAO.saveProof(proof, filename);
        return filename;
    }

    @Override
    public String saveRawProof(byte[] proof, String name) throws IOException {
        String filename = name + ".raw";
        fileDAO.saveRawProof(proof, filename);
        return filename;
    }

    public MerkleTree getMerkleTree() {
        return merkleTree;
    }

    public NullifierList getNullifiers() {
        return nullifiers;
    }

    @Override
    public List<byte[]> getMerkleRoots() {
        return merkleTree.getMerkleRoots();
    }

    public String getFolderName() {
        return fileDAO.getFolderName();
    }
}
