package org.qkdlab.nfc_app.nfc;

import android.content.Context;

import org.qkdlab.zksnark.zkclient.io.HttpHelper;
import org.qkdlab.zksnark.zkclient.proof.ProofClient;

/**
 * AndroidProofClient
 *
 * Extiende ProofClient (ZK-Client) para utilizar AndroidHttpHelper
 */
public class AndroidProofClient extends ProofClient {
    private Context context;

    public AndroidProofClient(String url, String fileFolder, Context context) {
        super(url, fileFolder);
        this.context = context;
    }

    @Override
    protected HttpHelper createHttpHelper(String url) {
        return new AndroidHttpHelper(url, context);
    }
}
