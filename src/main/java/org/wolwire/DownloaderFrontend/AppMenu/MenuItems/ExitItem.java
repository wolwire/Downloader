package org.wolwire.DownloaderFrontend.AppMenu.MenuItems;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitItem extends JMenuItem {
    private static String manuName = "Exit";
    public ExitItem() {
        super(manuName);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
}
