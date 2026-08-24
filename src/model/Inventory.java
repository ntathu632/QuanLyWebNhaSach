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
public class Inventory implements Serializable {

    private int bookID;
    private int importQuantity;
    private int exportQuantity;
    private int stockQuantity;

    // Không có trong class diagram gốc — chỉ dùng để hiển thị tên sách
    // (join từ Book.title) trong các màn quản lý kho, không lưu xuống CSDL.
    private String bookTitle;

    public Inventory() {
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getImportQuantity() {
        return importQuantity;
    }

    public void setImportQuantity(int importQuantity) {
        this.importQuantity = importQuantity;
    }

    public int getExportQuantity() {
        return exportQuantity;
    }

    public void setExportQuantity(int exportQuantity) {
        this.exportQuantity = exportQuantity;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
