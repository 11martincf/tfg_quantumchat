package org.qkdlab.zksnark.model.message;

import org.qkdlab.zksnark.zkclient.proof.CommitNoteKEM;

import java.io.Serial;
import java.io.Serializable;
import java.util.Base64;

public class CommitNoteMessage implements Serializable {
        @Serial
        private static final long serialVersionUID = 1797192995674551053L;
        private String encodedPublicKey;
        private String encodedPrivateKey;
        private String encodedSigma;

        public CommitNoteMessage(CommitNoteKEM commitNote) {
                this.encodedPrivateKey = Base64.getEncoder().encodeToString(commitNote.getEncodedPrivateKey());
                this.encodedPublicKey = Base64.getEncoder().encodeToString(commitNote.getEncodedPublicKey());
                this.encodedSigma = Base64.getEncoder().encodeToString(commitNote.getSigma());
        }
        public CommitNoteMessage() {
        }

        public String getEncodedPublicKey() {
                return encodedPublicKey;
        }

        public void setEncodedPublicKey(String encodedPublicKey) {
                this.encodedPublicKey = encodedPublicKey;
        }

        public String getEncodedPrivateKey() {
                return encodedPrivateKey;
        }

        public void setEncodedPrivateKey(String encodedPrivateKey) {
                this.encodedPrivateKey = encodedPrivateKey;
        }

        public String getEncodedSigma() {
                return encodedSigma;
        }

        public void setEncodedSigma(String encodedSigma) {
                this.encodedSigma = encodedSigma;
        }
}
