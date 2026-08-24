/*
 * 1 "trang" nội dung trong khung Nhân viên kho (InventoryShellFrm) — cùng mô
 * hình điều hướng 1-cửa-sổ như AdminPage/CustomerPage.
 */
package view.inventory;
import view.shared.*;

import javax.swing.JPanel;

public interface InventoryPage {

    /** Khóa mục sidebar tương ứng cần tô sáng khi đang ở trang này. */
    String navKey();

    /** Tiêu đề hiển thị ở topbar và title bar cửa sổ. */
    String title();

    /** Dựng nội dung trang. */
    JPanel content(InventoryShellFrm shell);
}
