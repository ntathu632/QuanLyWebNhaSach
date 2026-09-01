/*
 * Không có trong Entity Class.drawio gốc — bổ sung theo đúng ghi chú của
 * FR-CUS-09 (Rate Book) trong SRS.
 */
package model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author anhthu632
 */
public class Review implements Serializable {

    private int reviewID;
    private int bookID;
    private int customerID;
    private int rating;
    private String comment;
    private Date reviewDate;

    // Không lưu xuống CSDL — chỉ dùng để hiển thị tên khách đã đánh giá
    // (join từ Customer.fullName) trong danh sách đánh giá của 1 sách.
    private String customerName;

    // Không lưu xuống CSDL — chỉ dùng để hiển thị tên sách (join từ Book.title)
    // trong danh sách đánh giá của 1 khách hàng (chiều ngược lại với customerName).
    private String bookTitle;

    public Review() {
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
