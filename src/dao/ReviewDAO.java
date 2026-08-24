/*
 * FR-CUS-09 (Rate Book). Bảng Review không có trong Entity Class.drawio gốc
 * — bổ sung theo đúng ghi chú trong SRS khi triển khai chức năng này.
 */
package dao;

import model.Review;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhthu632
 */
public class ReviewDAO extends DAO {

    public ReviewDAO() {
    }

    // AS-01 (điều kiện đánh giá): Customer chỉ được đánh giá 1 sách nếu đã
    // từng mua và đơn đã hoàn tất (status = "Paid", xem AS-03 — không có
    // trạng thái "Delivered" riêng trong hệ thống này).
    public boolean hasPurchased(int customerID, int bookID) {
        try {
            String sql = "SELECT 1 FROM OrderDetail od JOIN Orders o ON od.orderID = o.orderID "
                    + "WHERE o.customerID = ? AND od.bookID = ? AND o.status = 'Paid'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customerID);
            ps.setInt(2, bookID);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasReviewed(int customerID, int bookID) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Review WHERE customerID = ? AND bookID = ?");
            ps.setInt(1, customerID);
            ps.setInt(2, bookID);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Review> getReviewsForBook(int bookID) {
        List<Review> list = new ArrayList<>();
        try {
            String sql = "SELECT r.reviewID, r.rating, r.comment, r.reviewDate, c.fullName "
                    + "FROM Review r JOIN Customer c ON r.customerID = c.customerID "
                    + "WHERE r.bookID = ? ORDER BY r.reviewDate DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, bookID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Review r = new Review();
                r.setReviewID(rs.getInt("reviewID"));
                r.setBookID(bookID);
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setReviewDate(rs.getTimestamp("reviewDate"));
                r.setCustomerName(rs.getString("fullName"));
                list.add(r);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public float averageRating(int bookID) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT AVG(CAST(rating AS FLOAT)) FROM Review WHERE bookID = ?");
            ps.setInt(1, bookID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0f;
    }

    // Kiểm tra lại AS-01 + đã đánh giá chưa (UQ_Review_Book_Customer) ngay ở
    // DAO — không tin tưởng hoàn toàn vào UI đã chặn trước đó.
    public String addReview(Review review) {
        if (!hasPurchased(review.getCustomerID(), review.getBookID())) {
            return "Bạn chỉ có thể đánh giá sách đã mua và đơn hàng đã thanh toán thành công!";
        }
        if (hasReviewed(review.getCustomerID(), review.getBookID())) {
            return "Bạn đã đánh giá cuốn sách này rồi!";
        }
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Review (bookID, customerID, rating, comment) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, review.getBookID());
            ps.setInt(2, review.getCustomerID());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                review.setReviewID(keys.getInt(1));
            }
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Gửi đánh giá thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }
}
