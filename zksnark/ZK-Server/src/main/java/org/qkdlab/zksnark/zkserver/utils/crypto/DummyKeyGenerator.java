package org.qkdlab.zksnark.zkserver.utils.crypto;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DummyKeyGenerator implements KeyGenerator {
    @Override
    public byte[] getRandomBytes(int size) throws IOException {
        byte[] bytes = new byte[size];
        try {
            SecureRandom.getInstanceStrong().nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return bytes;
    }
}
