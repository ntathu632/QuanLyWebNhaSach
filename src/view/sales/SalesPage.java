/*
 * 1 "trang" nội dung trong khung Nhân viên bán hàng (SalesShellFrm) — cùng
 * mô hình điều hướng 1-cửa-sổ như AdminPage/CustomerPage/InventoryPage.
 */
package view.sales;
import view.shared.*;

import javax.swing.JPanel;

public interface SalesPage {

    /** Khóa mục sidebar tương ứng cần tô sáng khi đang ở trang này. */
    String navKey();

    /** Tiêu đề hiển thị ở topbar và title bar cửa sổ. */
    String title();

    /** Dựng nội dung trang. */
    JPanel content(SalesShellFrm shell);
}
