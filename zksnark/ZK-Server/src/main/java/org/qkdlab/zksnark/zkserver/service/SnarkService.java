package org.qkdlab.zksnark.zkserver.service;

import org.qkdlab.zksnark.zkserver.exception.InvalidProofException;
import org.qkdlab.zksnark.zkserver.utils.crypto.*;
import org.qkdlab.zksnark.zkserver.utils.FileServerDatabase;
import org.qkdlab.zksnark.model.ZKProof;
import org.qkdlab.zksnark.zkserver.utils.CommandHandler;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Service
public class SnarkService {

    @Autowired
    private FileServerDatabase fileServerDatabase;
    private CertificateHandler certificateHandler;
    private KeyGenerator keyGenerator;
    public SnarkService() {
        certificateHandler = new CertificateHandler();
        //keyGenerator = new RemoteKeyGenerator();
        keyGenerator = new DummyKeyGenerator();
    }

    public boolean authenticateUser(byte[] commitment, byte[] encodedCert, byte[] signature) {

        X509Certificate cert = certificateHandler.decodeCertificate(encodedCert);
        boolean isCertValid = certificateHandler.validateCertificate(cert);
        if(!isCertValid) return false;

        boolean isSigValid = certificateHandler.verifySignature(cert, signature, commitment);
        if(!isSigValid) return false;

        fileServerDatabase.addTreeLeaf(commitment);

        return true;
    }

    public boolean validateProof(ZKProof proof) throws InvalidProofException {
        byte[] merkleRoot = proof.getMerkleRoot();
        byte[] nullifier = proof.getNullifier();

        boolean result = false;
        try {
            checkRootAndNullifier(merkleRoot, nullifier);
            String name = Hex.encodeHexString(nullifier);
            String filename = fileServerDatabase.saveProof(proof, name);
            result = CommandHandler.executeZokrates(fileServerDatabase.getFolderName(), filename);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public byte[] generateRandomKey(int keySize, byte[] publicKeyBytes) throws IOException {
        byte[] qrngKey = keyGenerator.getRandomBytes(keySize);

        PublicKey publicKey;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encryptedKey;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedKey = cipher.doFinal(qrngKey);

            System.out.println("GENERATED: " + Base64.getEncoder().encodeToString(qrngKey));
            System.out.println("SEALED: " + Base64.getEncoder().encodeToString(encryptedKey));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return encryptedKey;
    }

    public void validateRawProof(String encodedProof, String encodedRoot, String encodedNullifier) throws InvalidProofException {
        byte[] proof = Base64.getDecoder().decode(encodedProof);
        byte[] merkleRoot = Base64.getDecoder().decode(encodedRoot);
        byte[] nullifier = Base64.getDecoder().decode(encodedNullifier);


        try {
            checkRootAndNullifier(merkleRoot, nullifier);
            String name = Hex.encodeHexString(nullifier);
            String filename = fileServerDatabase.saveRawProof(proof, name);

            String hexRoot = Hex.encodeHexString(merkleRoot);
            String hexNullifier = Hex.encodeHexString(nullifier);

            boolean result = CommandHandler.executeLibsnark(fileServerDatabase.getFolderName(), filename, hexRoot, hexNullifier);
            if(!result) throw new InvalidProofException("Proof rejected");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkRootAndNullifier(byte[] merkleRoot, byte[] nullifier) throws InvalidProofException{
        boolean isRootValid = fileServerDatabase.isTreeRootValid(merkleRoot);
        if(!isRootValid) throw new InvalidProofException("Unrecognized Merkle Root");

        boolean isNullifierValid = fileServerDatabase.addNullifier(nullifier);
        //if(!isNullifierValid) throw new InvalidProofException("Nullifier already exists");

    }

}
