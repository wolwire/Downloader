package org.wolwire.DownloaderFrontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DownloadsTable extends JTable {
    private static final String[] columnNames = {"ID", "FileName", "Download Location"};
    private static final Object[][] data = {};
    private static final DefaultTableModel model = new DefaultTableModel(data, columnNames);

    public DownloadsTable() {
        super(model);
    }
}
