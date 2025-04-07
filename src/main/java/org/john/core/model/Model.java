package org.john.core.model;

import org.john.core.utils.FileUtils;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {
    final int id;
    final byte[] data;

    public Model(int id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public static Model decode(File file) {
        var buffer = FileUtils.readFileToByteArray(file);
        int id;

        try {
            id = Integer.parseInt(file.getName().replace(".dat", ""));
        } catch (NumberFormatException ignore) {
            return null;
        }

        return new Model(id, buffer);
    }

    public byte[] encode() {
        int length = data.length;
        var buffer = ByteBuffer.allocate(length + 8);
        buffer.putInt(id);
        buffer.putInt(length);
        buffer.put(data);
        return buffer.array();
    }

    public static List<Model> getModelList(String path) {
        var directory = new File(path);
        var models = new ArrayList<Model>();

        if(!directory.exists()) {
            JOptionPane.showMessageDialog(null, "Directory does not exist");
            return models;
        }

        if(FileUtils.isEmpty(path)) {
            JOptionPane.showMessageDialog(null, "Empty path");
            return models;
        }

        var fileArray = directory.listFiles();
        assert fileArray != null;
        for (var modelFile : fileArray) {
            if(!modelFile.getName().endsWith(".dat")) continue;
            var model = decode(modelFile);
            if(model != null) models.add(model);
        }

        return models;
    }

    public static byte[] pack(List<Model> models) {;
        var buffer = new ByteArrayOutputStream();

        for(var model : models)
            buffer.writeBytes(model.encode());

        return buffer.toByteArray();
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
