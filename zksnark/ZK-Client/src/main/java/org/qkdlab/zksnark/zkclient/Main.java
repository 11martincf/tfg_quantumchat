package org.qkdlab.zksnark.zkclient;

import org.qkdlab.zksnark.model.Constants;
import org.qkdlab.zksnark.model.LibsnarkProof;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.ZKProof;
import org.qkdlab.zksnark.model.message.ProofMessage;
import org.qkdlab.zksnark.zkclient.io.CommandHandler;
import org.qkdlab.zksnark.zkclient.proof.CommitNoteKEM;
import org.qkdlab.zksnark.zkclient.proof.ProofClient;

import java.io.IOException;
import java.util.Base64;

public class Main {
    public static void main(String[] args) {
        ProofClient proofClient = new ProofClient("http://localhost:8080", Constants.DEFAULT_CLIENT_FOLDER);
        proofClient.init();


        int testSize = 1000;
        try {
            proofClient.doAuthenticate("alice.p12");
            proofClient.downloadMerkleTree();

            MerkleTree merkleTree = proofClient.loadMerkleTree();

            CommitNoteKEM commitNote = proofClient.loadCommitNote();
            ProofMessage proofMessage = proofClient.outsource(commitNote);
            ZKProof proof = proofMessage.getZkProof();

            //proofClient.generateZokratesProof(merkleTree, commitNote);
            //LibsnarkProof proof = proofClient.generateLibsnarkProof(merkleTree, commitNote);


            //ZKProof proof = proofClient.loadProof();
            //byte[] qrngKey = proofClient.sendLibsnarkProof(proof, commitNote, 32);
            byte[] qrngKey = proofClient.sendZokratesProof(proof, commitNote, 32);

            /*double witnessTimeMs = (CommandHandler.witnessTime / Math.pow(10, 6)) / testSize;
            double proofTimeMs = (CommandHandler.proofTime / Math.pow(10, 6)) / testSize;

            System.out.println(Constants.MERKLE_TREE_DEPTH + "\t\t\t" + witnessTimeMs + "\t\t\t" + proofTimeMs);*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}