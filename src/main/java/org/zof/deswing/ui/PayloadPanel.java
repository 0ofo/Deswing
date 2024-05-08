package org.zof.deswing.ui;

import org.zof.deswing.controller.PayloadController;
import org.zof.deswing.model.PayloadTableModel;
import org.zof.deswing.util.*;
import ysoserial.exploit.JRMPListener;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PayloadPanel implements ActionListener {
    private JTable table;
    private JPanel panel;
    private JTextField commandField;
    private PayloadTableModel tableModel;
    final JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem toFile = new JMenuItem("导出到文件");
    JMenuItem toBase64 = new JMenuItem("导出为Base64文本");
    JMenuItem toShiro550 = new JMenuItem("导出Shiro550 Payload");

    public PayloadPanel() {
        tableModel = new PayloadTableModel();
        initPopupMenu();
        table.setComponentPopupMenu(popupMenu);
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    public void initPopupMenu(){
        toFile.addActionListener(this);
        toBase64.addActionListener(this);
        toShiro550.addActionListener(this);
        popupMenu.add(toFile);
        popupMenu.add(toBase64);
        popupMenu.add(toShiro550);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()!=MouseEvent.BUTTON3) return;
                int focusedRowIndex = table.rowAtPoint(e.getPoint());
                if (focusedRowIndex == -1) {
                    return;
                }
                table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
            }
        });
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int selectedRow = table.getSelectedRow();
            String command = commandField.getText();
            if (selectedRow == -1) throw Except.PLEASE_SELECT_PAYLOAD;
            if (command.isEmpty()) throw Except.PLEASE_INPUT_COMMAND;
            String type = tableModel.getPayloadType(table.getSelectedRow());
            getExport(e.getSource()).export(type,command);
        }catch (Exception ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    public Export getExport(Object source){
        if (source.equals(toFile)) {
            return new ExportToFile();
        }else if (source.equals(toBase64)) {
            return new ExportToBase64();
        }else if (source.equals(toShiro550)) {
            return new ExportToShiro550();
        }else{
            return null;
        }
    }
}
