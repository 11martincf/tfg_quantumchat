package org.qkdlab.zksnark.model.message;

import java.io.Serializable;

/**
 * CommitMessage
 *
 * Encapsula el mensaje con el certificado, la firma y el commit
 */
public class CommitMessage implements Serializable {
    private String encodedCommit;
    private String encodedCert;
    private String encodedSign;

    public CommitMessage(String encodedCommit, String encodedCert, String encodedSign) {
        this.encodedCommit = encodedCommit;
        this.encodedCert = encodedCert;
        this.encodedSign = encodedSign;
    }

    public String getEncodedCommit() {
        return encodedCommit;
    }

    public void setEncodedCommit(String encodedCommit) {
        this.encodedCommit = encodedCommit;
    }

    public String getEncodedCert() {
        return encodedCert;
    }

    public void setEncodedCert(String encodedCert) {
        this.encodedCert = encodedCert;
    }

    public String getEncodedSign() {
        return encodedSign;
    }

    public void setEncodedSign(String encodedSign) {
        this.encodedSign = encodedSign;
    }
}
