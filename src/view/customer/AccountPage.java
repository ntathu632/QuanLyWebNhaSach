/*
 * Trang "Tài khoản của tôi" — Customer tự xem/sửa thông tin cá nhân
 * (Customer.fullName/phone/email/address). Trước đây không có trang nào cho
 * việc này: navbar chỉ hiện tên (không bấm được) và icon "Đổi mật khẩu" chỉ
 * mở dialog đổi mật khẩu, không xem/sửa được các trường còn lại.
 *
 * Ngoài form sửa, có thêm khối hồ sơ (avatar + tên + username) và 2 thẻ
 * thống kê (tổng đơn hàng, tổng chi tiêu — cùng dữ liệu OrderDAO đã dùng cho
 * "Đơn hàng của tôi") để trang không chỉ là 1 form trơn, giống cách trang
 * "Chi tiết khách hàng" bên Admin đã hiển thị cho chính khách hàng này.
 */
package view.customer;
import view.shared.*;

import dao.CustomerDAO;
import dao.OrderDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
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
import model.Order;

public class AccountPage implements CustomerPage {

    private static final int BLOCK_WIDTH = 900;

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
        List<Order> orders = new OrderDAO().getOrdersByCustomer(shell.customer.getCustomerID());
        float totalSpent = 0f;
        for (Order o : orders) {
            if ("Paid".equals(o.getStatus())) {
                totalSpent += o.getTotalAmount();
            }
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        content.add(buildProfileHeader(shell));
        content.add(Box.createVerticalStrut(20));

        content.add(buildStatRow(orders, totalSpent));
        content.add(Box.createVerticalStrut(20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(24, 32, 28, 32)));
        formCard.setMaximumSize(new Dimension(BLOCK_WIDTH, 300));
        formCard.setPreferredSize(new Dimension(BLOCK_WIDTH, 300));
        formCard.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 16, 0);
        JLabel sectionTitle = new JLabel("Thông tin cá nhân");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 15f));
        sectionTitle.setForeground(AppTheme.TEXT);
        formCard.add(sectionTitle, gbc);
        gbc.gridwidth = 1;

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
        buttonRow.setMaximumSize(new Dimension(BLOCK_WIDTH, 40));

        JButton btnSave = new JButton("Lưu thay đổi");
        AppTheme.primary(btnSave);
        btnSave.addActionListener(e -> onSave(shell, txtFullName, txtPhone, txtEmail, txtAddress));
        buttonRow.add(btnSave);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnChangePassword = new JButton("Đổi mật khẩu");
        AppTheme.secondary(btnChangePassword);
        btnChangePassword.addActionListener(e ->
                ChangePasswordDialog.show(shell, shell.customer.getUsername()));
        buttonRow.add(btnChangePassword);

        content.add(buttonRow);

        return content;
    }

    private JPanel buildProfileHeader(CustomerShellFrm shell) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(BLOCK_WIDTH, 70));

        row.add(avatarBadge());
        row.add(Box.createHorizontalStrut(16));

        JPanel names = new JPanel();
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.setOpaque(false);

        JLabel nameLabel = new JLabel(shell.customer.getFullName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));
        nameLabel.setForeground(AppTheme.TEXT);
        nameLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        names.add(nameLabel);

        JLabel usernameLabel = new JLabel("@" + shell.customer.getUsername());
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(13f));
        usernameLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        usernameLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        names.add(usernameLabel);

        row.add(names);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    /** Huy hiệu tròn nền màu nhạt + icon người dùng chính giữa, cùng kiểu iconBadge() bên Admin. */
    private JComponent avatarBadge() {
        int size = 64;
        FlatIcon icon = new FlatIcon(FlatIcon.Kind.USER, AppTheme.PRIMARY, 28);
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(lighten(AppTheme.PRIMARY, 0.78f));
                g2.fillOval(0, 0, size, size);
                g2.dispose();
                icon.paintIcon(this, g, (size - icon.getIconWidth()) / 2, (size - icon.getIconHeight()) / 2);
            }
        };
        badge.setPreferredSize(new Dimension(size, size));
        badge.setMaximumSize(new Dimension(size, size));
        badge.setMinimumSize(new Dimension(size, size));
        badge.setOpaque(false);
        return badge;
    }

    /** Trộn "color" về phía trắng theo tỉ lệ "ratio" — nền nhạt cho huy hiệu avatar. */
    private static Color lighten(Color color, float ratio) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * ratio);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * ratio);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * ratio);
        return new Color(r, g, b);
    }

    private JPanel buildStatRow(List<Order> orders, float totalSpent) {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(BLOCK_WIDTH, 90));
        row.setPreferredSize(new Dimension(BLOCK_WIDTH, 90));

        row.add(statCard(FlatIcon.Kind.RECEIPT, String.valueOf(orders.size()), "Tổng số đơn hàng", AppTheme.CHART_SKY));
        row.add(statCard(FlatIcon.Kind.MONEY, String.format("%,.0f₫", totalSpent), "Tổng chi tiêu (đơn đã thanh toán)", AppTheme.CHART_GOLD));

        return row;
    }

    private JPanel statCard(FlatIcon.Kind kind, String value, String caption, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel iconLabel = new JLabel(new FlatIcon(kind, accent, 20));
        iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(iconLabel);

        card.add(Box.createVerticalStrut(6));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 18f));
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(valueLabel);

        card.add(Box.createVerticalStrut(2));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(captionLabel.getFont().deriveFont(12f));
        captionLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        captionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(captionLabel);

        return card;
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
            shell.navigateTo(new AccountPage());
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }
}
