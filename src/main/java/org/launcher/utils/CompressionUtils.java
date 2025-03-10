package org.launcher.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class CompressionUtils {
    private CompressionUtils() {}

    public static void compressFile(String sourceFilePath, String destinationFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFilePath);
             FileOutputStream fos = new FileOutputStream(destinationFilePath);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, length);
            }
        }
    }

    public static void decompressFile(String inputFilePath, String outputFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipIS.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

}
