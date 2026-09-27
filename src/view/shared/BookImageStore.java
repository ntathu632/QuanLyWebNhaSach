/*
 * Nơi lưu ảnh bìa sách thật trên đĩa, dùng chung giữa Admin (ghi — khi Thêm/
 * Sửa sách) và phía đọc (BookCard bên Customer, BookCoverPanel bên Sales). Trước đây cột Book.image chỉ
 * là 1 chuỗi text vô tác dụng (không có file thật đi kèm); giờ lưu file thật
 * vào thư mục images/books/ (tương đối theo thư mục làm việc của app) với
 * tên = ISBN, để không phụ thuộc bookID (chưa có lúc Thêm sách) và không
 * đụng độ giữa các sách khác nhau.
 */
package view.shared;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class BookImageStore {

    private static final File FOLDER = new File("images", "books");

    private BookImageStore() {
    }

    /** File thật ứng với tên đã lưu trong Book.image, hoặc null nếu filename rỗng. */
    public static File resolve(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return new File(FOLDER, filename.trim());
    }

    /**
     * Copy sourceFile vào images/books/ với tên "<ISBN>.<phần mở rộng gốc>",
     * trả về tên file đã lưu (để ghi vào Book.image). Ném IOException nếu
     * copy thất bại — bên gọi tự quyết định thông báo lỗi.
     */
    public static String save(File sourceFile, String isbn) throws IOException {
        FOLDER.mkdirs();
        String ext = extensionOf(sourceFile.getName());
        String storedName = isbn + (ext.isEmpty() ? "" : "." + ext);
        File target = new File(FOLDER, storedName);
        Files.copy(sourceFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase();
    }
}
