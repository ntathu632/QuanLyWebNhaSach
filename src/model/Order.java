/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author anhthu632
 */
public class Order implements Serializable {

    private int orderID;
    private Date orderDate;
    private float totalAmount;
    private String shippingAddress;
    private String status;
    private Customer customer;
    private OrderDetail[] details;
    private String paymentMethod;

    // Không có trong class diagram gốc, nhưng cần thiết để OrderDAO biết phải xóa
    // CartItem của giỏ hàng nào sau khi đặt hàng thành công.
    private int cartID;

    public Order() {
    }

    public Order(int orderID, Date orderDate, float totalAmount, String shippingAddress, String status, Customer customer, OrderDetail[] details) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.customer = customer;
        this.details = details;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderDetail[] getDetails() {
        return details;
    }

    public void setDetails(OrderDetail[] details) {
        this.details = details;
    }

    // Không có trong class diagram gốc, nhưng cần thiết để CheckoutPage lưu phương thức
    // thanh toán đã chọn, sau đó PaymentPage/PaymentDAO đọc lại để tạo Payment tương ứng.
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getCartID() {
        return cartID;
    }

    public void setCartID(int cartID) {
        this.cartID = cartID;
    }
}
