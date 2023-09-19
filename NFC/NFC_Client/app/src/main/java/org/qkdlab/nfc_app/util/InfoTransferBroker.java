package org.qkdlab.nfc_app.util;

import java.util.ArrayList;
import java.util.List;

/**
 * InfoTransferBroker
 *
 * Singleton que almacena una cola de elementos a transmitir por NFC
 */
public class InfoTransferBroker {
    private static InfoTransferBroker instance;

    private final List<byte[]> infoToTransfer;

    public static InfoTransferBroker getInstance() {
        if(instance == null) {
            instance = new InfoTransferBroker();
        }
        return instance;
    }

    private InfoTransferBroker() {
        infoToTransfer = new ArrayList<>();
    }

    /**
     * Extraer primer elemento de la cola
     * @return información a transmitir por NFC
     */
    public byte[] getInfoToTransfer() {
        if(infoToTransfer.isEmpty()) return null;
        return infoToTransfer.remove(0);
    }

    /**
     * Añadir elemento al final de la cola
     * @param infoToTransfer elemento que se añade a la colaa
     */
    public void addInfoToTransfer(byte[] infoToTransfer) {
        this.infoToTransfer.add(infoToTransfer);
    }
}
