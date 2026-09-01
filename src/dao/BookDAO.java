/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Book;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhthu632
 */
public class BookDAO extends DAO {

    public BookDAO() {
    }

    public Book[] searchBook(String keyword) {
        ArrayList<Book> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Book WHERE title LIKE ? OR ISBN LIKE ? OR CAST(bookID AS NVARCHAR(20)) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book b = new Book(
                        rs.getInt("bookID"),
                        rs.getString("ISBN"),
                        rs.getString("title"),
                        rs.getFloat("price"),
                        rs.getString("description"),
                        rs.getString("image"));
                list.add(b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Book[0]);
    }

    // Dùng cho trang chủ Khách hàng: lấy sách thuộc 1 danh mục (qua bảng
    // trung gian BookCategory) — trước đây Customer chưa có cách nào duyệt
    // sách theo danh mục dù dữ liệu Category/BookCategory đã có sẵn thật.
    public Book[] searchByCategory(int categoryID) {
        ArrayList<Book> list = new ArrayList<>();
        try {
            String sql = "SELECT b.* FROM Book b JOIN BookCategory bc ON b.bookID = bc.bookID WHERE bc.categoryID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, categoryID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Book(rs.getInt("bookID"), rs.getString("ISBN"), rs.getString("title"),
                        rs.getFloat("price"), rs.getString("description"), rs.getString("image")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Book[0]);
    }

    // Sách bán chạy thật, tính theo tổng số lượng trong OrderDetail của mọi
    // đơn (không lọc trạng thái — phản ánh nhu cầu đặt hàng thực tế) — dùng
    // cho trang chủ Khách hàng, khác "Sách nổi bật" (vốn chỉ lấy N sách đầu
    // tiên, không dựa trên doanh số thật).
    public Book[] bestSellers(int limit) {
        ArrayList<Book> list = new ArrayList<>();
        try {
            String sql = "SELECT TOP (?) b.bookID, b.ISBN, b.title, b.price, b.description, b.image "
                    + "FROM Book b JOIN OrderDetail od ON b.bookID = od.bookID "
                    + "GROUP BY b.bookID, b.ISBN, b.title, b.price, b.description, b.image "
                    + "ORDER BY SUM(od.quantity) DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Book(rs.getInt("bookID"), rs.getString("ISBN"), rs.getString("title"),
                        rs.getFloat("price"), rs.getString("description"), rs.getString("image")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Book[0]);
    }

    // FR-ADM-01 (Add Book): kiểm tra trùng ISBN trước (Book.ISBN UNIQUE) để trả
    // về thông báo cụ thể thay vì để CSDL từ chối rồi báo lỗi chung chung.
    // Validate giá > 0 (Book.price CHECK) do phía gọi (AddBookPage) đảm nhiệm
    // trước khi gọi hàm này, theo đúng phân công trong SRS. publisherID bắt
    // buộc (Book.publisherID NOT NULL); categoryIDs/authorIDs có thể rỗng —
    // gán qua BookCategory/BookAuthor trong cùng transaction với INSERT Book
    // để không bao giờ để lại 1 Book "mồ côi" nếu nửa sau thất bại.
    public String addBook(Book book, int publisherID, List<Integer> categoryIDs, List<Integer> authorIDs) {
        try {
            con.setAutoCommit(false);

            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Book WHERE ISBN = ?");
            psCheck.setString(1, book.getISBN());
            if (psCheck.executeQuery().next()) {
                con.rollback();
                return "ISBN này đã tồn tại, vui lòng dùng ISBN khác!";
            }

            String sql = "INSERT INTO Book (ISBN, title, price, description, image, publisherID) VALUES (?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getISBN());
            ps.setString(2, book.getTitle());
            ps.setFloat(3, book.getPrice());
            ps.setString(4, book.getDescription());
            ps.setString(5, book.getImage());
            ps.setInt(6, publisherID);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (!keys.next()) {
                con.rollback();
                return "Thêm sách thất bại, vui lòng thử lại!";
            }
            int bookID = keys.getInt(1);
            book.setBookID(bookID);
            book.setPublisherID(publisherID);

            linkCategories(bookID, categoryIDs);
            linkAuthors(bookID, authorIDs);

            con.commit();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return "Thêm sách thất bại do lỗi hệ thống, vui lòng thử lại!";
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void linkCategories(int bookID, List<Integer> categoryIDs) throws java.sql.SQLException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO BookCategory (bookID, categoryID) VALUES (?,?)");
        for (int categoryID : categoryIDs) {
            ps.setInt(1, bookID);
            ps.setInt(2, categoryID);
            ps.executeUpdate();
        }
    }

    private void linkAuthors(int bookID, List<Integer> authorIDs) throws java.sql.SQLException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO BookAuthor (bookID, authorID) VALUES (?,?)");
        for (int authorID : authorIDs) {
            ps.setInt(1, bookID);
            ps.setInt(2, authorID);
            ps.executeUpdate();
        }
    }

    // Dùng để load lại lựa chọn hiện có khi mở EditBookPage.
    public int getPublisherID(int bookID) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT publisherID FROM Book WHERE bookID = ?");
            ps.setInt(1, bookID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("publisherID");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public List<Integer> getCategoryIDs(int bookID) {
        List<Integer> result = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT categoryID FROM BookCategory WHERE bookID = ?");
            ps.setInt(1, bookID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("categoryID"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public List<Integer> getAuthorIDs(int bookID) {
        List<Integer> result = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT authorID FROM BookAuthor WHERE bookID = ?");
            ps.setInt(1, bookID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("authorID"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // FR-ADM-01 (Delete Book): Book.bookID được nhiều bảng khác tham chiếu
    // (BookCategory, BookAuthor, Inventory, CartItem, OrderDetail) và các khóa
    // ngoại này KHÔNG có ON DELETE CASCADE (khác Category/Author — xem AS-02),
    // nên phải tự kiểm tra trước và báo rõ lý do thay vì để CSDL ném lỗi
    // ràng buộc khó hiểu.
    public String deleteBook(int bookID) {
        try {
            String[] checks = {
                "SELECT 1 FROM BookCategory WHERE bookID = ?",
                "SELECT 1 FROM BookAuthor WHERE bookID = ?",
                "SELECT 1 FROM Inventory WHERE bookID = ?",
                "SELECT 1 FROM CartItem WHERE bookID = ?",
                "SELECT 1 FROM OrderDetail WHERE bookID = ?"
            };
            for (String sql : checks) {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, bookID);
                if (ps.executeQuery().next()) {
                    return "Không thể xóa sách này vì đang có dữ liệu liên quan (danh mục, tác giả, tồn kho, giỏ hàng hoặc đơn hàng)!";
                }
            }

            PreparedStatement psDelete = con.prepareStatement("DELETE FROM Book WHERE bookID = ?");
            psDelete.setInt(1, bookID);
            psDelete.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Xóa sách thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }

    public boolean updateBook(Book book, int publisherID, List<Integer> categoryIDs, List<Integer> authorIDs) {
        try {
            con.setAutoCommit(false);

            String sql = "UPDATE Book SET ISBN = ?, title = ?, price = ?, description = ?, image = ?, publisherID = ? WHERE bookID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, book.getISBN());
            ps.setString(2, book.getTitle());
            ps.setFloat(3, book.getPrice());
            ps.setString(4, book.getDescription());
            ps.setString(5, book.getImage());
            ps.setInt(6, publisherID);
            ps.setInt(7, book.getBookID());
            ps.executeUpdate();
            book.setPublisherID(publisherID);

            PreparedStatement psDelCat = con.prepareStatement("DELETE FROM BookCategory WHERE bookID = ?");
            psDelCat.setInt(1, book.getBookID());
            psDelCat.executeUpdate();
            linkCategories(book.getBookID(), categoryIDs);

            PreparedStatement psDelAuthor = con.prepareStatement("DELETE FROM BookAuthor WHERE bookID = ?");
            psDelAuthor.setInt(1, book.getBookID());
            psDelAuthor.executeUpdate();
            linkAuthors(book.getBookID(), authorIDs);

            con.commit();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                con.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
