package org.wolwire.DownloaderBackend;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiThreadedDownloader {
    private String url;
    public long contentLength;
    private RandomAccessFile randomAccessFile; // Single output stream
    private String outputFileName; // To hold the final filename
    public AtomicLong totalBytesWritten = new AtomicLong(0); // To keep track of bytes written
    public AtomicInteger downloadPercentage = new AtomicInteger(0);
    List<Future<?>> futureList = new ArrayList<>();

    public void startDownload(String url, int chunkSize, int threadCount, String downloadLocation) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            validateUrl(url);
            outputFileName = getFilenameFromHeaders(connection, url); // Get filename from headers or URL
            getFileSize(url);
            connection.disconnect();

            String downloadFileLocation = downloadLocation + "/" + outputFileName;
            randomAccessFile = new RandomAccessFile(downloadFileLocation, "rws"); // Use extracted filename

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            long downloadPortions = contentLength / chunkSize;

            for (long i = 0; i <= downloadPortions; i++) {
                final long start = i * chunkSize; // Declare final variable
                final long end = (i == downloadPortions) ? contentLength - 1 : (start + chunkSize); // Declare final variable
                final long index = i; // Declare final variable
                Future<?> future = executorService.submit(() -> downloadFile(url, start, end, index, totalBytesWritten));
                futureList.add(future);
            }

            executorService.shutdown();
            while (!executorService.isTerminated() && !Thread.currentThread().isInterrupted()) {
                // Wait for all threads to complete
            }

            System.out.println("Total bytes written: " + totalBytesWritten.get());
        } catch (IOException e) {
            System.err.println("Error during download: " + e.getMessage());
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close(); // Close the output file stream
                }
            } catch (IOException e) {
                System.err.println("Error closing file output stream: " + e.getMessage());
            }
        }
    }

    public void stopDownload() {
        for (Future<?> future : futureList) {
            future.cancel(true);
        }
    }

    private void validateUrl(String url) {
        try {
            new URL(url); // Validate URL format
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    private void getFileSize(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        contentLength = connection.getContentLengthLong();
        connection.disconnect();

        if (contentLength <= 0) {
            throw new IOException("Invalid content length: " + contentLength);
        }
    }

    private void downloadFile(String url, long start, long end, long index, AtomicLong totalBytesWritten) {
        System.out.println("Trying to download chunk " + index);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            String range = "bytes=" + start + "-" + end;
            System.out.println("Downloading chunk " + index + " to " + range);
            connection.setRequestProperty("Range", range);
            connection.connect();
            // Use synchronized block to ensure thread safety when writing to the output file
            synchronized (randomAccessFile) {
                randomAccessFile.seek(start);
                try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                    byte[] buffer = new byte[8192]; // Buffer size
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.println("Download task interrupted. Stopping...");
                            connection.disconnect();
                            return; // Exit the task gracefully
                        }
                        randomAccessFile.write(buffer, 0, bytesRead);
                        totalBytesWritten.addAndGet(bytesRead);
                        downloadPercentage.set((int) (totalBytesWritten.get() * 100 / contentLength));
                    }
                }
            }
            System.out.println("Finished downloading chunk " + index);
        } catch (IOException e) {
            System.err.println("Error downloading chunk " + index + ": " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getFilenameFromHeaders(HttpURLConnection connection, String url) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            Pattern pattern = Pattern.compile("filename=\"?(.+?)\"?;", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return getFilenameFromUrl(url).trim();
    }

    private String getFilenameFromUrl(String url) {
        String[] urlParts = url.split("/");
        return urlParts[urlParts.length - 1].trim(); // Extracts the filename from the URL
    }
}
