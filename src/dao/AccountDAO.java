/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Account;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author anhthu632
 */
public class AccountDAO extends DAO {

    public AccountDAO() {
    }

    public boolean checkLogin(Account account) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Account WHERE username = ? AND password = ?");
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                account.setRole(rs.getString("role"));
                account.setStatus(rs.getString("status"));
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
