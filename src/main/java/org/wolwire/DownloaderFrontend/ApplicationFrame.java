package org.wolwire.DownloaderFrontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ApplicationFrame extends JFrame {
    // Main Frame
    private static final String applicationName = "Fast Downloader";
    private static final JMenuBar menuBar = new MenuBar();
    private static final JPanel panel = new JPanel();
    private static final DownloadsTableScrollPane scrollPane = new DownloadsTableScrollPane();
    private static final JButton button = new JButton("Start Download");
    ;
    public ApplicationFrame() {
        super(applicationName);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void renderApplication() {
        setJMenuBar(menuBar);
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(button, BorderLayout.NORTH);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DownloadDialog dialog = new DownloadDialog();
                dialog.renderDialog();
            }
        });
        add(panel);

        // Make the frame visible
        setVisible(true);
    }
}
