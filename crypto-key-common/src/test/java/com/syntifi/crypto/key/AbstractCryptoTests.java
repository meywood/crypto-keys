package com.syntifi.crypto.key;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public abstract class AbstractCryptoTests {
    /**
     * Loads test key file from resources
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    protected String getResourcesKeyPath(String filename) throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource(filename);

        String path = Paths.get(url.toURI()).toString();

        return path;
    }

    /**
     * Compare files
     * 
     * @param file1
     * @param file2
     * @throws IOException
     */
    protected boolean compareTextFiles(File file1, File file2) throws IOException {
        byte[] contentFile1 = Files.readAllBytes(file1.toPath());
        byte[] contentFile2 = Files.readAllBytes(file2.toPath());

        return Arrays.equals(contentFile1, contentFile2);
    }

    /**
     * 
     * @param file1
     * @param file2
     * @return
     * @throws IOException
     */
    protected boolean compareFiles(File file1, File file2) throws IOException {
        try (RandomAccessFile randomAccessFile1 = new RandomAccessFile(file1, "r");
                RandomAccessFile randomAccessFile2 = new RandomAccessFile(file2, "r")) {

            FileChannel ch1 = randomAccessFile1.getChannel();
            FileChannel ch2 = randomAccessFile2.getChannel();

            if (ch1.size() != ch2.size()) {
                return false;
            }

            long size = ch1.size();
            MappedByteBuffer m1 = ch1.map(FileChannel.MapMode.READ_ONLY, 0L, size);
            MappedByteBuffer m2 = ch2.map(FileChannel.MapMode.READ_ONLY, 0L, size);

            return m1.equals(m2);
        }
    }
}
