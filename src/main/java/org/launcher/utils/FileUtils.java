package org.launcher.utils;

import javax.swing.*;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class FileUtils {
    private FileUtils() {}

    public static byte[] readFile(String path, int modelId) {
        try {
            File file = new File(path);
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length() + 8);
           // byte[] data = new byte[(int) file.length() + 3];
            buffer.putInt(modelId);
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                byte[] bytes = dis.readAllBytes();
                buffer.putInt(bytes.length);
                buffer.put(bytes);
                System.out.println("Put: " + modelId + " length: " + bytes.length);
            }
            return buffer.array();
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
