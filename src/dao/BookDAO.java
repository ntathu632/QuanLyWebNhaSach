/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Book;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

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

    // FR-ADM-01 (Add Book): kiểm tra trùng ISBN trước (Book.ISBN UNIQUE) để trả
    // về thông báo cụ thể thay vì để CSDL từ chối rồi báo lỗi chung chung.
    // Validate giá > 0 (Book.price CHECK) do phía gọi (AddBookPage) đảm nhiệm
    // trước khi gọi hàm này, theo đúng phân công trong SRS.
    public String addBook(Book book) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Book WHERE ISBN = ?");
            psCheck.setString(1, book.getISBN());
            if (psCheck.executeQuery().next()) {
                return "ISBN này đã tồn tại, vui lòng dùng ISBN khác!";
            }

            String sql = "INSERT INTO Book (ISBN, title, price, description, image) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, book.getISBN());
            ps.setString(2, book.getTitle());
            ps.setFloat(3, book.getPrice());
            ps.setString(4, book.getDescription());
            ps.setString(5, book.getImage());
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Thêm sách thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
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

    public boolean updateBook(Book book) {
        try {
            String sql = "UPDATE Book SET ISBN = ?, title = ?, price = ?, description = ?, image = ? WHERE bookID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, book.getISBN());
            ps.setString(2, book.getTitle());
            ps.setFloat(3, book.getPrice());
            ps.setString(4, book.getDescription());
            ps.setString(5, book.getImage());
            ps.setInt(6, book.getBookID());
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
