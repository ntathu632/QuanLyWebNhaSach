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
public class ShoppingCart implements Serializable {

    private int cartID;
    private Date createdDate;
    private CartItem[] items;

    // Không có trong class diagram gốc, nhưng cần thiết để ShoppingCartFrm/CheckoutFrm
    // biết Order thuộc về Customer nào khi checkout, vì bản thân ShoppingCart không giữ sẵn Customer.
    private Customer customer;

    public ShoppingCart() {
    }

    public ShoppingCart(int cartID, Date createdDate, CartItem[] items) {
        this.cartID = cartID;
        this.createdDate = createdDate;
        this.items = items;
    }

    public int getCartID() {
        return cartID;
    }

    public void setCartID(int cartID) {
        this.cartID = cartID;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public CartItem[] getItems() {
        return items;
    }

    public void setItems(CartItem[] items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
