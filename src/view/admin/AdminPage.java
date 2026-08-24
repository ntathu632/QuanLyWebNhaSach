/*
 * 1 "trang" nội dung trong khung Admin (AdminShellFrm) — KHÔNG phải 1 cửa
 * sổ riêng. Điều hướng giữa các trang chỉ thay nội dung bên trong cùng 1
 * JFrame (sidebar/topbar giữ nguyên, không mở cửa sổ mới), giống cách các
 * dashboard admin chuyên nghiệp (kiểu SB Admin 2) hoạt động.
 */
package view.admin;
import view.shared.*;

import javax.swing.JPanel;

public interface AdminPage {

    /** Khóa mục sidebar tương ứng cần tô sáng khi đang ở trang này, vd "books". */
    String navKey();

    /** Tiêu đề hiển thị ở topbar và title bar cửa sổ. */
    String title();

    /** Dựng nội dung trang. "shell" dùng để trang tự điều hướng sang trang khác (vd double-click 1 dòng bảng). */
    JPanel content(AdminShellFrm shell);

    /** Trang đích khi bấm nút "quay lại" ở topbar; null = trang gốc, không hiện nút quay lại. */
    default AdminPage backTarget() {
        return null;
    }
}
