/*
 * Trang Admin Home — khung theo mẫu "SB Admin 2" (sidebar trái điều hướng +
 * topbar + khu nội dung), màu sắc theo đúng AppTheme hiện có của dự án.
 *
 * Danh sách mục sidebar lấy đúng theo phạm vi Administrator trong SRS
 * (FR-ADM-01..10). Chỉ "Quản lý sách" đã có màn hình thật (ManageBooksFrm);
 * các mục còn lại hiển thị để đúng khung tổng thể nhưng chưa có giao diện
 * phía sau — bấm vào sẽ báo "chưa được hỗ trợ", không giả vờ đã hoàn thiện.
 */
package view;

import dao.DAO;
import dao.DashboardDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Account;

/**
 *
 * @author anhthu632
 */
public class AdminHomeFrm extends javax.swing.JFrame implements ActionListener {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminHomeFrm.class.getName());

    private Account account;

    private JButton btnManageBooks;
    private JButton btnManageCategory;
    private JButton btnManageAuthor;
    private JButton btnManagePublisher;
    private JButton btnManageAccounts;
    private JButton btnManageEmployees;
    private JButton btnReports;
    private JButton btnLogout;

    /**
     * Creates new form AdminHomeFrm
     */
    public AdminHomeFrm(Account a) {
        this.account = a;
        initComponents();
        setTitle("Bookshop Management - Trang Quản Trị");
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        getContentPane().setLayout(new BorderLayout());
        // Title bar (FlatLaf vẽ riêng, cũng màu HEADER) và khối "Bookshop Admin"
        // ngay bên dưới cùng là màu xanh olive — nếu không có đường kẻ phân tách,
        // 2 khối sẽ nhìn dính liền thành 1. Thêm viền sáng màu nền để tách rõ.
        ((javax.swing.JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, AppTheme.BACKGROUND));

        // Sidebar phải nằm ở WEST của layout NGOÀI CÙNG để chiếm trọn chiều cao
        // cửa sổ. Topbar + content được gộp vào 1 panel con riêng (layout riêng)
        // rồi mới đặt panel đó vào CENTER — nếu đặt topbar thẳng vào NORTH của
        // layout ngoài cùng thì nó sẽ tràn full chiều ngang, đẩy sidebar tụt
        // xuống dưới nó thay vì nằm cạnh nó.
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.add(buildTopbar(), BorderLayout.NORTH);
        rightSide.add(buildContent(), BorderLayout.CENTER);

        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(rightSide, BorderLayout.CENTER);

        setSize(1000, 640);
        setLocationRelativeTo(null);
        AppTheme.maximize(this);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppTheme.HEADER);
        sidebar.setPreferredSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, 0));

        // Panel cao đúng bằng TOPBAR_HEIGHT để hàng "Bookshop Admin" luôn ngang
        // hàng với topbar bên phải, không bị lệch cao/thấp giữa 2 bên.
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(AppTheme.HEADER);
        brandPanel.setBorder(new EmptyBorder(0, 18, 0, 12));
        brandPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        brandPanel.setPreferredSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMinimumSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));

        JLabel brand = new JLabel("Bookshop Admin");
        brand.setIcon(new FlatIcon(FlatIcon.Kind.BOOK, Color.WHITE, 20));
        brand.setIconTextGap(10);
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 17f));
        brandPanel.add(brand, BorderLayout.CENTER);
        sidebar.add(brandPanel);

        // Đường phân cách rõ ràng giữa khối brand và danh sách điều hướng bên
        // dưới — dày hơn separator thường (2px) để dễ nhận ra ranh giới.
        JPanel brandDivider = new JPanel();
        brandDivider.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 2));
        brandDivider.setPreferredSize(new java.awt.Dimension(0, 2));
        brandDivider.setBackground(AppTheme.PRIMARY_HOVER);
        sidebar.add(brandDivider);

        sidebar.add(sidebarSectionLabel("QUẢN LÝ"));
        btnManageBooks = sidebarButton(FlatIcon.Kind.BOOK, "Quản lý sách", true);
        sidebar.add(btnManageBooks);
        btnManageCategory = sidebarButton(FlatIcon.Kind.TAG, "Quản lý danh mục", false);
        sidebar.add(btnManageCategory);
        btnManageAuthor = sidebarButton(FlatIcon.Kind.PEN, "Quản lý tác giả", false);
        sidebar.add(btnManageAuthor);
        btnManagePublisher = sidebarButton(FlatIcon.Kind.BUILDING, "Quản lý nhà xuất bản", false);
        sidebar.add(btnManagePublisher);
        btnManageAccounts = sidebarButton(FlatIcon.Kind.USER, "Quản lý tài khoản", false);
        sidebar.add(btnManageAccounts);
        btnManageEmployees = sidebarButton(FlatIcon.Kind.USERS, "Quản lý nhân viên", false);
        sidebar.add(btnManageEmployees);

        sidebar.add(sidebarSeparator());
        sidebar.add(sidebarSectionLabel("BÁO CÁO"));
        btnReports = sidebarButton(FlatIcon.Kind.CHART, "Báo cáo thống kê", false);
        sidebar.add(btnReports);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarSeparator());
        btnLogout = sidebarButton(FlatIcon.Kind.LOGOUT, "Đăng xuất", false);
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(8));

        btnManageBooks.addActionListener(this);
        btnManageCategory.addActionListener(this);
        btnManageAuthor.addActionListener(this);
        btnManagePublisher.addActionListener(this);
        btnManageAccounts.addActionListener(this);
        btnManageEmployees.addActionListener(this);
        btnReports.addActionListener(this);
        btnLogout.addActionListener(this);

        return sidebar;
    }

    private JButton sidebarButton(FlatIcon.Kind kind, String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setIcon(new FlatIcon(kind, active ? Color.WHITE : AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btn.setIconTextGap(10);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 42));
        if (active) {
            AppTheme.sidebarNav(btn);
        } else {
            AppTheme.sidebarNavMuted(btn);
        }
        return btn;
    }

    private JLabel sidebarSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setBorder(new EmptyBorder(14, 18, 6, 12));
        lbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel sidebarSeparator() {
        JPanel sep = new JPanel();
        sep.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 1));
        sep.setBackground(AppTheme.PRIMARY_HOVER);
        return sep;
    }

    private JPanel buildTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppTheme.BACKGROUND);
        topbar.setPreferredSize(new java.awt.Dimension(0, AppTheme.TOPBAR_HEIGHT));
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.INPUT_BORDER));

        String username = (account != null && account.getUsername() != null) ? account.getUsername() : "admin";
        JLabel userLabel = new JLabel("Xin chào, " + username + "  ");
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setForeground(AppTheme.TEXT);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.PLAIN, 13f));
        topbar.add(userLabel, BorderLayout.EAST);

        return topbar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        top.add(buildStatCards());

        top.add(Box.createVerticalStrut(18));
        top.add(buildChartsGrid());

        content.add(top, BorderLayout.NORTH);
        return content;
    }

    // 4 biểu đồ khác dạng nhau, mỗi cái dùng 1 loại số liệu thật khác nhau từ
    // DB qua DashboardDAO — không phải dữ liệu dựng sẵn để trang trí:
    //   - Cột đứng   : doanh thu theo ngày (đơn "Paid")
    //   - Đường/vùng : số lượng đơn hàng theo ngày (mọi trạng thái)
    //   - Donut      : tỉ lệ đơn hàng theo trạng thái (Pending/Paid/Cancelled)
    //   - Cột ngang  : số đơn theo từng phương thức thanh toán
    private JPanel buildChartsGrid() {
        DashboardDAO dashboardDAO = new DashboardDAO();

        java.util.List<BarChartPanel.Item> revenueItems = new java.util.ArrayList<>();
        for (DashboardDAO.RevenuePoint p : dashboardDAO.revenueByDay()) {
            revenueItems.add(new BarChartPanel.Item(p.label, p.amount));
        }

        java.util.List<BarChartPanel.Item> orderCountItems = new java.util.ArrayList<>();
        for (DashboardDAO.RevenuePoint p : dashboardDAO.orderCountByDay()) {
            orderCountItems.add(new BarChartPanel.Item(p.label, p.amount));
        }

        java.util.List<DashboardDAO.LabelCount> statusItems = dashboardDAO.orderStatusBreakdown();
        java.util.List<DashboardDAO.LabelCount> paymentItems = dashboardDAO.paymentMethodBreakdown();

        // Không đặt maximumSize/preferredSize theo chiều rộng cố định — để
        // BoxLayout giãn full chiều ngang giống hệt hàng thẻ thống kê phía
        // trên (row cũng dùng Integer.MAX_VALUE cho width), nhờ đó 2 hàng
        // luôn thẳng mép nhau thay vì hàng dưới bị bó hẹp hơn hàng trên.
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setBackground(AppTheme.BACKGROUND);
        grid.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        grid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 520));
        grid.setPreferredSize(new java.awt.Dimension(0, 520));

        grid.add(BarChartPanel.card("Doanh thu theo ngày (đơn đã thanh toán)", revenueItems, AppTheme.CHART_PINK, 250));
        grid.add(LineChartPanel.card("Số đơn hàng theo ngày", orderCountItems, AppTheme.CHART_SKY, 250));
        grid.add(DonutChartPanel.card("Tỉ lệ đơn hàng theo trạng thái", statusItems, AppTheme.CHART_GOLD, 250));
        grid.add(HorizontalBarChartPanel.card("Đơn hàng theo phương thức thanh toán", paymentItems, AppTheme.CHART_ORANGE, 250));

        return grid;
    }

    // Số liệu thật lấy từ DB qua DashboardDAO (COUNT/SUM) — không phải số giả
    // định để trang trí. Doanh thu chỉ tính đơn đã "Paid" (xem AS-03 trong SRS).
    private JPanel buildStatCards() {
        DashboardDAO dashboardDAO = new DashboardDAO();

        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setBackground(AppTheme.BACKGROUND);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 96));
        row.setPreferredSize(new java.awt.Dimension(0, 96));

        row.add(statCard(FlatIcon.Kind.BOOK, String.valueOf(dashboardDAO.countBooks()), "Tổng số sách", AppTheme.CHART_SKY));
        row.add(statCard(FlatIcon.Kind.RECEIPT, String.valueOf(dashboardDAO.countOrders()), "Tổng đơn hàng", AppTheme.CHART_ORANGE));
        row.add(statCard(FlatIcon.Kind.USERS, String.valueOf(dashboardDAO.countCustomers()), "Tổng khách hàng", AppTheme.CHART_PURPLE));
        row.add(statCard(FlatIcon.Kind.MONEY, String.format("%,.0f₫", dashboardDAO.totalRevenue()), "Doanh thu đã thu", AppTheme.CHART_GOLD));

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
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnManageBooks) {
            (new ManageBooksFrm(account)).setVisible(true);
            this.dispose();
        } else if (e.getSource() == btnLogout) {
            new DAO();
            (new LoginFrm()).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Chức năng này chưa được hỗ trợ trong phiên bản hiện tại!");
        }
    }
}
