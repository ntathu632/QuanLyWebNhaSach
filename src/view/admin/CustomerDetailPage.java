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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Account;
import model.Customer;
import model.Order;
import model.OrderDetail;
import model.Review;

public class CustomerDetailPage extends javax.swing.JPanel implements AdminPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final Customer customer;
    private final Account account;
    private final AdminPage backTarget;
    private final String navKey;

    private AdminShellFrm shell;

    private List<Order> orders = new ArrayList<>();

    private final DefaultTableModel orderModel = new DefaultTableModel(new String[]{"Mã đơn", "Ngày đặt", "Tổng tiền", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final DefaultTableModel reviewModel = new DefaultTableModel(new String[]{"Sách", "Số sao", "Nhận xét", "Ngày đánh giá"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

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
        initComponents();

        pageScroll.getVerticalScrollBar().setUnitIncrement(16);

        orderTable.setModel(orderModel);
        orderTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(orderTable, 0, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(orderTable, 1, SwingConstants.CENTER, 150, 150);
        AppTheme.alignTableColumn(orderTable, 2, SwingConstants.RIGHT, 130, 130);
        AppTheme.alignTableColumn(orderTable, 3, SwingConstants.CENTER, 120, 120);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground(statusColor(String.valueOf(value)));
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        AppTheme.primary(btnDetail);
        btnDetail.addActionListener(e -> {
            int row = orderTable.getSelectedRow();
            if (row < 0 || row >= orders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng để xem chi tiết!");
                return;
            }
            showOrderDetail(shell, orders.get(row));
        });

        reviewTable.setModel(reviewModel);
        reviewTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(reviewTable, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(reviewTable, 1, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(reviewTable, 2, SwingConstants.LEFT, 220, 220);
        AppTheme.alignTableColumn(reviewTable, 3, SwingConstants.CENTER, 150, 150);
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
        this.shell = shell;

        orders = new OrderDAO().getOrdersByCustomer(customer.getCustomerID());
        List<Review> reviews = new ReviewDAO().getReviewsByCustomer(customer.getCustomerID());

        float totalSpent = 0f;
        for (Order o : orders) {
            if ("Paid".equals(o.getStatus())) {
                totalSpent += o.getTotalAmount();
            }
        }

        heading.setText("Chi tiết khách hàng: " + customer.getFullName());

        fillInfoCard();
        fillStatRow(orders, reviews, totalSpent);
        fillOrderTable(orders);
        fillReviewTable(reviews);

        return this;
    }

    private void fillInfoCard() {
        infoValue1.setText(account == null ? "— (chưa có tài khoản)" : account.getUsername());
        infoValue2.setText(customer.getFullName());
        infoValue3.setText(account == null ? "—"
                : ("Active".equals(account.getStatus()) ? "Đang hoạt động" : "Đã khóa"));
        infoValue4.setText(nullToDash(customer.getEmail()));
        infoValue5.setText(nullToDash(customer.getPhone()));
        infoValue6.setText(nullToDash(customer.getAddress()));
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    private void fillStatRow(List<Order> orders, List<Review> reviews, float totalSpent) {
        statValue1.setText(String.valueOf(orders.size()));
        statValue2.setText(String.format("%,.0f₫", totalSpent));
        statValue3.setText(String.valueOf(reviews.size()));
    }

    private void fillOrderTable(List<Order> orders) {
        orderModel.setRowCount(0);
        for (Order o : orders) {
            orderModel.addRow(new Object[]{o.getOrderID(), DATE_FMT.format(o.getOrderDate()),
                String.format("%,.0f₫", o.getTotalAmount()), statusLabel(o.getStatus())});
        }
        orderEmpty.setVisible(orders.isEmpty());
    }

    private void fillReviewTable(List<Review> reviews) {
        reviewModel.setRowCount(0);
        for (Review r : reviews) {
            reviewModel.addRow(new Object[]{r.getBookTitle(), r.getRating() + " ★",
                r.getComment(), DATE_FMT.format(r.getReviewDate())});
        }
        reviewEmpty.setVisible(reviews.isEmpty());
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pageScroll = new javax.swing.JScrollPane();
        top = new javax.swing.JPanel();
        heading = new javax.swing.JLabel();
        infoCard = new javax.swing.JPanel();
        infoField1 = new javax.swing.JPanel();
        infoLabel1 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        infoValue1 = new javax.swing.JLabel();
        infoField2 = new javax.swing.JPanel();
        infoLabel2 = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        infoValue2 = new javax.swing.JLabel();
        infoField3 = new javax.swing.JPanel();
        infoLabel3 = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        infoValue3 = new javax.swing.JLabel();
        infoField4 = new javax.swing.JPanel();
        infoLabel4 = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        infoValue4 = new javax.swing.JLabel();
        infoField5 = new javax.swing.JPanel();
        infoLabel5 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        infoValue5 = new javax.swing.JLabel();
        infoField6 = new javax.swing.JPanel();
        infoLabel6 = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        infoValue6 = new javax.swing.JLabel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 18), new java.awt.Dimension(0, 18), new java.awt.Dimension(32767, 18));
        statRow = new javax.swing.JPanel();
        statCard1 = new javax.swing.JPanel();
        statIcon1 = new javax.swing.JLabel();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        statValue1 = new javax.swing.JLabel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        statCaption1 = new javax.swing.JLabel();
        statCard2 = new javax.swing.JPanel();
        statIcon2 = new javax.swing.JLabel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        statValue2 = new javax.swing.JLabel();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        statCaption2 = new javax.swing.JLabel();
        statCard3 = new javax.swing.JPanel();
        statIcon3 = new javax.swing.JLabel();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        statValue3 = new javax.swing.JLabel();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        statCaption3 = new javax.swing.JLabel();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 18), new java.awt.Dimension(0, 18), new java.awt.Dimension(32767, 18));
        orderCard = new javax.swing.JPanel();
        orderHeader = new javax.swing.JPanel();
        orderTitle = new javax.swing.JLabel();
        btnDetail = new javax.swing.JButton();
        orderScroll = new javax.swing.JScrollPane();
        orderTable = new javax.swing.JTable();
        orderEmpty = new javax.swing.JLabel();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 18), new java.awt.Dimension(0, 18), new java.awt.Dimension(32767, 18));
        reviewCard = new javax.swing.JPanel();
        reviewHeader = new javax.swing.JPanel();
        reviewTitle = new javax.swing.JLabel();
        reviewScroll = new javax.swing.JScrollPane();
        reviewTable = new javax.swing.JTable();
        reviewEmpty = new javax.swing.JLabel();

        setBackground(AppTheme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        setLayout(new java.awt.BorderLayout());

        pageScroll.setBorder(null);
        top.setBackground(AppTheme.BACKGROUND);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setText("Chi tiết khách hàng: ");
        heading.setAlignmentX(0.0F);
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));
        top.add(heading);
        infoCard.setBackground(new java.awt.Color(255, 255, 255));
        infoCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        infoCard.setAlignmentX(0.0F);
        infoCard.setMaximumSize(new java.awt.Dimension(2147483647, 130));
        infoCard.setLayout(new java.awt.GridLayout(2, 3, 24, 18));
        infoField1.setOpaque(false);
        infoField1.setLayout(new javax.swing.BoxLayout(infoField1, javax.swing.BoxLayout.Y_AXIS));
        infoLabel1.setFont(infoLabel1.getFont().deriveFont(12f));
        infoLabel1.setForeground(new java.awt.Color(138, 131, 116));
        infoLabel1.setText("Username");
        infoLabel1.setAlignmentX(0.0F);
        infoField1.add(infoLabel1);
        infoField1.add(filler1);
        infoValue1.setFont(infoValue1.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        infoValue1.setForeground(AppTheme.TEXT);
        infoValue1.setText("—");
        infoValue1.setAlignmentX(0.0F);
        infoField1.add(infoValue1);
        infoCard.add(infoField1);

        infoField2.setOpaque(false);
        infoField2.setLayout(new javax.swing.BoxLayout(infoField2, javax.swing.BoxLayout.Y_AXIS));
        infoLabel2.setFont(infoLabel2.getFont().deriveFont(12f));
        infoLabel2.setForeground(new java.awt.Color(138, 131, 116));
        infoLabel2.setText("Họ tên");
        infoLabel2.setAlignmentX(0.0F);
        infoField2.add(infoLabel2);
        infoField2.add(filler2);
        infoValue2.setFont(infoValue2.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        infoValue2.setForeground(AppTheme.TEXT);
        infoValue2.setText("—");
        infoValue2.setAlignmentX(0.0F);
        infoField2.add(infoValue2);
        infoCard.add(infoField2);

        infoField3.setOpaque(false);
        infoField3.setLayout(new javax.swing.BoxLayout(infoField3, javax.swing.BoxLayout.Y_AXIS));
        infoLabel3.setFont(infoLabel3.getFont().deriveFont(12f));
        infoLabel3.setForeground(new java.awt.Color(138, 131, 116));
        infoLabel3.setText("Trạng thái tài khoản");
        infoLabel3.setAlignmentX(0.0F);
        infoField3.add(infoLabel3);
        infoField3.add(filler3);
        infoValue3.setFont(infoValue3.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        infoValue3.setForeground(AppTheme.TEXT);
        infoValue3.setText("—");
        infoValue3.setAlignmentX(0.0F);
        infoField3.add(infoValue3);
        infoCard.add(infoField3);

        infoField4.setOpaque(false);
        infoField4.setLayout(new javax.swing.BoxLayout(infoField4, javax.swing.BoxLayout.Y_AXIS));
        infoLabel4.setFont(infoLabel4.getFont().deriveFont(12f));
        infoLabel4.setForeground(new java.awt.Color(138, 131, 116));
        infoLabel4.setText("Email");
        infoLabel4.setAlignmentX(0.0F);
        infoField4.add(infoLabel4);
        infoField4.add(filler4);
        infoValue4.setFont(infoValue4.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        infoValue4.setForeground(AppTheme.TEXT);
        infoValue4.setText("—");
        infoValue4.setAlignmentX(0.0F);
        infoField4.add(infoValue4);
        infoCard.add(infoField4);

        infoField5.setOpaque(false);
        infoField5.setLayout(new javax.swing.BoxLayout(infoField5, javax.swing.BoxLayout.Y_AXIS));
        infoLabel5.setFont(infoLabel5.getFont().deriveFont(12f));
        infoLabel5.setForeground(new java.awt.Color(138, 131, 116));
        infoLabel5.setText("Số điện thoại");
        infoLabel5.setAlignmentX(0.0F);
        infoField5.add(infoLabel5);
        infoField5.add(filler5);
        infoValue5.setFont(infoValue5.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        infoValue5.setForeground(AppTheme.TEXT);
        infoValue5.setText("—");
        infoValue5.setAlignmentX(0.0F);
        infoField5.add(infoValue5);
        infoCard.add(infoField5);

        infoField6.setOpaque(false);
        infoField6.setLayout(new javax.swing.BoxLayout(infoField6, javax.swing.BoxLayout.Y_AXIS));
        infoLabel6.setFont(infoLabel6.getFont().deriveFont(12f));
        infoLabel6.setForeground(new java.awt.Color(138, 131, 116));
        infoLabel6.setText("Địa chỉ");
        infoLabel6.setAlignmentX(0.0F);
        infoField6.add(infoLabel6);
        infoField6.add(filler6);
        infoValue6.setFont(infoValue6.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        infoValue6.setForeground(AppTheme.TEXT);
        infoValue6.setText("—");
        infoValue6.setAlignmentX(0.0F);
        infoField6.add(infoValue6);
        infoCard.add(infoField6);

        top.add(infoCard);

        top.add(filler7);
        statRow.setBackground(AppTheme.BACKGROUND);
        statRow.setAlignmentX(0.0F);
        statRow.setMaximumSize(new java.awt.Dimension(2147483647, 96));
        statRow.setPreferredSize(new java.awt.Dimension(0, 96));
        statRow.setLayout(new java.awt.GridLayout(1, 3, 18, 0));
        statCard1.setBackground(new java.awt.Color(255, 255, 255));
        statCard1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_SKY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        statCard1.setLayout(new javax.swing.BoxLayout(statCard1, javax.swing.BoxLayout.Y_AXIS));
        statIcon1.setIcon(new FlatIcon(FlatIcon.Kind.RECEIPT, AppTheme.CHART_SKY, 22));
        statIcon1.setAlignmentX(0.0F);
        statCard1.add(statIcon1);
        statCard1.add(filler8);
        statValue1.setFont(statValue1.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        statValue1.setForeground(AppTheme.TEXT);
        statValue1.setText("0");
        statValue1.setAlignmentX(0.0F);
        statCard1.add(statValue1);
        statCard1.add(filler9);
        statCaption1.setFont(statCaption1.getFont().deriveFont(12f));
        statCaption1.setForeground(new java.awt.Color(138, 131, 116));
        statCaption1.setText("Tổng số đơn hàng");
        statCaption1.setAlignmentX(0.0F);
        statCard1.add(statCaption1);
        statRow.add(statCard1);

        statCard2.setBackground(new java.awt.Color(255, 255, 255));
        statCard2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_GOLD), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        statCard2.setLayout(new javax.swing.BoxLayout(statCard2, javax.swing.BoxLayout.Y_AXIS));
        statIcon2.setIcon(new FlatIcon(FlatIcon.Kind.MONEY, AppTheme.CHART_GOLD, 22));
        statIcon2.setAlignmentX(0.0F);
        statCard2.add(statIcon2);
        statCard2.add(filler10);
        statValue2.setFont(statValue2.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        statValue2.setForeground(AppTheme.TEXT);
        statValue2.setText("0₫");
        statValue2.setAlignmentX(0.0F);
        statCard2.add(statValue2);
        statCard2.add(filler11);
        statCaption2.setFont(statCaption2.getFont().deriveFont(12f));
        statCaption2.setForeground(new java.awt.Color(138, 131, 116));
        statCaption2.setText("Tổng chi tiêu (đơn đã thanh toán)");
        statCaption2.setAlignmentX(0.0F);
        statCard2.add(statCaption2);
        statRow.add(statCard2);

        statCard3.setBackground(new java.awt.Color(255, 255, 255));
        statCard3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_PURPLE), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        statCard3.setLayout(new javax.swing.BoxLayout(statCard3, javax.swing.BoxLayout.Y_AXIS));
        statIcon3.setIcon(new FlatIcon(FlatIcon.Kind.PEN, AppTheme.CHART_PURPLE, 22));
        statIcon3.setAlignmentX(0.0F);
        statCard3.add(statIcon3);
        statCard3.add(filler12);
        statValue3.setFont(statValue3.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        statValue3.setForeground(AppTheme.TEXT);
        statValue3.setText("0");
        statValue3.setAlignmentX(0.0F);
        statCard3.add(statValue3);
        statCard3.add(filler13);
        statCaption3.setFont(statCaption3.getFont().deriveFont(12f));
        statCaption3.setForeground(new java.awt.Color(138, 131, 116));
        statCaption3.setText("Số đánh giá đã viết");
        statCaption3.setAlignmentX(0.0F);
        statCard3.add(statCaption3);
        statRow.add(statCard3);

        top.add(statRow);

        top.add(filler14);
        orderCard.setBackground(new java.awt.Color(255, 255, 255));
        orderCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        orderCard.setAlignmentX(0.0F);
        orderCard.setMaximumSize(new java.awt.Dimension(2147483647, 300));
        orderCard.setPreferredSize(new java.awt.Dimension(0, 300));
        orderCard.setLayout(new java.awt.BorderLayout(0, 10));
        orderHeader.setBackground(new java.awt.Color(255, 255, 255));
        orderHeader.setLayout(new java.awt.BorderLayout());
        orderTitle.setFont(orderTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        orderTitle.setForeground(AppTheme.TEXT);
        orderTitle.setText("Lịch sử đơn hàng");
        orderHeader.add(orderTitle, java.awt.BorderLayout.WEST);
        btnDetail.setText("Xem chi tiết");
        orderHeader.add(btnDetail, java.awt.BorderLayout.EAST);
        orderCard.add(orderHeader, java.awt.BorderLayout.NORTH);

        orderScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        orderTable.setGridColor(AppTheme.TABLE_GRID);
        orderTable.setRowHeight(28);
        orderScroll.setViewportView(orderTable);
        orderCard.add(orderScroll, java.awt.BorderLayout.CENTER);

        orderEmpty.setForeground(new java.awt.Color(138, 131, 116));
        orderEmpty.setText("Khách hàng chưa có đơn hàng nào.");
        orderCard.add(orderEmpty, java.awt.BorderLayout.SOUTH);
        top.add(orderCard);

        top.add(filler15);
        reviewCard.setBackground(new java.awt.Color(255, 255, 255));
        reviewCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        reviewCard.setAlignmentX(0.0F);
        reviewCard.setMaximumSize(new java.awt.Dimension(2147483647, 260));
        reviewCard.setPreferredSize(new java.awt.Dimension(0, 260));
        reviewCard.setLayout(new java.awt.BorderLayout(0, 10));
        reviewHeader.setBackground(new java.awt.Color(255, 255, 255));
        reviewHeader.setLayout(new java.awt.BorderLayout());
        reviewTitle.setFont(reviewTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        reviewTitle.setForeground(AppTheme.TEXT);
        reviewTitle.setText("Đánh giá đã viết");
        reviewHeader.add(reviewTitle, java.awt.BorderLayout.WEST);
        reviewCard.add(reviewHeader, java.awt.BorderLayout.NORTH);

        reviewScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        reviewTable.setGridColor(AppTheme.TABLE_GRID);
        reviewTable.setRowHeight(28);
        reviewScroll.setViewportView(reviewTable);
        reviewCard.add(reviewScroll, java.awt.BorderLayout.CENTER);

        reviewEmpty.setForeground(new java.awt.Color(138, 131, 116));
        reviewEmpty.setText("Khách hàng chưa viết đánh giá nào.");
        reviewCard.add(reviewEmpty, java.awt.BorderLayout.SOUTH);
        top.add(reviewCard);

        pageScroll.setViewportView(top);

        add(pageScroll, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDetail;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel heading;
    private javax.swing.JPanel infoCard;
    private javax.swing.JPanel infoField1;
    private javax.swing.JPanel infoField2;
    private javax.swing.JPanel infoField3;
    private javax.swing.JPanel infoField4;
    private javax.swing.JPanel infoField5;
    private javax.swing.JPanel infoField6;
    private javax.swing.JLabel infoLabel1;
    private javax.swing.JLabel infoLabel2;
    private javax.swing.JLabel infoLabel3;
    private javax.swing.JLabel infoLabel4;
    private javax.swing.JLabel infoLabel5;
    private javax.swing.JLabel infoLabel6;
    private javax.swing.JLabel infoValue1;
    private javax.swing.JLabel infoValue2;
    private javax.swing.JLabel infoValue3;
    private javax.swing.JLabel infoValue4;
    private javax.swing.JLabel infoValue5;
    private javax.swing.JLabel infoValue6;
    private javax.swing.JPanel orderCard;
    private javax.swing.JLabel orderEmpty;
    private javax.swing.JPanel orderHeader;
    private javax.swing.JScrollPane orderScroll;
    private javax.swing.JTable orderTable;
    private javax.swing.JLabel orderTitle;
    private javax.swing.JScrollPane pageScroll;
    private javax.swing.JPanel reviewCard;
    private javax.swing.JLabel reviewEmpty;
    private javax.swing.JPanel reviewHeader;
    private javax.swing.JScrollPane reviewScroll;
    private javax.swing.JTable reviewTable;
    private javax.swing.JLabel reviewTitle;
    private javax.swing.JLabel statCaption1;
    private javax.swing.JLabel statCaption2;
    private javax.swing.JLabel statCaption3;
    private javax.swing.JPanel statCard1;
    private javax.swing.JPanel statCard2;
    private javax.swing.JPanel statCard3;
    private javax.swing.JLabel statIcon1;
    private javax.swing.JLabel statIcon2;
    private javax.swing.JLabel statIcon3;
    private javax.swing.JPanel statRow;
    private javax.swing.JLabel statValue1;
    private javax.swing.JLabel statValue2;
    private javax.swing.JLabel statValue3;
    private javax.swing.JPanel top;
    // End of variables declaration//GEN-END:variables
}
