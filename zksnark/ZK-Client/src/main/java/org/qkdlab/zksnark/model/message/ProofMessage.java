package org.qkdlab.zksnark.model.message;


import org.qkdlab.zksnark.model.ZKProof;

import java.io.Serial;
import java.io.Serializable;

/**
 * ProofMessage
 *
 * Encapsula el zk-SNARK junto al tamaño de clave solicitado
 */
public class ProofMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -3257192997374560953L;
    private ZKProof zkProof;
    private String encodedPublicKey;
    private int keySize;
    public ProofMessage() {
    }

    public ProofMessage(ZKProof zkProof, String encodedPublicKey, int keySize) {
        this.zkProof = zkProof;
        this.encodedPublicKey = encodedPublicKey;
        this.keySize = keySize;
    }

    public ZKProof getZkProof() {
        return zkProof;
    }

    public void setZkProof(ZKProof zkProof) {
        this.zkProof = zkProof;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getEncodedPublicKey() {
        return encodedPublicKey;
    }

    public void setEncodedPublicKey(String encodedPublicKey) {
        this.encodedPublicKey = encodedPublicKey;
    }
}
