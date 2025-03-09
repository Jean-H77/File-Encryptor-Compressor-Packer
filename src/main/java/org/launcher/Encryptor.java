package org.launcher;

import javax.crypto.SecretKey;
import java.io.File;

import static org.launcher.utils.FileUtils.*;
import static org.launcher.utils.KeyUtils.loadAESKey;

public class Encryptor {
    public enum Result { NO_DIRECTORY, NO_FILES, SUCCESS}

    public static byte[] encryptData(byte[] data, SecretKey key) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decryptData(byte[] encryptedData, SecretKey key) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    public static Result encrypt(String base64Key) {
        File directory = new File("./models/");

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("The models directory doesn't exist or is not a directory.");
            return Result.NO_DIRECTORY;
        }

        File[] fileArray = directory.listFiles();
        if (fileArray == null || fileArray.length == 0) {
            System.out.println("No files in the raw directory.");
            return Result.NO_FILES;
        }

        createDirectory("./encrypted/");

        SecretKey secretKey = loadAESKey(base64Key);
        for (File modelFile : fileArray) {
            String fileName = modelFile.getName();
            try {
                String filePath = modelFile.getAbsolutePath();
                byte[] originalData = readFile(filePath);
                if (originalData == null) continue;

                byte[] encryptedData = encryptData(originalData, secretKey);

                saveEncryptedFile(encryptedData, "./encrypted/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Result.SUCCESS;
    }
}
