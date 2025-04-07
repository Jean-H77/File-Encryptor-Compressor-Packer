package org.launcher;

import org.launcher.utils.CompressionUtils;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.launcher.utils.FileUtils.*;
import static org.launcher.utils.KeyUtils.loadAESKey;

public class Encryptor {
    public enum Result { NO_DIRECTORY, NO_FILES, SUCCESS}

    private static final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public static byte[] encryptData(byte[] data, SecretKey key) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static Result encrypt(String base64Key) throws Exception {
        File directory = new File("./models/");

        if (!directory.exists() || !directory.isDirectory()) {
            return Result.NO_DIRECTORY;
        }

        File[] fileArray = directory.listFiles();
        if (fileArray == null || fileArray.length == 0) {
            return Result.NO_FILES;
        }

        createDirectory("./encrypted/");

        for (File modelFile : fileArray) {
            String fileName = modelFile.getName();
            try {
                String filePath = modelFile.getAbsolutePath();
                byte[] originalData = readFile(filePath,Integer.parseInt(fileName.replace(".dat", "")));

                if (originalData == null) continue;
                buffer.writeBytes(originalData);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error Encrypting: " + e);
            }
        }

        SecretKey secretKey = loadAESKey(base64Key);
        System.out.println(Base64.getEncoder().encodeToString(base64Key.getBytes()));
        byte[] encryptedData = encryptData(buffer.toByteArray(), secretKey);
        saveEncryptedFile(encryptedData, "./encrypted/cache_m0_temp.dat");

        CompressionUtils.compressFile("./encrypted/cache_m0_temp.dat", "./encrypted/cache_m0.dat");
        new File("./encrypted/cache_m0_temp.dat").delete();
        return Result.SUCCESS;
    }
}
