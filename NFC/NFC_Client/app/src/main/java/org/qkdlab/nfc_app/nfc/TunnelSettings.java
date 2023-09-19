package org.qkdlab.nfc_app.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.qkdlab.nfc_app.util.Constants;

/**
 * SetUrlDialogFragment
 *
 * Residuo de una implementación antigua. IGNORAR ESTA CLASE
 */
public class TunnelSettings {

    private static final String TAG = Constants.LOG_TAG;

    private static final String PREFS_NAME = "nfc_app";
    private static final String PREF_KEY_URL = "url";


    public static void setUrl(Context context, String url) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_KEY_URL, url).apply();
        Log.i(TAG, "Stored url: " + url);
    }

    public static String getUrl(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("url", "1234");
    }
}
