package org.launcher;

import javax.crypto.SecretKey;
import java.io.File;

import static org.launcher.Main.decryptData;
import static org.launcher.Main.encryptData;
import static org.launcher.utils.FileUtils.*;
import static org.launcher.utils.KeyUtils.loadAESKey;

public class Cipher {

    private String base64Key;

    public static void processFiles(String base64Key) {
        File directory = new File("./raw/");
        File encryptedDirectory = new File("./encrypted/");
        File decryptedDirectory = new File("./decrypted/");

        // Create directories if they don't exist
        if (!encryptedDirectory.exists()) {
            encryptedDirectory.mkdirs();
        }
        if (!decryptedDirectory.exists()) {
            decryptedDirectory.mkdirs();
        }

        // Check if directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("The raw directory doesn't exist or is not a directory.");
            return;
        }

        File[] fileArray = directory.listFiles();
        if (fileArray == null || fileArray.length == 0) {
            System.out.println("No files in the raw directory.");
            return;
        }

        // Process each file
        for (File modelFile : fileArray) {
            String fileName = modelFile.getName();
            try {
                String filePath = modelFile.getAbsolutePath();
                byte[] originalData = readFile(filePath);
                if (originalData == null) return;

                // Encrypt the data
                SecretKey secretKey = loadAESKey(base64Key);
                byte[] encryptedData = encryptData(originalData, secretKey);

                // Save encrypted file immediately
                saveEncryptedFile(encryptedData, "./encrypted/" + fileName);

                // Decrypt the data
                byte[] decryptedData = decryptData(encryptedData, secretKey);

                // Save decrypted file after decryption process
                saveDecryptedFile(decryptedData, "./decrypted/" + fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
