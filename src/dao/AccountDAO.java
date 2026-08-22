/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Account;
import model.Customer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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

    // FR-CUS-01 (Register Account): tạo đồng thời Customer + Account trong 1
    // transaction (Account.customerID tham chiếu Customer vừa tạo), theo đúng
    // pattern transaction đã dùng ở OrderDAO.addOrder(). Trả về null nếu đăng ký
    // thành công, ngược lại trả về thông báo lỗi cụ thể để hiển thị cho người dùng.
    public String register(Customer customer, Account account) {
        try {
            con.setAutoCommit(false);

            PreparedStatement psCheckUser = con.prepareStatement("SELECT 1 FROM Account WHERE username = ?");
            psCheckUser.setString(1, account.getUsername());
            if (psCheckUser.executeQuery().next()) {
                con.rollback();
                return "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!";
            }

            PreparedStatement psCheckEmail = con.prepareStatement("SELECT 1 FROM Customer WHERE email = ?");
            psCheckEmail.setString(1, customer.getEmail());
            if (psCheckEmail.executeQuery().next()) {
                con.rollback();
                return "Email này đã được đăng ký, vui lòng dùng email khác!";
            }

            String sqlCustomer = "INSERT INTO Customer (fullName, phone, email, address) VALUES (?,?,?,?)";
            PreparedStatement psCustomer = con.prepareStatement(sqlCustomer, Statement.RETURN_GENERATED_KEYS);
            psCustomer.setString(1, customer.getFullName());
            psCustomer.setString(2, customer.getPhone());
            psCustomer.setString(3, customer.getEmail());
            psCustomer.setString(4, customer.getAddress());
            psCustomer.executeUpdate();

            ResultSet keys = psCustomer.getGeneratedKeys();
            if (!keys.next()) {
                con.rollback();
                return "Đăng ký thất bại, vui lòng thử lại!";
            }
            int customerID = keys.getInt(1);
            customer.setCustomerID(customerID);

            String sqlAccount = "INSERT INTO Account (username, password, role, status, customerID) VALUES (?,?,?,?,?)";
            PreparedStatement psAccount = con.prepareStatement(sqlAccount);
            psAccount.setString(1, account.getUsername());
            psAccount.setString(2, account.getPassword());
            psAccount.setString(3, "Customer");
            psAccount.setString(4, "Active");
            psAccount.setInt(5, customerID);
            psAccount.executeUpdate();

            con.commit();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return "Đăng ký thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
