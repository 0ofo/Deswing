package org.zof.deswing;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.zof.deswing.ui.ExploitPanel;
import org.zof.deswing.ui.PayloadPanel;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private JPanel contentPane;
    private JTabbedPane tabbedPane;

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        JFrame frame = new JFrame("Java反序列化利用工具 by 零溢出");
        frame.setContentPane(new MainWindow().contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
