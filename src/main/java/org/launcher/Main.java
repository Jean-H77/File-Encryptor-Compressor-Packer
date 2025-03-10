package org.launcher;

import org.launcher.utils.FileUtils;
import org.launcher.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main extends JFrame {

    private static String key;

    public static void main(String[] args) throws Exception {
        key = FileUtils.readBase64Key();
        JFrame frame = new JFrame("Devote Model Encryptor");
        frame.setLayout(new FlowLayout());

        JTextField textField = new JTextField(33);
        textField.setText(key);
        frame.add(textField);

        JButton encryptButton = new JButton("Encrypt");
        frame.add(encryptButton);

        JButton generateKeyButton = new JButton("Generate Key");
        frame.add(generateKeyButton);

        generateKeyButton.addActionListener(_ -> {
            try {
                String key = KeyUtils.GenerateBase64Key();
                FileUtils.saveKey(key);
                textField.setText(key);
            } catch (RuntimeException | NoSuchAlgorithmException e) {
                JOptionPane.showMessageDialog(null, "Cannot Generate Key: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Cannot create .pem file: " + e.getMessage());
                e.printStackTrace();
            }
        });

        encryptButton.addActionListener(_ -> {
            String base64Key = textField.getText();
            Encryptor.Result result = null;
            try {
                result = Encryptor.encrypt(base64Key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if(result == Encryptor.Result.SUCCESS) {
                JOptionPane.showMessageDialog(null, "Encryption successfully");
            } else if(result == Encryptor.Result.NO_DIRECTORY) {
                JOptionPane.showMessageDialog(null, "No models directory");
            } else {
                JOptionPane.showMessageDialog(null, "No model files");
            }
        });

        frame.setSize(400, 115);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
