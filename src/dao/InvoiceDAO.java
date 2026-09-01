/*
 * FR-SALE-01 (Create In-Store Invoice) — include Check Stock Availability
 * (BR-04) và Update Stock Quantity (BR-03, FR-INV-02). Cùng logic kiểm tra/
 * trừ tồn kho như OrderDAO.addOrder() (kênh online) — 2 kênh dùng chung 1
 * cách xử lý tồn kho, đúng tinh thần BR-03.
 */
package dao;

import model.OrderDetail;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author anhthu632
 */
public class InvoiceDAO extends DAO {

    public InvoiceDAO() {
    }

    // Trả về null nếu thành công, hoặc thông báo lỗi cụ thể (thiếu tồn kho,
    // hoặc lỗi hệ thống) — theo đúng pattern String-error đã dùng ở các DAO khác.
    public String createInStoreSale(int customerID, List<OrderDetail> details, String paymentMethod, int employeeID) {
        if (details == null || details.isEmpty()) {
            return "Hóa đơn chưa có sách nào!";
        }
        try {
            con.setAutoCommit(false);

            // BR-04: kiểm tra đủ tồn kho cho TỪNG sách trước khi tạo gì cả.
            for (OrderDetail d : details) {
                PreparedStatement psStock = con.prepareStatement(
                        "SELECT ISNULL(stockQuantity, 0) FROM Inventory WHERE bookID = ?");
                psStock.setInt(1, d.getBook().getBookID());
                ResultSet rs = psStock.executeQuery();
                int stock = rs.next() ? rs.getInt(1) : 0;
                if (stock < d.getQuantity()) {
                    con.rollback();
                    return "Không đủ tồn kho cho sách \"" + d.getBook().getTitle()
                            + "\" (còn " + stock + ", cần " + d.getQuantity() + ")!";
                }
            }

            float total = 0f;
            for (OrderDetail d : details) {
                total += d.getSubtotal();
            }

            String sqlOrder = "INSERT INTO Orders (orderDate, totalAmount, shippingAddress, status, customerID) VALUES (?,?,?,?,?)";
            PreparedStatement psOrder = con.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            psOrder.setFloat(2, total);
            psOrder.setString(3, "Mua tại quầy");
            psOrder.setString(4, "Paid");
            psOrder.setInt(5, customerID);
            psOrder.executeUpdate();

            ResultSet keys = psOrder.getGeneratedKeys();
            if (!keys.next()) {
                con.rollback();
                return "Lập hóa đơn thất bại, vui lòng thử lại!";
            }
            int orderID = keys.getInt(1);

            for (OrderDetail d : details) {
                PreparedStatement psDetail = con.prepareStatement(
                        "INSERT INTO OrderDetail (orderID, bookID, quantity, unitPrice, subtotal) VALUES (?,?,?,?,?)");
                psDetail.setInt(1, orderID);
                psDetail.setInt(2, d.getBook().getBookID());
                psDetail.setInt(3, d.getQuantity());
                psDetail.setFloat(4, d.getUnitPrice());
                psDetail.setFloat(5, d.getSubtotal());
                psDetail.executeUpdate();

                PreparedStatement psStockUpdate = con.prepareStatement(
                        "UPDATE Inventory SET exportQuantity = exportQuantity + ?, stockQuantity = stockQuantity - ? WHERE bookID = ?");
                psStockUpdate.setInt(1, d.getQuantity());
                psStockUpdate.setInt(2, d.getQuantity());
                psStockUpdate.setInt(3, d.getBook().getBookID());
                psStockUpdate.executeUpdate();
            }

            PreparedStatement psInvoice = con.prepareStatement(
                    "INSERT INTO Invoice (totalAmount, paymentMethod, orderID, employeeID) VALUES (?,?,?,?)");
            psInvoice.setFloat(1, total);
            psInvoice.setString(2, paymentMethod);
            psInvoice.setInt(3, orderID);
            psInvoice.setInt(4, employeeID);
            psInvoice.executeUpdate();

            // Ghi nhận giao dịch vào bảng Payment, cùng transaction với Invoice,
            // để khớp với kênh online (PaymentPage/MockPaymentGatewayPage) —
            // trước đây bán tại quầy chỉ có Invoice, Payment không có bản ghi nào.
            PreparedStatement psPayment = con.prepareStatement(
                    "INSERT INTO Payment (paymentDate, amount, paymentMethod, status, orderID) VALUES (?,?,?,?,?)");
            psPayment.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            psPayment.setFloat(2, total);
            psPayment.setString(3, paymentMethod);
            psPayment.setString(4, "Paid");
            psPayment.setInt(5, orderID);
            psPayment.executeUpdate();

            con.commit();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return "Lập hóa đơn thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
