package utils;

import static data.deduplication.AdminHome.UploadFileStatus;
import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
//import data.deduplication.DataDeduplication.uuser;

public class DbHelper {
    private String[] chunkPaths;
    private Connection con;
    private ResultSet rs;
    private PreparedStatement s=null;
    public long insertChunksRecordInDB(String checkSum, String filePath, String fileName, String fileOriginalName, String fileExtension, long chunkSize, String user,String shared)
    {
        try
        {
            con=DBConnector.getConnection();
            
            
            String ifAvailable="select * from chunks_information where chunk_hash='"+ checkSum +"'"; //and user='"+user+"'";
            String sql="insert into chunks_information values (?,?,?,?,?,?,?,?)";
            

            s=con.prepareStatement(ifAvailable);
            rs=s.executeQuery();
            
            long sizeOccupiedOnStorage = 0;
            
            if(rs.next())
            {
                //StringBuilder resultStringBuilder = new StringBuilder();
                String checksum= rs.getString(2);
                String filepath=rs.getString(3);
                String filename=rs.getString(4);
                String fileoriginalname=rs.getString(5);
                
                String fileextension=rs.getString(6);
                String original=rs.getString(7);
                String newuser=user;

                String ref="select * from chunks_information where chunk_hash='"+ checkSum +"' and user='"+user+"'";
                s=con.prepareStatement(ref);
                rs=s.executeQuery();
                if(rs.next()){
                    UploadFileStatus.append("Duplicate chunk - " + checkSum + "\n");
                    UploadFileStatus.update(UploadFileStatus.getGraphics());
                    System.out.println("duplicate chunk");
                    File presentChunk = new File(filePath);
                    presentChunk.delete();
                    
                    
                    
                    
                }
                else{
                    s=con.prepareStatement(sql);
                    
                    s.setInt(1,0);
                    s.setString(2, checksum);
                    s.setString(3, filepath);
                //System.out.println(rs.getString(1));
                s.setString(4, filename);
                s.setString(5, fileoriginalname);
                s.setString(6, fileextension);
                s.setString(7, newuser);
                s.setString(8,original);
               
                s.executeUpdate();
                s.close();
                File presentChunk = new File(filePath+".enc");
                
                presentChunk.delete();
                System.out.println("Chunk exists in Storage at "+filepath+"  --owned by "+original);
                System.out.println("Giving user "+newuser+" reference to user "+original);

                }
                //if(rs.getString(1))
                /*s=con.prepareStatement(sql);

                //s.setInt(1, 0);
                s.setString(2, checkSum);
                s.setString(3, rs.getString(1));
                //System.out.println(rs.getString(1));
                s.setString(4, fileName);
                s.setString(5, fileOriginalName);
                s.setString(6, fileExtension);
                s.setString(7, user);
                s.executeUpdate();
                s.close();
                
                File presentChunk = new File(filePath);
                presentChunk.delete();*/
                
                //UploadFileStatus.append("Duplicate chunk - " + checkSum + "\n");
                //UploadFileStatus.update(UploadFileStatus.getGraphics());
            }
            else
            {
                s=con.prepareStatement(sql);

                s.setInt(1, 0);
                s.setString(2, checkSum);
                s.setString(3, filePath);
                s.setString(4, fileName);
                s.setString(5, fileOriginalName);
                s.setString(6, fileExtension);
                s.setString(7, user);
                s.setString(8, user);
              
                s.executeUpdate();
                s.close();
                
                System.out.println("Saving the encrypted chunks in "+filePath);
                sizeOccupiedOnStorage = chunkSize;
            }
            /*if(sizeOccupiedOnStorage==0){
            File file = new File(filePath);
            String directory = file.getParent();
            System.out.println(directory);
            File dir = new File(directory);
            dir.delete();

        // Delete the file.
        
            }*/
            return sizeOccupiedOnStorage;
        }
        catch(SQLException e)
        {
            return 0;
        }
    }
    public String[] selectChunksPath(String directoryName)
    {
        try
        {
            con=DBConnector.getConnection();
            //String ifAvailable="select chunk_path from chunks_information where file_directory='"+ directoryName +"'";

            s=con.prepareStatement("select chunk_path from chunks_information where file_directory='"+ directoryName +"'",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=s.executeQuery();
            rs.last();
            int rows = rs.getRow();
            rs.beforeFirst();
            
            int count = 0;
            chunkPaths = new String[rows];
            
            while(rs.next())
            {
                chunkPaths[count] = rs.getString(1);
                count++;
            }
            
            return chunkPaths;
        }
        catch(SQLException e)
        {
            return null;
        }
    }
    
    public String getFileExtension(String directoryName)
    {
        try
        {
            con=DBConnector.getConnection();
            //String ifAvailable="select file_extension from chunks_information where file_directory='"+ directoryName +"'";

            s=con.prepareStatement("select file_extension from chunks_information where file_directory='"+ directoryName +"'",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=s.executeQuery();
            String fileExt = null;
            
            if(rs.next())
            {
                fileExt = rs.getString(1);
            }
            
            return fileExt;
        }
        catch(SQLException e)
        {
            return null;
        }
    }
    
    public int getRecordCount(ResultSet resultSet){
        int size = 0;
        try {
            resultSet.last();
            size = resultSet.getRow();
            resultSet.beforeFirst();
        }
        catch(Exception ex) {
            return 0;
        }
        return size;
    }
    
    public Object[][] getAllFilesTableData(String user){
        Object[][] tableData = null;
        try
        {
           con=DBConnector.getConnection();            
        s=con.prepareStatement("SELECT distinct file_original_name, file_directory, file_extension FROM chunks_information WHERE user='"+user+"'",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=s.executeQuery();
        int rowCount = getRecordCount(rs);
        if(rowCount == 0) {
            System.out.println("HI2");
        }
        if (rowCount > 0) {
            tableData = new Object[rowCount][3];
            int i = 0;
            rs.beforeFirst();
            while(rs.next())
            {
                //System.out.println("inside");

                tableData[i][0] = i+1;
                tableData[i][1] = rs.getString("file_original_name") + "." + rs.getString("file_extension");
                tableData[i][2] = rs.getString("file_directory");   
                i++;
            }
            }
        }catch(Exception ex){return null;}
        return tableData;
    }
    
    public Object[][] getSelectedFileTableData(String fileDirectory,String user){
        Object[][] tableData = null;
        try
        {
            con = DBConnector.getConnection();
        s = con.prepareStatement("select * from chunks_information where file_directory = ? and user='"+user+"'",
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        s.setString(1, fileDirectory);
        rs = s.executeQuery();

        // Get the number of rows in the result set
        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();
            
            tableData = new Object[rowCount][6];

        int i = 0;
            while(rs.next())
            {
                tableData[i][0] = i+1;
                tableData[i][1] = rs.getString("chunk_hash");
                tableData[i][2] = rs.getString("chunk_path");   
                tableData[i][3] = rs.getString("file_directory");   
                tableData[i][4] = rs.getString("file_original_name");   
                tableData[i][5] = rs.getString("file_extension");   
                i++;
            }
        }catch(Exception ex){return null;}
        return tableData;
    }
    
    public PrivateKey getkeys(String user)
    {
        try
        {
            con=DBConnector.getConnection();
            //String ifAvailable="select file_extension from chunks_information where file_directory='"+ directoryName +"'";

            s=con.prepareStatement("select privatekey from users u join chunks_information c on u.user=c.shared and c.user='"+user+"'",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=s.executeQuery();
            String key=null;
            
            if(rs.next())
            {
                key = rs.getString(1);
            }
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
    
}


