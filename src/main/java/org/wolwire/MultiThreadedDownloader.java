package org.wolwire;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiThreadedDownloader {
    private final String url;
    private final int chunkSize; // in bytes
    private final int threadCount;
    private long contentLength;
    private RandomAccessFile randomAccessFile; // Single output stream
    private String outputFileName; // To hold the final filename

    public MultiThreadedDownloader(String url, int chunkSize, int threadCount) {
        this.url = url;
        this.chunkSize = chunkSize;
        this.threadCount = threadCount;
        validateUrl();
    }

    private void validateUrl() {
        try {
            new URL(url); // Validate URL format
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    private long getFileSize() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        contentLength = connection.getContentLengthLong();
        connection.disconnect();

        if (contentLength <= 0) {
            throw new IOException("Invalid content length: " + contentLength);
        }
        return contentLength;
    }

    private void downloadFile(long start, long end, long index, AtomicLong totalBytesWritten) {
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
                        randomAccessFile.write(buffer, 0, bytesRead);
                        totalBytesWritten.addAndGet(bytesRead);
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

    private String getFilenameFromHeaders(HttpURLConnection connection) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            Pattern pattern = Pattern.compile("filename=\"?(.+?)\"?;", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return getFilenameFromUrl();
    }

    private String getFilenameFromUrl() {
        String[] urlParts = url.split("/");
        return urlParts[urlParts.length - 1]; // Extracts the filename from the URL
    }

    public void startDownload() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            outputFileName = getFilenameFromHeaders(connection); // Get filename from headers or URL
            getFileSize();
            connection.disconnect();

            // Initialize a single output file for writing
            randomAccessFile = new RandomAccessFile(outputFileName, "rws"); // Use extracted filename
            AtomicLong totalBytesWritten = new AtomicLong(0); // To keep track of bytes written

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            long downloadPortions = contentLength / chunkSize;

            for (long i = 0; i <= downloadPortions; i++) {
                final long start = i * chunkSize; // Declare final variable
                final long end = (i == downloadPortions) ? contentLength - 1 : (start + chunkSize); // Declare final variable
                final long index = i; // Declare final variable

                executorService.execute(() -> downloadFile(start, end, index, totalBytesWritten));
            }

            executorService.shutdown();
            while (!executorService.isTerminated()) {
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

    public static void main(String[] args) {
        String url = "https://jsoncompare.org/LearningContainer/SampleFiles/Video/MP4/sample-mp4-file.mp4";
        int chunkSize = 10 * 1024; // 10 MB
        int threadCount = 8;

        MultiThreadedDownloader downloader = new MultiThreadedDownloader(url, chunkSize, threadCount);
        downloader.startDownload();
    }
}
