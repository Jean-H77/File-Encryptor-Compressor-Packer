package org.john.core;

import org.apache.commons.io.FilenameUtils;
import org.john.core.InputFile.PackedInputFileTableViewer;
import org.john.core.config.Config;
import org.john.core.InputFile.InputFile;
import org.john.core.utils.CompressionUtils;
import org.john.core.utils.FileUtils;
import org.john.core.utils.KeyUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.john.core.utils.FileUtils.*;
import static org.john.core.utils.KeyUtils.loadAESKey;

public class AppGUI {

    private final JFrame frame;
    private final JTextField keyTextField;
    private final JTextArea unEncryptedModelsTextArea;
    private final JTextField outputTextField;
    private final JTextField inputTextField;
    private final JButton encryptButton;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public AppGUI(String title, int width, int height) {
        frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        URL iconUrl = getClass().getResource("/icon.png");
        assert iconUrl != null;
        ImageIcon icon = new ImageIcon(iconUrl);
        frame.setIconImage(icon.getImage());

        outputTextField = new JTextField();
        outputTextField.setEditable(false);

        inputTextField = new JTextField();
        inputTextField.setEditable(false);

        unEncryptedModelsTextArea = new JTextArea(0, 1);
        unEncryptedModelsTextArea.setEditable(false);

        encryptButton = new JButton("Encrypt");
        encryptButton.setEnabled(false);

        keyTextField = new JTextField(45);
        keyTextField.setEditable(false);
        keyTextField.setHighlighter(null);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(contentPanel);

        contentPanel.add(getFilesPanel(), BorderLayout.CENTER);
        contentPanel.add(getInformationPanel(), BorderLayout.NORTH);

        encryptButton.addActionListener(_ -> encryptAction());
        contentPanel.add(encryptButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel getInformationPanel() {
        JPanel informationPanel = new JPanel(new GridBagLayout());
        informationPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        informationPanel.add(new JLabel("Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        keyTextField.setText(Config.getInstance().getEncryptionKey());
        informationPanel.add(keyTextField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton generateKeyButton = new JButton("Generate Key");
        generateKeyButton.addActionListener(_ -> generateKeyAction());
        informationPanel.add(generateKeyButton, gbc);

        gbc.gridx = 3;
        JButton loadKeyButton = new JButton("Load Key");
        loadKeyButton.addActionListener(_ -> showKeyFileChooser());
        informationPanel.add(loadKeyButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        informationPanel.add(new JLabel("Output Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        informationPanel.add(outputTextField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        JButton outputButton = new JButton("Browse...");
        outputButton.addActionListener(_ -> showOutputDirectoryChooser());
        informationPanel.add(outputButton, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        informationPanel.add(new JLabel("Input Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        informationPanel.add(inputTextField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        JButton modelsButton = new JButton("Browse...");
        modelsButton.addActionListener(_ -> showInputDirectoryChooser());
        informationPanel.add(modelsButton, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        JCheckBox includeFileName = new JCheckBox("Include file name", false);
        includeFileName.addActionListener(_ -> Config.getInstance().setIncludeFileName(includeFileName.isSelected()));
        includeFileName.setEnabled(false);
        informationPanel.add(includeFileName, gbc);

        gbc.gridx = 1;
        JCheckBox includeFileLength = new JCheckBox("Include file length", false);
        includeFileLength.addActionListener(_ -> Config.getInstance().setIncludeFileLength(includeFileLength.isSelected()));
        includeFileLength.setEnabled(false);
        informationPanel.add(includeFileLength, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JCheckBox pack = new JCheckBox("Pack", false);
        pack.addActionListener(_ -> {
            if(pack.isSelected()) {
                Config.getInstance().setPack(true);
                includeFileName.setEnabled(true);
                includeFileLength.setEnabled(true);
            } else {
                Config.getInstance().setPack(false);
                includeFileName.setEnabled(false);
                includeFileLength.setEnabled(false);
            }
        });
        informationPanel.add(pack, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JCheckBox compress = new JCheckBox("GZIP Compress", false);
        compress.addActionListener(_ -> Config.getInstance().setCompress(compress.isSelected()));
        informationPanel.add(compress, gbc);

        return informationPanel;
    }

    private void showKeyFileChooser() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.pem", "pem");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            var bytes = readFileToByteArray(chooser.getSelectedFile());
            var key = new String(bytes);
            keyTextField.setText(key);
            Config.getInstance().setEncryptionKey(key);
            enableEncryptButton();
        }
    }

    private void showOutputDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!FileUtils.isEmpty(path)) {
                JOptionPane.showMessageDialog(null, "Output path must be empty");
                return;
            }
            Config.getInstance().setOutputDir(path);
            outputTextField.setText(path);
            enableEncryptButton();
        }
    }

    private void showInputDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (FileUtils.exists(path) && FileUtils.isEmpty(path)) {
                JOptionPane.showMessageDialog(null, "Directory cannot be empty");
                return;
            }
            Config.getInstance().setInputDir(path);
            var loadedInputFiles = InputFile.decodeAllInDir();
            InputFile.getLoadedFiles().addAll(loadedInputFiles);

            unEncryptedModelsTextArea.setText("");
            for (var m : loadedInputFiles) {
                unEncryptedModelsTextArea.append(m.getName() + "\n");
            }

            enableEncryptButton();
            inputTextField.setText(path);
        }
    }

    private void showConfigFileChooser(boolean isSave) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.json", "json");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = isSave ? chooser.showSaveDialog(frame) : chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!FileUtils.isEmpty(path)) {
                JOptionPane.showMessageDialog(null, "Output path must be empty");
                return;
            }
            outputTextField.setText(path);
        }
    }

    private JPanel getFilesPanel() {
        JPanel modelsPanel = new JPanel(new GridLayout(1, 1, 1, 0));

        JScrollPane uMScrollPane = new JScrollPane(unEncryptedModelsTextArea);
        uMScrollPane.setBorder(BorderFactory.createTitledBorder("Files"));

        modelsPanel.add(uMScrollPane);
        return modelsPanel;
    }

    private void generateKeyAction() {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("key.pem"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.pem", ".pem");
            chooser.setFileFilter(filter);
            int opt = chooser.showSaveDialog(frame);
            if(opt == JFileChooser.APPROVE_OPTION) {
                var file = chooser.getSelectedFile();
                if(!file.getName().endsWith(".pem")) {
                    file = new File(file.getAbsolutePath() + ".pem");
                }
                try(FileWriter fw = new FileWriter(file)) {
                    String newKey = KeyUtils.GenerateBase64Key();
                    fw.write(newKey);
                    keyTextField.setText(newKey);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Cannot create .pem file: " + e.getMessage());
                }
            }
    }

    private void enableEncryptButton() {
        var config = Config.getInstance();
        if(config.getInputDir() != null && config.getOutputDir() != null && config.getEncryptionKey() != null) {
            encryptButton.setEnabled(true);
        }
    }

    private void encryptAction() {
        var config = Config.getInstance();

        if (config.getEncryptionKey() == null) {
            showError("Encryption Key cannot be empty");
            return;
        }

        encryptButton.setEnabled(false);
        String outputPath = String.valueOf(config.getOutputDir());

        if (!FileUtils.exists(outputPath)) {
            showError("Cannot find output path: " + outputPath);
            return;
        }

        String unpackedPath = outputPath + "/unpacked/";
        String packedPath = outputPath + "/packed/";

        if (!config.isPack()) {
            processIndividualFiles(unpackedPath);
        } else {
            processPackedFiles(unpackedPath);
        }

        JOptionPane.showMessageDialog(null, "Encrypted Models successfully");
        encryptButton.setEnabled(true);
    }

    private void processIndividualFiles(String unpackedPath) {
        createDirectory(unpackedPath);
        var loadedInputFiles = InputFile.getLoadedFiles();

        for (var inputFile : loadedInputFiles) {
            byte[] encryptedData = encryptData(inputFile.getData());

            if (Config.getInstance().isCompress()) {
                try {
                    String compressedName = FilenameUtils.removeExtension(inputFile.getName()) + ".gz";
                    CompressionUtils.compressFile(encryptedData, unpackedPath + compressedName);
                } catch (Exception e) {
                    showError("Cannot compress file: " + e.getMessage());
                }
            } else {
                saveFile(encryptedData, unpackedPath + inputFile.getName());
            }
        }
    }

    private void processPackedFiles(String packedPath) {
        byte[] buffer = InputFile.packLoadedFiles();
        byte[] encryptedData = encryptData(buffer);
        createDirectory(packedPath);

        if (Config.getInstance().isCompress()) {
            try {
                CompressionUtils.compressFile(encryptedData, packedPath + "packed.gz");
            } catch (RuntimeException | IOException e) {
                showError("Cannot pack: " + e.getMessage());
            }
        } else {
            saveFile(encryptedData, packedPath + "packed.dat");
        }

        new PackedInputFileTableViewer();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private static byte[] encryptData(byte[] data)  {
        try {
            var key = Config.getInstance().getEncryptionKey();
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
