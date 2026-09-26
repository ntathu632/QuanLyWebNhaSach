/*
 * Điều hướng dùng chung giữa các trang Nhân viên kho (JFrame Form). Mỗi
 * trang có sidebar riêng thiết kế trong Design; bấm mục nào thì mở JFrame
 * Form của mục đó đúng vị trí/kích thước cửa sổ hiện tại
 * (AppTheme.switchFrame), rồi đóng cửa sổ cũ.
 */
package view.inventory;

import javax.swing.JButton;
import javax.swing.JFrame;
import model.Employee;
import view.customer.CustomerHomeFrm;
import view.shared.AppTheme;

final class InventoryNav {

    private InventoryNav() {
    }

    /** Mở trang ứng với mục sidebar "key" (lowstock, import, return, checking, history, books). */
    static void go(JFrame from, Employee employee, String key) {
        JFrame to = switch (key) {
            case "lowstock" -> new LowStockFrm(employee);
            case "books" -> new InventorySearchBookFrm(employee);
            case "history" -> new StockHistoryFrm(employee);
            case "import" -> new ImportBooksFrm(employee);
            case "return" -> new ReturnToSupplierFrm(employee);
            case "checking" -> new InventoryCheckingFrm(employee);
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
        String name = (employee != null && employee.getFullName() != null) ? employee.getFullName() : "nhân viên kho";
        return "Xin chào, " + name;
    }
}
