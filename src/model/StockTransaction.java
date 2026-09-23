/*
 * Không có trong Entity Class.drawio gốc — 1 dòng nhật ký nhập/trả hàng kho
 * (xem BookshopManagement.sql, mục 16. STOCKTRANSACTION). "type" = "Import"
 * hoặc "Return".
 */
package model;

import java.io.Serializable;
import java.util.Date;

public class StockTransaction implements Serializable {

    private int stockTransactionID;
    private String type;
    private int quantity;
    private Date transactionDate;
    private String note;

    // Không lưu FK thô (bookID/supplierID/employeeID) trong model hiển thị —
    // populate thẳng tên qua JOIN ở StockTransactionDAO.getHistory(), đủ dùng
    // cho màn hình lịch sử, không cần load lại từng entity con.
    private String bookTitle;
    private String supplierName;
    private String employeeName;

    public StockTransaction() {
    }

    public int getStockTransactionID() {
        return stockTransactionID;
    }

    public void setStockTransactionID(int stockTransactionID) {
        this.stockTransactionID = stockTransactionID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
