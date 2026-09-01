/*
 * Trang "Tài khoản của tôi" — Customer tự xem/sửa thông tin cá nhân
 * (Customer.fullName/phone/email/address). Trước đây không có trang nào cho
 * việc này: navbar chỉ hiện tên (không bấm được) và icon "Đổi mật khẩu" chỉ
 * mở dialog đổi mật khẩu, không xem/sửa được các trường còn lại.
 */
package view.customer;
import view.shared.*;

import dao.CustomerDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class AccountPage implements CustomerPage {

    @Override
    public String navKey() {
        return "account";
    }

    @Override
    public String title() {
        return "Tài Khoản Của Tôi";
    }

    @Override
    public CustomerPage backTarget() {
        return new CustomerHomePage();
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Tài khoản của tôi");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        content.add(heading);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(32, 40, 32, 40)));
        formCard.setMaximumSize(new Dimension(900, 340));
        formCard.setPreferredSize(new Dimension(900, 340));
        formCard.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblUsername = new JLabel(shell.customer.getUsername());
        lblUsername.setFont(lblUsername.getFont().deriveFont(Font.BOLD));
        formRow(formCard, gbc, 0, "Username:", lblUsername);

        JTextField txtFullName = new JTextField(shell.customer.getFullName());
        formRow(formCard, gbc, 1, "Họ tên: *", txtFullName);

        JTextField txtPhone = new JTextField(shell.customer.getPhone());
        formRow(formCard, gbc, 2, "Số điện thoại: *", txtPhone);

        JTextField txtEmail = new JTextField(shell.customer.getEmail());
        formRow(formCard, gbc, 3, "Email: *", txtEmail);

        JTextField txtAddress = new JTextField(shell.customer.getAddress());
        formRow(formCard, gbc, 4, "Địa chỉ:", txtAddress);

        content.add(formCard);
        content.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(900, 40));

        JButton btnSave = new JButton("Lưu thay đổi");
        AppTheme.primary(btnSave);
        btnSave.addActionListener(e -> onSave(shell, txtFullName, txtPhone, txtEmail, txtAddress));
        buttonRow.add(btnSave);

        content.add(buttonRow);

        return content;
    }

    private void formRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 10, 14);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(AppTheme.TEXT);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void onSave(CustomerShellFrm shell, JTextField txtFullName, JTextField txtPhone,
            JTextField txtEmail, JTextField txtAddress) {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập đầy đủ các trường bắt buộc (*)!");
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(shell, "Email không hợp lệ!");
            return;
        }

        shell.customer.setFullName(fullName);
        shell.customer.setPhone(phone);
        shell.customer.setEmail(email);
        shell.customer.setAddress(address.isEmpty() ? null : address);

        String error = new CustomerDAO().updateCustomer(shell.customer);
        if (error == null) {
            JOptionPane.showMessageDialog(shell, "Cập nhật thông tin thành công!");
            shell.refreshUserLabel();
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }
}
