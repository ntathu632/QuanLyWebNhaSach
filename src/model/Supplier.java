/*
 * Không có trong Entity Class.drawio gốc — bổ sung để nhân viên kho ghi nhận
 * nhập/trả hàng gắn với 1 nhà cung cấp cụ thể (xem BookshopManagement.sql,
 * mục 15. SUPPLIER).
 */
package model;

import java.io.Serializable;

public class Supplier implements Serializable {

    private int supplierID;
    private String supplierName;
    private String address;
    private String phone;
    private String email;

    public Supplier() {
    }

    public Supplier(int supplierID, String supplierName, String address, String phone, String email) {
        this.supplierID = supplierID;
        this.supplierName = supplierName;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public String toString() {
        return supplierName;
    }
}
