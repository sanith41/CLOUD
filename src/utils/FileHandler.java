package utils;

import java.awt.HeadlessException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class FileHandler {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private FileCheckSumSHA512 fileCheckSumsha = new FileCheckSumSHA512();
    private static List<FileChunkInfoAdapter> chunkInfo;
    private static final String PROVIDER = "BC";
    private static final String ALGORITHM = "ECIES";
    private static final String CURVE_NAME = "prime256v1";// Key size in bits
    private Connection con;
    private ResultSet rs;
    private PreparedStatement s=null;
private static final int CHUNK_SIZE = 128; // Chunk size for padding
    
    public void join(String outputPath, String outputFileName, String fileExtension, String[] pathArray,String user) throws GeneralSecurityException, Exception {
        
        long leninfile = 0, leng = 0;
        int count = 1, data = 0, chunkCount = 0;
        
        int index = pathArray[0].lastIndexOf('.');
        
        String ext=pathArray[0].substring(index + 1);
        
        
        File g=new File(pathArray[0]);
        try {
            File filename = new File(CommonProperties.outputFilePath + outputFileName + "." + fileExtension);
            
            
            //System.out.println(g.getParent().toString()+"  hhhhhhhhhhhhhhhh");
            try (OutputStream outfile = new BufferedOutputStream(new FileOutputStream(filename))) {
                while (chunkCount < pathArray.length) {
                    //System.out.println(pathArray[chunkCount]+"chunk path aa");
                    //int dot=pathArray[chunkCount].lastIndexOf(".");
                    String result=pathArray[chunkCount];
                    File temp = new File(result+".enc");
                    filename=new File(pathArray[chunkCount]);
                    String path=filename.getAbsolutePath();
                    int i=path.indexOf(".");
                    int len=path.length();
                    String sub=path.substring(i, len);
                    File encfile=new File(path+sub);
                    System.out.println(path);
                    System.out.print("Retrieving privatekey for user "+user+" from  ");
                    PrivateKey key=getprikey(user,path);
                    
                    System.out.println(key);
                    decryptFile(result,key);
                    System.out.println("Decrypting........");
                    
                    
                    
                    
                    
                    if (encfile.exists()) {
                        try (InputStream infile = new BufferedInputStream(new FileInputStream(encfile))) {
                            data = infile.read();
                            while (data != -1) {
                                outfile.write(data);
                                data = infile.read();
                                
                            }
                            leng++;
                            //System.out.println("existessss");

                        }
                        count++;
                        chunkCount++;
                        
                    } else {
                        //System.out.println("doesnt existssssa");

                        break;
                    }
                }
                System.out.println("Download complete :)");
                FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(ext);
            }
        };

        // Get all the files in the directory that match the extension
        File[] files = new File(g.getParent()).listFiles(filter);

        // Delete each file
        for (File file : files) {
            file.delete();
        }
        System.out.println("Deleting the Decrypted chunks");
        System.out.println("\t\t---------------------------------------------------------------------------------------\t\t");

                JOptionPane.showMessageDialog(null, "File Downloaded: " + CommonProperties.outputFilePath + outputFileName + "." + fileExtension);
            }
            
        } catch (IOException | HeadlessException e) {  System.out.println("caughttt");
}
    }



    public List<FileChunkInfoAdapter> split(String FilePath, long splitlen, String user) throws GeneralSecurityException, Exception {
        chunkInfo = new ArrayList<>();
        long leninfile = 0, leng = 0;
        int count = 1, data;
        
        try {
                    System.out.println("Spliting the file into chunks....");
        int dateHashCode = new Date().hashCode();
        new File(CommonProperties.tempFilePath + dateHashCode).mkdirs();
        File filename = new File(FilePath);
        String fileNameWithoutExtension = getFileNameWithoutExtension(filename);
        String fileExtension = getFileExtension(filename);
        InputStream infile = new BufferedInputStream(new FileInputStream(filename));
        data = infile.read();
        while (data != -1) {
            String tempFilePath = CommonProperties.tempFilePath + dateHashCode + "\\" + dateHashCode + count + "." + fileExtension;
            filename = new File(tempFilePath);

            try (OutputStream outfile = new BufferedOutputStream(new FileOutputStream(filename))) {
                while (data != -1 && leng < splitlen) {
                    outfile.write(data);
                    leng++;
                    data = infile.read();
                }
                leninfile += leng;
                //leng = 0;
                System.out.println("length of the chunk"+leninfile/1024+"Kb");
                
                 leng = 0;
                 
            }
            //System.out.println("till here it is okkk");
            System.out.println("Retriveing the public key for the user "+user);
            PublicKey pubkey=getpubkey(user);
            
            System.out.println("public key "+pubkey);
            encryptFile(filename.getAbsolutePath(),pubkey);
            System.out.println("encrypting "+filename.getAbsolutePath());
            //filename.delete();
            count++;
            //System.out.println(filename + "hiiiiiiiiii");
            System.out.println("calculating checksum");
            String tempFileChecksum = fileCheckSumsha.checksum(tempFilePath);
            System.out.println(tempFileChecksum);
            chunkInfo.add(new FileChunkInfoAdapter(tempFileChecksum, tempFilePath, dateHashCode, fileNameWithoutExtension, fileExtension, filename.length(), user,null));
            filename.delete();
        }
        return chunkInfo;
    } catch (IOException | NoSuchAlgorithmException e) {
        return null;
    }
}

    
    private String getFileNameWithoutExtension(File file) {
        String fileName = "";
 
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            fileName = "";
        }
 
        return fileName;
    }
    
    private String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    
     public static void encryptFile(String inputFilePath, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        System.out.println(publicKey.toString());
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
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int i=inputFilePath.indexOf(".");
        int len=inputFilePath.length();
        String sub=inputFilePath.substring(i, len);

        try (FileInputStream in = new FileInputStream(inputFilePath+".enc");
             FileOutputStream out = new FileOutputStream(inputFilePath+sub)) {
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

     
     
     public PrivateKey getprikey(String user,String path)
    {
        try
        {
            con=DBConnector.getConnection();
            
            s=con.prepareStatement("SELECT privatekey, username FROM users,chunks_information WHERE shared=username and user = ? AND chunk_path LIKE ?",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            s.setString(1, user); // Assuming user is a string variable containing the username
            s.setString(2, "%" + path.replace("\\", "\\\\") + "%"); // Replace backslashes with double backslashes for SQL
   
            rs=s.executeQuery();
            String key=null;
            String share=null;
            if(rs.next())
            {
                key = rs.getString(1);
                share=rs.getString(2);
            }
            System.out.println(share);
            try {
            // Decode the Base64 encoded private key
            byte[] decodedPrivateKey = Base64.getDecoder().decode(key);

            // Reconstruct the private key object
            KeyFactory keyFactory = KeyFactory.getInstance("EC"); // Adjust algorithm as needed
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            

            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) { // Handle or log the exception
                // Handle or log the exception
            return null;
        }
            
            
        }
        catch(SQLException e)
        {
            return null;
        }
    }
     
     
     
     
     public PublicKey getpubkey(String user)
    {
        try
        {
            con=DBConnector.getConnection();
            //String ifAvailable="select file_extension from chunks_information where file_directory='"+ directoryName +"'";

            s=con.prepareStatement("select publickey from users where username='"+user+"'",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=s.executeQuery();
            String key=null;
            
            if(rs.next())
            {
                key = rs.getString(1);
            }
            try {
    // Decode the Base64 encoded public key
    byte[] decodedPublicKey = Base64.getDecoder().decode(key);

    // Reconstruct the public key object
    KeyFactory keyFactory = KeyFactory.getInstance("EC"); // Assuming the key algorithm is EC
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedPublicKey);
    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
    System.out.println(publicKey);
    return publicKey;
} catch (NoSuchAlgorithmException | InvalidKeySpecException e) { // Handle or log the exception
                // Handle or log the exception
    return null;
}
            
            
        }
        catch(SQLException e)
        {
            return null;
        }
    }
     
     
     
     
}



    
    
    
    
    
