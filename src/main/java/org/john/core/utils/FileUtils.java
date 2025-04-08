package org.john.core.utils;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {

    private FileUtils() {}

    public static byte[] readFileToByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + file + "::" + e);
        }
        return new byte[0];
    }

    public static void createDirectory(String path) {
        File encryptedDirectory = new File(path);
        if (!encryptedDirectory.exists()) {
            encryptedDirectory.mkdirs();
        }
    }

    public static void saveFile(byte[] encryptedData, String outputPath) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(encryptedData);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving file: " + outputPath + "::" + e);
        }
    }

    public static void saveString(String contents, String path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(contents);
        }
    }

    public static String readBase64Key(String path) {
        StringBuilder base64Content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path+"/key.pem"))) {
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

    public static boolean isEmpty(String directory) {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(Path.of(directory))) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
