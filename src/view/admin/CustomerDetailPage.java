/*
 * Trang "Chi tiết khách hàng" — mở từ Quản lý tài khoản (chỉ áp dụng cho tài
 * khoản vai trò Customer, vì Employee không có Customer/đơn hàng/đánh giá đi
 * kèm). Gộp thông tin cá nhân (Customer) + lịch sử mua hàng (OrderDAO, cùng
 * dữ liệu FR-CUS-08 dùng cho "Đơn hàng của tôi" bên khách hàng) + đánh giá đã
 * viết (ReviewDAO) vào 1 trang để admin xem tổng quan 1 khách hàng cụ thể mà
 * trước đó "Quản lý tài khoản" không có cách nào xem sâu hơn ngoài list phẳng.
 */
package view.admin;
import view.shared.*;

import dao.OrderDAO;
import dao.ReviewDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Account;
import model.Customer;
import model.Order;
import model.OrderDetail;
import model.Review;

public class CustomerDetailPage implements AdminPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final Customer customer;
    private final Account account;
    private final AdminPage backTarget;
    private final String navKey;

    public CustomerDetailPage(Customer customer, Account account) {
        this(customer, account, new ManageAccountPage(), "accounts");
    }

    // Cho phép mở từ ManageCustomerPage (Quản lý khách hàng) thay vì luôn
    // quay về ManageAccountPage — nút "quay lại" và mục sidebar tô sáng phải
    // khớp đúng nơi đã điều hướng tới đây.
    public CustomerDetailPage(Customer customer, Account account, AdminPage backTarget, String navKey) {
        this.customer = customer;
        this.account = account;
        this.backTarget = backTarget;
        this.navKey = navKey;
    }

    @Override
    public String navKey() {
        return navKey;
    }

    @Override
    public String title() {
        return "Chi Tiết Khách Hàng";
    }

    @Override
    public AdminPage backTarget() {
        return backTarget;
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        List<Order> orders = new OrderDAO().getOrdersByCustomer(customer.getCustomerID());
        List<Review> reviews = new ReviewDAO().getReviewsByCustomer(customer.getCustomerID());

        float totalSpent = 0f;
        for (Order o : orders) {
            if ("Paid".equals(o.getStatus())) {
                totalSpent += o.getTotalAmount();
            }
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Chi tiết khách hàng: " + customer.getFullName());
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        top.add(buildInfoCard());
        top.add(Box.createVerticalStrut(18));

        top.add(buildStatRow(orders, reviews, totalSpent));
        top.add(Box.createVerticalStrut(18));

        top.add(buildOrderTableCard(shell, orders));
        top.add(Box.createVerticalStrut(18));

        top.add(buildReviewTableCard(reviews));

        JScrollPane pageScroll = new JScrollPane(top);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(pageScroll, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new GridLayout(2, 3, 24, 18));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        card.add(infoField("Username", account == null ? "— (chưa có tài khoản)" : account.getUsername()));
        card.add(infoField("Họ tên", customer.getFullName()));
        card.add(infoField("Trạng thái tài khoản", account == null ? "—"
                : ("Active".equals(account.getStatus()) ? "Đang hoạt động" : "Đã khóa")));
        card.add(infoField("Email", nullToDash(customer.getEmail())));
        card.add(infoField("Số điện thoại", nullToDash(customer.getPhone())));
        card.add(infoField("Địa chỉ", nullToDash(customer.getAddress())));

        return card;
    }

    private JPanel infoField(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(12f));
        lbl.setForeground(new Color(0x8A, 0x83, 0x74));
        lbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        p.add(lbl);

        p.add(Box.createVerticalStrut(4));

        JLabel val = new JLabel(value);
        val.setFont(val.getFont().deriveFont(Font.BOLD, 14f));
        val.setForeground(AppTheme.TEXT);
        val.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        p.add(val);

        return p;
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    private JPanel buildStatRow(List<Order> orders, List<Review> reviews, float totalSpent) {
        JPanel row = new JPanel(new GridLayout(1, 3, 18, 0));
        row.setBackground(AppTheme.BACKGROUND);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        row.setPreferredSize(new Dimension(0, 96));

        row.add(statCard(FlatIcon.Kind.RECEIPT, String.valueOf(orders.size()), "Tổng số đơn hàng", AppTheme.CHART_SKY));
        row.add(statCard(FlatIcon.Kind.MONEY, String.format("%,.0f₫", totalSpent), "Tổng chi tiêu (đơn đã thanh toán)", AppTheme.CHART_GOLD));
        row.add(statCard(FlatIcon.Kind.PEN, String.valueOf(reviews.size()), "Số đánh giá đã viết", AppTheme.CHART_PURPLE));

        return row;
    }

    private JPanel statCard(FlatIcon.Kind kind, String value, String caption, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel iconLabel = new JLabel(new FlatIcon(kind, accent, 22));
        iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(iconLabel);

        card.add(Box.createVerticalStrut(6));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
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

    private JPanel buildOrderTableCard(AdminShellFrm shell, List<Order> orders) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(0, 300));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JLabel title = new JLabel("Lịch sử đơn hàng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(AppTheme.TEXT);
        header.add(title, BorderLayout.WEST);

        String[] columns = {"Mã đơn", "Ngày đặt", "Tổng tiền", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Order o : orders) {
            model.addRow(new Object[]{o.getOrderID(), DATE_FMT.format(o.getOrderDate()),
                String.format("%,.0f₫", o.getTotalAmount()), statusLabel(o.getStatus())});
        }
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 150, 150);
        AppTheme.alignTableColumn(table, 2, SwingConstants.RIGHT, 130, 130);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 120, 120);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground(statusColor(String.valueOf(value)));
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JButton btnDetail = new JButton("Xem chi tiết");
        AppTheme.primary(btnDetail);
        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= orders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng để xem chi tiết!");
                return;
            }
            showOrderDetail(shell, orders.get(row));
        });
        header.add(btnDetail, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        if (orders.isEmpty()) {
            JLabel empty = new JLabel("Khách hàng chưa có đơn hàng nào.");
            empty.setForeground(new Color(0x8A, 0x83, 0x74));
            card.add(empty, BorderLayout.SOUTH);
        }

        return card;
    }

    private JPanel buildReviewTableCard(List<Review> reviews) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(0, 260));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JLabel title = new JLabel("Đánh giá đã viết");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(AppTheme.TEXT);
        header.add(title, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);

        String[] columns = {"Sách", "Số sao", "Nhận xét", "Ngày đánh giá"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Review r : reviews) {
            model.addRow(new Object[]{r.getBookTitle(), r.getRating() + " ★",
                r.getComment(), DATE_FMT.format(r.getReviewDate())});
        }
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 220, 220);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 150, 150);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        if (reviews.isEmpty()) {
            JLabel empty = new JLabel("Khách hàng chưa viết đánh giá nào.");
            empty.setForeground(new Color(0x8A, 0x83, 0x74));
            card.add(empty, BorderLayout.SOUTH);
        }

        return card;
    }

    private void showOrderDetail(AdminShellFrm shell, Order order) {
        List<OrderDetail> details = new OrderDAO().getOrderDetails(order.getOrderID());

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(480, 300));

        JLabel info = new JLabel("<html>Trạng thái: <b>" + statusLabel(order.getStatus()) + "</b><br>"
                + "Địa chỉ giao hàng: " + (order.getShippingAddress() == null ? "" : order.getShippingAddress()) + "<br>"
                + "Tổng tiền: <b>" + String.format("%,.0f₫", order.getTotalAmount()) + "</b></html>");
        panel.add(info, BorderLayout.NORTH);

        String[] columns = {"Sách", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (OrderDetail d : details) {
            model.addRow(new Object[]{d.getBook().getTitle(), d.getQuantity(),
                String.format("%,.0f₫", d.getUnitPrice()), String.format("%,.0f₫", d.getSubtotal())});
        }
        JTable table = new JTable(model);
        table.setRowHeight(26);
        AppTheme.alignTableColumn(table, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(table, 2, SwingConstants.RIGHT, 100, 100);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 100, 100);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(shell, panel, "Chi tiết đơn hàng #" + order.getOrderID(), JOptionPane.PLAIN_MESSAGE);
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "Pending" -> "Chờ xử lý";
            case "Paid" -> "Đã thanh toán";
            case "Cancelled" -> "Đã hủy";
            default -> status;
        };
    }

    private Color statusColor(String label) {
        if ("Đã thanh toán".equals(label)) {
            return AppTheme.PRIMARY_HOVER;
        }
        if ("Đã hủy".equals(label)) {
            return AppTheme.ACCENT_TEXT;
        }
        return AppTheme.CHART_ORANGE;
    }
}
