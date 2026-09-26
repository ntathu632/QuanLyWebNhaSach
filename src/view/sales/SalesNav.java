/*
 * Điều hướng dùng chung giữa các trang Nhân viên bán hàng (JFrame Form). Mỗi
 * trang có sidebar riêng thiết kế trong Design; bấm mục nào thì mở JFrame
 * Form của mục đó đúng vị trí/kích thước cửa sổ hiện tại
 * (AppTheme.switchFrame), rồi đóng cửa sổ cũ.
 */
package view.sales;

import javax.swing.JButton;
import javax.swing.JFrame;
import model.Employee;
import view.customer.CustomerHomeFrm;
import view.shared.AppTheme;

final class SalesNav {

    private SalesNav() {
    }

    /** Mở trang ứng với mục sidebar "key" (instore, invoices, orders, customers, books, lowstock). */
    static void go(JFrame from, Employee employee, String key) {
        JFrame to = switch (key) {
            case "customers" -> new SearchCustomerFrm(employee);
            case "books" -> new SalesSearchBookFrm(employee);
            case "lowstock" -> new SalesLowStockFrm(employee);
            case "instore" -> new InStoreSaleFrm(employee);
            case "invoices" -> new InvoiceHistoryFrm(employee);
            case "orders" -> new OrderManagementFrm(employee);
            default -> throw new IllegalArgumentException("Mục sidebar không tồn tại: " + key);
        };
        AppTheme.switchFrame(from, to);
    }

    /** Đăng xuất: quay về trang chủ Khách hàng ở chế độ khách vãng lai. */
    static void logout(JFrame from) {
        CustomerHomeFrm.openAsGuest();
        from.dispose();
    }

    /**
     * Hiệu ứng hover/bo góc của sidebar (FlatLaf style, không đặt được trong
     * Design): nút của trang đang mở chữ sáng, các nút còn lại chữ mờ.
     */
    static void styleSidebar(JButton active, JButton... all) {
        for (JButton b : all) {
            if (b == active) {
                AppTheme.sidebarNav(b);
            } else {
                AppTheme.sidebarNavMuted(b);
            }
        }
    }

    /** Dòng chào ở topbar. */
    static String greeting(Employee employee) {
        String name = (employee != null && employee.getFullName() != null) ? employee.getFullName() : "nhân viên";
        return "Xin chào, " + name;
    }
}
