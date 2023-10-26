package org.qkdlab.zksnark.zkclient.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.ZKProof;
import org.qkdlab.zksnark.zkclient.proof.CommitNoteKEM;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * FileDAO
 *
 * Clase que traduce necesidades de información del programa a accesos a ficheros
 * Contiene todos los FileAccessor e información sobre la carpeta
 *
 * NOTA: esta clase es similar a FileDAO de ZK-Server, pero sin utilizar FileAccessor
 */
public class FileDAO {
    private String folder;
    private String certificateFolder;
    private final Gson gson;

    public FileDAO(String folder, String certificateFolder) {
        this.folder = folder;
        this.certificateFolder = certificateFolder;

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        this.gson = builder.create();
    }

    /**
     * Inicializar la estructura de carpetas
     * @throws IOException
     */
    public void init() throws IOException {
        File folderFile = new File(folder);
        if(!folderFile.exists()) {
            folderFile.createNewFile();
        }
    }

    public void saveCommitNote(CommitNoteKEM commitNote, String filename) throws IOException {
        Writer writer = createFileWriter(filename);
        gson.toJson(commitNote, writer);
        writer.close();
    }

    public CommitNoteKEM loadCommitNote(String filename) throws IOException {
        Reader reader = createFileReader(filename);
        CommitNoteKEM commitNote = gson.fromJson(reader, CommitNoteKEM.class);
        reader.close();
        return commitNote;
    }

    public MerkleTree loadMerkleTree(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(buildFilePath(filename));
        ObjectInputStream ois = new ObjectInputStream(fis);

        try {
            return (MerkleTree)ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    public void downloadFile(BufferedInputStream in, String filename) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(buildFilePath(filename));
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
    }

    public ZKProof loadProof(String filename) throws IOException {
        Reader reader = createFileReader(filename);
        ZKProof proof = gson.fromJson(reader, ZKProof.class);
        reader.close();
        return proof;
    }

    public byte[] loadRawProof(String filename) throws IOException{
        Path path = Paths.get(buildFilePath(filename));
        return Files.readAllBytes(path);
    }

    private Reader createFileReader(String filename) throws IOException {
        File file = new File(buildFilePath(filename));

        if(!file.exists()) {
            throw new IOException("Could not find resource.");
        }

       return new FileReader(file);
    }

    private Writer createFileWriter(String filename) throws IOException {
        File file = new File(buildFilePath(filename));

        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException("Error creating resource.");
            }
        }

        return new FileWriter(file);
    }

    private String buildFilePath(String filename) {
        return folder + File.separator + filename;
    }
    private String buildCertificateFilePath(String filename) {
        return folder + File.separator + certificateFolder + File.separator + filename;
    }

    /**
     * Incluye el certificado del usuario en la KeyStore
     * @param filename nombre del fichero del certificado
     * @return KeyStore inicializada
     * @throws IOException
     */
    public KeyStore initKeyStore(String filename) throws IOException {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(buildCertificateFilePath(filename)), "".toCharArray());
            return keyStore;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
