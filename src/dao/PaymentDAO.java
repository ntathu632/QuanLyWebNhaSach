/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Payment;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author anhthu632
 */
public class PaymentDAO extends DAO {

    public PaymentDAO() {
    }

    public boolean addPayment(Payment payment) {
        try {
            String sql = "INSERT INTO Payment (paymentDate, amount, paymentMethod, status, orderID) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, new java.sql.Timestamp(payment.getPaymentDate().getTime()));
            ps.setFloat(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getStatus());
            ps.setInt(5, payment.getOrderID());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                payment.setPaymentID(keys.getInt(1));
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Dùng khi hủy 1 hóa đơn tại quầy đã thanh toán (InvoiceHistoryPage) — đổi
    // trạng thái giao dịch sang "Refunded" thay vì để "Paid" treo dù đơn đã
    // Cancelled. UNIQUE(orderID) trên Payment nên mỗi order chỉ có đúng 1 dòng.
    public boolean updatePaymentStatus(int orderID, String status) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Payment SET status = ? WHERE orderID = ?");
            ps.setString(1, status);
            ps.setInt(2, orderID);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
