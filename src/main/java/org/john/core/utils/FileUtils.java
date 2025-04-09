package org.john.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS);

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

    public static <T> T jsonFromFile(File file, Class<T> clazz) throws IOException {
        return mapper.readValue(file, clazz);
    }

    public static void jsonToFile(File file, Object obj) throws IOException {
        mapper.writeValue(file, obj);
    }
}
