/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Employee;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author anhthu632
 */
public class EmployeeDAO extends DAO {

    public EmployeeDAO() {
    }

    public Employee[] searchEmployee(String keyword) {
        ArrayList<Employee> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Employee WHERE fullName LIKE ? OR CAST(employeeID AS NVARCHAR(20)) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Employee(rs.getInt("employeeID"), rs.getString("fullName"),
                        rs.getString("position"), rs.getString("phone"), rs.getString("email")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Employee[0]);
    }

    // FR-ADM-06 (Add Employee — include Add Account, "bắt buộc" theo SRS):
    // tạo đồng thời Employee + Account trong 1 transaction, role Account mặc
    // định lấy theo đúng "position" vừa chọn (cùng 1 tập giá trị Admin/Sales
    // Staff/Inventory Staff) — sau này admin muốn đổi role riêng thì dùng
    // "Quản lý tài khoản" (FR-ADM-07). Employee.email UNIQUE, username Account
    // cũng phải duy nhất — cùng pattern kiểm tra như AccountDAO.register().
    public String addEmployee(Employee employee, String username, String password) {
        try {
            con.setAutoCommit(false);

            PreparedStatement psCheckEmail = con.prepareStatement("SELECT 1 FROM Employee WHERE email = ?");
            psCheckEmail.setString(1, employee.getEmail());
            if (psCheckEmail.executeQuery().next()) {
                con.rollback();
                return "Email này đã được dùng cho nhân viên khác, vui lòng dùng email khác!";
            }

            PreparedStatement psCheckUser = con.prepareStatement("SELECT 1 FROM Account WHERE username = ?");
            psCheckUser.setString(1, username);
            if (psCheckUser.executeQuery().next()) {
                con.rollback();
                return "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!";
            }

            String sqlEmployee = "INSERT INTO Employee (fullName, position, phone, email) VALUES (?,?,?,?)";
            PreparedStatement psEmployee = con.prepareStatement(sqlEmployee, Statement.RETURN_GENERATED_KEYS);
            psEmployee.setString(1, employee.getFullName());
            psEmployee.setString(2, employee.getPosition());
            psEmployee.setString(3, employee.getPhone());
            psEmployee.setString(4, employee.getEmail());
            psEmployee.executeUpdate();

            ResultSet keys = psEmployee.getGeneratedKeys();
            if (!keys.next()) {
                con.rollback();
                return "Thêm nhân viên thất bại, vui lòng thử lại!";
            }
            int employeeID = keys.getInt(1);
            employee.setEmployeeID(employeeID);

            String sqlAccount = "INSERT INTO Account (username, password, role, status, employeeID) VALUES (?,?,?,?,?)";
            PreparedStatement psAccount = con.prepareStatement(sqlAccount);
            psAccount.setString(1, username);
            psAccount.setString(2, password);
            psAccount.setString(3, employee.getPosition());
            psAccount.setString(4, "Active");
            psAccount.setInt(5, employeeID);
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
            return "Thêm nhân viên thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String updateEmployee(Employee employee) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Employee WHERE email = ? AND employeeID <> ?");
            psCheck.setString(1, employee.getEmail());
            psCheck.setInt(2, employee.getEmployeeID());
            if (psCheck.executeQuery().next()) {
                return "Email này đã được dùng cho nhân viên khác, vui lòng dùng email khác!";
            }

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Employee SET fullName = ?, position = ?, phone = ?, email = ? WHERE employeeID = ?");
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getPosition());
            ps.setString(3, employee.getPhone());
            ps.setString(4, employee.getEmail());
            ps.setInt(5, employee.getEmployeeID());
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Cập nhật nhân viên thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }

    // Mỗi Employee tạo qua Add Employee đều có sẵn 1 Account đi kèm
    // (FK_Account_Employee, không có ON DELETE CASCADE) nên không thể xóa
    // thẳng Employee khi tài khoản còn tồn tại — hướng admin sang khóa tài
    // khoản (Quản lý tài khoản) thay vì xóa hẳn nhân viên.
    public String deleteEmployee(int employeeID) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Account WHERE employeeID = ?");
            psCheck.setInt(1, employeeID);
            if (psCheck.executeQuery().next()) {
                return "Nhân viên này đang có tài khoản đăng nhập — vui lòng khóa tài khoản ở mục \"Quản lý tài khoản\" thay vì xóa nhân viên.";
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM Employee WHERE employeeID = ?");
            ps.setInt(1, employeeID);
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Xóa nhân viên thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }
}
