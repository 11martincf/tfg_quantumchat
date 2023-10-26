package org.qkdlab.zksnark.zkvalidator.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * FileAccessor
 *
 * Clase de acceso a ficheros para serializar / deserializar
 * NOTA: esta clase tiene pequeñas diferencias con FileAccessor de ZK-Server.
 *
 * @param <T> Clase del objeto a serializar / deserializar
 */
public class FileAccessor<T> {
    private final File file;
    private T data;
    private final Class<T> dataClass;
    private long lastModified;
    private final Gson gson;
    /**
     *
     * @param filename Nombre del fichero
     * @param dataClass Clase del objeto a serializar / deserializar
     */
    public FileAccessor(String filename, Class<T> dataClass) {
        this.file = new File(filename);
        this.dataClass = dataClass;

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    /**
     * Leer fichero (JSON)
     * @return Objeto deserializado
     * @throws IOException
     */
    public T readFile() throws IOException {
        if(file.lastModified() == lastModified) {
            //System.out.println("NOT MODIFIED");
            return data;
        }

        //System.out.println("MODIFIED");
        Reader reader = createFileReader(file);
        data = gson.fromJson(reader, dataClass);
        reader.close();
        lastModified = file.lastModified();

        return data;
    }

    /**
     * Guardar fichero (JSON)
     * @param data Objeto a serializar
     * @throws IOException
     */
    public void saveFile(T data) throws IOException {
        String filename = file.getPath();
        saveFile(data, filename);
    }

    /**
     * Guardar fichero (JSON)
     * @param data Objeto a serializar
     * @param filename nombre del fichero
     * @throws IOException
     */
    public void saveFile(T data, String filename) throws IOException {

        Writer writer = createFileWriter(filename);
        gson.toJson(data, writer);
        writer.close();

    }

    /**
     * Función auxiliar para leer en fichero JSON
     * @param file nombre del fichero
     * @return FileReader
     * @throws IOException
     */
    private Reader createFileReader(File file) throws IOException {
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

    /**
     * Leer fichero (Raw)
     * @return Objeto deserializado
     * @throws IOException
     */
    public T readRawFile() throws IOException {
        if(file.lastModified() == lastModified) {
            //System.out.println("NOT MODIFIED");
            return data;
        }
        FileInputStream fis = new FileInputStream(file);
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
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
    }
}
