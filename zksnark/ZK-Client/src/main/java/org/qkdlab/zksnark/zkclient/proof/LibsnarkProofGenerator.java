package org.qkdlab.zksnark.zkclient.proof;

import org.apache.commons.codec.binary.Hex;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.zkclient.io.CommandHandler;

import java.io.IOException;
import java.util.ArrayList;

public class LibsnarkProofGenerator extends ProofGenerator {

    public LibsnarkProofGenerator(CommitNoteKEM commitNote, MerkleTree merkleTree) {
        super(commitNote, merkleTree);
    }

    public void generateProof(String folder) throws IOException {

        String encodedRoot = Hex.encodeHexString(merkleRoot);
        String encodedNullifier = Hex.encodeHexString(nullifier);
        String encodedCommitment = Hex.encodeHexString(commitment);
        String encodedPubKey = Hex.encodeHexString(commitNote.getPublicKey());
        String encodedPrivKey = Hex.encodeHexString(commitNote.getPrivateKey());
        String encodedSigma = Hex.encodeHexString(commitNote.getSigma());

        ArrayList<String> path = new ArrayList<>();
        for (byte[] pathHash : merklePath) {
            path.add(Hex.encodeHexString(pathHash));
        }

        CommandHandler.executeLibsnark(folder, encodedRoot, encodedNullifier, encodedCommitment, commitmentIndex,
                path, encodedPubKey, encodedPrivKey, encodedSigma);
    }
}
