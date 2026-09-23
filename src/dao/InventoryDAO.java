/*
 * FR-INV-02 (Update Stock Quantity — dùng chung), FR-INV-03 (Inventory
 * Checking), FR-INV-04 (View Low-Stock Books). FR-INV-01 (Import Books) giờ
 * nằm ở StockTransactionDAO.recordImport() — gộp chung 1 transaction với
 * việc ghi log nhập kho (StockTransaction).
 */
package dao;

import model.Inventory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhthu632
 */
public class InventoryDAO extends DAO {

    public InventoryDAO() {
    }

    // Không phải sách nào cũng đã có sẵn dòng Inventory (sách mới thêm qua
    // AddBookPage bên Admin chưa có) — trả về bản ghi 0/0/0 nếu chưa có, để
    // các màn Inventory Staff vẫn thao tác được bình thường (sẽ tự tạo dòng
    // Inventory khi nhập kho lần đầu).
    public Inventory getInventoryForBook(int bookID) {
        Inventory inv = new Inventory();
        inv.setBookID(bookID);
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT importQuantity, exportQuantity, stockQuantity FROM Inventory WHERE bookID = ?");
            ps.setInt(1, bookID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                inv.setImportQuantity(rs.getInt("importQuantity"));
                inv.setExportQuantity(rs.getInt("exportQuantity"));
                inv.setStockQuantity(rs.getInt("stockQuantity"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return inv;
    }

    // FR-INV-03: sửa thẳng stockQuantity theo số lượng kiểm đếm thực tế —
    // không đổi importQuantity/exportQuantity (2 cột đó là số cộng dồn lịch
    // sử nhập/xuất, không phải số cần khớp với 1 lần kiểm kê).
    public boolean adjustStock(int bookID, int actualQuantity) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Inventory WHERE bookID = ?");
            psCheck.setInt(1, bookID);
            if (psCheck.executeQuery().next()) {
                PreparedStatement ps = con.prepareStatement("UPDATE Inventory SET stockQuantity = ? WHERE bookID = ?");
                ps.setInt(1, actualQuantity);
                ps.setInt(2, bookID);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Inventory (bookID, importQuantity, exportQuantity, stockQuantity) VALUES (?,0,0,?)");
                ps.setInt(1, bookID);
                ps.setInt(2, actualQuantity);
                ps.executeUpdate();
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // FR-INV-04: sách dưới ngưỡng "threshold" — dùng LEFT JOIN vì sách chưa
    // từng có dòng Inventory coi như tồn kho 0, cũng phải tính là sắp hết hàng.
    public List<Inventory> lowStock(int threshold) {
        List<Inventory> list = new ArrayList<>();
        try {
            String sql = "SELECT b.bookID, b.title, ISNULL(i.importQuantity,0) AS importQuantity, "
                    + "ISNULL(i.exportQuantity,0) AS exportQuantity, ISNULL(i.stockQuantity,0) AS stockQuantity "
                    + "FROM Book b LEFT JOIN Inventory i ON b.bookID = i.bookID "
                    + "WHERE ISNULL(i.stockQuantity,0) < ? ORDER BY stockQuantity ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setBookID(rs.getInt("bookID"));
                inv.setBookTitle(rs.getString("title"));
                inv.setImportQuantity(rs.getInt("importQuantity"));
                inv.setExportQuantity(rs.getInt("exportQuantity"));
                inv.setStockQuantity(rs.getInt("stockQuantity"));
                list.add(inv);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Toàn bộ sách kèm tồn kho hiện tại — dùng cho màn Kiểm kê kho.
    public List<Inventory> allInventory() {
        List<Inventory> list = new ArrayList<>();
        try {
            String sql = "SELECT b.bookID, b.title, ISNULL(i.importQuantity,0) AS importQuantity, "
                    + "ISNULL(i.exportQuantity,0) AS exportQuantity, ISNULL(i.stockQuantity,0) AS stockQuantity "
                    + "FROM Book b LEFT JOIN Inventory i ON b.bookID = i.bookID ORDER BY b.title";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setBookID(rs.getInt("bookID"));
                inv.setBookTitle(rs.getString("title"));
                inv.setImportQuantity(rs.getInt("importQuantity"));
                inv.setExportQuantity(rs.getInt("exportQuantity"));
                inv.setStockQuantity(rs.getInt("stockQuantity"));
                list.add(inv);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
