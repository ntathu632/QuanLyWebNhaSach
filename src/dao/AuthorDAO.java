/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Author;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author anhthu632
 */
public class AuthorDAO extends DAO {

    public AuthorDAO() {
    }

    public Author[] searchAuthor(String keyword) {
        ArrayList<Author> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Author WHERE authorName LIKE ? OR CAST(authorID AS NVARCHAR(20)) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Author(rs.getInt("authorID"), rs.getString("authorName"),
                        rs.getString("nationality"), rs.getString("description")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Author[0]);
    }

    public boolean addAuthor(Author author) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Author (authorName, nationality, description) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, author.getAuthorName());
            ps.setString(2, author.getNationality());
            ps.setString(3, author.getDescription());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                author.setAuthorID(keys.getInt(1));
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateAuthor(Author author) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE Author SET authorName = ?, nationality = ?, description = ? WHERE authorID = ?");
            ps.setString(1, author.getAuthorName());
            ps.setString(2, author.getNationality());
            ps.setString(3, author.getDescription());
            ps.setInt(4, author.getAuthorID());
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // AS-02: không cho xóa Author đang có sách gắn với mình. FK_BookAuthor_Author
    // có ON DELETE CASCADE ở tầng CSDL, nhưng đây là ràng buộc nghiệp vụ chặt
    // hơn — phải tự kiểm tra ở tầng ứng dụng.
    public String deleteAuthor(int authorID) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM BookAuthor WHERE authorID = ?");
            psCheck.setInt(1, authorID);
            if (psCheck.executeQuery().next()) {
                return "Không thể xóa tác giả này vì đang có sách gắn với tác giả này!";
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM Author WHERE authorID = ?");
            ps.setInt(1, authorID);
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Xóa tác giả thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }
}
