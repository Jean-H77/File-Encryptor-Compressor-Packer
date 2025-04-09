package org.john.core.GUI;

import org.john.core.InputFile.InputFile;
import org.john.core.context.Context;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

public class PackedInputFileTableViewer extends JFrame {

    public int totalBytes;

    public PackedInputFileTableViewer() {
        setTitle("String Data Viewer");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        URL iconUrl = getClass().getResource("/viewer_icon.png");
        assert iconUrl != null;
        ImageIcon icon = new ImageIcon(iconUrl);
        setIconImage(icon.getImage());

        JTable table = getJTable();
        table.setFont(new Font("Monospaced", Font.PLAIN, 14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel summary = new JLabel("Total bytes: " + totalBytes);
        add(summary, BorderLayout.SOUTH);
        setVisible(true);
    }

    public static void open() {
        SwingUtilities.invokeLater(PackedInputFileTableViewer::new);
    }

    private JTable getJTable() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };
        tableModel.addColumn("Position");
        tableModel.addColumn("Payload");

        var loadedInputFiles = InputFile.getLoadedFiles();
        boolean includeFileName = Context.getInstance().isIncludeFileName();
        boolean includeFileLength = Context.getInstance().isIncludeFileLength();

        int index = 0;
        for(var loadedFile : loadedInputFiles) {
            var name = loadedFile.name();
            var data = loadedFile.data();

            if(includeFileName) {
                Vector<String> fileNameLengthRow = new Vector<>();
                fileNameLengthRow.add(String.valueOf(index));
                int fileLength = name.length();
                fileNameLengthRow.add(String.valueOf(fileLength));
                index += fileLength;
                tableModel.addRow(fileNameLengthRow);

                Vector<String> fileNameRow = new Vector<>();
                fileNameRow.add(String.valueOf(index));
                fileNameRow.add(name);
                index += fileLength;
                tableModel.addRow(fileNameRow);
            }

            if(includeFileLength) {
                Vector<String> payloadLengthRow = new Vector<>();
                payloadLengthRow.add(String.valueOf(index));
                payloadLengthRow.add(String.valueOf(data.length));
                index += 4;
                tableModel.addRow(payloadLengthRow);
            }

            Vector<String> payloadRow = new Vector<>();
            payloadRow.add(String.valueOf(index));
            payloadRow.add(Arrays.toString(data));
            index += data.length;
            tableModel.addRow(payloadRow);
        }

        totalBytes = index;

        return new JTable(tableModel);
    }
}