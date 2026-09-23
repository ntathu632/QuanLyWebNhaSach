/*
 * Nhật ký nhập/trả hàng kho (StockTransaction) — gộp phần cập nhật Inventory
 * + ghi log vào cùng 1 transaction, thay vì gọi InventoryDAO.importStock()
 * riêng (mỗi DAO có 1 Connection JDBC riêng, không đảm bảo atomic được nếu
 * tách 2 lệnh ra 2 DAO khác nhau).
 */
package dao;

import model.StockTransaction;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO extends DAO {

    public StockTransactionDAO() {
    }

    // Nhập kho: cộng importQuantity + stockQuantity (cùng logic
    // InventoryDAO.importStock()) rồi ghi 1 dòng StockTransaction type='Import'.
    public String recordImport(int bookID, int supplierID, int employeeID, int quantity, String note) {
        if (quantity <= 0) {
            return "Số lượng phải lớn hơn 0!";
        }
        try {
            con.setAutoCommit(false);

            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Inventory WHERE bookID = ?");
            psCheck.setInt(1, bookID);
            if (psCheck.executeQuery().next()) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE Inventory SET importQuantity = importQuantity + ?, stockQuantity = stockQuantity + ? WHERE bookID = ?");
                ps.setInt(1, quantity);
                ps.setInt(2, quantity);
                ps.setInt(3, bookID);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Inventory (bookID, importQuantity, exportQuantity, stockQuantity) VALUES (?,?,0,?)");
                ps.setInt(1, bookID);
                ps.setInt(2, quantity);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }

            insertTransaction(bookID, supplierID, employeeID, "Import", quantity, note);

            con.commit();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return "Nhập kho thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Trả hàng nhà cung cấp: trừ stockQuantity, KHÔNG đụng exportQuantity —
    // cột đó chỉ phản ánh số sách đã bán, trả về NCC không phải 1 lượt bán.
    public String recordReturn(int bookID, int supplierID, int employeeID, int quantity, String note) {
        if (quantity <= 0) {
            return "Số lượng phải lớn hơn 0!";
        }
        try {
            con.setAutoCommit(false);

            PreparedStatement psStock = con.prepareStatement(
                    "SELECT ISNULL(stockQuantity, 0) FROM Inventory WHERE bookID = ?");
            psStock.setInt(1, bookID);
            ResultSet rs = psStock.executeQuery();
            int stock = rs.next() ? rs.getInt(1) : 0;
            if (stock < quantity) {
                con.rollback();
                return "Không đủ tồn kho để trả hàng (còn " + stock + ", cần " + quantity + ")!";
            }

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Inventory SET stockQuantity = stockQuantity - ? WHERE bookID = ?");
            ps.setInt(1, quantity);
            ps.setInt(2, bookID);
            ps.executeUpdate();

            insertTransaction(bookID, supplierID, employeeID, "Return", quantity, note);

            con.commit();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return "Trả hàng thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void insertTransaction(int bookID, int supplierID, int employeeID, String type, int quantity, String note) throws Exception {
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO StockTransaction (bookID, supplierID, employeeID, type, quantity, note) VALUES (?,?,?,?,?,?)");
        ps.setInt(1, bookID);
        ps.setInt(2, supplierID);
        ps.setInt(3, employeeID);
        ps.setString(4, type);
        ps.setInt(5, quantity);
        ps.setString(6, (note == null || note.isEmpty()) ? null : note);
        ps.executeUpdate();
    }

    // Lịch sử giao dịch kho, mới nhất trước. typeFilter null = tất cả
    // ("Import"/"Return" để lọc riêng).
    public List<StockTransaction> getHistory(String typeFilter) {
        List<StockTransaction> list = new ArrayList<>();
        try {
            String sql = "SELECT st.stockTransactionID, st.type, st.quantity, st.transactionDate, st.note, "
                    + "b.title AS bookTitle, s.supplierName, e.fullName AS employeeName "
                    + "FROM StockTransaction st "
                    + "JOIN Book b ON b.bookID = st.bookID "
                    + "JOIN Supplier s ON s.supplierID = st.supplierID "
                    + "JOIN Employee e ON e.employeeID = st.employeeID "
                    + (typeFilter != null ? "WHERE st.type = ? " : "")
                    + "ORDER BY st.transactionDate DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            if (typeFilter != null) {
                ps.setString(1, typeFilter);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StockTransaction t = new StockTransaction();
                t.setStockTransactionID(rs.getInt("stockTransactionID"));
                t.setType(rs.getString("type"));
                t.setQuantity(rs.getInt("quantity"));
                t.setTransactionDate(rs.getTimestamp("transactionDate"));
                t.setNote(rs.getString("note"));
                t.setBookTitle(rs.getString("bookTitle"));
                t.setSupplierName(rs.getString("supplierName"));
                t.setEmployeeName(rs.getString("employeeName"));
                list.add(t);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
