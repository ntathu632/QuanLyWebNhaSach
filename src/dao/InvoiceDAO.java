/*
 * FR-SALE-01 (Create In-Store Invoice) — include Check Stock Availability
 * (BR-04) và Update Stock Quantity (BR-03, FR-INV-02). Cùng logic kiểm tra/
 * trừ tồn kho như OrderDAO.addOrder() (kênh online) — 2 kênh dùng chung 1
 * cách xử lý tồn kho, đúng tinh thần BR-03.
 */
package dao;

import model.Invoice;
import model.OrderDetail;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author anhthu632
 */
public class InvoiceDAO extends DAO {

    public InvoiceDAO() {
    }

    // Trả về null nếu thành công, hoặc thông báo lỗi cụ thể (thiếu tồn kho,
    // hoặc lỗi hệ thống) — theo đúng pattern String-error đã dùng ở các DAO khác.
    // outOrderId[0] được set = orderID vừa tạo khi thành công — cần cho
    // InStoreSalePage in hóa đơn ngay sau khi lập (số hóa đơn trên biên lai).
    public String createInStoreSale(int customerID, List<OrderDetail> details, String paymentMethod, int employeeID, int[] outOrderId) {
        if (details == null || details.isEmpty()) {
            return "Hóa đơn chưa có sách nào!";
        }
        try {
            con.setAutoCommit(false);

            // BR-04: kiểm tra đủ tồn kho cho TỪNG sách trước khi tạo gì cả.
            // Cộng dồn các dòng cùng 1 sách (thêm cùng sách nhiều lần vào hóa
            // đơn) rồi mới so với tồn kho — so riêng từng dòng thì 2 dòng mỗi
            // dòng vừa đủ vẫn lọt qua và làm tồn kho bị âm.
            Map<Integer, Integer> neededByBook = new LinkedHashMap<>();
            Map<Integer, String> titleByBook = new LinkedHashMap<>();
            for (OrderDetail d : details) {
                int bookID = d.getBook().getBookID();
                neededByBook.merge(bookID, d.getQuantity(), Integer::sum);
                titleByBook.putIfAbsent(bookID, d.getBook().getTitle());
            }
            for (Map.Entry<Integer, Integer> e : neededByBook.entrySet()) {
                PreparedStatement psStock = con.prepareStatement(
                        "SELECT ISNULL(stockQuantity, 0) FROM Inventory WHERE bookID = ?");
                psStock.setInt(1, e.getKey());
                ResultSet rs = psStock.executeQuery();
                int stock = rs.next() ? rs.getInt(1) : 0;
                if (stock < e.getValue()) {
                    con.rollback();
                    return "Không đủ tồn kho cho sách \"" + titleByBook.get(e.getKey())
                            + "\" (còn " + stock + ", cần " + e.getValue() + ")!";
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
            if (outOrderId != null && outOrderId.length > 0) {
                outOrderId[0] = orderID;
            }
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

    // FR-SALE-07 (Invoice History): toàn bộ hóa đơn lập tại quầy, kèm tên
    // khách hàng/nhân viên và trạng thái đơn hiện tại (Paid/Cancelled) — cần
    // để InvoiceHistoryPage tra cứu lại và hủy hóa đơn đã lập.
    public List<Invoice> getInStoreInvoices() {
        List<Invoice> list = new ArrayList<>();
        try {
            String sql = "SELECT iv.invoiceID, iv.invoiceDate, iv.totalAmount, iv.paymentMethod, "
                    + "iv.orderID, iv.employeeID, o.status, c.fullName AS customerName, "
                    + "e.fullName AS employeeName "
                    + "FROM Invoice iv "
                    + "JOIN Orders o ON o.orderID = iv.orderID "
                    + "JOIN Customer c ON c.customerID = o.customerID "
                    + "JOIN Employee e ON e.employeeID = iv.employeeID "
                    + "ORDER BY iv.invoiceDate DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invoice iv = new Invoice();
                iv.setInvoiceID(rs.getInt("invoiceID"));
                iv.setInvoiceDate(rs.getTimestamp("invoiceDate"));
                iv.setTotalAmount(rs.getFloat("totalAmount"));
                iv.setPaymentMethod(rs.getString("paymentMethod"));
                iv.setOrderID(rs.getInt("orderID"));
                iv.setEmployeeID(rs.getInt("employeeID"));
                iv.setOrderStatus(rs.getString("status"));
                iv.setCustomerName(rs.getString("customerName"));
                iv.setEmployeeName(rs.getString("employeeName"));
                list.add(iv);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
