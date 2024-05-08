package org.zof.deswing.util;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ExportToFile extends Export {
    private static final JFileChooser chooser;
    static {
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("./"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.showSaveDialog(null);
    }

    @Override
    public void export(String type, String command) throws Exception {
        File file = chooser.getSelectedFile();
        if (file == null) throw new Exception("请选择保存路径");
        FileOutputStream fos = new FileOutputStream(file);
        exportToStream(type, command, fos);
        fos.close();
        JOptionPane.showMessageDialog(null, "success");
    }
}
