package org.qkdlab.zksnark.zkclient.proof;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * CommitNote
 *
 * Encapsula la información de (pk, sk) y sigma para serialización
 */
public class CommitNoteKEM extends CommitNote {
    private transient KeyPair keyPair;

    public CommitNoteKEM() {
    }

    public CommitNoteKEM(byte[] sigma) {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        this.sigma = sigma;
        this.publicKey = keyPair.getPublic().getEncoded();
        this.privateKey = keyPair.getPrivate().getEncoded();
    }

    public CommitNoteKEM(byte[] sigma, byte[] privateKey, byte[] publicKey) {
        this.sigma = sigma;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public byte[] getPublicKey() {
        return HashUtil.hash(this.publicKey);
    }

    @Override
    public byte[] getPrivateKey() {
        return HashUtil.hash(this.privateKey);
    }

    public byte[] getEncodedPublicKey() {
        return this.publicKey;
    }

    public byte[] getEncodedPrivateKey() {
        return this.privateKey;
    }
}