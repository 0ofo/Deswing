package org.zof.deswing.util;

import org.zof.deswing.ui.CopyableDialog;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Base64;

public class ExportToBase64 extends Export{

    @Override
    public void export(String type, String command) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exportToStream(type,command,bos);
        String base64Text = Base64.getEncoder().encodeToString(bos.toByteArray());
        bos.close();
        JDialog dialog = new CopyableDialog("导出到Base64",base64Text);
        dialog.setVisible(true);
    }
}
