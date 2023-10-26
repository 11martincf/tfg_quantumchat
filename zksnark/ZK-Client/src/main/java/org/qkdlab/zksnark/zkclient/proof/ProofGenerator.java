package org.qkdlab.zksnark.zkclient.proof;

import org.apache.commons.codec.binary.Hex;
import org.qkdlab.zksnark.model.MerkleTree;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ProofGenerator
 *
 * Se encarga de preparar los inputs y generar el zk-SNARK
 */
public abstract class ProofGenerator {
    protected CommitNoteKEM commitNote;
    protected byte[] commitment;
    protected byte[] nullifier;
    protected int commitmentIndex;
    protected MerkleTree merkleTree;
    protected ArrayList<byte[]> merklePath;
    protected byte[] merkleRoot;

    public ProofGenerator() {
    }

    public ProofGenerator(CommitNoteKEM commitNote, MerkleTree merkleTree) {
        this.commitNote = commitNote;

        this.commitment = HashUtil.generateCommitment(commitNote);
        this.nullifier = HashUtil.generateNullifier(commitNote);
        this.merkleTree = merkleTree;

        generateMerklePath();
    }


    public void generateMerklePath() throws IndexOutOfBoundsException {
        this.commitmentIndex = merkleTree.findCommitment(commitment);

        if (commitmentIndex == -1) {
            throw new IndexOutOfBoundsException("Commitment not found in Merkle Tree");
        }
        this.merklePath = merkleTree.getMerklePath(commitmentIndex);
        this.merkleRoot = merkleTree.getRoot();
    }

    /**
     * Modifica el formato de los inputs para ser leídos por ZoKrates, y ejecuta el programa
     * @param folder carpeta con datos del programa
     * @throws IOException
     */
    public abstract void generateProof(String folder) throws IOException;

    protected void printHex() {
        StringBuilder builder = new StringBuilder();

        builder.append(Hex.encodeHexString(merkleRoot) + " ");
        builder.append(Hex.encodeHexString(nullifier) + " ");
        builder.append(Hex.encodeHexString(commitment) + " ");
        builder.append(Hex.encodeHexString(commitNote.getPublicKey()) + " ");
        builder.append(Hex.encodeHexString(commitNote.getPrivateKey()) + " ");
        builder.append(Hex.encodeHexString(commitNote.getSigma()) + " ");
        builder.append(commitmentIndex + " ");
        for (byte[] pathHash : merklePath) {
            builder.append(Hex.encodeHexString(pathHash) + " ");
        }

        System.out.println(builder.toString());
    }

    public byte[] getNullifier() {
        return nullifier;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }
}
