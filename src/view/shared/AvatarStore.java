/*
 * Lưu trữ file ảnh đại diện khách hàng trên đĩa cục bộ, dưới thư mục
 * images/avatars/ ngay tại thư mục làm việc của ứng dụng (cùng cấp với
 * BookshopManagement.sql) — DB chỉ lưu tên file (Customer.avatar), không
 * lưu blob ảnh.
 */
package view.shared;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AvatarStore {

    private static final String DIR = "images" + File.separator + "avatars";

    private AvatarStore() {
    }

    /** Trả về File thực tế trên đĩa cho 1 tên file đã lưu trong Customer.avatar (có thể không tồn tại/null). */
    public static File resolve(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        return new File(DIR, filename);
    }

    /**
     * Copy file ảnh khách vừa chọn vào thư mục lưu trữ, đặt tên theo customerID
     * (duy nhất, đã biết trước khi upload — khác BookImageStore dự kiến dùng
     * ISBN vì Book có thể chưa có ISBN xác định lúc chọn ảnh).
     * Trả về tên file đã lưu (để ghi vào Customer.avatar), hoặc null nếu lỗi.
     */
    public static String save(File sourceFile, int customerID) {
        try {
            File dir = new File(DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String name = sourceFile.getName();
            int dot = name.lastIndexOf('.');
            String ext = dot >= 0 ? name.substring(dot) : "";
            String filename = customerID + ext;
            Path target = new File(dir, filename).toPath();
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
