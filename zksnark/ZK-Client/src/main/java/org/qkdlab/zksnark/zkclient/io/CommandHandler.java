package org.qkdlab.zksnark.zkclient.io;


import org.qkdlab.zksnark.model.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * CommandHandler
 *
 * Clase estática para la ejecución de comandos de Bash
 */
public class CommandHandler {
    public static long witnessTime;
    public static long proofTime;

    /**
     * Llana a ZoKrates para crear una prueba zk-SNARK
     * @param folder carpeta donde se encuentra la información
     * @param root raíz del árbol Merkle
     * @param nullifier hash que corresponde al commitment
     * @param commitment hash que se encuentra en el árbol Merkle
     * @param index índice del commitment en el árbol Merkle
     * @param path camino para validar el commitment en el árbol Merkle
     * @param pubKey clave pública
     * @param privKey clave privada
     * @param sigma valor que genera el commitment y el nullifier
     * @throws IOException
     */
    public static void executeZokrates(String folder, long[] root, long[] nullifier, long[] commitment, int index, ArrayList<long[]> path,
                                       long[] pubKey, long[] privKey, long[] sigma) throws  IOException {

        File folderFile = new File(folder);

        String zokrates_path;
        String OS = System.getProperty("os.name");
        if(OS.startsWith("Windows")) {
            zokrates_path = Constants.ZOKRATES_PATH_WINDOWS_CLIENT;
        }
        else {
            zokrates_path = Constants.ZOKRATES_PATH_LINUX;
        }

        String command = zokrates_path + " compute-witness -i MerkleTreeCommitment -a";
        command = concatArgToCommand(command, root);
        command = concatArgToCommand(command, nullifier);
        command = concatArgToCommand(command, commitment);
        command += " "   + index;
        for (long[] pathHash : path) {
            command = concatArgToCommand(command, pathHash);
        }
        command = concatArgToCommand(command, pubKey);
        command = concatArgToCommand(command, privKey);
        command = concatArgToCommand(command, sigma);
        System.out.println("----WITNESS COMMAND----");
        System.out.println(command);

        String[] commands = command.split(" ");
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.inheritIO();

        File errorFile = new File(folderFile, "error.txt");
        pb.redirectError(errorFile);

        /*File nullFile = new File(
                (System.getProperty("os.name")
                        .startsWith("Windows") ? "NUL" : "/dev/null"));
        pb.redirectError(nullFile);*/

        pb.directory(folderFile);

        long startTime = System.nanoTime();
        Process proc = pb.start();
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        witnessTime += System.nanoTime() - startTime;

        String command2 = zokrates_path + " generate-proof -i MerkleTreeCommitment";
        //command2 += " -s gm17";
        System.out.println("----PROOF GENERATION COMMAND----");
        System.out.println(command2);
        String[] commands2 = command2.split(" ");

        pb = new ProcessBuilder(commands2);
        pb.inheritIO();
        pb.directory(folderFile);

        startTime = System.nanoTime();
        Process proc2 = pb.start();
        try {
            proc2.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        proofTime += System.nanoTime() - startTime;
    }

    /**
     * Función auxiliar que añade todos los elementos de una lista al comando
     * @param command estado actual del comando
     * @param array elementos a añadir al comando
     * @return comando con los elementos añadidos
     */
    private static String concatArgToCommand(String command, long[] array) {
        for (long r : array) {
            command += " " + r;
        }
        return command;
    }

    public static void executeLibsnark(String folder, String root, String nullifier, String commitment, int index, ArrayList<String> path,
                                       String pubKey, String privKey, String sigma) throws IOException {

        File folderFile = new File(folder);

        String[] commands = null;
        String OS = System.getProperty("os.name");
        if (OS.startsWith("Windows")) {
            String[] cygwinCommands = {"C:\\cygwin64\\bin\\bash", "--login", "-c",  " "};

            String libsnarkPath = "cd " + toCygwinPath(folder) + "; ./" + Constants.LIBSNARK_PATH_WINDOWS_CLIENT;
            StringBuilder command = new StringBuilder();
            command.append(libsnarkPath + " prove ");
            command.append(root + " ");
            command.append(nullifier + " ");
            command.append(commitment + " ");
            command.append(pubKey + " ");
            command.append(privKey + " ");
            command.append(sigma + " ");
            command.append(index + " ");
            for (String pathHash : path) {
                command.append(pathHash + " ");
            }

            cygwinCommands[3] = "\"" + command.toString() + "\"";
            commands = cygwinCommands;
        } else {
            // Corregir en el futuro cuando se ejecute en linux
            // libsnark_path = Constants.ZOKRATES_PATH_LINUX;
            System.out.println("Under construction...");
            System.exit(1);
            //libsnark_path = Constants.LIBSNARK_PATH_WINDOWS_CLIENT;
        }

        System.out.println(String.join(" ", commands));

        ProcessBuilder pb = new ProcessBuilder(commands);
        //pb.inheritIO();

        File errorFile = new File(folderFile, "error.txt");
        pb.redirectError(errorFile);

        /*File nullFile = new File(
                (System.getProperty("os.name")
                        .startsWith("Windows") ? "NUL" : "/dev/null"));
        pb.redirectError(nullFile);*/

        pb.directory(folderFile);

        long startTime = System.nanoTime();
        Process proc = pb.start();
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        proofTime += System.nanoTime() - startTime;
    }

    private static String toCygwinPath(String folder) {
        String[] pathFolders = folder.split("\\\\");
        StringBuilder builder = new StringBuilder("/cygdrive/c/");

        for(int i = 1; i < pathFolders.length; i++) {
            builder.append(pathFolders[i]);
            builder.append("/");
        }

        return builder.toString();
    }
}
