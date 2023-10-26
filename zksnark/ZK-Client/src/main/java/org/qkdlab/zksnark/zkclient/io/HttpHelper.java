package org.qkdlab.zksnark.zkclient.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.qkdlab.zksnark.model.message.CommitMessage;
import org.qkdlab.zksnark.model.message.CommitNoteMessage;
import org.qkdlab.zksnark.model.message.ProofMessage;
import org.qkdlab.zksnark.model.message.RawProofMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * HttpHelper
 *
 * Realiza funciones HTTP sin especificar la implementación concreta
 */
public abstract class HttpHelper {
    private String serverUrl;
    private Gson gson;

    public HttpHelper(String serverUrl) {
        this.serverUrl = serverUrl;
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        this.gson = builder.create();

    }

    /**
     * Realiza autenticación con certificado
     * @param endpoint endpoint de autenticación del servidor
     * @param commitMessage Mensaje con el certificado y el commitment
     * @return Respuesta del servidor
     * @throws IOException
     */
    public String doAuthentication(String endpoint, CommitMessage commitMessage) throws IOException {
        String url = serverUrl + endpoint;

        String message = gson.toJson(commitMessage);

        String response = null;
        try {
            response = postHttp(url, message);
        }
        catch (IOException e) {
            throw new IOException("Authentication Error: " + e.getMessage(), e);
        }

        return response;
    }

    public String outsource(String endpoint, CommitNoteMessage commitNoteMessage) throws IOException {
        String url = serverUrl + endpoint;

        String message = gson.toJson(commitNoteMessage);

        String response = null;
        try {
            response = postHttp(url, message);
        }
        catch (IOException e) {
            throw new IOException("Authentication Error: " + e.getMessage(), e);
        }

        return response;
    }

    /**
     * Envía el zk-SNARK a través de la web
     * @param endpoint endpoint de validación del servidor
     * @param proofMessage Mensaje con el zk-SNARK
     * @return Respuesta del servidor
     * @throws IOException
     */
    public String sendProof(String endpoint, ProofMessage proofMessage) throws IOException {
        String url = serverUrl + endpoint;

        String message = gson.toJson(proofMessage);

        String response = null;
        try {
            response = postHttp(url, message);
        }
        catch (IOException e) {
            throw new IOException("Proof Error: " + e.getMessage(), e);
        }

        return response;
    }

    public String sendRawProof(String endpoint, RawProofMessage rawProofMessage) throws IOException {
        String url = serverUrl + endpoint;

        String message = gson.toJson(rawProofMessage);

        String response = null;
        try {
            response = postHttp(url, message);
        }
        catch (IOException e) {
            throw new IOException("Proof Error: " + e.getMessage(), e);
        }

        return response;
    }

    public BufferedInputStream openDownloadStream(String endpoint) throws IOException {
        return new BufferedInputStream(new URL(serverUrl + endpoint).openStream());
    }

    protected abstract String postHttp(String url, String message) throws IOException;


}
