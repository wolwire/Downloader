package org.wolwire;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomWriteExample {

    public static void main(String[] args) {
        String filePath = "random_access_file.txt"; // Specify your file path here
        byte[] dataToWrite = {0x48, 0x65, 0x6c, 0x6c, 0x6f}; // Data to write

        // Writing data at various random positions
        try {
            writeAtRandomPosition(filePath, 0, dataToWrite); // Write at position 100
            writeAtRandomPosition(filePath, 2, dataToWrite); // Write at position 200
            writeAtRandomPosition(filePath, 3, dataToWrite); // Write at position 300
            System.out.println("Data written to the file at random positions.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeAtRandomPosition(String filePath, long position, byte[] data) throws IOException {
        File file = new File(filePath);

        // Create or open the file in read/write mode
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // Move the file pointer to the specified position
            raf.seek(position);
            // Write data at the current position
            raf.write(data);
        }
    }
}