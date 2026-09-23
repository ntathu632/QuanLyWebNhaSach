/*
 * DAO cho Supplier (nhà cung cấp) — cùng pattern với PublisherDAO.
 */
package dao;

import model.Supplier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SupplierDAO extends DAO {

    public SupplierDAO() {
    }

    public Supplier[] searchSupplier(String keyword) {
        ArrayList<Supplier> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Supplier WHERE supplierName LIKE ? OR CAST(supplierID AS NVARCHAR(20)) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Supplier(rs.getInt("supplierID"), rs.getString("supplierName"),
                        rs.getString("address"), rs.getString("phone"), rs.getString("email")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Supplier[0]);
    }

    public boolean addSupplier(Supplier supplier) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Supplier (supplierName, address, phone, email) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getAddress());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                supplier.setSupplierID(keys.getInt(1));
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateSupplier(Supplier supplier) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Supplier SET supplierName = ?, address = ?, phone = ?, email = ? WHERE supplierID = ?");
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getAddress());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setInt(5, supplier.getSupplierID());
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Không cho xóa Supplier đang có giao dịch nhập/trả hàng gắn với mình
    // (StockTransaction.supplierID) — cùng tinh thần AS-02 đã áp dụng cho
    // Publisher (không xóa nếu còn dữ liệu phụ thuộc).
    public String deleteSupplier(int supplierID) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM StockTransaction WHERE supplierID = ?");
            psCheck.setInt(1, supplierID);
            if (psCheck.executeQuery().next()) {
                return "Không thể xóa nhà cung cấp này vì đang có giao dịch nhập/trả hàng gắn với nhà cung cấp này!";
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM Supplier WHERE supplierID = ?");
            ps.setInt(1, supplierID);
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Xóa nhà cung cấp thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }
}
