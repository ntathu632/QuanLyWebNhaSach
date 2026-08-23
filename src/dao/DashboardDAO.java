/*
 * Truy vấn thống kê tổng quan cho trang Dashboard (Admin Home). Chỉ đọc dữ
 * liệu (COUNT/SUM), không sửa gì — tách riêng khỏi các DAO khác vì các số
 * liệu này tổng hợp từ nhiều bảng (Book, Orders, Customer), không thuộc
 * riêng 1 entity nào.
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
public class DashboardDAO extends DAO {

    // 1 điểm dữ liệu cho biểu đồ: nhãn (ngày, dạng dd/MM) + số tiền ngày đó.
    public static class RevenuePoint {

        public final String label;
        public final float amount;

        public RevenuePoint(String label, float amount) {
            this.label = label;
            this.amount = amount;
        }
    }

    // 1 điểm dữ liệu dạng nhãn + số lượng — dùng cho biểu đồ tròn/cột ngang
    // (trạng thái đơn hàng, phương thức thanh toán...).
    public static class LabelCount {

        public final String label;
        public final float count;

        public LabelCount(String label, float count) {
            this.label = label;
            this.count = count;
        }
    }

    public DashboardDAO() {
    }

    // Doanh thu gộp theo từng ngày, chỉ tính đơn đã "Paid" (khớp totalRevenue()),
    // sắp theo ngày tăng dần — dùng để vẽ biểu đồ cột trên Dashboard.
    public List<RevenuePoint> revenueByDay() {
        List<RevenuePoint> result = new ArrayList<>();
        try {
            String sql = "SELECT CAST(orderDate AS DATE) AS d, SUM(totalAmount) AS total "
                    + "FROM Orders WHERE status = 'Paid' "
                    + "GROUP BY CAST(orderDate AS DATE) ORDER BY d";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.sql.Date d = rs.getDate("d");
                String label = new java.text.SimpleDateFormat("dd/MM").format(d);
                result.add(new RevenuePoint(label, rs.getFloat("total")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // Số lượng đơn hàng theo từng ngày (mọi trạng thái) — dùng cho biểu đồ
    // đường, thể hiện xu hướng số đơn khác với biểu đồ doanh thu.
    public List<RevenuePoint> orderCountByDay() {
        List<RevenuePoint> result = new ArrayList<>();
        try {
            String sql = "SELECT CAST(orderDate AS DATE) AS d, COUNT(*) AS cnt "
                    + "FROM Orders GROUP BY CAST(orderDate AS DATE) ORDER BY d";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.sql.Date d = rs.getDate("d");
                String label = new java.text.SimpleDateFormat("dd/MM").format(d);
                result.add(new RevenuePoint(label, rs.getFloat("cnt")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // Tỉ lệ đơn hàng theo trạng thái (Pending/Paid/Cancelled — xem AS-03) —
    // dùng cho biểu đồ tròn/donut.
    public List<LabelCount> orderStatusBreakdown() {
        return labelCountQuery("SELECT status, COUNT(*) AS cnt FROM Orders GROUP BY status ORDER BY cnt DESC");
    }

    // Số đơn theo từng phương thức thanh toán — dùng cho biểu đồ cột ngang.
    public List<LabelCount> paymentMethodBreakdown() {
        return labelCountQuery("SELECT paymentMethod, COUNT(*) AS cnt FROM Payment GROUP BY paymentMethod ORDER BY cnt DESC");
    }

    private List<LabelCount> labelCountQuery(String sql) {
        List<LabelCount> result = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new LabelCount(rs.getString(1), rs.getFloat(2)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public int countBooks() {
        return countRows("SELECT COUNT(*) FROM Book");
    }

    public int countOrders() {
        return countRows("SELECT COUNT(*) FROM Orders");
    }

    public int countCustomers() {
        return countRows("SELECT COUNT(*) FROM Customer");
    }

    // Chỉ tính doanh thu từ các đơn đã thanh toán thành công (status = "Paid",
    // xem AS-03 trong SRS) — không tính đơn Pending/Cancelled vào doanh thu.
    public float totalRevenue() {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT ISNULL(SUM(totalAmount), 0) FROM Orders WHERE status = 'Paid'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private int countRows(String sql) {
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
