/*
 * Luồng đăng nhập dùng chung cho các cửa sổ phía Khách hàng: mở LoginDialog,
 * rồi điều hướng theo vai trò — Khách hàng thì trả về Customer để cửa sổ
 * hiện tại tự cập nhật; nhân viên thì mở khung theo vai trò và đóng cửa sổ
 * hiện tại.
 */
package view.customer;

import dao.AccountDAO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import model.Account;
import model.Customer;
import view.admin.AdminShellFrm;
import view.inventory.InventoryShellFrm;
import view.sales.SalesShellFrm;
import view.shared.LoginDialog;

final class CustomerLogin {

    private CustomerLogin() {
    }

    /**
     * Trả về Customer nếu đăng nhập bằng tài khoản Khách hàng; null nếu người
     * dùng đóng hộp thoại, hoặc đăng nhập bằng tài khoản nhân viên (khi đó
     * "owner" đã bị đóng, thay bằng khung của vai trò đó).
     */
    static Customer show(JFrame owner, boolean startWithRegister) {
        Account account = LoginDialog.show(owner, startWithRegister);
        if (account == null) {
            return null;
        }
        AccountDAO accountDAO = new AccountDAO();
        switch (account.getRole()) {
            case "Customer" -> {
                return accountDAO.getCustomerByUsername(account.getUsername());
            }
            case "Admin" -> new AdminShellFrm(account).setVisible(true);
            case "Inventory Staff" -> new InventoryShellFrm(accountDAO.getEmployeeByUsername(account.getUsername())).setVisible(true);
            case "Sales Staff" -> new SalesShellFrm(accountDAO.getEmployeeByUsername(account.getUsername())).setVisible(true);
            default -> {
                JOptionPane.showMessageDialog(owner, "Vai trò này chưa được hỗ trợ trong phiên bản hiện tại!");
                return null;
            }
        }
        owner.dispose();
        return null;
    }
}
