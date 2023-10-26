package org.qkdlab.zksnark.zkclient.proof;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * CommitNote
 *
 * Encapsula la información de (pk, sk) y sigma para serialización
 */
public class CommitNote {
    protected byte[] privateKey;
    protected byte[] publicKey;
    protected byte[] sigma;

    public CommitNote() {
    }

    public CommitNote(byte[] sigma, byte[] privateKey) {
        this.sigma = sigma;
        this.privateKey = privateKey;
        calculatePublicKey();
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
        calculatePublicKey();
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getSigma() {
        return sigma;
    }

    public void setSigma(byte[] sigma) {
        this.sigma = sigma;
    }

    private void calculatePublicKey() {
        try {
            publicKey = MessageDigest.getInstance("SHA-256").digest(privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
