package org.qkdlab.nfc_app.nfc;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.qkdlab.zksnark.zkclient.io.HttpHelper;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AndroidHttpHelper
 *
 * Subclase de HttpHelper (de ZK-Client) para mandar peticiones HTTP desde Android
 * NOTA: Esta clase es un prototipo y no está terminada
 */
public class AndroidHttpHelper extends HttpHelper {
    private Context context;
    public AndroidHttpHelper(String url, Context context) {
        super(url);
        this.context = context;
    }

    @Override
    protected String postHttp(String requestURL, String postMesssage) throws IOException {
        JsonObjectRequest postRequest = null;
        try {
            postRequest = new JsonObjectRequest(Request.Method.POST, requestURL,
                    new JSONObject(postMesssage),
                    (Response.Listener) response -> {

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Volley.newRequestQueue(context).add(postRequest);
        return "";
    }

}
