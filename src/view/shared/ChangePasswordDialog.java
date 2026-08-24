/*
 * FR-COM-02 (Change Password) — dùng chung cho mọi khung (Admin/Customer/
 * Sales/Inventory), vì đây là chức năng chung cho mọi actor có tài khoản
 * (mục 3.0 trong SRS), không riêng vai trò nào.
 */
package view.shared;

import dao.AccountDAO;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public final class ChangePasswordDialog {

    private ChangePasswordDialog() {
    }

    public static void show(Component parent, String username) {
        if (username == null) {
            JOptionPane.showMessageDialog(parent, "Không xác định được tài khoản đang đăng nhập!");
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JPasswordField txtCurrent = new JPasswordField(18);
        JPasswordField txtNew = new JPasswordField(18);
        JPasswordField txtConfirm = new JPasswordField(18);

        addRow(panel, gbc, 0, "Mật khẩu hiện tại:", txtCurrent);
        addRow(panel, gbc, 1, "Mật khẩu mới:", txtNew);
        addRow(panel, gbc, 2, "Xác nhận mật khẩu mới:", txtConfirm);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Đổi mật khẩu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String current = new String(txtCurrent.getPassword());
        String newPass = new String(txtNew.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Vui lòng nhập đầy đủ các trường!");
            return;
        }
        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(parent, "Xác nhận mật khẩu mới không khớp!");
            return;
        }

        String error = new AccountDAO().changePassword(username, current, newPass);
        if (error == null) {
            JOptionPane.showMessageDialog(parent, "Đổi mật khẩu thành công!");
        } else {
            JOptionPane.showMessageDialog(parent, error);
        }
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JPasswordField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}
