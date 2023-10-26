package org.qkdlab.zksnark.zkclient.proof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;

/**
 * HashUtil
 *
 * Clase estática para la generación de hashes
 */
public class HashUtil {

    private static final MessageDigest hashAlg;
    static {
        try {
            hashAlg = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Genera commitment a partir de una commitnote
     * @param commitNote objeto con (pk,sk) y sigma
     * @return commitment
     */
    public static byte[] generateCommitment(CommitNoteKEM commitNote) {
        byte[] preimage = concatBytes(commitNote.getPublicKey(), commitNote.getSigma());
        return hashAlg.digest(preimage);
    }

    /**
     * Genera nullifier a partir de una commitnote
     * @param commitNote objeto con (pk,sk) y sigma
     * @return nullifier
     */
    public static byte[] generateNullifier(CommitNoteKEM commitNote) {
        byte[] preimage = concatBytes(commitNote.getPrivateKey(), commitNote.getSigma());
        return hashAlg.digest(preimage);
    }

    /**
     * Función auxiliar para concatenar bytearrays
     * @param a
     * @param b
     * @return a || b
     */
    private static byte[] concatBytes(byte[] a, byte[] b) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(a);
            outputStream.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    /**
     * Firmar el commitment con la clave privada del usuario
     * @param commitment
     * @param privateKey clave privada
     * @return firma del commitment
     */
    public static byte[] signCommitment(byte[] commitment, PrivateKey privateKey) {
        byte[] result;
        Signature sig;
        try {
            sig = Signature.getInstance("NONEwithRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            sig.initSign(privateKey);
            sig.update(commitment);
            result = sig.sign();
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static byte[] hash(byte[] data) {
        return hashAlg.digest(data);
    }
}
