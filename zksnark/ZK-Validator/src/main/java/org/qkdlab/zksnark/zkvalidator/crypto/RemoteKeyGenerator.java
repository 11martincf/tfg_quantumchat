package org.qkdlab.zksnark.zkvalidator.crypto;

import org.qkdlab.zksnark.zkvalidator.dao.CommandHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * RemoteKeyGenerator
 *
 * Genera claves utilizando el QRNG del CESGA
 */
public class RemoteKeyGenerator implements KeyGenerator {

    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024 * 1024;
    private int bufferSize;
    private ByteBuffer buffer;

    private String remoteUsername;

    private String remoteHost;

    private int remotePort;

    public RemoteKeyGenerator() {
        this.bufferSize = DEFAULT_BUFFER_SIZE;
        init();
    }

    public RemoteKeyGenerator(int bufferSize) {
        this.bufferSize = bufferSize;
        init();
    }

    /**
     * Inicializa los parámetros
     */
    private void init() {
        buffer = ByteBuffer.allocate(bufferSize);
        buffer.position(buffer.limit());

        InputStream inputStream = RemoteKeyGenerator.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        remoteUsername = properties.getProperty("remote.username");
        remoteHost = properties.getProperty("remote.host");
        remotePort = Integer.parseInt(properties.getProperty("remote.port"));
    }

    /**
     * Devuelve clave de QRNG. Si existe material en el buffer, lo utiliza. Si no, llena el buffer.
     * @param size tamaño de la clave solicitada
     * @return clave QRNG
     * @throws IOException
     */
    @Override
    public byte[] getRandomBytes(int size) throws IOException {
        if(size > bufferSize) throw new IOException("Requests cannot exceed buffer size (" + bufferSize + " bytes)");

        if(size > buffer.remaining()) {
            int requestSize = buffer.position();
            byte[] requestedBytes = CommandHandler.sshCommand(remoteUsername, remoteHost, remotePort, requestSize);
            buffer.rewind();
            buffer.put(requestedBytes);
            buffer.rewind();
        }

        byte[] result = new byte[size];
        buffer.get(result);

        return result;
    }
}
