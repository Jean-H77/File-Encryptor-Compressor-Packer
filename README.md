# 🔐 Model Encryptor

Each model entry in the byte buffer is structured as:

| Field       | Size    | Description                         |
|-------------|---------|-------------------------------------|
| Model ID    | 4 bytes | Unique integer identifier for model |
| Data Length | 4 bytes | Length of the model data in bytes   |
| Model Data  | N bytes | Raw or compressed byte data         |

📌 **Total entry size** = 8 bytes + N bytes  
N is determined by the `Data Length` field.

## 🚀 Usage Example

```java

 public static byte[] decryptData(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
}

public static void decode(File file) {
    // Optional: Decompress if data is compressed
    byte[] encryptedBytes = Files.readAllBytes(file.toPath());
    byte[] decryptedBytes = decryptData(encryptedBytes, /*SECRET_KEY_HERE*/);
    ByteBuffer buf = ByteBuffer.wrap(buffer);

    while (buf.hasRemaining()) {
        int modelId = buf.getInt();
        int length = buf.getInt();
        byte[] modelData = new byte[length];
        buf.get(modelData);
        
        Model.readModelData(modelData, length, modelId);
    }
}

```

## 🗜️ Decompression Utility

```java
    public static byte[] decompress(byte[] compressedData) throws IOException {
    try (
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    ) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray(); 
    }
}
```




