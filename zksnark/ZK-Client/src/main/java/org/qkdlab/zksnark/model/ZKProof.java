package org.qkdlab.zksnark.model;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * ZKProof
 *
 * Encapsula una prueba zk-SNARK tal como es generada por ZoKrates, para facilitar la serialización
 */
public class ZKProof implements Serializable {
    @Serial
    private static final long serialVersionUID = -1611688926280540674L;

    class ProofParameters implements Serializable  {
        @Serial
        private static final long serialVersionUID = 426976676684591252L;
        private List<String> a;
        private List<List<String>> b;
        private List<String> c;

        public ProofParameters() {
        }

        public List<String> getA() {
            return a;
        }

        public void setA(List<String> a) {
            this.a = a;
        }

        public List<List<String>> getB() {
            return b;
        }

        public void setB(List<List<String>> b) {
            this.b = b;
        }

        public List<String> getC() {
            return c;
        }

        public void setC(List<String> c) {
            this.c = c;
        }
    }

    private String scheme;
    private String curve;
    private ProofParameters proof;
    private List<String> inputs;

    public ZKProof() {
    }

    public byte[] getMerkleRoot() {
        byte[][] bytes = new byte[8][4];

        try {
            for (int i = 0; i<8; i++) {
                byte[] decoded = Hex.decodeHex(inputs.get(i).replace("0x", ""));
                bytes[i] = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
            }
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }

        return concatBytes(bytes);
    }

    public byte[] getNullifier() {
        byte[][] bytes = new byte[8][4];

        try {
            for (int i = 0; i<8; i++) {
                byte[] decoded = Hex.decodeHex(inputs.get(i + 8).replace("0x", ""));
                bytes[i] = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
            }
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }

        return concatBytes(bytes);
    }

    private static byte[] concatBytes(byte[][] bytes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (byte[] b : bytes) {
                outputStream.write(b);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getCurve() {
        return curve;
    }

    public void setCurve(String curve) {
        this.curve = curve;
    }

    public ProofParameters getProof() {
        return proof;
    }

    public void setProof(ProofParameters proof) {
        this.proof = proof;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Scheme: ").append(scheme).append("\n");
        stringBuilder.append("Curve: ").append(curve).append("\n");
        stringBuilder.append("Nullifier: ").append(Hex.encodeHexString(getNullifier())).append("\n");
        stringBuilder.append("MerkleRoot: ").append(Hex.encodeHexString(getMerkleRoot())).append("\n");

        stringBuilder.append("---- PROOF ----\n");
        stringBuilder.append("A: {\n");
        for (String paramA : proof.a) {
            stringBuilder.append(paramA + ", ");
        }
        stringBuilder.append("\n}\n");

        stringBuilder.append("B: {");
        for (List<String> listB : proof.b) {
            stringBuilder.append("\n{");
            for (String paramB : listB) {
                stringBuilder.append(paramB + ", ");
            }
            stringBuilder.append("\n}");
        }
        stringBuilder.append("\n}\n");

        stringBuilder.append("C: {");
        for (String paramC : proof.c) {
            stringBuilder.append(paramC + ", ");
        }
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }
}
