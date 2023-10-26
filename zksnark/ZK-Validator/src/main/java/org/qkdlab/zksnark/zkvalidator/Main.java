package org.qkdlab.zksnark.zkvalidator;

import org.qkdlab.zksnark.zkvalidator.validator.ProofValidator;

public class Main {
    public static void main(String[] args) {
        ProofValidator proofValidator = new ProofValidator();

        proofValidator.run();
    }
}