package org.john.core.GUI;

import org.apache.commons.io.FilenameUtils;
import org.john.core.AES.AESInfo;
import org.john.core.context.Context;
import org.john.core.InputFile.InputFile;
import org.john.core.utils.CompressionUtils;
import org.john.core.utils.FileUtils;

import javax.crypto.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.john.core.utils.FileUtils.*;

public class AppGUI extends JFrame {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final JTextField keyTextField;
    private final JTextArea unEncryptedModelsTextArea;
    private final JTextField outputTextField;
    private final JTextField inputTextField;
    private final JButton encryptButton;

    public AppGUI(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        URL iconUrl = getClass().getResource("/icon.png");
        assert iconUrl != null;
        ImageIcon icon = new ImageIcon(iconUrl);
        setIconImage(icon.getImage());

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
        add(contentPanel);

        contentPanel.add(getFilesPanel(), BorderLayout.CENTER);
        contentPanel.add(getInformationPanel(), BorderLayout.NORTH);

        encryptButton.addActionListener(_ -> encryptAction());
        contentPanel.add(encryptButton, BorderLayout.SOUTH);

        setVisible(true);
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
        informationPanel.add(keyTextField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton generateKeyButton = new JButton("Generate Key");
        generateKeyButton.addActionListener(_ -> KeyGeneratorForm.open());
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
        includeFileName.addActionListener(_ -> Context.getInstance().setIncludeFileName(includeFileName.isSelected()));
        includeFileName.setEnabled(false);
        informationPanel.add(includeFileName, gbc);

        gbc.gridx = 1;
        JCheckBox includeFileLength = new JCheckBox("Include file length", false);
        includeFileLength.addActionListener(_ -> Context.getInstance().setIncludeFileLength(includeFileLength.isSelected()));
        includeFileLength.setEnabled(false);
        informationPanel.add(includeFileLength, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JCheckBox pack = new JCheckBox("Pack", false);
        pack.addActionListener(_ -> {
            if (pack.isSelected()) {
                Context.getInstance().setPack(true);
                includeFileName.setEnabled(true);
                includeFileLength.setEnabled(true);
            } else {
                Context.getInstance().setPack(false);
                includeFileName.setEnabled(false);
                includeFileLength.setEnabled(false);
            }
        });
        informationPanel.add(pack, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JCheckBox compress = new JCheckBox("GZIP Compress", false);
        compress.addActionListener(_ -> Context.getInstance().setCompress(compress.isSelected()));
        informationPanel.add(compress, gbc);

        return informationPanel;
    }

    private void showKeyFileChooser() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.json", "json");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                var aesInfo = FileUtils.jsonFromFile(chooser.getSelectedFile(), AESInfo.class);
                keyTextField.setText(aesInfo.getKey().toUpperCase());
                Context.getInstance().setAESInfo(aesInfo);
            } catch (IOException e) {
                showPopupMessage(e.getMessage());
            }
            enableEncryptButton();
        }
    }

    private void showOutputDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!FileUtils.isEmpty(path)) {
                showPopupMessage("Output path must be empty");
                return;
            }
            Context.getInstance().setOutputDir(path);
            outputTextField.setText(path);
            enableEncryptButton();
        }
    }

    private void showInputDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (FileUtils.exists(path) && FileUtils.isEmpty(path)) {
                showPopupMessage("Directory cannot be empty");
                return;
            }
            Context.getInstance().setInputDir(path);
            var loadedInputFiles = InputFile.decodeAllInDir();

            unEncryptedModelsTextArea.setText("");
            for (var m : loadedInputFiles) {
                unEncryptedModelsTextArea.append(m.name() + "\n");
            }

            enableEncryptButton();
            inputTextField.setText(path);
        }
    }

    private JPanel getFilesPanel() {
        JPanel modelsPanel = new JPanel(new GridLayout(1, 1, 1, 0));

        JScrollPane uMScrollPane = new JScrollPane(unEncryptedModelsTextArea);
        uMScrollPane.setBorder(BorderFactory.createTitledBorder("Files"));

        modelsPanel.add(uMScrollPane);
        return modelsPanel;
    }


    private void enableEncryptButton() {
        var context = Context.getInstance();
        if (context.getInputDir() != null && context.getOutputDir() != null && context.getKeyAndCipher() != null) {
            encryptButton.setEnabled(true);
        }
    }

    private void encryptAction() {
        var config = Context.getInstance();

        if (config.getKeyAndCipher() == null) {
            showPopupMessage("Encryption Key and Cipher cannot be empty");
            return;
        }

        encryptButton.setEnabled(false);
        String outputPath = String.valueOf(config.getOutputDir());

        if (!FileUtils.exists(outputPath)) {
            showPopupMessage("Cannot find output path: " + outputPath);
            return;
        }

        var unpackedPath = outputPath + "/unpacked/";
        var packedPath = outputPath + "/packed/";

        if (!config.isPack()) {
            processIndividualFiles(unpackedPath);
        } else {
            processPackedFiles(packedPath);
        }

        showPopupMessage("Encrypted Models successfully");
        encryptButton.setEnabled(true);
    }

    private void processIndividualFiles(String unpackedPath) {
        createDirectory(unpackedPath);
        var loadedInputFiles = InputFile.getLoadedFiles();

        for (var inputFile : loadedInputFiles) {
            byte[] encryptedData = encryptData(inputFile.data());
            if(encryptedData == null) {
                return;
            }

            if (Context.getInstance().isCompress()) {
                try {
                    String compressedName = FilenameUtils.removeExtension(inputFile.name()) + ".gz";
                    CompressionUtils.compressFile(encryptedData, unpackedPath + compressedName);
                } catch (Exception e) {
                    showPopupMessage("Cannot compress file: " + e.getMessage());
                }
            } else {
                saveFile(encryptedData, unpackedPath + inputFile.name());
            }
        }
    }

    private void processPackedFiles(String packedPath) {
        var buffer = InputFile.packLoadedFiles();
        var encryptedData = encryptData(buffer);
        if(encryptedData == null) {
            return;
        }

        createDirectory(packedPath);

        if (Context.getInstance().isCompress()) {
            try {
                CompressionUtils.compressFile(encryptedData, packedPath + "packed.gz");
            } catch (RuntimeException | IOException e) {
                showPopupMessage("Cannot pack: " + e.getMessage());
            }
        } else {
            saveFile(encryptedData, packedPath + "packed.dat");
        }

        PackedInputFileTableViewer.open();
    }

    private static void showPopupMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private static byte[] encryptData(byte[] data) {
        var aesInfo = Context.getInstance().getAESInfo();
        aesInfo.initSecretKeyAndCipher();

        try {
            return aesInfo.getCipherInstance().doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            showPopupMessage(e.getMessage());
        }
        return null;
    }

}
