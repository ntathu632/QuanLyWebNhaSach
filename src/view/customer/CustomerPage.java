/*
 * 1 "trang" nội dung trong khung Khách hàng (CustomerShellFrm) — KHÔNG phải 1
 * cửa sổ riêng. Điều hướng giữa các trang chỉ thay nội dung bên dưới navbar,
 * giống AdminPage bên Admin.
 */
package view.customer;

import javax.swing.JPanel;
import model.Customer;

public interface CustomerPage {

    /** Khóa mục thanh bên cần tô sáng khi đang ở trang này ("home", "orders", "cart"); khóa khác (vd. "search") hoặc "" = không tô mục nào. */
    String navKey();

    /** Tiêu đề hiển thị trên title bar cửa sổ (sau "Vintage Books - "). */
    String title();

    /** Dựng nội dung trang. "shell" dùng để điều hướng và lấy trạng thái đăng nhập. */
    JPanel content(CustomerShellFrm shell);

    /** Khách vừa đăng nhập trong lúc đang ở trang này (mặc định: không làm gì). */
    default void customerLoggedIn(model.Customer customer) {
    }
}
