package org.qkdlab.zksnark.zkvalidator.dao;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.qkdlab.zksnark.model.Constants;

import java.io.*;
import java.util.Arrays;

/**
 * CommandHandler
 *
 * Clase estática para la ejecución de comandos de Bash
 */
public class CommandHandler {
    /*private static long verificationTime;
    private static int verificationNum;
    private static int testSize = 20;*/

    /**
     * Llama a ZoKrates para validar una prueba zk-SNARK
     * @param filename nombre de la prueba
     * @throws IOException
     */
    public static boolean executeZokrates(String folder, String filename) throws IOException {
        boolean result = false;

        String[] commands = generateCommand(filename);
        ProcessBuilder pb = new ProcessBuilder(commands);
        //pb.inheritIO();
        pb.directory(new File(folder));

        //long startTime = System.nanoTime();
        Process process = pb.start();
        try {
            process.waitFor();
            String processOutput = parseOutput(process.getInputStream());
            if(processOutput.contains("PASSED")) {
                result = true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*verificationTime += System.nanoTime() - startTime;

        verificationNum++;
        if(verificationNum == testSize) {
            double verifyTimeMs = (verificationTime / Math.pow(10, 6)) / testSize;
            System.out.println(Constants.MERKLE_TREE_DEPTH + "\t\t\t" + verifyTimeMs);
        }*/

        return result;
    }

    /**
     * Establece una conexión SSH con el CESGA para solicitar claves
     * @param username usuario CESGA
     * @param host dirección IP CESGA
     * @param port puerto CESGA
     * @param keySize tamaño de clave solicitado
     * @return clave QRNG
     * @throws IOException
     */
    public static byte[] sshCommand(String username, String host, int port, int keySize) throws IOException {
        Session session = null;
        ChannelExec channel = null;

        String password = askForPassword();
        String command = "module load qrng; python qrng/get_qrng.py " + keySize;

        byte[] result = {91, 91};

        try {

            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            //channel.setErrStream(System.err);
            channel.connect();

            while (channel.isConnected()) {
                Thread.sleep(100);
            }
            /*String unparsedResult = responseStream.toString();
            System.out.println(unparsedResult);
            String unparsedResult1 = unparsedResult.split("_____")[0];
            System.out.println(unparsedResult1);
            String unparsedResult2 = unparsedResult1.split("=====")[1];
            System.out.println(unparsedResult2);
            System.out.println(unparsedResult2.length());
            result = unparsedResult2.getBytes(StandardCharsets.UTF_8);
            System.out.println("len: " + result.length);*/

            result = Arrays.copyOfRange(responseStream.toByteArray(), 0, keySize);
        } catch (Exception e) {
            //System.out.println("ERROR");
            throw new IOException(e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        //System.out.println(command);
        return result;
    }

    /**
     * Función auxiliar para generar el comando
     * @param filename nombre del fichero de la prueba
     * @return comando de ZoKrates
     */
    protected static String[] generateCommand(String filename) {

        String zokrates_path;
        String OS = System.getProperty("os.name");
        if(OS.startsWith("Windows")) {
            zokrates_path = Constants.ZOKRATES_PATH_WINDOWS_SERVER;
        }
        else {
            zokrates_path = Constants.ZOKRATES_PATH_LINUX;
        }

        String command = zokrates_path + " verify -j " + Constants.DEFAULT_PROOF_FOLDER + File.separator + filename;
        return command.split(" ");
    }

    /**
     * Lee el output del proceso
     * @param inputStream stream del proceso
     * @return String con la salida del proceso
     * @throws IOException
     */
    private static String parseOutput(InputStream inputStream) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ( (line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    private static String askForPassword() {
        /*Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }

        console.printf("Testing password%n");
        char[] passwordArray = console.readPassword("Enter your secret password: ");
        return new String(passwordArray);*/

        return "lia2admin$$";
    }

}
