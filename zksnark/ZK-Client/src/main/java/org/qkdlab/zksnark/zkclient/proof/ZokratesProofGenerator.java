package org.qkdlab.zksnark.zkclient.proof;

import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.zkclient.io.CommandHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class ZokratesProofGenerator extends ProofGenerator{

    public ZokratesProofGenerator(CommitNoteKEM commitNote, MerkleTree merkleTree) {
        super(commitNote, merkleTree);
    }

    public void generateProof(String folder) throws IOException {
        //printHex();

        long[] formattedRoot = formatHash(merkleRoot);
        long[] formattedNullifier = formatHash(nullifier);
        long[] formattedCommit = formatHash(commitment);
        long[] formattedPubKey = formatHash(commitNote.getPublicKey());
        long[] formattedPrivKey = formatHash(commitNote.getPrivateKey());
        long[] formattedSigma = formatHash(commitNote.getSigma());

        ArrayList<long[]> formattedPath = new ArrayList<>();
        for (byte[] pathHash : merklePath) {
            formattedPath.add(formatHash(pathHash));
        }

        CommandHandler.executeZokrates(folder, formattedRoot, formattedNullifier, formattedCommit, commitmentIndex, formattedPath,
                formattedPubKey, formattedPrivKey, formattedSigma);
    }

    /**
     * Función auxiliar para convertir un hash (por ejemplo, 32 bytes) en "longs" de 4 bytes
     * @param hash
     * @return lista de longs
     */
    private long[] formatHash(byte[] hash) {
        long[] result = new long[hash.length / 4];

        for (int i = 0; i<result.length; i++) {
            byte[] subarray = Arrays.copyOfRange(hash, 4*i, (4*i)+4);
            result[i] = ByteBuffer.wrap(subarray).getInt() & 0xFFFFFFFFL;
        }

        return result;
    }
}
