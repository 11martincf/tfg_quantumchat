package org.qkdlab.nfc_app.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AsyncRequestQueue;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.qkdlab.nfc_app.nfc.AndroidProofClient;
import org.qkdlab.nfc_app.util.InfoTransferBroker;
import org.qkdlab.nfc_app.R;
import org.qkdlab.nfc_app.nfc.TunnelSettings;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.ZKProof;
import org.qkdlab.zksnark.model.message.ProofMessage;
import org.qkdlab.zksnark.zkclient.proof.CommitNote;
import org.qkdlab.zksnark.zkclient.proof.ProofClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.stream.Collectors;

/**
 * TunnelActivity
 *
 * Actividad principal de la aplicación. Contiene una tab para cada Fragment
 */
public class TunnelActivity extends AppCompatActivity implements SetUrlDialogFragment.SetUrlDialogListener {

    private TabLayout tabs;
    private ProofClient proofClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tunnel_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        tabs = (TabLayout)findViewById(R.id.tabs);

        // Selección de Fragment utilizando el Tab
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    //HttpAuthenticationFragment httpFragment = HttpAuthenticationFragment.newInstance(proofClient);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HttpAuthenticationFragment()).commit();
                } else if (tab.getPosition() == 1) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SendProofFragment()).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

        });
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HttpAuthenticationFragment()).commit();


        String filesPath = getApplicationContext().getFilesDir().getAbsolutePath();
        File certDir = new File(filesPath + File.separator + "certs");
        File cert = new File(certDir, "bob.p12");

        // Copiar el certificado de la carpeta "Assets" (Carpeta del proyecto)
        // a la carpeta "Files" (/data/data/org.qkdlab.nfc_app/files)
        try {
            if (!cert.exists()) {
                if(!certDir.exists()) {
                    certDir.mkdir();
                }
                cert.createNewFile();
                FileOutputStream fos = new FileOutputStream(cert);
                AssetManager assetManager = getAssets();
                InputStream inputStream = assetManager.open("bob.p12");
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[65536];
                int nRead;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                fos.write(buffer.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Crear cliente
        ProofClient proofClient = new AndroidProofClient("http://10.68.96.151:8080", filesPath, this);
        proofClient.init();

        // Autenticación y creación de prueba (no funciona)
        /*try {
            //proofClient.doAuthenticate(cert.getName());
            //proofClient.downloadMerkleTree();
            MerkleTree merkleTree = proofClient.loadMerkleTree();
            CommitNote commitNote = proofClient.loadCommitNote();
            proofClient.generateProof(merkleTree, commitNote);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        // Cargar prueba
        ByteArrayOutputStream baos;
        ZKProof proof = null;
        try {
            proof = proofClient.loadProof();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Añadir 25 pruebas de tamaño 32 a la cola
        try {
            ProofMessage proofMessage = new ProofMessage(proof, 32);
            baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(proofMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 25; i++) {
            InfoTransferBroker.getInstance().addInfoToTransfer(baos.toByteArray());
        }

        //InfoTransferBroker.getInstance().addInfoToTransfer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccc".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForNFC();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_set_url) {
            final SetUrlDialogFragment setUrlDialog = new SetUrlDialogFragment();
            setUrlDialog.show(getSupportFragmentManager(), null);
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSetUrlDialogCancelled() {
    }

    @Override
    public void onSetUrlDialogSucceeded(String urlSpec) {
        TunnelSettings.setUrl(this, urlSpec);
    }

    private void checkForNFC() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (!nfcAdapter.isEnabled()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error_nfc_disabled_title);
            builder.setMessage(R.string.error_nfc_disabled_message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }


}
