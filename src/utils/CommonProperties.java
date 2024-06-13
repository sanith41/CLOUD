package utils;

public class CommonProperties {
    public static String tempFilePath = "C:\\data_dump1\\";
    public static String outputFilePath = "C:\\data_dump2\\";
    public static long chunkSize = 1024 * 128; // 128Kb
    public static int storageSize = 1024 * 1024 * 1024; //1GB
    public static String sqlDriver = "com.mysql.jdbc.Driver";
    public static String sqlDbUrl = "jdbc:mysql://localhost:3306/data_deduplication";
    public static String sqlUser = "root";
    public static String sqlPassword = "";
}
