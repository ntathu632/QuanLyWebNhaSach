/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;

/**
 *
 * @author anhthu632
 */
public class Customer implements Serializable {

    private int customerID;
    private String fullName;
    private String phone;
    private String email;
    private String address;

    // Tên file ảnh đại diện, lưu trong thư mục images/avatars/ (xem
    // view.shared.AvatarStore) — NULL nghĩa là chưa upload, dùng icon mặc định.
    private String avatar;

    // Không có trong class diagram gốc — chỉ dùng để nhớ username của Account
    // gắn với Customer này (populate qua AccountDAO.getCustomerByUsername()),
    // cần cho FR-COM-02 (Change Password) vì CustomerShellFrm chỉ giữ Customer,
    // không giữ Account. Không lưu xuống CSDL.
    private String username;

    public Customer() {
    }

    public Customer(int customerID, String fullName, String phone, String email, String address) {
        this.customerID = customerID;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
