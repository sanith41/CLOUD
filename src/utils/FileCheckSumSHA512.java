package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileCheckSumSHA512 {
    public String checksum(String filepath) throws IOException, NoSuchAlgorithmException {
        // file hashing with DigestInputStream
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) {
                // Empty loop to clear the data
            }
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}