/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author anhthu632
 */
public class DAO {

    protected Connection con;

    public DAO() {
        /*thư viện để kết nối database với DAO: JDBC */
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String connectionUrl = "jdbc:sqlserver://localhost;database=BookshopManagementDB"
                    + ";encrypt=true;trustServerCertificate=true";
            String user = "sa";
            String password = "AnhThu632123";

            con = DriverManager.getConnection(connectionUrl, user, password);
            System.out.println("Ket noi thanh cong");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
