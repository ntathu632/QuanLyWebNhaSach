/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Category;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author anhthu632
 */
public class CategoryDAO extends DAO {

    public CategoryDAO() {
    }

    public Category[] searchCategory(String keyword) {
        ArrayList<Category> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Category WHERE categoryName LIKE ? OR CAST(categoryID AS NVARCHAR(20)) LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Category(rs.getInt("categoryID"), rs.getString("categoryName"), rs.getString("description")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list.toArray(new Category[0]);
    }

    // FR-ADM-02 (Add Category): Category.categoryName UNIQUE — kiểm tra trùng
    // trước để trả thông báo cụ thể thay vì để CSDL từ chối.
    public String addCategory(Category category) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Category WHERE categoryName = ?");
            psCheck.setString(1, category.getCategoryName());
            if (psCheck.executeQuery().next()) {
                return "Tên danh mục này đã tồn tại, vui lòng dùng tên khác!";
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Category (categoryName, description) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                category.setCategoryID(keys.getInt(1));
            }
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Thêm danh mục thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }

    public String updateCategory(Category category) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM Category WHERE categoryName = ? AND categoryID <> ?");
            psCheck.setString(1, category.getCategoryName());
            psCheck.setInt(2, category.getCategoryID());
            if (psCheck.executeQuery().next()) {
                return "Tên danh mục này đã tồn tại, vui lòng dùng tên khác!";
            }

            PreparedStatement ps = con.prepareStatement("UPDATE Category SET categoryName = ?, description = ? WHERE categoryID = ?");
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryID());
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Cập nhật danh mục thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }

    // AS-02: không cho xóa Category đang có sách thuộc về nó. FK_BookCategory_Category
    // có ON DELETE CASCADE ở tầng CSDL (xóa Category sẽ tự xóa luôn dòng liên kết
    // trong BookCategory), nhưng đây là RÀNG BUỘC NGHIỆP VỤ chặt hơn khả năng của
    // CSDL — phải tự kiểm tra ở tầng ứng dụng, không dựa vào CSDL từ chối giúp.
    public String deleteCategory(int categoryID) {
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM BookCategory WHERE categoryID = ?");
            psCheck.setInt(1, categoryID);
            if (psCheck.executeQuery().next()) {
                return "Không thể xóa danh mục này vì đang có sách thuộc về nó!";
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM Category WHERE categoryID = ?");
            ps.setInt(1, categoryID);
            ps.executeUpdate();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Xóa danh mục thất bại do lỗi hệ thống, vui lòng thử lại!";
        }
    }
}
