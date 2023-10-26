package org.qkdlab.zksnark.zkvalidator.crypto;

import java.io.IOException;

/**
 * KeyGenerator
 *
 * Interfaz para la generación de claves QRNG
 */
public interface KeyGenerator {
    byte[] getRandomBytes(int size) throws IOException;
}
