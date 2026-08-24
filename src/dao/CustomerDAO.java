/*
 * FR-SALE-05 (Search Customer) + hỗ trợ khách vãng lai cho FR-SALE-01
 * (Create In-Store Invoice).
 */
package dao;

import model.Customer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author anhthu632
 */
public class CustomerDAO extends DAO {

    // Email cố định dùng làm "khóa" nhận diện Customer đại diện khách vãng
    // lai — theo hướng đã thống nhất với người dùng: BR-06 ("mỗi Order thuộc
    // đúng 1 Customer") vẫn giữ nguyên, không sửa customerID thành NULL được
    // (tránh phải sửa mọi chỗ đang JOIN Customer sang LEFT JOIN).
    private static final String WALK_IN_EMAIL = "khachvanglai@bookshop.local";

    public CustomerDAO() {
    }

    public Customer[] searchCustomer(String keyword) {
        ArrayList<Customer> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Customer WHERE fullName LIKE ? OR phone LIKE ? OR email LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Customer(rs.getInt("customerID"), rs.getString("fullName"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Customer[0]);
    }

    // Trả về Customer đại diện "Khách vãng lai" dùng chung cho mọi lượt bán
    // tại quầy không tra cứu khách quen — tạo mới đúng 1 lần nếu chưa có.
    public Customer getOrCreateWalkInCustomer() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Customer WHERE email = ?");
            ps.setString(1, WALK_IN_EMAIL);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer(rs.getInt("customerID"), rs.getString("fullName"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address"));
            }

            PreparedStatement psInsert = con.prepareStatement(
                    "INSERT INTO Customer (fullName, phone, email, address) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psInsert.setString(1, "Khách vãng lai");
            psInsert.setString(2, "0000000000");
            psInsert.setString(3, WALK_IN_EMAIL);
            psInsert.setString(4, null);
            psInsert.executeUpdate();

            ResultSet keys = psInsert.getGeneratedKeys();
            Customer c = new Customer();
            c.setFullName("Khách vãng lai");
            c.setPhone("0000000000");
            c.setEmail(WALK_IN_EMAIL);
            if (keys.next()) {
                c.setCustomerID(keys.getInt(1));
            }
            return c;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
