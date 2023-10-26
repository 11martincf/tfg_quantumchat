package org.qkdlab.zksnark.zkclient.proof;

import com.google.gson.Gson;
import org.qkdlab.zksnark.model.LibsnarkProof;
import org.qkdlab.zksnark.model.message.CommitNoteMessage;
import org.qkdlab.zksnark.model.message.RawProofMessage;
import org.qkdlab.zksnark.zkclient.io.FileDAO;
import org.qkdlab.zksnark.zkclient.io.HttpHelper;
import org.qkdlab.zksnark.zkclient.io.JaxHttpHelper;
import org.qkdlab.zksnark.model.message.CommitMessage;
import org.qkdlab.zksnark.model.message.ProofMessage;
import org.qkdlab.zksnark.model.Constants;
import org.qkdlab.zksnark.model.MerkleTree;
import org.qkdlab.zksnark.model.ZKProof;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLOutput;
import java.util.Base64;

/**
 * ProofClient
 *
 * Clase principal de ZK-Client. El cliente interactúa con ella para autenticarse con certificado y para generar y enviar zk-SNARKs
 */
public class ProofClient {
    private HttpHelper httpHelper = null;
    private FileDAO fileDAO;
    private String url;
    private String fileFolder;

    public ProofClient(String url, String fileFolder) {
        this.url = url;
        this.fileFolder = fileFolder;
    }

    /**
     * Inicializar parámetros
     */
    public void init() {
        httpHelper = createHttpHelper(url);
        fileDAO = new FileDAO(fileFolder, Constants.DEFAULT_CERTIFICATE_FOLDER);

        try {
            fileDAO.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Genera HttpHelper por defecto
     * @param url url del servidor
     * @return httphelper
     */
    protected HttpHelper createHttpHelper(String url) {
        return new JaxHttpHelper(url);
    }


    /**
     * Genera un commitMessage y lo envía al servidor por HTTP
     * @param certFile nombre del fichero del certificado
     * @throws IOException
     */
    public void doAuthenticate(String certFile) throws IOException {

        //Should caller provide certificate?

        KeyStore keyStore = fileDAO.initKeyStore(certFile);
        Certificate cert;
        PrivateKey privateKey;
        try {
            String alias = keyStore.aliases().nextElement();
            cert = keyStore.getCertificate(alias);
            privateKey = (PrivateKey) keyStore.getKey(alias, "".toCharArray());
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        CommitNoteKEM commitNote = createCommitNote();
        CommitMessage commitMessage = null;
        try {
            byte[] digest = HashUtil.generateCommitment(commitNote);
            byte[] signedCommit = HashUtil.signCommitment(digest, privateKey);
            String encodedDigest = Base64.getEncoder().encodeToString(digest);

            commitMessage = new CommitMessage(encodedDigest,
                    Base64.getEncoder().encodeToString(cert.getEncoded()),
                    Base64.getEncoder().encodeToString(signedCommit));

        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        String endpoint = Constants.COMMIT_ENDPOINT;
        String response = httpHelper.doAuthentication(endpoint, commitMessage);
    }

    /**
     * Inicializa un commitnote con los valores aleatorios
     * @return commitnote
     */
    private CommitNoteKEM createCommitNote() {
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] sigma = new byte[Constants.KEYPAIR_SIZE];
        byte[] privateKey = new byte[Constants.KEYPAIR_SIZE];
        random.nextBytes(sigma);
        random.nextBytes(privateKey);

        CommitNoteKEM commitNote = new CommitNoteKEM(sigma);
        try {
            fileDAO.saveCommitNote(commitNote, Constants.DEFAULT_COMMIT_FILENAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return commitNote;
    }

    public ProofMessage outsource(CommitNoteKEM commitNote) {
        CommitNoteMessage commitNoteMessage = new CommitNoteMessage(commitNote);

        String response;
        try {
            response = httpHelper.outsource(Constants.OUTSOURCE_ENDPOINT, commitNoteMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        ProofMessage proofMessage = gson.fromJson(response, ProofMessage.class);

        return proofMessage;
    }

    /**
     * Descargar árbol Merkle del servidor
     * @throws IOException
     */
    public void downloadMerkleTree() throws IOException {
        String endpoint = Constants.DOWNLOAD_ENDPOINT;
        BufferedInputStream in = httpHelper.openDownloadStream(endpoint);
        fileDAO.downloadFile(in, Constants.DEFAULT_TREE_FILENAME);
    }

    public MerkleTree loadMerkleTree() throws IOException{
        return fileDAO.loadMerkleTree(Constants.DEFAULT_TREE_FILENAME);
    }

    public CommitNoteKEM loadCommitNote() throws IOException{
        return fileDAO.loadCommitNote(Constants.DEFAULT_COMMIT_FILENAME);
    }

    public ZKProof loadProof() throws IOException{
        return fileDAO.loadProof(Constants.DEFAULT_PROOF_FILENAME);
    }

    private String loadRawProof() throws IOException{
        return Base64.getEncoder().encodeToString(fileDAO.loadRawProof(Constants.DEFAULT_RAW_PROOF_FILENAME));
    }

    /**
     * Genera zk-SNARK
     * @param merkleTree árbol Merkle descargado del servidor
     * @param commitNote commitNote generado durante la autenticación
     */
    public void generateZokratesProof(MerkleTree merkleTree, CommitNoteKEM commitNote) {

        ProofGenerator proofGenerator = new ZokratesProofGenerator(commitNote, merkleTree);

        try {
            proofGenerator.generateProof(fileFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LibsnarkProof generateLibsnarkProof(MerkleTree merkleTree, CommitNoteKEM commitNote) {

        ProofGenerator proofGenerator = new LibsnarkProofGenerator(commitNote, merkleTree);

        LibsnarkProof proof = null;
        try {
            proofGenerator.generateProof(fileFolder);

            String encodedProof = loadRawProof();
            String encodedRoot = Base64.getEncoder().encodeToString(proofGenerator.getMerkleRoot());
            String encodedNullifier = Base64.getEncoder().encodeToString(proofGenerator.getNullifier());

            proof = new LibsnarkProof(encodedProof, encodedRoot, encodedNullifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return proof;
    }

    /**
     * Enviar la prueba al servidor
     * @param proof
     * @param keySize
     * @return
     * @throws IOException
     */
    public byte[] sendZokratesProof(ZKProof proof, CommitNoteKEM commitNote, int keySize) throws IOException {
        byte[] publicKey = commitNote.getEncodedPublicKey();
        String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey);
        ProofMessage proofMessage = new ProofMessage(proof, encodedPublicKey, keySize);
        String response = httpHelper.sendProof(Constants.PROOF_ENDPOINT, proofMessage);

        byte[] sealedKey = Base64.getDecoder().decode(response);

        return unsealKey(sealedKey, commitNote);
    }

    public byte[] sendLibsnarkProof(LibsnarkProof proof, CommitNoteKEM commitNote, int keySize) throws IOException {
        byte[] publicKey = commitNote.getEncodedPublicKey();
        String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey);
        RawProofMessage rawProofMessage = new RawProofMessage(proof, encodedPublicKey, keySize);
        String response = httpHelper.sendRawProof(Constants.RAW_PROOF_ENDPOINT, rawProofMessage);

        byte[] sealedKey = Base64.getDecoder().decode(response);

        return unsealKey(sealedKey, commitNote);
    }

    private byte[] unsealKey(byte[] sealedKey, CommitNoteKEM commitNote) {
        PrivateKey privKey;

        try {
            byte[] privKeyBytes = commitNote.getEncodedPrivateKey();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
            privKey = keyFactory.generatePrivate(privKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] unsealedKey;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            unsealedKey = cipher.doFinal(sealedKey);

            System.out.println("SEALED: " + Base64.getEncoder().encodeToString(sealedKey));
            System.out.println("UNSEALED: " + Base64.getEncoder().encodeToString(unsealedKey));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return unsealedKey;
    }

}
