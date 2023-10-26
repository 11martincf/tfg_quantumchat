package org.qkdlab.zksnark.zkvalidator.crypto;

import com.idquantique.quantis.Quantis;
import com.idquantique.quantis.QuantisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * QuantisKeyGenerator
 *
 * Obtiene claves QRNG de Quantis QRNG USB
 */
public class QuantisKeyGenerator implements KeyGenerator {
    private Quantis qrng;
    private static final Logger LOG = LoggerFactory.getLogger(RemoteKeyGenerator.class);

    /**
     * Inicializa parámetros
     */
    public QuantisKeyGenerator() {
        //LOG.info("Using Quantis Library v" + Quantis.GetLibVersion() + "\n");

        int countPci = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI);
        int countUsb = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB);

        if (countPci > 0) {
            LOG.info("Found " + countPci + " Quantis PCI devices.");
            qrng = new Quantis(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, 0);
        } else if (countUsb > 0) {
            LOG.info("Found " + countUsb + " Quantis USB devices.");
            qrng = new Quantis(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB, 0);
        } else {
            LOG.info("No Quants device installed.");
            throw new RuntimeException("No Quants device installed.");
        }


    }

    /**
     * Genera clave QRNG
     * @param size Tamaño de la clave a generar
     * @return clave QRNG
     * @throws IOException
     */
    public byte[] getRandomBytes(int size) throws IOException {
        byte[] randomBytes = new byte[0];
        try {
            randomBytes = qrng.Read(size);
        } catch (QuantisException e) {
            throw new IOException("Unable to provide QRNG Key", e);
        }
        return randomBytes;
    }
}
