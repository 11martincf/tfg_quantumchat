package org.qkdlab.zksnark.model;

import java.io.Serial;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Merkle Tree
 *
 * Estructura de datos que representa un arbol Merkle.
 * Al crearse, todas las hojas se inicializan como H(0). Los commits que se insertan los van sustituyendo.
 */
public class MerkleTree implements Serializable {
    @Serial
    private static final long serialVersionUID = -2902349745366597839L;
    private int depth = Constants.MERKLE_TREE_DEPTH;
    private int currentSize = 0;
    private ArrayList<ArrayList<byte[]>> tree;
    private static MessageDigest hashAlg;

    //Lista de roots antiguos. Es necesario almacenarlos para validar commits de árboles viejos
    private ArrayList<byte[]> previousRoots;

    static {
        try {
            hashAlg = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public MerkleTree() {
        initTree();
    }

    /**
     * Crea un árbol a partir de una lista de hojas. Actualmente no se utiliza
     * @param leafList Lista de hojas
     */
    public MerkleTree(ArrayList<byte[]> leafList) {
        initTree();
        for (int i = 0; i< leafList.size(); i++) {
            tree.get(0).add(leafList.get(i));
        }
        currentSize += leafList.size();
    }

    /**
     * Inicializa los ArrayList que forman el árbol
     */
    private void initTree() {
        previousRoots = new ArrayList<>();
        tree = new ArrayList<>();
        for (int i = 0; i < depth + 1; i++) {
            tree.add(new ArrayList<>());
        }
    }

    /**
     * Añade H(0) a todas las hojas del árbol
     */
    public void populateEmpty() {
        int leafNum = (int)Math.pow(2, depth);
        leafNum -= currentSize;

        byte[] emptyNode = hashAlg.digest(Constants.EMPTY_MERKLE_LEAF);
        for (int i = 0; i<leafNum; i++) {
            tree.get(0).add(emptyNode);
        }

        populateBranches();
    }

    /**
     * Calcula todos los nodos intermedios y root a partir de las hojas
     */
    private void populateBranches() {
        for (int i = 1; i<depth + 1; i++) {
            int numOfNodes = (int)Math.pow(2, (depth - i));
            ArrayList<byte[]> currentLevel = tree.get(i);
            ArrayList<byte[]> inferiorLevel = tree.get(i - 1);

            for(int j = 0; j < numOfNodes; j++) {
                byte[] leftInput = inferiorLevel.get(2 * j);
                byte[] rightInput = inferiorLevel.get(2 * j + 1);



                currentLevel.add(calculateHash(leftInput, rightInput));
            }
        }
    }

    /**
     * Calcula el hash de la concatenación de dos elementos
     */
    private byte[] calculateHash(byte[] leftInput, byte[] rightInput) {
        byte[] preimage = new byte[leftInput.length + rightInput.length];
        System.arraycopy(leftInput, 0, preimage, 0, leftInput.length);
        System.arraycopy(rightInput, 0, preimage, leftInput.length, rightInput.length);

        return hashAlg.digest(preimage);
    }

    /**
     * Sustituye un H(0) por un commit
     */
    public void addLeaf(byte[] leaf) {
        tree.get(0).set(currentSize, leaf);
        recalculateBranches(currentSize);

        currentSize++;
    }

    /**
     * Calcula solo los nodos intermedios necesarios tras la inserción de un nodo
     */
    private void recalculateBranches(int index) {

        addPreviousRoot(tree.get(depth).get(0));

        for (int i = 0; i<depth; i++) {

            int updatedInputIndex = index / (int)Math.pow(2, i);
            byte[] leftInput;
            byte[] rightInput;

            if(updatedInputIndex % 2 == 0) {
                leftInput = tree.get(i).get(updatedInputIndex);
                int rightInputIndex = (index / (int)Math.pow(2, i)) + 1;
                rightInput = tree.get(i).get(rightInputIndex);
            }
            else {
                rightInput = tree.get(i).get(updatedInputIndex);
                int leftInputIndex = (index / (int)Math.pow(2, i)) - 1;
                leftInput = tree.get(i).get(leftInputIndex);
            }

            byte[] digest = calculateHash(leftInput, rightInput);
            int indexToUpdate = index / (int)Math.pow(2, i + 1);
            tree.get(i + 1).set(indexToUpdate, digest);
        }
    }

    public byte[] getRoot() {
        return tree.get(depth).get(0);
    }

    private void addPreviousRoot(byte[] root) {
        previousRoots.add(root);
    }

    /**
     * Comprueba si un hash corresponde al root actual o a uno anterior
     */
    public boolean isRootValid(byte[] root) {
        if (Arrays.equals(root, getRoot())) {
            return true;
        }
        else {
            for (int i = 0; i< previousRoots.size();i++) {
                if(Arrays.equals(root, previousRoots.get(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<byte[]> getMerkleRoots() {
        List<byte[]> merkleRoots = (List<byte[]>) previousRoots.clone();
        merkleRoots.add(getRoot());

        return merkleRoots;
    }

    /**
     * Encuentra el índice de un commitment
     * @param commitment
     * @return índice del commitment
     */
    public int findCommitment(byte[] commitment) {
        ArrayList<byte[]> leafLevel = tree.get(0);
        for (int i = 0; i<currentSize; i++) {
            if (Arrays.equals(commitment, leafLevel.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Devuelve el camino Merkle para un índice concreto
     * @param index índice del commitment que se quiere validar
     * @return lista de nodos intermedios para validar el commitment
     */
    public ArrayList<byte[]> getMerklePath(int index) {
        ArrayList<byte[]> merklePath = new ArrayList<>();
        for (int i = 0; i < this.depth; i++) {
            int commitmentIndex = index / (int)Math.pow(2, i);
            if(commitmentIndex % 2 == 0) {
                merklePath.add(getNodeAt(i, commitmentIndex + 1));
            }
            else {
                merklePath.add(getNodeAt(i, commitmentIndex - 1));
            }
        }

        return merklePath;
    }

    public byte[] getNodeAt(int depth, int pos) {
        return tree.get(depth).get(pos);
    }

}
