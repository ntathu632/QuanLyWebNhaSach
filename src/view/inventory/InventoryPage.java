/*
 * 1 "trang" nội dung trong khung Nhân viên kho (InventoryShellFrm) — KHÔNG phải 1 cửa sổ riêng.
 * Điều hướng giữa các trang chỉ thay nội dung bên trong cùng 1 JFrame
 * (sidebar/topbar giữ nguyên), giống AdminPage bên Admin.
 */
package view.inventory;

import javax.swing.JPanel;

public interface InventoryPage {

    /** Khóa mục sidebar cần tô sáng khi đang ở trang này, vd "lowstock". */
    String navKey();

    /** Tiêu đề hiển thị trên title bar cửa sổ. */
    String title();

    /** Dựng nội dung trang. "shell" dùng để trang tự điều hướng sang trang khác. */
    JPanel content(InventoryShellFrm shell);
}
