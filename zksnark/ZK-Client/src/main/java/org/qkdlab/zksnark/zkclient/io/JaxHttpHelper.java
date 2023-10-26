package org.qkdlab.zksnark.zkclient.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.qkdlab.zksnark.model.message.CommitMessage;
import org.qkdlab.zksnark.model.message.ProofMessage;


import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * JaxHttpHelper
 *
 * Realiza las peticiones HTTP mediante la librería javax.ws.rs
 */
public class JaxHttpHelper extends HttpHelper {
    private final Client client;

    public JaxHttpHelper(String serverUrl) {
        super(serverUrl);
        client = ClientBuilder.newClient();
    }

    @Override
    protected String postHttp(String URL, String msg) throws IOException {
        String res = "";

        WebTarget target = client.target(URL);

        Invocation.Builder request = target.request();

        Response post = request.post(Entity.json(msg));
        String responseJson = post.readEntity(String.class);
        res = responseJson;

        //System.out.println("Status: " + post.getStatus());

        switch (post.getStatus()) {
            case 200:
                res = responseJson;
                break;
            default:
                res = "Error";
                throw new IOException(responseJson);
        }

        return res;

    }
}
