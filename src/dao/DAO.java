/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 *
 * @author anhthu632
 */
public class DAO {

    /**
     * Thông tin kết nối CSDL đọc từ file db.properties ở thư mục gốc project
     * (không commit lên Git vì chứa mật khẩu). Mỗi thành viên copy
     * db.properties.example thành db.properties rồi điền tài khoản SQL Server
     * của máy mình.
     */
    private static final String CONFIG_FILE = "db.properties";
    private static final Properties CONFIG = loadConfig();

    protected Connection con;

    public DAO() {
        /*thư viện để kết nối database với DAO: JDBC */
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String connectionUrl = CONFIG.getProperty("db.url");
            String user = CONFIG.getProperty("db.user");
            String password = CONFIG.getProperty("db.password");

            con = DriverManager.getConnection(connectionUrl, user, password);
            System.out.println("Ket noi thanh cong");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException ex) {
            System.err.println("Khong doc duoc file " + CONFIG_FILE
                    + " - hay copy db.properties.example thanh db.properties va dien thong tin ket noi.");
        }
        return props;
    }
}
