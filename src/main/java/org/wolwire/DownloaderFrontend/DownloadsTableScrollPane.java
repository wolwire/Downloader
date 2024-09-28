package org.wolwire.DownloaderFrontend;

import javax.swing.*;

public class DownloadsTableScrollPane extends JScrollPane {
    private static DownloadsTable table = new DownloadsTable();

    public DownloadsTableScrollPane() {
        super(table);
    }
}
