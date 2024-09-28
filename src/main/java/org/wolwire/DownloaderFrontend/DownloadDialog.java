package org.wolwire.DownloaderFrontend;

import org.wolwire.DownloaderBackend.MultiThreadedDownloader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadDialog {
    private final JFrame frame;
    private final JTextField textField;
    private final JProgressBar progressBar;
    private final JButton downloadButton;
    private final JButton selectFolderButton;
    private final MultiThreadedDownloader downloader;
    private final JLabel downloadLocationLabel;

    private ExecutorService executor;
    private Future<?> downloaderFuture;
    private Future<?> progressBarMonitorFuture;


    private File selectedFolder;

    public DownloadDialog() {
        frame = new JFrame("Internet Download Application");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new FlowLayout());
        JLabel label = new JLabel("Enter download Url:");
        textField = new JTextField(30);

        downloadButton = new JButton("Download");
        selectFolderButton = new JButton("Select Folder");
        downloadLocationLabel = new JLabel("Download Location:");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(350, 100));

        addDownloadButtonListener();
        addSelectFolderButtonListener();
        addCloseButtonListener();

        frame.add(label);
        frame.add(textField);
        frame.add(selectFolderButton);
        frame.add(downloadButton);
        frame.add(downloadLocationLabel);
        frame.add(progressBar);

        executor = Executors.newFixedThreadPool(2);
        downloader = new MultiThreadedDownloader();
    }

    // Render function
    public void renderDialog() {
        frame.setVisible(true);
    }

    // Add Listeners Here
    private void addDownloadButtonListener() {
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // When the button is clicked, update the output label
                String url = textField.getText();
                downloaderFuture = executor.submit(() -> downloader.startDownload(url,
                        1024 * 10,
                        8,
                        selectedFolder.getAbsolutePath()));
                progressBarMonitorFuture = executor.submit(() -> startMonitoring(progressBar, downloader.downloadPercentage));
            }
        });
    }

    private void addSelectFolderButtonListener() {
        // Add action to 'Select Folder' menu item
        selectFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFolder();
            }
        });
    }

    private void addCloseButtonListener() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });
    }

    // Add Listener functions here
    private void onWindowClosing() {
        int response = JOptionPane.showConfirmDialog(frame, "Do you want to stop the task and exit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            // Cancel the task
            downloaderFuture.cancel(true);
            progressBarMonitorFuture.cancel(true);
            downloader.stopDownload();
            executor.shutdownNow();

            try {
                // Wait for tasks to terminate (with a timeout)
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Executor did not terminate in the allotted time.");
                }
            } catch (InterruptedException e) {
                System.out.println("Executor was interrupted during shutdown.");
            }

            // Dispose the frame and exit
            frame.dispose();
        }
    }

    private static void startMonitoring(JProgressBar progressBar, AtomicInteger downloadPercentage) {
        Timer timer = new Timer(30, e -> {
            SwingUtilities.invokeLater(() -> progressBar.setValue(downloadPercentage.get()));
        });
        timer.start();
    }

    private void selectFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = folderChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFolder = folderChooser.getSelectedFile();
            JOptionPane.showMessageDialog(frame, "Selected folder: " + selectedFolder.getAbsolutePath());
        }
    }
}
