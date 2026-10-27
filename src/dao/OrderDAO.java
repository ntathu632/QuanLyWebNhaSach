/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Book;
import model.Customer;
import model.Order;
import model.OrderDetail;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhthu632
 */
public class OrderDAO extends DAO {

    public OrderDAO() {
    }

    // Dùng transaction: nếu lưu OrderDetail bị lỗi giữa chừng thì rollback toàn bộ,
    // tránh để lại 1 Orders mồ côi không có chi tiết đơn hàng đi kèm.
    //
    // BR-04 (kiểm tra đủ tồn kho) + BR-03 (tự động cập nhật tồn kho sau khi
    // bán) — trước đây chỉ InvoiceDAO.createInStoreSale() (kênh tại quầy) làm
    // việc này, khiến 2 kênh bán lệch nhau (online không trừ kho). Nay áp
    // dụng cùng logic cho cả kênh online, đúng tinh thần BR-03: "bán online
    // và bán tại quầy dùng chung dữ liệu tồn kho".
    // Trả về null nếu thành công, hoặc thông báo lỗi cụ thể (thiếu tồn kho
    // hoặc lỗi hệ thống) — đổi từ boolean sang String để báo đúng lý do.
    public String addOrder(Order order) {
        if (order.getDetails() == null || order.getDetails().length == 0) {
            return "Đơn hàng chưa có sách nào!";
        }
        try {
            con.setAutoCommit(false);

            for (OrderDetail d : order.getDetails()) {
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

            String sqlOrder = "INSERT INTO Orders (orderDate, totalAmount, shippingAddress, status, customerID) VALUES (?,?,?,?,?)";
            PreparedStatement psOrder = con.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setTimestamp(1, new java.sql.Timestamp(order.getOrderDate().getTime()));
            psOrder.setFloat(2, order.getTotalAmount());
            psOrder.setString(3, order.getShippingAddress());
            psOrder.setString(4, order.getStatus());
            psOrder.setInt(5, order.getCustomer().getCustomerID());
            psOrder.executeUpdate();

            ResultSet keys = psOrder.getGeneratedKeys();
            if (!keys.next()) {
                con.rollback();
                return "Đặt hàng thất bại, vui lòng thử lại!";
            }
            int orderID = keys.getInt(1);
            order.setOrderID(orderID);

            for (OrderDetail d : order.getDetails()) {
                String sqlDetail = "INSERT INTO OrderDetail (orderID, bookID, quantity, unitPrice, subtotal) VALUES (?,?,?,?,?)";
                PreparedStatement psDetail = con.prepareStatement(sqlDetail);
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

            if (order.getCartID() > 0) {
                PreparedStatement psClearCart = con.prepareStatement("DELETE FROM CartItem WHERE cartID = ?");
                psClearCart.setInt(1, order.getCartID());
                psClearCart.executeUpdate();
            }

            con.commit();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return "Đặt hàng thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // FR-CUS-08 (View Purchase History): toàn bộ đơn hàng của 1 Customer,
    // mới nhất trước — đúng yêu cầu "sắp xếp theo ngày đặt hàng".
    public List<Order> getOrdersByCustomer(int customerID) {
        List<Order> list = new ArrayList<>();
        try {
            String sql = "SELECT orderID, orderDate, totalAmount, shippingAddress, status "
                    + "FROM Orders WHERE customerID = ? ORDER BY orderDate DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customerID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setOrderID(rs.getInt("orderID"));
                o.setOrderDate(rs.getTimestamp("orderDate"));
                o.setTotalAmount(rs.getFloat("totalAmount"));
                o.setShippingAddress(rs.getString("shippingAddress"));
                o.setStatus(rs.getString("status"));
                list.add(o);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // FR-CUS-07 (Track Order Status — "extend của View Purchase History"):
    // chi tiết từng sách trong 1 đơn, dùng khi Customer chọn xem 1 đơn cụ thể.
    public List<OrderDetail> getOrderDetails(int orderID) {
        List<OrderDetail> list = new ArrayList<>();
        try {
            String sql = "SELECT od.quantity, od.unitPrice, od.subtotal, "
                    + "b.bookID, b.ISBN, b.title, b.price, b.description, b.image "
                    + "FROM OrderDetail od JOIN Book b ON od.bookID = b.bookID WHERE od.orderID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Book book = new Book(rs.getInt("bookID"), rs.getString("ISBN"), rs.getString("title"),
                        rs.getFloat("price"), rs.getString("description"), rs.getString("image"));
                list.add(new OrderDetail(rs.getInt("quantity"), rs.getFloat("unitPrice"), rs.getFloat("subtotal"), book));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // FR-SALE-02 (Process Online Order): "đơn hàng trực tuyến" = đơn CHƯA có
    // Invoice (Invoice chỉ được tạo qua kênh tại quầy — InvoiceDAO.createInStoreSale()),
    // nên dùng NOT EXISTS làm tiêu chí phân biệt kênh thay vì thêm cột mới —
    // khớp đúng cách hiểu Payment/Invoice độc lập đã đính chính trong SRS (BR-09).
    public List<Order> getOrdersWithoutInvoice() {
        List<Order> list = new ArrayList<>();
        try {
            String sql = "SELECT o.orderID, o.orderDate, o.totalAmount, o.shippingAddress, o.status, "
                    + "c.customerID, c.fullName, c.phone, c.email, c.address "
                    + "FROM Orders o JOIN Customer c ON o.customerID = c.customerID "
                    + "WHERE NOT EXISTS (SELECT 1 FROM Invoice iv WHERE iv.orderID = o.orderID) "
                    + "ORDER BY o.orderDate DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setOrderID(rs.getInt("orderID"));
                o.setOrderDate(rs.getTimestamp("orderDate"));
                o.setTotalAmount(rs.getFloat("totalAmount"));
                o.setShippingAddress(rs.getString("shippingAddress"));
                o.setStatus(rs.getString("status"));
                Customer c = new Customer();
                c.setCustomerID(rs.getInt("customerID"));
                c.setFullName(rs.getString("fullName"));
                c.setPhone(rs.getString("phone"));
                c.setEmail(rs.getString("email"));
                c.setAddress(rs.getString("address"));
                o.setCustomer(c);
                list.add(o);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // FR-CUS-10/FR-SALE-04 (Cancel Online Order) include Update Stock Quantity
    // (BR-03, FR-INV-02): khi chuyển đơn sang "Cancelled" (và trước đó chưa
    // Cancelled — tránh hoàn kho 2 lần nếu gọi lặp), hoàn lại đúng số lượng
    // từng sách trong OrderDetail — đối xứng với phần trừ kho ở addOrder().
    public boolean updateOrderStatus(int orderID, String status) {
        try {
            con.setAutoCommit(false);

            PreparedStatement psCurrent = con.prepareStatement("SELECT status FROM Orders WHERE orderID = ?");
            psCurrent.setInt(1, orderID);
            ResultSet rsCurrent = psCurrent.executeQuery();
            String currentStatus = rsCurrent.next() ? rsCurrent.getString(1) : null;

            // Đơn đã hủy là trạng thái cuối: sách đã được hoàn kho (và thanh toán
            // nếu có đã chuyển "Refunded"), đổi ngược lại sẽ làm lệch tồn kho.
            if ("Cancelled".equals(currentStatus) && !"Cancelled".equals(status)) {
                con.rollback();
                return false;
            }

            if ("Cancelled".equals(status)) {
                if (!"Cancelled".equals(currentStatus)) {
                    PreparedStatement psDetails = con.prepareStatement("SELECT bookID, quantity FROM OrderDetail WHERE orderID = ?");
                    psDetails.setInt(1, orderID);
                    ResultSet rsDetails = psDetails.executeQuery();
                    while (rsDetails.next()) {
                        PreparedStatement psRestock = con.prepareStatement(
                                "UPDATE Inventory SET stockQuantity = stockQuantity + ?, exportQuantity = exportQuantity - ? WHERE bookID = ?");
                        psRestock.setInt(1, rsDetails.getInt("quantity"));
                        psRestock.setInt(2, rsDetails.getInt("quantity"));
                        psRestock.setInt(3, rsDetails.getInt("bookID"));
                        psRestock.executeUpdate();
                    }
                }
            }

            // Nhân viên đặt "Paid" bằng tay (đã thu tiền khi giao hàng) mà đơn
            // chưa có Payment thì tạo luôn 1 Payment "Paid" cho khớp. Đơn khách tự
            // thanh toán đã có Payment từ trước (UNIQUE orderID) nên bỏ qua.
            if ("Paid".equals(status)) {
                PreparedStatement psPayment = con.prepareStatement(
                        "INSERT INTO Payment (amount, paymentMethod, status, orderID) "
                        + "SELECT totalAmount, 'Cash on Delivery', 'Paid', orderID FROM Orders o "
                        + "WHERE o.orderID = ? AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.orderID = o.orderID)");
                psPayment.setInt(1, orderID);
                psPayment.executeUpdate();
            }

            PreparedStatement ps = con.prepareStatement("UPDATE Orders SET status = ? WHERE orderID = ?");
            ps.setString(1, status);
            ps.setInt(2, orderID);
            ps.executeUpdate();

            con.commit();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
