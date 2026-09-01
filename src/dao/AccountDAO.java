/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Account;
import model.Customer;
import model.Employee;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

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

    // FR-ADM-05 (Manage User Accounts): danh sách tài khoản kèm tên chủ tài
    // khoản thật (Customer.fullName hoặc Employee.fullName tùy CK_Account_Owner —
    // xem AS-04) để admin biết tài khoản đó là của ai, không chỉ mỗi username.
    public Account[] searchAccounts(String keyword) {
        ArrayList<Account> list = new ArrayList<>();
        try {
            String sql = "SELECT a.username, a.role, a.status, "
                    + "COALESCE(c.fullName, e.fullName) AS ownerName "
                    + "FROM Account a "
                    + "LEFT JOIN Customer c ON a.customerID = c.customerID "
                    + "LEFT JOIN Employee e ON a.employeeID = e.employeeID "
                    + "WHERE a.username LIKE ? OR COALESCE(c.fullName, e.fullName) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Account a = new Account();
                a.setUsername(rs.getString("username"));
                a.setRole(rs.getString("role"));
                a.setStatus(rs.getString("status"));
                a.setOwnerName(rs.getString("ownerName"));
                list.add(a);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Account[0]);
    }

    // FR-ADM-05 (Update/Delete-Lock Account) + FR-ADM-07 (Assign Permissions —
    // "thay đổi giá trị trường role", theo đúng SRS). "Delete-Lock" nghĩa là
    // khóa tài khoản qua status="Locked" thay vì DELETE thật (an toàn hơn, giữ
    // nguyên lịch sử đơn hàng/dữ liệu gắn với Customer/Employee chủ tài khoản).
    public boolean updateAccountRoleStatus(String username, String role, String status) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE Account SET role = ?, status = ? WHERE username = ?");
            ps.setString(1, role);
            ps.setString(2, status);
            ps.setString(3, username);
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Lấy Customer gắn với 1 Account (qua Account.customerID) — cần thiết để
    // sau khi đăng nhập thành công với role="Customer", LoginFrm có Customer
    // thật để mở CustomerShellFrm (các màn khách hàng đều thao tác trên Customer,
    // không phải trên Account).
    public Customer getCustomerByUsername(String username) {
        try {
            String sql = "SELECT c.customerID, c.fullName, c.phone, c.email, c.address, c.avatar "
                    + "FROM Customer c JOIN Account a ON c.customerID = a.customerID "
                    + "WHERE a.username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("customerID"),
                        rs.getString("fullName"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"));
                c.setUsername(username);
                c.setAvatar(rs.getString("avatar"));
                return c;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Lấy Employee gắn với 1 Account (qua Account.employeeID) — cần thiết để
    // sau khi đăng nhập thành công với role nhân viên (Sales Staff/Inventory
    // Staff/Admin qua đường nhân viên), có Employee thật để mở khung tương ứng.
    public Employee getEmployeeByUsername(String username) {
        try {
            String sql = "SELECT e.employeeID, e.fullName, e.position, e.phone, e.email "
                    + "FROM Employee e JOIN Account a ON e.employeeID = a.employeeID "
                    + "WHERE a.username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Employee emp = new Employee(
                        rs.getInt("employeeID"),
                        rs.getString("fullName"),
                        rs.getString("position"),
                        rs.getString("phone"),
                        rs.getString("email"));
                emp.setUsername(username);
                return emp;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // FR-COM-02 (Change Password) — chung cho mọi actor có tài khoản. Xác
    // minh lại mật khẩu hiện tại trực tiếp trong câu UPDATE (WHERE password
    // = mật khẩu cũ) để chỉ đổi được đúng 1 dòng nếu khớp, tránh phải SELECT
    // rồi so sánh riêng.
    public String changePassword(String username, String currentPassword, String newPassword) {
        try {
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT 1 FROM Account WHERE username = ? AND password = ?");
            psCheck.setString(1, username);
            psCheck.setString(2, currentPassword);
            if (!psCheck.executeQuery().next()) {
                return "Mật khẩu hiện tại không đúng!";
            }

            PreparedStatement ps = con.prepareStatement("UPDATE Account SET password = ? WHERE username = ?");
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Đổi mật khẩu thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
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
