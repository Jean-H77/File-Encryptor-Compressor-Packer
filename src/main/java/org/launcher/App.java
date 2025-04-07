package org.launcher;

import org.launcher.utils.FileUtils;
import org.launcher.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class App {

    private final JFrame frame;
    private final JTextField keyTextField;

    private String key;

    public App(String title) {
        frame = new JFrame(title);
        keyTextField = new JTextField(33);

        try {
            if(!FileUtils.exists("./key.pem")) {
                key = KeyUtils.GenerateBase64Key();
                FileUtils.saveKey(key);
            } else {
                key = FileUtils.readBase64Key();
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        create();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void create() {
        frame.setLayout(new FlowLayout());

        keyTextField.setText(key);
        frame.add(keyTextField);

        JButton encryptButton = new JButton("Encrypt");
        encryptButton.addActionListener(eKeyActionListener());
        frame.add(encryptButton);

        JButton generateKeyButton = new JButton("Generate Key");
        generateKeyButton.addActionListener(gKeyActionListener());
        frame.add(generateKeyButton);

        frame.setSize(400, 115);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private ActionListener gKeyActionListener() {
        return _ -> {
            try {
                String newKey = KeyUtils.GenerateBase64Key();
                FileUtils.saveKey(newKey);
                keyTextField.setText(newKey);
            } catch (IOException | NoSuchAlgorithmException e) {
                JOptionPane.showMessageDialog(null, "Cannot create .pem file: " + e.getMessage());
            }
        };
    }

    private ActionListener eKeyActionListener() {
        return _ -> {
            String base64Key = keyTextField.getText();
            Encryptor.Result result;
            try {
                result = Encryptor.encrypt(base64Key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if(result == Encryptor.Result.SUCCESS) {
                JOptionPane.showMessageDialog(null, "Encryption successfully");
            } else if(result == Encryptor.Result.NO_DIRECTORY) {
                JOptionPane.showMessageDialog(null, "No models directory");
            } else if(result == Encryptor.Result.NO_FILES) {
                JOptionPane.showMessageDialog(null, "No model files");
            } else {
                JOptionPane.showMessageDialog(null, "Encryption failed");
            }
        };
    }
}
