package org.qkdlab.zksnark.zkvalidator.validator;

import org.apache.commons.codec.binary.Hex;
import org.qkdlab.zksnark.model.message.ProofMessage;
import org.qkdlab.zksnark.zkserver.utils.crypto.DummyKeyGenerator;
import org.qkdlab.zksnark.zkvalidator.ValidatorGUI;
import org.qkdlab.zksnark.zkvalidator.crypto.KeyGenerator;
import org.qkdlab.zksnark.zkvalidator.crypto.QuantisKeyGenerator;
import org.qkdlab.zksnark.zkvalidator.crypto.RemoteKeyGenerator;
import org.qkdlab.zksnark.zkvalidator.dao.CommandHandler;
import org.qkdlab.zksnark.zkvalidator.dao.LocalValidatorDAO;
import org.qkdlab.zksnark.zkvalidator.dao.ValidatorDAO;
import org.qkdlab.zksnark.model.Constants;
import org.qkdlab.zksnark.model.ZKProof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * ProofValidator
 *
 * Clase principal de ZK-Validator. Se mantiene a la escucha de clientes, valida las pruebas y genera las claves QRNG.
 * Utiliza interfaz gráfica para mostrar la información
 */
public class ProofValidator {
    private ValidatorDAO validatorDAO;
    private IOProofManager ioProofManager;
    private KeyGenerator keyGenerator;
    private ValidatorGUI validatorGUI;
    private List<byte[]> treeRoots;
    int keySize = 32;
    long keyGenTime = 0;
    int testSize = 25;
    public long transmissionTime;

    private static final Logger LOG = LoggerFactory.getLogger(ProofValidator.class);

    /**
     * Inicializar parámetros
     */
    public ProofValidator() {
        this.validatorDAO = new LocalValidatorDAO(
                Constants.DEFAULT_SERVER_FOLDER,
                Constants.DEFAULT_PROOF_FOLDER,
                Constants.DEFAULT_TREE_FILENAME,
                Constants.DEFAULT_NULLIFIERS_FILENAME
        );

        this.ioProofManager = new NfcIOProofManager();
        //keyGenerator = new RemoteKeyGenerator(testSize * keySize);
        keyGenerator = (KeyGenerator) new DummyKeyGenerator();
        this.validatorGUI = new ValidatorGUI();
    }

    /**
     * Ejecuta el bucle principal. Cuando establece una conexión, valida el zk-SNARK
     * Informa a ValidatorGUI de cualquier situación
     */
    public void run() {
        try {
            ioProofManager.init();
            validatorGUI.init("Validator");
            validatorGUI.setReaderName(ioProofManager.getIoManagerName());
            treeRoots = validatorDAO.getTreeRoots();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int numRequest = 0;
        while (true) {
            try {
                IOProofConnection connection = ioProofManager.listen();
                validatorGUI.notifyNewConnection(connection.getConnectionName());
                ProofMessage proofMessage = connection.receiveData();
                validatorGUI.showProofMessage(proofMessage);
                //ProofMessage proofMessage = new ProofMessage(null, keySize);
                handleRequest(proofMessage, connection);
            }
            catch (IOException e) {
                e.printStackTrace();
                validatorGUI.showTransmissionError(e.getMessage());
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            numRequest++;
            /*if(numRequest % testSize == 0) {
                double transTime = (transmissionTime / (Math.pow(10, 6)) / testSize);
                //double genTime = (keyGenTime / (Math.pow(10, 6)) / testSize);
                //double totTime = (totalTime / (Math.pow(10, 6)) / testSize);

                //System.out.println("[KEY]\tSize = " + + keySize + "\tTotal(s): " + totTime + "\tTransmission(s): " + transTime + "\tKeyGeneration(s): " + genTime);
                System.out.println(keySize + "\t\t\t" + transTime);
                //System.out.println(genTime);
                transmissionTime = 0;
                keyGenTime = 0;
                //totalTime = 0;
                keySize *= 2;
                if (keySize > 131072) {
                    System.exit(1);
                }


                keyGenerator = new RemoteKeyGenerator(testSize * keySize);
            }*/
        }
    }

    /**
     * Valida el zk-SNARK
     * Si tiene éxito, devuelve clave QRNG al cliente
     * @param proofMessage Prueba recibida del cliente
     * @param connection Conexión con el cliente
     * @throws IOException
     */
    private void handleRequest(ProofMessage proofMessage, IOProofConnection connection) throws IOException {
        ZKProof proof = proofMessage.getZkProof();

        boolean isValid = checkRootAndNullifier(proof);
        if(!isValid) {
            connection.sendError();
            return;
        }

        String filename = Hex.encodeHexString(proof.getNullifier());
        validatorDAO.writeProof(proof, filename);

        isValid = CommandHandler.executeZokrates(Constants.DEFAULT_SERVER_FOLDER, filename + ".json");
        validatorGUI.setProofOkLabel(isValid);

        if(!isValid) {
            connection.sendError();
            return;
        }
        validatorDAO.addNullifier(proof.getNullifier());

        // long startTime = System.nanoTime();
        byte[] publicKey = Base64.getDecoder().decode(proofMessage.getEncodedPublicKey());
        byte[] sealedKey = generateRandomKey(proofMessage.getKeySize(), publicKey);
        //keyGenTime += System.nanoTime() - startTime;
        //startTime = System.nanoTime();
        connection.sendSuccess(sealedKey);
        //transmissionTime += System.nanoTime() - startTime;
        validatorGUI.setSentKey(sealedKey);
    }

    private byte[] generateRandomKey(int keySize, byte[] publicKeyBytes) throws IOException {
        byte[] qrngKey = keyGenerator.getRandomBytes(keySize);
        PublicKey publicKey;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encryptedKey;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedKey = cipher.doFinal(qrngKey);

            System.out.println("GENERATED: " + Base64.getEncoder().encodeToString(qrngKey));
            System.out.println("SEALED: " + Base64.getEncoder().encodeToString(encryptedKey));


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

        return encryptedKey;
    }

    /**
     * Comprueba que el nullifier no existe en la lista de nullifiers, y que el root sí que existe en la lista de roots
     * @param proof zk-SNARK recibido del cliente
     * @return true si todo correcto, false si no
     * @throws IOException
     */
    private boolean checkRootAndNullifier(ZKProof proof) throws IOException {
        List<byte[]> nullifiers = validatorDAO.getNullifiers();
        //boolean nullifierExists = isByterarrayInList(proof.getNullifier(), nullifiers);
        boolean nullifierExists = false;
        if(nullifierExists) {
            validatorGUI.setNullifierOkLabel(false);
            LOG.info("Nullifier already exists");
            return false;
        }
        validatorGUI.setNullifierOkLabel(true);

        boolean isRootValid = isByterarrayInList(proof.getMerkleRoot(), treeRoots);
        //boolean isRootValid = true;
        if(!isRootValid) {
            treeRoots = validatorDAO.getTreeRoots();
            isRootValid = isByterarrayInList(proof.getMerkleRoot(), treeRoots);
        }
        if(!isRootValid) {
            validatorGUI.setRootOkLabel(false);
            LOG.info("Invalid Merkle Root");
            return false;
        }
        validatorGUI.setRootOkLabel(true);

        return true;
    }

    /**
     * Función auxiliar para comprobar si un bytearray se encuentra en una lista de bytearrays
     * @param bytearray elemento a comprobar si existe
     * @param list lista de bytearrays
     * @return true si está, false si no
     */
    private boolean isByterarrayInList(byte[] bytearray, List<byte[]> list) {
        return list.stream().anyMatch(item -> Arrays.equals(bytearray, item));
    }

}
