package org.john.core.AES;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.util.Arrays;

public class AESInfo {

    private String key;
    private int keySize;
    private String ivHex;
    private String cipher;
    private String keyFormat;
    private String paddingMode;

    private transient SecretKey secretKey;
    private transient Cipher cipherInstance;

    public AESInfo() {

    }

    public AESInfo(String key, int keySize, String ivHex, String cipher, String keyFormat, String paddingMode) {
        this.key = key;
        this.keySize = keySize;
        this.ivHex = ivHex;
        this.cipher = cipher;
        this.keyFormat = keyFormat;
        this.paddingMode = paddingMode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIvHex() {
        return ivHex;
    }

    @JsonIgnore
    public byte[] getIvBytes() throws DecoderException {
        return Hex.decodeHex(ivHex);
    }

    public void setIvHex(String ivHex) {
        this.ivHex = ivHex;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getPaddingMode() {
        return paddingMode;
    }

    public void setPaddingMode(String paddingMode) {
        this.paddingMode = paddingMode;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    @JsonIgnore
    public SecretKey getSecretKey() {
        return secretKey;
    }

    @JsonIgnore
    public Cipher getCipherInstance() {
        return cipherInstance;
    }

    public void initSecretKeyAndCipher()  {
        byte[] decodedKey;

        try {
            if (keyFormat.equals("HEX")) {
                decodedKey = Hex.decodeHex(key);
            } else {
                decodedKey = Base64.decodeBase64(key);
            }

            System.out.println("IVKEY: "  + Arrays.toString(Hex.decodeHex(ivHex)));
            secretKey = new SecretKeySpec(decodedKey, "AES");

            var transformation = new StringBuilder();
            transformation.append("AES/");
            transformation.append(cipher);
            transformation.append("/");
            transformation.append(paddingMode);

            cipherInstance = Cipher.getInstance(transformation.toString());
            cipherInstance.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(Hex.decodeHex(ivHex)));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
