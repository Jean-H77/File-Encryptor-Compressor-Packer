package org.john.core.config;

import java.nio.file.Path;

public class Config {

    private String encryptionKey;
    private String outputDir;
    private String inputDir;

    private boolean pack;
    private boolean includeFileName;
    private boolean includeFileLength;
    private boolean compress;

    private static Config instance;

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public boolean isIncludeFileName() {
        return includeFileName;
    }

    public void setIncludeFileName(boolean includeFileName) {
        this.includeFileName = includeFileName;
    }

    public boolean isIncludeFileLength() {
        return includeFileLength;
    }

    public void setIncludeFileLength(boolean includeFileLength) {
        this.includeFileLength = includeFileLength;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isPack() {
        return pack;
    }

    public void setPack(boolean pack) {
        this.pack = pack;
    }

    public static Config getInstance() {
        if(instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
