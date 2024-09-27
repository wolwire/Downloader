# Multi-Threaded File Downloader

## Project Description

This project is designed to download large files using a multi-threaded approach, allowing multiple parts of the file to be downloaded concurrently. It aims to efficiently handle large files by breaking the download into chunks and downloading each chunk in parallel, thereby speeding up the process.

### Key Responsibilities

1. **Multi-Threaded File Downloading**:
   - Utilizes multiple threads to download different parts of the file concurrently. The number of threads can be configured based on available resources and network bandwidth.

2. **Partial Download Support**:
   - The downloader makes use of the HTTP `Range` header to download specific byte ranges of a file. This allows for partial downloads, ensuring that different threads download non-overlapping parts of the file.

3. **File Integrity**:
   - After downloading, the different chunks are written at their respective positions in the output file. The `FileOutputStream` is synchronized to ensure safe, concurrent writing by multiple threads.

4. **Error Handling**:
   - Handles network or connection issues by retrying failed download parts.

5. **Resumable Downloads**:
   - Allows for the resumption of downloads by ensuring that already downloaded parts are not re-downloaded.
