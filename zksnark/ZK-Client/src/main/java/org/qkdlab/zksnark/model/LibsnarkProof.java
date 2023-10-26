package org.qkdlab.zksnark.model;

public class LibsnarkProof {
    String encodedProof;
    String encodedRoot;
    String encodedNullifier;

    public LibsnarkProof() {
    }

    public LibsnarkProof(String encodedProof, String encodedRoot, String encodedNullifier) {
        this.encodedProof = encodedProof;
        this.encodedRoot = encodedRoot;
        this.encodedNullifier = encodedNullifier;
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
}
