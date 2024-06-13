/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
/**
 *
 * @author SANITH 41
 */
public class testtt {
    


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();

        // Set the file chooser to only allow selection of files (no directories)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Set file filter to only show specific file extensions (e.g., txt files)
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(filter);
String inputFile="";
        // Show the file chooser dialog
        int returnValue = fileChooser.showOpenDialog(null);

        // Check if a file was selected
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();

            // Print the selected file path
            inputFile= selectedFile.getAbsolutePath();
        } else {
            System.out.println("No file selected.");
        }
    
        try {
            
            
            // Get file paths from user
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            
            System.out.print("Enter path of the file to encrypt: ");
            //String inputFile = "‪C:\\Users\\SANITH 41\\Desktop\\testing11111.txt";
            System.out.print("Enter path to save encrypted file: ");
            //String encryptedFile = "‪C:\\Users\\SANITH 41\\Desktop\\doing.txt";
            System.out.print("Enter path to save decrypted file: ");
            //String decryptedFile = "‪C:\\Users\\SANITH 41\\Desktop\\";

            // Generate key pair
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGen.initialize(256);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Encryption
            encryptFile(inputFile, publicKey);
            

            // Decryption
            decryptFile(inputFile, privateKey);

            System.out.println("Encryption and Decryption completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void encryptFile(String inputFilePath, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        System.out.println(inputFilePath);
       // File o=new File(C:\\Users\\SANITH 41\\Desktop\\doing.enc);
        try (FileInputStream in = new FileInputStream(inputFilePath);
             FileOutputStream out = new FileOutputStream(inputFilePath+".enc")) {
            byte[] inputBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(inputBuffer)) != -1) {
                byte[] outputBuffer = cipher.update(inputBuffer, 0, bytesRead);
                if (outputBuffer != null) {
                    out.write(outputBuffer);
                }
            }
            byte[] outputBuffer = cipher.doFinal();
            if (outputBuffer != null) {
                out.write(outputBuffer);
            }
        }
    }

    public static void decryptFile(String inputFilePath, PrivateKey privateKey) throws Exception {
        int i=inputFilePath.indexOf(".");
        int len=inputFilePath.length();
        String sub=inputFilePath.substring(i, len);
        System.out.println(sub+"uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        

        try (FileInputStream in = new FileInputStream(inputFilePath+".enc");
             FileOutputStream out = new FileOutputStream(inputFilePath+".jpg")) {
            byte[] inputBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(inputBuffer)) != -1) {
                byte[] outputBuffer = cipher.update(inputBuffer, 0, bytesRead);
                if (outputBuffer != null) {
                    out.write(outputBuffer);
                }
            }
            byte[] outputBuffer = cipher.doFinal();
            if (outputBuffer != null) {
                out.write(outputBuffer);
            }
        }
    }
}
