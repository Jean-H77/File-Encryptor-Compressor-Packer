package org.john.core.utils;

import java.io.ByteArrayInputStream;
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

    public static void compressFile(byte[] src, String destinationFilePath) throws IOException {
        try (ByteArrayInputStream fis = new ByteArrayInputStream(src);
             FileOutputStream fos = new FileOutputStream(destinationFilePath);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, length);
            }
        }
    }
}
