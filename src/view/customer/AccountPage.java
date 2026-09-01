/*
 * Trang "Tài khoản của tôi" — theo mẫu layout dạng "Manage Account" (sidebar
 * phụ bên trái + khối hồ sơ tóm tắt + từng mục thông tin có nút "Sửa" riêng)
 * mà chị thu gửi làm ví dụ. Chỉ đưa vào những gì hệ thống thực sự có dữ liệu
 * (Customer.fullName/phone/email/address, đơn hàng qua OrderDAO) — không bịa
 * các mục không tồn tại trong DB như điểm thưởng, thú cưng, gói thành viên...
 */
package view.customer;
import view.shared.*;

import dao.CustomerDAO;
import dao.OrderDAO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Customer;
import model.Order;

public class AccountPage implements CustomerPage {

    private static final Color MUTED = new Color(0x8A, 0x83, 0x74);

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
        JPanel content = new JPanel(new BorderLayout(24, 0));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        content.add(buildSidebar(shell), BorderLayout.WEST);

        JScrollPane mainScroll = new JScrollPane(buildMain(shell));
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(mainScroll, BorderLayout.CENTER);

        return content;
    }

    // Mục lục phụ bên trái — chỉ gồm các luồng có thật trong app: trang này,
    // lịch sử đơn hàng (đã có ở navbar trên cùng), đổi mật khẩu, đăng xuất.
    private JPanel buildSidebar(CustomerShellFrm shell) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(16, 4, 16, 4)));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setMaximumSize(new Dimension(220, Integer.MAX_VALUE));

        sidebar.add(sidebarItem("Quản lý tài khoản", FlatIcon.Kind.USER, true, null));
        sidebar.add(sidebarItem("Lịch sử đơn hàng", FlatIcon.Kind.RECEIPT, false, () -> shell.navigateTo(new OrderHistoryPage())));
        sidebar.add(sidebarItem("Đổi mật khẩu", FlatIcon.Kind.PEN, false,
                () -> ChangePasswordDialog.show(shell, shell.customer.getUsername())));
        sidebar.add(sidebarItem("Đăng xuất", FlatIcon.Kind.LOGOUT, false, shell::logout));

        return sidebar;
    }

    private JComponent sidebarItem(String text, FlatIcon.Kind icon, boolean active, Runnable onClick) {
        JButton btn = new JButton(text);
        btn.setIcon(new FlatIcon(icon, active ? AppTheme.PRIMARY : MUTED, 16));
        btn.setIconTextGap(10);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(active ? AppTheme.PRIMARY : AppTheme.TEXT);
        btn.setFont(btn.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN, 13f));
        btn.putClientProperty("FlatLaf.style",
                "background: " + (active ? "#F3EFE6" : "#FFFFFF") + "; hoverBackground: #F3EFE6;"
                + " borderWidth: 0; arc: 8; margin: 10,12,10,12; focusWidth: 0");
        btn.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (onClick != null) {
            btn.addActionListener(e -> onClick.run());
        } else {
            btn.setEnabled(false);
        }
        return btn;
    }

    private JPanel buildMain(CustomerShellFrm shell) {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);

        JLabel heading = new JLabel("Quản lý tài khoản");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        main.add(heading);

        main.add(buildStatRow(shell));
        main.add(Box.createVerticalStrut(18));

        main.add(buildProfileSummary(shell));
        main.add(Box.createVerticalStrut(18));

        main.add(passwordRow(shell));
        main.add(Box.createVerticalStrut(12));
        main.add(fieldRow(shell, "Họ tên", Customer::getFullName, Customer::setFullName, true, false));
        main.add(Box.createVerticalStrut(12));
        main.add(fieldRow(shell, "Số điện thoại", Customer::getPhone, Customer::setPhone, true, false));
        main.add(Box.createVerticalStrut(12));
        main.add(fieldRow(shell, "Email", Customer::getEmail, Customer::setEmail, true, true));
        main.add(Box.createVerticalStrut(12));
        main.add(fieldRow(shell, "Địa chỉ", Customer::getAddress, (c, v) -> c.setAddress(v.isEmpty() ? null : v), false, false));

        return main;
    }

    // Thẻ thống kê thật (đơn hàng + chi tiêu), dùng chung dữ liệu OrderDAO với
    // "Đơn hàng của tôi" — thay cho khối "Loyalty Credit" trong mẫu (không có
    // hệ thống điểm thưởng thật nên không đưa số bịa vào).
    private JPanel buildStatRow(CustomerShellFrm shell) {
        List<Order> orders = new OrderDAO().getOrdersByCustomer(shell.customer.getCustomerID());
        float totalSpent = 0f;
        for (Order o : orders) {
            if ("Paid".equals(o.getStatus())) {
                totalSpent += o.getTotalAmount();
            }
        }

        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setPreferredSize(new Dimension(0, 90));

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
        captionLabel.setForeground(MUTED);
        captionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(captionLabel);

        return card;
    }

    // Khối hồ sơ tóm tắt (nền xám nhạt, giống khối "Nam Hà / Customer Since /
    // Email / Customer ID / Phone" trong mẫu) — không có "Customer Since" vì
    // Customer/Account không lưu ngày tạo tài khoản trong DB hiện tại.
    private JPanel buildProfileSummary(CustomerShellFrm shell) {
        Customer c = shell.customer;

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0xF0, 0xEC, 0xE2));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JComponent avatar = buildAvatar(shell);
        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setBorder(new EmptyBorder(0, 0, 0, 20));
        avatarWrap.add(avatar, BorderLayout.CENTER);
        card.add(avatarWrap, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel nameLabel = new JLabel(c.getFullName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 19f));
        nameLabel.setForeground(AppTheme.TEXT);
        nameLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(6));

        text.add(summaryLine("Mã khách hàng: " + c.getCustomerID()));
        text.add(summaryLine("Username: " + c.getUsername()));
        text.add(summaryLine("Email: " + c.getEmail()));
        text.add(summaryLine("Số điện thoại: " + (isBlank(c.getPhone()) ? "Chưa cập nhật" : c.getPhone())));

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JLabel summaryLine(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(12.5f));
        lbl.setForeground(new Color(0x5A, 0x54, 0x48));
        lbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Ảnh đại diện — hiển thị ảnh thật (Customer.avatar, đọc qua AvatarStore)
     * nếu khách đã upload, ngược lại vẽ icon mặc định như cũ. Bấm vào ảnh để
     * chọn ảnh mới từ máy (JFileChooser) và lưu lại.
     */
    private JComponent buildAvatar(CustomerShellFrm shell) {
        int size = 64;
        BufferedImage img = loadAvatarImage(shell.customer.getAvatar());
        FlatIcon icon = new FlatIcon(FlatIcon.Kind.USER, AppTheme.PRIMARY, 28);
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Float(0, 0, size, size));
                if (img != null) {
                    g2.drawImage(img, 0, 0, size, size, null);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(0, 0, size, size);
                    icon.paintIcon(this, g2, (size - icon.getIconWidth()) / 2, (size - icon.getIconHeight()) / 2);
                }
                g2.setClip(null);

                int dot = 20;
                int bx = size - dot;
                int by = size - dot;
                g2.setColor(AppTheme.SECONDARY);
                g2.fillOval(bx, by, dot, dot);
                g2.setColor(Color.WHITE);
                new FlatIcon(FlatIcon.Kind.PEN, Color.WHITE, 11).paintIcon(this, g2, bx + (dot - 11) / 2, by + (dot - 11) / 2);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(size, size));
        badge.setMaximumSize(new Dimension(size, size));
        badge.setMinimumSize(new Dimension(size, size));
        badge.setOpaque(false);
        badge.setToolTipText("Đổi ảnh đại diện");
        badge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        badge.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseAndUploadAvatar(shell);
            }
        });
        return badge;
    }

    private BufferedImage loadAvatarImage(String filename) {
        File file = AvatarStore.resolve(filename);
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (Exception ex) {
            return null;
        }
    }

    private void chooseAndUploadAvatar(CustomerShellFrm shell) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (jpg, jpeg, png, gif)", "jpg", "jpeg", "png", "gif"));
        int result = chooser.showOpenDialog(shell);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String filename = AvatarStore.save(chooser.getSelectedFile(), shell.customer.getCustomerID());
        if (filename == null) {
            JOptionPane.showMessageDialog(shell, "Không thể lưu ảnh, vui lòng thử lại!");
            return;
        }
        if (new CustomerDAO().updateAvatar(shell.customer.getCustomerID(), filename)) {
            shell.customer.setAvatar(filename);
            shell.navigateTo(new AccountPage());
        } else {
            JOptionPane.showMessageDialog(shell, "Cập nhật ảnh đại diện thất bại, vui lòng thử lại!");
        }
    }

    /** Hàng "Mật khẩu" — chấm ẩn + nút Sửa mở thẳng ChangePasswordDialog (không sửa tại chỗ như các trường khác). */
    private JPanel passwordRow(CustomerShellFrm shell) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(16, 20, 16, 20));
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row.setPreferredSize(new Dimension(0, 78));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        JLabel lbl = new JLabel("Mật khẩu");
        lbl.setFont(lbl.getFont().deriveFont(12f));
        lbl.setForeground(MUTED);
        textCol.add(lbl);
        textCol.add(Box.createVerticalStrut(4));
        JLabel dots = new JLabel("**********");
        dots.setFont(dots.getFont().deriveFont(Font.BOLD, 15f));
        dots.setForeground(AppTheme.TEXT);
        textCol.add(dots);
        row.add(textCol, BorderLayout.WEST);

        JButton btnEdit = new JButton("Sửa");
        AppTheme.primary(btnEdit);
        btnEdit.addActionListener(e -> ChangePasswordDialog.show(shell, shell.customer.getUsername()));
        row.add(btnEdit, BorderLayout.EAST);

        return row;
    }

    /**
     * 1 hàng thông tin dạng "xem" (nhãn + giá trị + nút Sửa) chuyển được sang
     * "sửa" (JTextField + Lưu/Hủy) bằng CardLayout — đúng kiểu từng mục có nút
     * Sửa riêng trong mẫu, thay vì gộp hết vào 1 form dài.
     */
    private JPanel fieldRow(CustomerShellFrm shell, String label, Function<Customer, String> getter,
            BiConsumer<Customer, String> setter, boolean required, boolean isEmail) {
        Supplier<String> read = () -> getter.apply(shell.customer);

        JPanel row = new JPanel(new CardLayout());
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row.setPreferredSize(new Dimension(0, 78));
        CardLayout cl = (CardLayout) row.getLayout();

        JPanel viewCard = new JPanel(new BorderLayout());
        viewCard.setBackground(Color.WHITE);
        viewCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(12f));
        lbl.setForeground(MUTED);
        textCol.add(lbl);
        textCol.add(Box.createVerticalStrut(4));
        String currentValue = read.get();
        JLabel valueLabel = new JLabel(isBlank(currentValue) ? "Chưa cập nhật" : currentValue);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 15f));
        valueLabel.setForeground(AppTheme.TEXT);
        textCol.add(valueLabel);
        viewCard.add(textCol, BorderLayout.WEST);

        JButton btnEdit = new JButton("Sửa");
        AppTheme.secondary(btnEdit);
        viewCard.add(btnEdit, BorderLayout.EAST);

        JPanel editCard = new JPanel(new BorderLayout(10, 0));
        editCard.setBackground(Color.WHITE);
        editCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JTextField txt = new JTextField(currentValue);
        editCard.add(txt, BorderLayout.CENTER);

        JPanel editButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        editButtons.setOpaque(false);
        JButton btnCancel = new JButton("Hủy");
        AppTheme.secondary(btnCancel);
        JButton btnSave = new JButton("Lưu");
        AppTheme.primary(btnSave);
        editButtons.add(btnCancel);
        editButtons.add(btnSave);
        editCard.add(editButtons, BorderLayout.EAST);

        row.add(viewCard, "view");
        row.add(editCard, "edit");

        btnEdit.addActionListener(e -> {
            txt.setText(read.get());
            cl.show(row, "edit");
        });
        btnCancel.addActionListener(e -> cl.show(row, "view"));
        btnSave.addActionListener(e -> {
            String v = txt.getText().trim();
            if (required && v.isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Trường \"" + label + "\" không được để trống!");
                return;
            }
            if (isEmail && !v.isEmpty() && !v.contains("@")) {
                JOptionPane.showMessageDialog(shell, "Email không hợp lệ!");
                return;
            }
            setter.accept(shell.customer, v);
            String error = new CustomerDAO().updateCustomer(shell.customer);
            if (error == null) {
                shell.refreshUserLabel();
                shell.navigateTo(new AccountPage());
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });

        return row;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
