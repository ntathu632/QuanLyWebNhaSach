/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Publisher;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author anhthu632
 */
public class PublisherDAO extends DAO {

    public PublisherDAO() {
    }

    public Publisher[] searchPublisher(String keyword) {
        ArrayList<Publisher> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Publisher WHERE publisherName LIKE ? OR CAST(publisherID AS NVARCHAR(20)) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Publisher(rs.getInt("publisherID"), rs.getString("publisherName"),
                        rs.getString("address"), rs.getString("phone"), rs.getString("email")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Publisher[0]);
    }

    public boolean addPublisher(Publisher publisher) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Publisher (publisherName, address, phone, email) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, publisher.getPublisherName());
            ps.setString(2, publisher.getAddress());
            ps.setString(3, publisher.getPhone());
            ps.setString(4, publisher.getEmail());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                publisher.setPublisherID(keys.getInt(1));
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updatePublisher(Publisher publisher) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Publisher SET publisherName = ?, address = ?, phone = ?, email = ? WHERE publisherID = ?");
            ps.setString(1, publisher.getPublisherName());
            ps.setString(2, publisher.getAddress());
            ps.setString(3, publisher.getPhone());
            ps.setString(4, publisher.getEmail());
            ps.setInt(5, publisher.getPublisherID());
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // AS-02: không cho xóa Publisher đang có sách thuộc về mình. Khác Category/
    // Author, FK_Book_Publisher KHÔNG có ON DELETE CASCADE nên CSDL vốn đã tự
    // chặn — vẫn tự kiểm tra trước để báo lý do rõ ràng thay vì lỗi CSDL thô.
    public String deletePublisher(int publisherID) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Book WHERE publisherID = ?");
            psCheck.setInt(1, publisherID);
            if (psCheck.executeQuery().next()) {
                return "Không thể xóa nhà xuất bản này vì đang có sách thuộc về nhà xuất bản này!";
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM Publisher WHERE publisherID = ?");
            ps.setInt(1, publisherID);
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Xóa nhà xuất bản thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }
}
