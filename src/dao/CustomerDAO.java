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
                Customer c = new Customer(rs.getInt("customerID"), rs.getString("fullName"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address"));
                c.setAvatar(rs.getString("avatar"));
                list.add(c);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Customer[0]);
    }

    // FR-COM (Update Profile, bổ sung theo yêu cầu người dùng) — Customer tự
    // sửa thông tin cá nhân của chính mình. Kiểm tra trùng email TRƯỚC khi
    // update (loại trừ chính customerID này), cùng cách làm với
    // AccountDAO.register() thay vì để UQ_Customer_Email tự ném lỗi SQL.
    public String updateCustomer(Customer customer) {
        try {
            PreparedStatement psCheckEmail = con.prepareStatement(
                    "SELECT 1 FROM Customer WHERE email = ? AND customerID <> ?");
            psCheckEmail.setString(1, customer.getEmail());
            psCheckEmail.setInt(2, customer.getCustomerID());
            if (psCheckEmail.executeQuery().next()) {
                return "Email này đã được dùng bởi tài khoản khác, vui lòng dùng email khác!";
            }

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Customer SET fullName = ?, phone = ?, email = ?, address = ? WHERE customerID = ?");
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setInt(5, customer.getCustomerID());
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Cập nhật thông tin thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }

    // Cập nhật ảnh đại diện — tách riêng khỏi updateCustomer() vì đây là 1
    // thao tác độc lập (chọn file rồi lưu ngay), không đi qua flow "Sửa" từng
    // trường của AccountFrm.
    public boolean updateAvatar(int customerID, String avatarFilename) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE Customer SET avatar = ? WHERE customerID = ?");
            ps.setString(1, avatarFilename);
            ps.setInt(2, customerID);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // FR-SALE-09 (Quick Add Customer): tạo nhanh 1 Customer thật (không có
    // Account đi kèm — khách không cần đăng nhập, chỉ cần có customerID để
    // gắn vào Order) ngay tại quầy khi khách chưa có trong hệ thống, thay vì
    // phải dùng chung "Khách vãng lai". Kiểm tra trùng email trước khi insert,
    // cùng cách làm với updateCustomer()/AccountDAO.register(). Set lại
    // customerID lên đối tượng truyền vào khi thành công.
    public String addCustomer(Customer customer) {
        try {
            PreparedStatement psCheckEmail = con.prepareStatement("SELECT 1 FROM Customer WHERE email = ?");
            psCheckEmail.setString(1, customer.getEmail());
            if (psCheckEmail.executeQuery().next()) {
                return "Email này đã được dùng bởi khách hàng khác, vui lòng dùng email khác!";
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Customer (fullName, phone, email, address) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                customer.setCustomerID(keys.getInt(1));
            }
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Thêm khách hàng thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }

    // Trả về Customer đại diện "Khách vãng lai" dùng chung cho mọi lượt bán
    // tại quầy không tra cứu khách quen — tạo mới đúng 1 lần nếu chưa có.
    public Customer getOrCreateWalkInCustomer() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Customer WHERE email = ?");
            ps.setString(1, WALK_IN_EMAIL);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(rs.getInt("customerID"), rs.getString("fullName"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address"));
                c.setAvatar(rs.getString("avatar"));
                return c;
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
