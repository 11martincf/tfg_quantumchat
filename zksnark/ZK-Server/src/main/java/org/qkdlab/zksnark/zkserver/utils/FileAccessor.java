package org.qkdlab.zksnark.zkserver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.qkdlab.zksnark.model.MerkleTree;

import java.io.*;

/**
 * FileAccessor
 *
 * Clase de acceso a ficheros para serializar / deserializar
 * NOTA: esta clase tiene pequeñas diferencias con FileAccessor de ZK-Validator.
 *
 * @param <T> Clase del objeto a serializar / deserializar
 */
public class FileAccessor<T> {
    private final String folder;
    private final Class<T> dataClass;
    private final Gson gson;

    /**
     *
     * @param folder Carpeta donde se almacenará el objeto
     * @param dataClass Clase del objeto a serializar / deserializar
     */
    public FileAccessor(String folder, Class<T> dataClass) {
        this.folder = folder;
        this.dataClass = dataClass;

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    /**
     * Leer fichero (JSON)
     * @param filename nombre del fichero JSON
     * @return Objeto deserializado
     * @throws IOException
     */
    public T readFile(String filename) throws IOException {
        Reader reader = createFileReader(buildFilePath(filename));
        T data = gson.fromJson(reader, dataClass);
        reader.close();

        return data;
    }

    /**
     * Guardar fichero (JSON)
     * @param data Objeto a serializar
     * @param filename nombre del fichero
     * @throws IOException
     */
    public void saveFile(T data, String filename) throws IOException {

        Writer writer = createFileWriter(buildFilePath(filename));
        gson.toJson(data, writer);
        writer.close();

    }

    public String getAbsolutePath(String filename) {
        return buildFilePath(filename);
    }

    /**
     * Función auxiliar para leer en fichero JSON
     * @param filename nombre del fichero
     * @return FileReader
     * @throws IOException
     */
    private Reader createFileReader(String filename) throws IOException {
        File file = new File(filename);
        if(!file.exists()) {
            throw new IOException("Could not find resource.");
        }

        Reader reader = new FileReader(file);

        return reader;
    }

    /**
     * Función auxiliar para escribir en fichero JSON
     * @param filename nombre del fichero
     * @return FileWriter
     * @throws IOException
     */
    private Writer createFileWriter(String filename) throws IOException {
        File file = new File(filename);
        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException("Error creating resource.");
            }
        }

        Writer writer = new FileWriter(file);

        return writer;
    }

    private String buildFilePath(String filename) {
        return folder + File.separator + filename;
    }

    /**
     * Leer fichero (Raw)
     * @param filename nombre del fichero
     * @return Objeto deserializado
     * @throws IOException
     */
    public T readRawFile(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(buildFilePath(filename));
        ObjectInputStream ois = new ObjectInputStream(fis);

        try {
            return (T)ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    /**
     * Guardar en fichero (Raw)
     * @param object objeto a guardar
     * @param filename nombre del fichero
     * @throws IOException
     */
    public void saveRawFile(T object, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(buildFilePath(filename));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
    }
}
