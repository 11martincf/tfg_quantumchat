package org.qkdlab.zksnark.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NullifierList
 *
 * Lista de nullifiers, encapsulada para serialización
 */
public class NullifierList {
    private ArrayList<byte[]> nullifiers;

    public NullifierList() {
        nullifiers = new ArrayList<>();
    }

    public void addNullifier(byte[] nullif) {
        nullifiers.add(nullif);
    }

    public boolean checkIfNullifierExists(byte[] nullifier) {
        for (byte[] nullif : nullifiers) {
            if(Arrays.equals(nullif, nullifier)) {
                return true;
            }
        }
        return false;
    }


    public List<byte[]> getNullifiers() {
        return nullifiers;
    }
}
