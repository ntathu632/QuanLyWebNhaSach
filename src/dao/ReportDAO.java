/*
 * Truy vấn cho 3 loại báo cáo Admin (FR-ADM-08 Doanh thu, FR-ADM-09 Tồn kho,
 * FR-ADM-10 Bán hàng) — chỉ đọc dữ liệu, tổng hợp từ nhiều bảng nên tách
 * riêng khỏi các DAO theo từng entity, giống cách DashboardDAO đã làm.
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhthu632
 */
public class ReportDAO extends DAO {

    public static class RevenueOrderRow {

        public final int orderID;
        public final String orderDate;
        public final String customerName;
        public final float totalAmount;

        public RevenueOrderRow(int orderID, String orderDate, String customerName, float totalAmount) {
            this.orderID = orderID;
            this.orderDate = orderDate;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
        }
    }

    public static class InventoryRow {

        public final String bookTitle;
        public final int importQuantity;
        public final int exportQuantity;
        public final int stockQuantity;

        public InventoryRow(String bookTitle, int importQuantity, int exportQuantity, int stockQuantity) {
            this.bookTitle = bookTitle;
            this.importQuantity = importQuantity;
            this.exportQuantity = exportQuantity;
            this.stockQuantity = stockQuantity;
        }
    }

    public static class BestSellerRow {

        public final String bookTitle;
        public final int totalSold;

        public BestSellerRow(String bookTitle, int totalSold) {
            this.bookTitle = bookTitle;
            this.totalSold = totalSold;
        }
    }

    public static class EmployeeSalesRow {

        public final String employeeName;
        public final int orderCount;
        public final float totalAmount;

        public EmployeeSalesRow(String employeeName, int orderCount, float totalAmount) {
            this.employeeName = employeeName;
            this.orderCount = orderCount;
            this.totalAmount = totalAmount;
        }
    }

    public ReportDAO() {
    }

    // FR-ADM-08: chỉ tính đơn đã "Paid" (khớp AS-03, giống DashboardDAO.totalRevenue()),
    // lọc theo ngày đặt hàng (không tính giờ) nằm trong [start, end].
    public List<RevenueOrderRow> revenueOrders(java.sql.Date start, java.sql.Date end) {
        List<RevenueOrderRow> result = new ArrayList<>();
        try {
            String sql = "SELECT o.orderID, o.orderDate, o.totalAmount, c.fullName "
                    + "FROM Orders o JOIN Customer c ON o.customerID = c.customerID "
                    + "WHERE o.status = 'Paid' AND CAST(o.orderDate AS DATE) BETWEEN ? AND ? "
                    + "ORDER BY o.orderDate";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, start);
            ps.setDate(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dateLabel = new java.text.SimpleDateFormat("dd/MM/yyyy").format(rs.getTimestamp("orderDate"));
                result.add(new RevenueOrderRow(rs.getInt("orderID"), dateLabel, rs.getString("fullName"), rs.getFloat("totalAmount")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // FR-ADM-09: Inventory chỉ lưu số liệu hiện tại theo từng sách (không có
    // cột ngày), nên báo cáo "theo khoảng thời gian" trong SRS không áp dụng
    // được với dữ liệu thật hiện có — chỉ làm được "theo sách" (đúng như SRS
    // đã nêu 2 lựa chọn "theo khoảng thời gian HOẶC theo sách").
    public List<InventoryRow> inventoryReport() {
        List<InventoryRow> result = new ArrayList<>();
        try {
            String sql = "SELECT b.title, i.importQuantity, i.exportQuantity, i.stockQuantity "
                    + "FROM Inventory i JOIN Book b ON i.bookID = b.bookID "
                    + "ORDER BY i.stockQuantity ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new InventoryRow(rs.getString("title"), rs.getInt("importQuantity"),
                        rs.getInt("exportQuantity"), rs.getInt("stockQuantity")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // FR-ADM-10 (sách bán chạy): tổng số lượng bán ra theo OrderDetail của mọi
    // đơn (không lọc trạng thái — phản ánh nhu cầu đặt hàng thực tế, kể cả đơn
    // đang chờ/đã hủy vẫn cho thấy sách được quan tâm).
    public List<BestSellerRow> bestSellers(int limit) {
        List<BestSellerRow> result = new ArrayList<>();
        try {
            String sql = "SELECT TOP (?) b.title, SUM(od.quantity) AS totalSold "
                    + "FROM OrderDetail od JOIN Book b ON od.bookID = b.bookID "
                    + "GROUP BY b.title ORDER BY totalSold DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new BestSellerRow(rs.getString("title"), rs.getInt("totalSold")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // FR-ADM-10 (doanh số theo nhân viên xử lý): dựa vào Invoice.employeeID
    // (xem BR-09) — LƯU Ý: ứng dụng hiện tại chưa có luồng nào tự tạo Invoice
    // khi thanh toán (OrderDAO.addOrder()/PaymentDAO chỉ ghi Orders/Payment),
    // nên số liệu này chỉ phản ánh đúng các Invoice đã có sẵn trong CSDL.
    public List<EmployeeSalesRow> salesByEmployee() {
        List<EmployeeSalesRow> result = new ArrayList<>();
        try {
            String sql = "SELECT e.fullName, COUNT(*) AS orderCount, SUM(iv.totalAmount) AS total "
                    + "FROM Invoice iv JOIN Employee e ON iv.employeeID = e.employeeID "
                    + "GROUP BY e.fullName ORDER BY total DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new EmployeeSalesRow(rs.getString("fullName"), rs.getInt("orderCount"), rs.getFloat("total")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
