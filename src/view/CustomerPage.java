/*
 * 1 "trang" nội dung trong khung Khách hàng (CustomerShellFrm) — cùng mô hình
 * điều hướng 1-cửa-sổ như AdminPage/AdminShellFrm bên phần Admin.
 */
package view;

import javax.swing.JPanel;

public interface CustomerPage {

    /** Khóa mục navbar tương ứng cần tô sáng khi đang ở trang này, vd "home". */
    String navKey();

    /** Tiêu đề hiển thị ở title bar cửa sổ. */
    String title();

    /** Dựng nội dung trang. "shell" dùng để trang tự điều hướng sang trang khác. */
    JPanel content(CustomerShellFrm shell);

    /** Trang đích khi bấm nút "quay lại" ở topbar; null = trang gốc, không hiện nút quay lại. */
    default CustomerPage backTarget() {
        return null;
    }
}
