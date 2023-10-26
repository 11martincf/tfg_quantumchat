package org.qkdlab.zksnark.zkserver.controller;

import org.qkdlab.zksnark.model.Constants;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.message.CommitNoteMessage;
import org.qkdlab.zksnark.model.message.RawProofMessage;
import org.qkdlab.zksnark.zkclient.proof.CommitNoteKEM;
import org.qkdlab.zksnark.zkclient.proof.ProofClient;
import org.qkdlab.zksnark.zkserver.exception.InvalidProofException;
import org.qkdlab.zksnark.model.NullifierList;
import org.qkdlab.zksnark.model.ZKProof;
import org.qkdlab.zksnark.model.message.CommitMessage;
import org.qkdlab.zksnark.model.message.ProofMessage;
import org.qkdlab.zksnark.zkserver.utils.FileServerDatabase;
import org.qkdlab.zksnark.zkserver.service.SnarkService;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MerkleTreeController
 *
 * Endpoints para el acciones relacionadas con las pruebas zk-SNARK
 * Contiene operaciones del Paso de Autenticación y de Validación de prueba
 * Nota: esta clase realiza validación de zk-SNARKs pero se recomienda utilizar el proyecto ZK-Validator para estas acciones
 */
@Controller
public class SnarkController {
    private static final Logger LOG = LoggerFactory.getLogger(SnarkController.class);

    @Autowired
    private SnarkService snarkService;
    @Autowired
    private FileServerDatabase fileServerDatabase;
    long keyGenTime = 0;
    int testSize = 1000;
    int numRequest = 0;

    /**
     * Recibe una solicitud de autenticación de un usuario, y se la redirecciona al SnarkService
     * @param msg Mensaje que incluye el certificado, el commitment y la firma
     * @return Resultado de la operación
     */
    @PostMapping(path = "/commit", produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> commit(@RequestBody CommitMessage msg) {

        byte[] commitment = Base64.getDecoder().decode(msg.getEncodedCommit());
        byte[] encodedCert = Base64.getDecoder().decode(msg.getEncodedCert());
        byte[] signature = Base64.getDecoder().decode(msg.getEncodedSign());

        boolean result = snarkService.authenticateUser(commitment, encodedCert, signature);
        Map response = new HashMap<>();
        response.put("response", Boolean.toString(result));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Solicita la lista de nullifiers
     * @return Lista de nullifiers
     */
    @GetMapping(path = "/nullifiers")
    public ResponseEntity<List<byte[]>> getNullifiers() {
        NullifierList nullifiers = fileServerDatabase.getNullifiers();
        Gson gson = new Gson();
        String encodedNullifiers = gson.toJson(nullifiers);

        return new ResponseEntity<>(nullifiers.getNullifiers(), HttpStatus.OK);
    }

    /**
     * Envía una prueba para ser validada
     * @param proofMessage Mensaje que contiene la prueba y el tamaño de clave solicitada
     * @return Si la prueba es correcta, clave QRNG. Si no, error
     */
    @PostMapping(path = "/proof", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendProof(@RequestBody ProofMessage proofMessage) {

        ZKProof proof = proofMessage.getZkProof();
        String response;
        try {
            // long time = System.nanoTime();
            snarkService.validateProof(proof);
            // keyGenTime += System.nanoTime() - time;
            byte[] publicKey = Base64.getDecoder().decode(proofMessage.getEncodedPublicKey());
            byte[] sealedKey = snarkService.generateRandomKey(proofMessage.getKeySize(), publicKey);
            response = Base64.getEncoder().encodeToString(sealedKey);
        } catch (InvalidProofException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }

        /*numRequest++;
        if(numRequest % testSize == 0) {
            double genTime = (keyGenTime / (Math.pow(10, 6)) / testSize);
            System.out.println(genTime);
            keyGenTime = 0;
        }*/

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/outsource", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProofMessage> outsource(@RequestBody CommitNoteMessage commitNoteMessage) {

        byte[] publicKey = Base64.getDecoder().decode(commitNoteMessage.getEncodedPublicKey());
        byte[] privateKey = Base64.getDecoder().decode(commitNoteMessage.getEncodedPrivateKey());
        byte[] sigma = Base64.getDecoder().decode(commitNoteMessage.getEncodedSigma());

        CommitNoteKEM commitNote = new CommitNoteKEM(sigma, privateKey, publicKey);
        ProofClient proofClient = new ProofClient("http://localhost:8080", Constants.DEFAULT_SERVER_FOLDER);
        proofClient.init();
        MerkleTree merkleTree = fileServerDatabase.getMerkleTree();
        ProofMessage response = null;
        try {
            proofClient.generateZokratesProof(merkleTree, commitNote);
            //LibsnarkProof proof = proofClient.generateLibsnarkProof(merkleTree, commitNote);
            ZKProof proof = proofClient.loadProof();
            String encodedPublicKey = Base64.getEncoder().encodeToString(commitNote.getEncodedPublicKey());
            response = new ProofMessage(proof, encodedPublicKey, 0);
        } catch (IOException e) {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/rawProof", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendRawProof(@RequestBody RawProofMessage rawProofMessage) {

        String encodedProof = rawProofMessage.getEncodedProof();
        String encodedRoot = rawProofMessage.getEncodedRoot();
        String encodedNullifier = rawProofMessage.getEncodedNullifier();
        String response;
        try {
            // long time = System.nanoTime();
            snarkService.validateRawProof(encodedProof, encodedRoot, encodedNullifier);
            // keyGenTime += System.nanoTime() - time;
            byte[] publicKey = Base64.getDecoder().decode(rawProofMessage.getEncodedPublicKey());
            byte[] sealedKey = snarkService.generateRandomKey(rawProofMessage.getKeySize(), publicKey);
            response = Base64.getEncoder().encodeToString(sealedKey);
        } catch (InvalidProofException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }

        /*numRequest++;
        if(numRequest % testSize == 0) {
            double genTime = (keyGenTime / (Math.pow(10, 6)) / testSize);
            System.out.println(genTime);
            keyGenTime = 0;
        }*/

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
