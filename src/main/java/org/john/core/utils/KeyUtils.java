package org.john.core.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class KeyUtils {
    private KeyUtils() {}

    public static SecretKey loadAESKey(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static String GenerateBase64Key() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static String getOrGenerateKey(String inputPath) {
        String key;
        String path = inputPath + "/key.pem";
        try {
            if(!FileUtils.exists(path)) {
                key = KeyUtils.GenerateBase64Key();
                FileUtils.saveString(key, path);
            } else {
                key = FileUtils.readBase64Key(inputPath);
            }

            return key;
        } catch (NoSuchAlgorithmException | IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error Generating key", JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }
}
