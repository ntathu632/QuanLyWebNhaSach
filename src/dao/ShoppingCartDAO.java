/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Book;
import model.CartItem;
import model.Customer;
import model.ShoppingCart;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author anhthu632
 */
public class ShoppingCartDAO extends DAO {

    public ShoppingCartDAO() {
    }

    // Không có trong class diagram gốc, nhưng cần thiết để lấy (hoặc tạo mới nếu
    // Customer chưa từng có giỏ hàng) ShoppingCart tương ứng, vì DB quy định mỗi
    // Customer có tối đa 1 ShoppingCart.
    public ShoppingCart getOrCreateCart(Customer customer) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT cartID, createdDate FROM ShoppingCart WHERE customerID = ?");
            ps.setInt(1, customer.getCustomerID());
            ResultSet rs = ps.executeQuery();

            ShoppingCart cart = new ShoppingCart();
            cart.setCustomer(customer);

            if (rs.next()) {
                cart.setCartID(rs.getInt("cartID"));
                cart.setCreatedDate(rs.getTimestamp("createdDate"));
                return cart;
            }

            PreparedStatement psInsert = con.prepareStatement(
                    "INSERT INTO ShoppingCart (customerID) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            psInsert.setInt(1, customer.getCustomerID());
            psInsert.executeUpdate();

            ResultSet keys = psInsert.getGeneratedKeys();
            if (keys.next()) {
                cart.setCartID(keys.getInt(1));
            }
            return cart;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Đếm số loại sách (số dòng) trong giỏ hàng để hiện badge ở navbar — 1 loại
    // sách dù số lượng 2 vẫn tính là 1 — CHỈ ĐỌC,
    // không tạo mới ShoppingCart nếu khách chưa từng có giỏ (khác với
    // getOrCreateCart(), vốn tạo giỏ ngay cả khi chỉ đang xem trang, không phải
    // hành vi mong muốn cho việc hiện badge).
    public int countItemsIfCartExists(Customer customer) {
        try {
            String sql = "SELECT COUNT(ci.bookID) AS cnt FROM ShoppingCart sc "
                    + "LEFT JOIN CartItem ci ON ci.cartID = sc.cartID WHERE sc.customerID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customer.getCustomerID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean addCartItem(CartItem item) {
        try {
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT quantity, subtotal FROM CartItem WHERE cartID = ? AND bookID = ?");
            psCheck.setInt(1, item.getCartID());
            psCheck.setInt(2, item.getBook().getBookID());
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                int newQuantity = rs.getInt("quantity") + item.getQuantity();
                float newSubtotal = rs.getFloat("subtotal") + item.getSubtotal();

                PreparedStatement psUpdate = con.prepareStatement(
                        "UPDATE CartItem SET quantity = ?, subtotal = ? WHERE cartID = ? AND bookID = ?");
                psUpdate.setInt(1, newQuantity);
                psUpdate.setFloat(2, newSubtotal);
                psUpdate.setInt(3, item.getCartID());
                psUpdate.setInt(4, item.getBook().getBookID());
                psUpdate.executeUpdate();
            } else {
                PreparedStatement psInsert = con.prepareStatement(
                        "INSERT INTO CartItem (cartID, bookID, quantity, unitPrice, subtotal) VALUES (?,?,?,?,?)");
                psInsert.setInt(1, item.getCartID());
                psInsert.setInt(2, item.getBook().getBookID());
                psInsert.setInt(3, item.getQuantity());
                psInsert.setFloat(4, item.getUnitPrice());
                psInsert.setFloat(5, item.getSubtotal());
                psInsert.executeUpdate();
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean removeCartItem(CartItem item) {
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM CartItem WHERE cartID = ? AND bookID = ?");
            ps.setInt(1, item.getCartID());
            ps.setInt(2, item.getBook().getBookID());
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public CartItem[] getCartItems(ShoppingCart cart) {
        ArrayList<CartItem> list = new ArrayList<>();
        try {
            String sql = "SELECT ci.quantity, ci.unitPrice, ci.subtotal, b.bookID, b.ISBN, b.title, b.price, b.description, b.image "
                    + "FROM CartItem ci JOIN Book b ON ci.bookID = b.bookID WHERE ci.cartID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, cart.getCartID());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("bookID"),
                        rs.getString("ISBN"),
                        rs.getString("title"),
                        rs.getFloat("price"),
                        rs.getString("description"),
                        rs.getString("image"));

                CartItem item = new CartItem(rs.getInt("quantity"), rs.getFloat("unitPrice"), rs.getFloat("subtotal"), book);
                item.setCartID(cart.getCartID());
                list.add(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new CartItem[0]);
    }
}
