package org.qkdlab.zksnark.model.message;

import org.qkdlab.zksnark.model.LibsnarkProof;

public class RawProofMessage {

    String encodedProof;
    String encodedRoot;
    String encodedNullifier;
    String encodedPublicKey;
    int keySize;

    public RawProofMessage() {
    }

    public RawProofMessage(String encodedProof, String encodedRoot, String encodedNullifier, String encodedPublicKey, int keySize) {
        this.encodedProof = encodedProof;
        this.encodedRoot = encodedRoot;
        this.encodedNullifier = encodedNullifier;
        this.encodedPublicKey = encodedPublicKey;
        this.keySize = keySize;
    }

    public RawProofMessage(LibsnarkProof libsnarkProof, String encodedPublicKey, int keySize) {
        this.encodedProof = libsnarkProof.getEncodedProof();
        this.encodedRoot = libsnarkProof.getEncodedRoot();
        this.encodedNullifier = libsnarkProof.getEncodedNullifier();
        this.encodedPublicKey = encodedPublicKey;
        this.keySize = keySize;
    }

    public String getEncodedProof() {
        return encodedProof;
    }

    public void setEncodedProof(String encodedProof) {
        this.encodedProof = encodedProof;
    }

    public String getEncodedRoot() {
        return encodedRoot;
    }

    public void setEncodedRoot(String encodedRoot) {
        this.encodedRoot = encodedRoot;
    }

    public String getEncodedNullifier() {
        return encodedNullifier;
    }

    public void setEncodedNullifier(String encodedNullifier) {
        this.encodedNullifier = encodedNullifier;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getEncodedPublicKey() {
        return encodedPublicKey;
    }

    public void setEncodedPublicKey(String encodedPublicKey) {
        this.encodedPublicKey = encodedPublicKey;
    }
}
