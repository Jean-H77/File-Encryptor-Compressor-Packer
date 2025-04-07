package org.launcher.utils;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;

public final class FileUtils {

    private FileUtils() {}

    public static byte[] readFile(String path, int modelId) {
        try {
            File file = new File(path);
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length() + 8);
            buffer.putInt(modelId);
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                byte[] bytes = dis.readAllBytes();
                buffer.putInt(bytes.length);
                buffer.put(bytes);
            }
            return buffer.array();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving encrypted file: " + path + "::" + e);
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
            System.out.println("Saved encrypted file: " + outputPath);
        } catch (IOException e) {
            System.out.println("Error saving encrypted file: " + outputPath);
            JOptionPane.showMessageDialog(null, "Error saving encrypted file: " + outputPath + "::" + e);
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

    public static boolean exists(String path) {
        return new File(path).exists();
    }
}
