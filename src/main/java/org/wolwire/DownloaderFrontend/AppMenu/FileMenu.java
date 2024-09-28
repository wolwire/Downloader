package org.wolwire.DownloaderFrontend.AppMenu;

import org.wolwire.DownloaderFrontend.AppMenu.MenuItems.ExitItem;
import org.wolwire.DownloaderFrontend.AppMenu.MenuItems.OpenItem;
import org.wolwire.DownloaderFrontend.AppMenu.MenuItems.SaveItem;

import javax.swing.*;

public class FileMenu extends JMenu {
    private static String menuName = "File";

    public FileMenu() {
        super(menuName);
        JMenuItem openItem = new OpenItem();
        add(openItem);

        // Create 'Save' menu item
        JMenuItem saveItem = new SaveItem();
        add(saveItem);

        // Create 'Exit' menu item
        JMenuItem exitItem = new ExitItem();
        add(exitItem);
    }
}
