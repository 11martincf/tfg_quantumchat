package org.qkdlab.nfc_app.ui;

import static org.qkdlab.nfc_app.util.Constants.LOG_TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qkdlab.nfc_app.R;
import org.qkdlab.nfc_app.nfc.TunnelApduService;

/**
 * SendProofFragment
 */
public class SendProofFragment extends Fragment {
    private static final String TAG = LOG_TAG;
    private LocalBroadcastManager lbm;
    private TextView statusLabel;
    private ProgressBar progressBar;
    private boolean dataReceived;

    public SendProofFragment() {
        // Required empty public constructor
    }

    public static SendProofFragment newInstance(String param1, String param2) {
        SendProofFragment fragment = new SendProofFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_proof, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusLabel = (TextView) getView().findViewById(R.id.status_label);
        progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);


        lbm = LocalBroadcastManager.getInstance(this.getContext());

        statusLabel.setText(R.string.tunnel_status_ready);
    }

    @Override
    public void onResume() {
        super.onResume();

        lbm.registerReceiver(linkEstablishedReceiver, new IntentFilter(TunnelApduService.BROADCAST_INTENT_LINK_ESTABLISHED));
        lbm.registerReceiver(progressUpdateReceiver, new IntentFilter(TunnelApduService.BROADCAST_INTENT_PROGRESS_UPDATED));
        lbm.registerReceiver(authSuccessReceiver, new IntentFilter(TunnelApduService.BROADCAST_INTENT_AUTH_SUCCESS));
        lbm.registerReceiver(authErrorReceiver, new IntentFilter(TunnelApduService.BROADCAST_INTENT_AUTH_ERROR));
        lbm.registerReceiver(linkDeactivatedReceiver, new IntentFilter(TunnelApduService.BROADCAST_INTENT_LINK_DEACTIVATED));

        statusLabel.setText(R.string.tunnel_status_ready);
    }

    @Override
    public void onPause() {
        lbm.unregisterReceiver(linkEstablishedReceiver);
        lbm.unregisterReceiver(progressUpdateReceiver);
        lbm.unregisterReceiver(authSuccessReceiver);
        lbm.unregisterReceiver(authErrorReceiver);
        lbm.unregisterReceiver(linkDeactivatedReceiver);

        super.onPause();
    }

    private BroadcastReceiver linkEstablishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            statusLabel.setText(R.string.tunnel_status_link_established);
            progressBar.setVisibility(View.VISIBLE);
            dataReceived = false;

            int progressMax = intent.getIntExtra("msgSize", 100);
            progressBar.setMax(progressMax);
        }
    };

    private BroadcastReceiver progressUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int bytesSent = intent.getIntExtra("numSent", -1);
            final int totalBytes = intent.getIntExtra("numTotal", -1);
            statusLabel.setText(getString(R.string.tunnel_status_progress, bytesSent, totalBytes));
            progressBar.setProgress(bytesSent);
        }
    };

    private BroadcastReceiver authSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dataReceived = true;
            final byte[] key = intent.getByteArrayExtra("key");
            String encodedKey = Base64.encodeToString(key, Base64.DEFAULT);
            Log.e(TAG, "AUTH SUCCESS: " + encodedKey);
            statusLabel.setText(getString(R.string.tunnel_status_auth_success, encodedKey));
        }
    };

    private BroadcastReceiver authErrorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dataReceived = false;
            Log.e(TAG, "AUTH ERROR");
            statusLabel.setText(R.string.tunnel_status_auth_error);
        }
    };

    private BroadcastReceiver linkDeactivatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int reason = intent.getIntExtra("reason", 0);
            if (!dataReceived && reason == 0) { // link terminated before data was received
                statusLabel.setText(R.string.tunnel_status_link_deactivated);
            }

            progressBar.setVisibility(View.INVISIBLE);
        }
    };
}