package org.john.core;

import org.john.core.model.Model;
import org.john.core.utils.CompressionUtils;
import org.john.core.utils.FileUtils;
import org.john.core.utils.KeyUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.john.core.utils.FileUtils.*;
import static org.john.core.utils.KeyUtils.loadAESKey;

public class App  {

    private final JFrame frame;
    private final JTextField keyTextField;
    private final JTextArea unEncryptedModelsTextArea;
    private final JTextArea encryptedModelsTextArea;
    private final JTextField outputTextField;
    private final JTextField modelsTextField;
    private final JButton encryptButton;

    private String encKey;
    private List<Model> loadedModels;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public App(String title, int width, int height) {
        encKey = KeyUtils.getOrGenerateKey();

        frame = new JFrame(title);
        frame.setSize(width, height);

        outputTextField = new JTextField();
        outputTextField.setEditable(false);

        modelsTextField = new JTextField();
        modelsTextField.setEditable(false);

        unEncryptedModelsTextArea = new JTextArea(0, 1);
        unEncryptedModelsTextArea.setEditable(false);

        encryptedModelsTextArea = new JTextArea(0, 1);
        encryptedModelsTextArea.setEditable(false);

        encryptButton = new JButton("Encrypt");
        encryptButton.setEnabled(false);

        keyTextField = new JTextField(45);
        keyTextField.setText(encKey);
        keyTextField.setEditable(false);
        create();
    }

    private void create() {
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(contentPanel);

        contentPanel.add(getModelsPanel(), BorderLayout.CENTER);
        contentPanel.add(getInformationPanel(), BorderLayout.NORTH);

        encryptButton.addActionListener(_ -> encryptAction());
        contentPanel.add(encryptButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel getInformationPanel() {
        JPanel informationPanel = new JPanel(new GridBagLayout());
        informationPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        informationPanel.add(new JLabel("Enc. Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        keyTextField.setText(encKey);
        informationPanel.add(keyTextField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton generateKeyButton = new JButton("New Key");
        generateKeyButton.addActionListener(_ -> generateKeyAction());
        informationPanel.add(generateKeyButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        informationPanel.add(new JLabel("Output Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        informationPanel.add(outputTextField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JButton outputButton = new JButton("Browse...");
        outputButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(informationPanel) == JFileChooser.APPROVE_OPTION) {
                if(!FileUtils.isEmpty(chooser.getSelectedFile().getAbsolutePath())) {
                    JOptionPane.showMessageDialog(null, "Output path must be empty");
                    return;
                }

                outputTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        informationPanel.add(outputButton, gbc);


        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        informationPanel.add(new JLabel("Models Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        informationPanel.add(modelsTextField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JButton modelsButton = new JButton("Browse...");
        modelsButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(informationPanel) == JFileChooser.APPROVE_OPTION) {
                if(FileUtils.isEmpty(chooser.getSelectedFile().getAbsolutePath())) {
                    JOptionPane.showMessageDialog(null, "Models path cannot be empty");
                    return;
                }

                modelsTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                loadedModels = Model.getModelList(modelsTextField.getText());

                for(var m : loadedModels) { //C:\Users\jeanh\Desktop\Encryptor\models
                    unEncryptedModelsTextArea.append(m.getId() + ".dat\n"); // Adding .dat for visual only, checking if extension is .dat beforehand
                }

                encryptButton.setEnabled(true);
            }
        });
        informationPanel.add(modelsButton, gbc);

        return informationPanel;
    }

    private JPanel getModelsPanel() {
        JPanel modelsPanel = new JPanel(new GridLayout(1, 1, 1, 0));

        JScrollPane uMScrollPane = new JScrollPane(unEncryptedModelsTextArea);
        uMScrollPane.setBorder(BorderFactory.createTitledBorder("Models"));

        modelsPanel.add(uMScrollPane);
        return modelsPanel;
    }

    private void generateKeyAction() {
        try {
            encKey = KeyUtils.GenerateBase64Key();
            FileUtils.saveString(encKey, "./key.pem");
            keyTextField.setText(encKey);
        } catch (IOException | NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "Cannot create .pem file: " + e.getMessage());
        }
    }

    private void encryptAction() {
        encryptButton.setEnabled(false);
        var buffer = Model.pack(loadedModels);
        String outputPath = outputTextField.getText();

        if(!FileUtils.exists(outputPath)) {
            JOptionPane.showMessageDialog(null, "Cannot find output path: " + outputPath);
            return;
        }

        String packedPath = outputPath + "/packed";
        FileUtils.createDirectory(packedPath);

        String uncompressedFile = packedPath + "/models_uncompressed.dat";
        saveFile(encryptData(buffer, encKey), uncompressedFile);

        try {
            String compressedFile = packedPath + "/models_compressed.dat";
            CompressionUtils.compressFile(uncompressedFile, compressedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String unPacked = outputPath + "/unpacked";
        createDirectory(unPacked);
        for(var m : loadedModels) {
            saveFile(encryptData(m.getData(), encKey), unPacked + "/" + m.getId() + ".dat");
        }

        JOptionPane.showMessageDialog(null, "Encrypted Models successfully");
        encryptButton.setEnabled(true);
    }

    private static byte[] encryptData(byte[] data, String key)  {
        try {
            var secretKey = loadAESKey(key);
            var cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
