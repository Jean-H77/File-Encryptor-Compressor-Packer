package org.launcher.utils;

import javax.swing.*;
import java.io.*;

public final class FileUtils {
    private FileUtils() {}

    public static byte[] readFile(String path) {
        try {
            File file = new File(path);
            byte[] data = new byte[(int) file.length()];
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                dis.readFully(data);
            }
            return data;
        } catch (IOException e) {
            System.out.println("Error reading file: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public static void createDirectory(String path) {
        File encryptedDirectory = new File(path);

        if (!encryptedDirectory.exists()) {
            encryptedDirectory.mkdirs();
        }
    }

    public static void saveEncryptedFile(byte[] encryptedData, String outputPath) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(encryptedData);
            System.out.println("Encrypted file saved to: " + outputPath);
        } catch (IOException e) {
            System.out.println("Error saving encrypted file: " + outputPath);
            e.printStackTrace();
        }
    }

    public static void saveDecryptedFile(byte[] decryptedData, String outputPath) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(decryptedData);
            System.out.println("Decrypted file saved to: " + outputPath);
        } catch (IOException e) {
            System.out.println("Error saving decrypted file: " + outputPath);
            e.printStackTrace();
        }
    }

    public static void saveKey(String key) throws IOException {
        try (FileWriter fileWriter = new FileWriter("./key.pem")) {
            fileWriter.write(key);
        }
    }

    public static String readBase64Key() {
        StringBuilder base64Content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("./key.pem"))) {
            String line = br.readLine();
            if (line != null) {
                base64Content.append(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading the PEM file.");
            JOptionPane.showMessageDialog(null, "Error reading the PEM file.");
            return null;
        }

        return base64Content.toString();
    }

}
