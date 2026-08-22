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
