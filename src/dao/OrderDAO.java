/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Order;
import model.OrderDetail;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author anhthu632
 */
public class OrderDAO extends DAO {

    public OrderDAO() {
    }

    // Dùng transaction: nếu lưu OrderDetail bị lỗi giữa chừng thì rollback toàn bộ,
    // tránh để lại 1 Orders mồ côi không có chi tiết đơn hàng đi kèm.
    public boolean addOrder(Order order) {
        try {
            con.setAutoCommit(false);

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
                return false;
            }
            int orderID = keys.getInt(1);
            order.setOrderID(orderID);

            if (order.getDetails() != null) {
                for (OrderDetail d : order.getDetails()) {
                    String sqlDetail = "INSERT INTO OrderDetail (orderID, bookID, quantity, unitPrice, subtotal) VALUES (?,?,?,?,?)";
                    PreparedStatement psDetail = con.prepareStatement(sqlDetail);
                    psDetail.setInt(1, orderID);
                    psDetail.setInt(2, d.getBook().getBookID());
                    psDetail.setInt(3, d.getQuantity());
                    psDetail.setFloat(4, d.getUnitPrice());
                    psDetail.setFloat(5, d.getSubtotal());
                    psDetail.executeUpdate();
                }
            }

            if (order.getCartID() > 0) {
                PreparedStatement psClearCart = con.prepareStatement("DELETE FROM CartItem WHERE cartID = ?");
                psClearCart.setInt(1, order.getCartID());
                psClearCart.executeUpdate();
            }

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
