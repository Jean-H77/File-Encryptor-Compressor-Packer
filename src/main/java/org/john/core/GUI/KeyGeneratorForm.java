package org.john.core.GUI;

import org.apache.commons.codec.binary.Hex;
import org.john.core.AES.AESInfo;
import org.john.core.utils.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyGeneratorForm extends JFrame {

    private JComboBox<String> keySizeComboBox;
    private JComboBox<String> outputFormatComboBox;
    private JComboBox<String> cipherModeComboBox;
    private JComboBox<String> paddingComboBox;

    private JTextArea keyArea;

    private final byte[] iv = new byte[16];
    private final SecureRandom random = new SecureRandom();

    public static void open() {
        SwingUtilities.invokeLater(KeyGeneratorForm::new);
    }

    private KeyGeneratorForm() {
        setTitle("AES Key Configuration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);

        URL iconUrl = getClass().getResource("/config_icon.png");
        assert iconUrl != null;
        ImageIcon icon = new ImageIcon(iconUrl);
        setIconImage(icon.getImage());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(BorderFactory.createTitledBorder("Key Settings"));

        JPanel sizePanel = createLabeledComboPanel("Key Size:", new String[]{"128 bits", "192 bits", "256 bits"}, 1);
        keySizeComboBox = (JComboBox<String>) ((JPanel)sizePanel.getComponent(1)).getComponent(0);

        JPanel formatPanel = createLabeledComboPanel("Format:", new String[]{"HEX", "Base64"}, 0);
        outputFormatComboBox = (JComboBox<String>) ((JPanel)formatPanel.getComponent(1)).getComponent(0);

        JPanel algoPanel = createLabeledComboPanel("Cipher Mode:", new String[]{"CBC", "CTR"}, 0);
        cipherModeComboBox = (JComboBox<String>) ((JPanel)algoPanel.getComponent(1)).getComponent(0);

        JPanel paddingPanel = createLabeledComboPanel("Padding Mode:", new String[]{"NoPadding", "PKCS5Padding"}, 0);
        paddingComboBox = (JComboBox<String>) ((JPanel)paddingPanel.getComponent(1)).getComponent(0);

        JPanel ivPanel = new JPanel();
        ivPanel.setLayout(new GridLayout(1, 3));
        ivPanel.add(new JLabel("IV:"));
        JTextField ivField = new JTextField(16);
        ivField.setEditable(false);
        ivPanel.add(ivField);
        ivField.setText(genIV());
        JButton randomIVButton = new JButton("Random");
        randomIVButton.addActionListener(_ -> {
            ivField.setText(genIV());
        });
        ivPanel.add(randomIVButton);

        configPanel.add(sizePanel);
        configPanel.add(Box.createVerticalStrut(5));
        configPanel.add(formatPanel);
        configPanel.add(Box.createVerticalStrut(5));
        configPanel.add(algoPanel);
        configPanel.add(Box.createVerticalStrut(5));
        configPanel.add(paddingPanel);
        configPanel.add(Box.createVerticalStrut(5));
        configPanel.add(ivPanel);

        JPanel keyDisplayPanel = new JPanel(new BorderLayout());
        keyDisplayPanel.setBorder(BorderFactory.createTitledBorder("Generated Key"));
        keyArea = new JTextArea(6, 30);
        keyArea.setEditable(false);
        keyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        keyDisplayPanel.add(new JScrollPane(keyArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton generateButton = new JButton("Generate Key");
        generateButton.addActionListener(_ -> keyArea.setText(generateAESKey()));

        JButton saveButton = new JButton("Save Key");
        saveButton.addActionListener(e -> {
            if (!keyArea.getText().isEmpty()) {
                saveKeyToFile(keyArea.getText());
            } else {
                JOptionPane.showMessageDialog(this, "Please generate a key first.");
            }
        });

        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);

        mainPanel.add(configPanel, BorderLayout.NORTH);
        mainPanel.add(keyDisplayPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createLabeledComboPanel(String labelText, String[] items, int selectedIndex) {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel(labelText), BorderLayout.WEST);

        JComboBox<String> combo = new JComboBox<>(items);
        combo.setSelectedIndex(selectedIndex);
        combo.setPreferredSize(new Dimension(85, combo.getPreferredSize().height));

        JPanel comboPanel = new JPanel();
        comboPanel.add(combo);
        panel.add(comboPanel, BorderLayout.CENTER);

        return panel;
    }

    public int getKeySize() {
        return switch (keySizeComboBox.getSelectedIndex()) {
            case 1 -> 192;
            case 2 -> 256;
            default -> 128;
        };
    }

    private String generateAESKey() {
        try {
            int keySize = getKeySize();
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();

            Cipher cipher = Cipher.getInstance("AES/"+ cipherModeComboBox.getSelectedItem() + "/" + paddingComboBox.getSelectedItem());

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            String keyFormat = (String) outputFormatComboBox.getSelectedItem();
            if ("Base64".equals(keyFormat)) {
                return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            } else {
                StringBuilder hexString = new StringBuilder();
                for (byte b : secretKey.getEncoded()) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            }
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return e.getMessage();
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveKeyToFile(String key) {
        var fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("secrets.json"));
        var filter = new FileNameExtensionFilter("*.json", ".json");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var selectedFile = fileChooser.getSelectedFile();
            var AESInfo = new AESInfo(
                    key, getKeySize(), Hex.encodeHexString(iv),
                    (String) cipherModeComboBox.getSelectedItem(),
                    (String) outputFormatComboBox.getSelectedItem(),
                    (String) paddingComboBox.getSelectedItem()
            );

            try {
                FileUtils.jsonToFile(selectedFile, AESInfo);
                JOptionPane.showMessageDialog(null, "Secrets saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving key: " + e.getMessage());
            }
        }
    }

    private String genIV() {
        random.nextBytes(iv);
        return Hex.encodeHexString(iv);
    }
}
