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
public class Employee implements Serializable {

    private int employeeID;
    private String fullName;
    private String position;
    private String phone;
    private String email;

    // Không có trong class diagram gốc — chỉ dùng để nhớ username của Account
    // gắn với Employee này (populate qua AccountDAO.getEmployeeByUsername()),
    // cần cho FR-COM-02 (Change Password). Không lưu xuống CSDL.
    private String username;

    public Employee() {
    }

    public Employee(int employeeID, String fullName, String position, String phone, String email) {
        this.employeeID = employeeID;
        this.fullName = fullName;
        this.position = position;
        this.phone = phone;
        this.email = email;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
