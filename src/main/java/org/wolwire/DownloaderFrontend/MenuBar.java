package org.wolwire.DownloaderFrontend;

import org.wolwire.DownloaderFrontend.AppMenu.FileMenu;

import javax.swing.*;

public class MenuBar extends JMenuBar {
    public MenuBar() {

        // Create the 'File' menu
        JMenu fileMenu = new FileMenu();
        add(fileMenu);
    }
}
