/*
 * Khung Admin DUY NHẤT (1 JFrame sống suốt phiên làm việc) — sidebar trái +
 * topbar cố định, chỉ có phần nội dung ở giữa được thay khi điều hướng
 * (navigateTo). Trước đây mỗi trang là 1 JFrame riêng (dispose() rồi mở
 * JFrame mới) khiến cả cửa sổ "nháy" lại từ đầu mỗi lần chuyển trang, giống
 * mở tab mới hơn là 1 ứng dụng thật — AdminShellFrm thay thế cách làm đó.
 */
package view.admin;
import view.shared.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import model.Account;

public class AdminShellFrm extends javax.swing.JFrame {

    final Account account;

    private static final class NavItem {

        final String key;
        final JButton button;
        final FlatIcon.Kind kind;

        NavItem(String key, JButton button, FlatIcon.Kind kind) {
            this.key = key;
            this.button = button;
            this.kind = kind;
        }
    }
    private final List<NavItem> navItems = new ArrayList<>();

    public AdminShellFrm(Account a) {
        this.account = a;
        initComponents();
        initShell();
        navigateTo(new DashboardPage());
    }

    /** Thay nội dung trang hiện tại bằng "page", cập nhật topbar + tô sáng đúng mục sidebar — không mở cửa sổ mới. */
    public void navigateTo(AdminPage page) {
        contentContainer.removeAll();
        contentContainer.add(page.content(this), BorderLayout.CENTER);
        contentContainer.revalidate();
        contentContainer.repaint();

        updateTopbarLeft(page);
        setActiveNav(page.navKey());
        setTitle("Vintage Books - " + page.title());
    }

    private void initShell() {
        getContentPane().setBackground(AppTheme.BACKGROUND);
        // Title bar (FlatLaf vẽ riêng, cũng màu HEADER) và khối "Vintage Books Admin"
        // ngay bên dưới cùng là màu xanh olive — nếu không có đường kẻ phân tách,
        // 2 khối sẽ nhìn dính liền thành 1. Thêm viền sáng màu nền để tách rõ.
        ((JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, AppTheme.BACKGROUND));

        // Sidebar (WEST) + rightSide (topbar NORTH, contentContainer CENTER)
        // được dựng trong form (initComponents). Ở đây chỉ gắn hành vi.
        addNavItem(btnNavDashboard, "dashboard", FlatIcon.Kind.HOME, () -> navigateTo(new DashboardPage()));
        addNavItem(btnNavBooks, "books", FlatIcon.Kind.BOOK, () -> navigateTo(new ManageBooksPage()));
        addNavItem(btnNavCategory, "category", FlatIcon.Kind.TAG, () -> navigateTo(new ManageCategoryPage()));
        addNavItem(btnNavAuthor, "author", FlatIcon.Kind.PEN, () -> navigateTo(new ManageAuthorPage()));
        addNavItem(btnNavPublisher, "publisher", FlatIcon.Kind.BUILDING, () -> navigateTo(new ManagePublisherPage()));
        addNavItem(btnNavSupplier, "supplier", FlatIcon.Kind.BUILDING, () -> navigateTo(new ManageSupplierPage()));
        addNavItem(btnNavCustomers, "customers", FlatIcon.Kind.USER, () -> navigateTo(new ManageCustomerPage()));
        addNavItem(btnNavEmployees, "employees", FlatIcon.Kind.USERS, () -> navigateTo(new ManageEmployeePage()));
        addNavItem(btnNavAccounts, "accounts", FlatIcon.Kind.SEARCH, () -> navigateTo(new ManageAccountPage()));
        addNavItem(btnNavReports, "reports", FlatIcon.Kind.CHART, () -> navigateTo(new ReportPage()));

        AppTheme.sidebarNavMuted(btnChangePassword);
        btnChangePassword.addActionListener(e -> ChangePasswordDialog.show(this, account.getUsername()));

        AppTheme.sidebarNavMuted(btnLogout);
        btnLogout.addActionListener(e -> {
            view.customer.CustomerHomeFrm.openAsGuest();
            this.dispose();
        });

        String username = (account != null && account.getUsername() != null) ? account.getUsername() : "admin";
        userLabel.setText("Xin chào, " + username + "  ");

        setSize(1000, 640);
        setLocationRelativeTo(null);
        AppTheme.maximize(this);
    }

    private void addNavItem(JButton btn, String key, FlatIcon.Kind kind, Runnable onClick) {
        AppTheme.sidebarNavMuted(btn);
        btn.addActionListener(e -> onClick.run());
        navItems.add(new NavItem(key, btn, kind));
    }

    private JButton unsupportedNavItem(FlatIcon.Kind kind, String text) {
        JButton btn = sidebarButton(kind, text);
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chức năng này chưa được hỗ trợ trong phiên bản hiện tại!"));
        return btn;
    }

    /** Tô sáng đúng mục sidebar tương ứng "key" đang active, làm mờ các mục còn lại. */
    private void setActiveNav(String key) {
        for (NavItem item : navItems) {
            boolean active = item.key.equals(key);
            item.button.setIcon(new FlatIcon(item.kind, active ? Color.WHITE : AppTheme.SIDEBAR_TEXT_MUTED, 16));
            if (active) {
                AppTheme.sidebarNav(item.button);
            } else {
                AppTheme.sidebarNavMuted(item.button);
            }
        }
    }

    private JButton sidebarButton(FlatIcon.Kind kind, String text) {
        JButton btn = new JButton(text);
        btn.setIcon(new FlatIcon(kind, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btn.setIconTextGap(10);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        AppTheme.sidebarNavMuted(btn);
        return btn;
    }

    /**
     * Dựng lại nút "quay lại" bên trái topbar mỗi lần điều hướng (nếu trang
     * có backTarget). Không hiện lại tên trang ở đây nữa — mỗi trang đã tự
     * có heading riêng trong nội dung, để cả 2 chỗ cùng hiện bị trùng lặp.
     */
    private void updateTopbarLeft(AdminPage page) {
        topbarLeft.removeAll();

        AdminPage back = page.backTarget();
        if (back != null) {
            JButton btnBack = new JButton();
            btnBack.setIcon(new FlatIcon(FlatIcon.Kind.BACK, AppTheme.TEXT, 16));
            btnBack.setToolTipText("Quay lại");
            AppTheme.iconButton(btnBack);
            btnBack.addActionListener(e -> navigateTo(back));
            topbarLeft.add(btnBack);
        }

        topbarLeft.revalidate();
        topbarLeft.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebar = new javax.swing.JPanel();
        brandPanel = new javax.swing.JPanel();
        brand = new javax.swing.JLabel();
        brandDivider = new javax.swing.JPanel();
        btnNavDashboard = new javax.swing.JButton();
        sidebarSeparator1 = new javax.swing.JPanel();
        lblSectionManage = new javax.swing.JLabel();
        btnNavBooks = new javax.swing.JButton();
        btnNavCategory = new javax.swing.JButton();
        btnNavAuthor = new javax.swing.JButton();
        btnNavPublisher = new javax.swing.JButton();
        btnNavSupplier = new javax.swing.JButton();
        btnNavCustomers = new javax.swing.JButton();
        btnNavEmployees = new javax.swing.JButton();
        btnNavAccounts = new javax.swing.JButton();
        sidebarSeparator2 = new javax.swing.JPanel();
        lblSectionReport = new javax.swing.JLabel();
        btnNavReports = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        sidebarSeparator3 = new javax.swing.JPanel();
        btnChangePassword = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        rightSide = new javax.swing.JPanel();
        topbar = new javax.swing.JPanel();
        topbarLeft = new javax.swing.JPanel();
        userLabel = new javax.swing.JLabel();
        contentContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(3);
        getContentPane().setLayout(new java.awt.BorderLayout());

        sidebar.setBackground(AppTheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        brandPanel.setBackground(AppTheme.SIDEBAR_BG);
        brandPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 12));
        brandPanel.setAlignmentX(0.0F);
        brandPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMinimumSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setPreferredSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setLayout(new java.awt.BorderLayout());
        brand.setText("Vintage Books");
        brand.setForeground(new java.awt.Color(122, 74, 51));
        brand.setFont(new java.awt.Font("Cormorant Garamond", 1, 32)); // NOI18N
        brandPanel.add(brand, java.awt.BorderLayout.CENTER);
        sidebar.add(brandPanel);

        brandDivider.setMaximumSize(new java.awt.Dimension(2147483647, 2));
        brandDivider.setPreferredSize(new java.awt.Dimension(0, 2));
        brandDivider.setBackground(AppTheme.LINE);
        brandDivider.setLayout(new java.awt.FlowLayout());
        sidebar.add(brandDivider);

        btnNavDashboard.setText("Tổng quan");
        btnNavDashboard.setIcon(new FlatIcon(FlatIcon.Kind.HOME, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavDashboard.setIconTextGap(10);
        btnNavDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavDashboard.setAlignmentX(0.0F);
        btnNavDashboard.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavDashboard);
        sidebarSeparator1.setBackground(AppTheme.LINE);
        sidebarSeparator1.setMaximumSize(new java.awt.Dimension(2147483647, 1));
        sidebarSeparator1.setPreferredSize(new java.awt.Dimension(0, 1));
        sidebarSeparator1.setLayout(new java.awt.FlowLayout());
        sidebar.add(sidebarSeparator1);

        lblSectionManage.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        lblSectionManage.setFont(lblSectionManage.getFont().deriveFont(java.awt.Font.BOLD, 11f));
        lblSectionManage.setText("QUẢN LÝ");
        lblSectionManage.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 6, 12));
        lblSectionManage.setAlignmentX(0.0F);
        sidebar.add(lblSectionManage);
        btnNavBooks.setText("Quản lý sách");
        btnNavBooks.setIcon(new FlatIcon(FlatIcon.Kind.BOOK, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavBooks.setIconTextGap(10);
        btnNavBooks.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavBooks.setAlignmentX(0.0F);
        btnNavBooks.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavBooks);
        btnNavCategory.setText("Quản lý danh mục");
        btnNavCategory.setIcon(new FlatIcon(FlatIcon.Kind.TAG, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavCategory.setIconTextGap(10);
        btnNavCategory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavCategory.setAlignmentX(0.0F);
        btnNavCategory.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavCategory);
        btnNavAuthor.setText("Quản lý tác giả");
        btnNavAuthor.setIcon(new FlatIcon(FlatIcon.Kind.PEN, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavAuthor.setIconTextGap(10);
        btnNavAuthor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavAuthor.setAlignmentX(0.0F);
        btnNavAuthor.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavAuthor);
        btnNavPublisher.setText("Quản lý nhà xuất bản");
        btnNavPublisher.setIcon(new FlatIcon(FlatIcon.Kind.BUILDING, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavPublisher.setIconTextGap(10);
        btnNavPublisher.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPublisher.setAlignmentX(0.0F);
        btnNavPublisher.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavPublisher);
        btnNavSupplier.setText("Quản lý nhà cung cấp");
        btnNavSupplier.setIcon(new FlatIcon(FlatIcon.Kind.BUILDING, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavSupplier.setIconTextGap(10);
        btnNavSupplier.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavSupplier.setAlignmentX(0.0F);
        btnNavSupplier.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavSupplier);
        btnNavCustomers.setText("Quản lý khách hàng");
        btnNavCustomers.setIcon(new FlatIcon(FlatIcon.Kind.USER, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavCustomers.setIconTextGap(10);
        btnNavCustomers.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavCustomers.setAlignmentX(0.0F);
        btnNavCustomers.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavCustomers);
        btnNavEmployees.setText("Quản lý nhân viên");
        btnNavEmployees.setIcon(new FlatIcon(FlatIcon.Kind.USERS, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavEmployees.setIconTextGap(10);
        btnNavEmployees.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavEmployees.setAlignmentX(0.0F);
        btnNavEmployees.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavEmployees);
        btnNavAccounts.setText("Quản lý tài khoản");
        btnNavAccounts.setIcon(new FlatIcon(FlatIcon.Kind.SEARCH, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavAccounts.setIconTextGap(10);
        btnNavAccounts.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavAccounts.setAlignmentX(0.0F);
        btnNavAccounts.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavAccounts);
        sidebarSeparator2.setBackground(AppTheme.LINE);
        sidebarSeparator2.setMaximumSize(new java.awt.Dimension(2147483647, 1));
        sidebarSeparator2.setPreferredSize(new java.awt.Dimension(0, 1));
        sidebarSeparator2.setLayout(new java.awt.FlowLayout());
        sidebar.add(sidebarSeparator2);

        lblSectionReport.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        lblSectionReport.setFont(lblSectionReport.getFont().deriveFont(java.awt.Font.BOLD, 11f));
        lblSectionReport.setText("BÁO CÁO");
        lblSectionReport.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 6, 12));
        lblSectionReport.setAlignmentX(0.0F);
        sidebar.add(lblSectionReport);
        btnNavReports.setText("Báo cáo thống kê");
        btnNavReports.setIcon(new FlatIcon(FlatIcon.Kind.CHART, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnNavReports.setIconTextGap(10);
        btnNavReports.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavReports.setAlignmentX(0.0F);
        btnNavReports.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnNavReports);
        sidebar.add(filler1);
        sidebarSeparator3.setBackground(AppTheme.LINE);
        sidebarSeparator3.setMaximumSize(new java.awt.Dimension(2147483647, 1));
        sidebarSeparator3.setPreferredSize(new java.awt.Dimension(0, 1));
        sidebarSeparator3.setLayout(new java.awt.FlowLayout());
        sidebar.add(sidebarSeparator3);

        btnChangePassword.setText("Đổi mật khẩu");
        btnChangePassword.setIcon(new FlatIcon(FlatIcon.Kind.USER, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnChangePassword.setIconTextGap(10);
        btnChangePassword.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnChangePassword.setAlignmentX(0.0F);
        btnChangePassword.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnChangePassword);
        btnLogout.setText("Đăng xuất");
        btnLogout.setIcon(new FlatIcon(FlatIcon.Kind.LOGOUT, AppTheme.SIDEBAR_TEXT_MUTED, 16));
        btnLogout.setIconTextGap(10);
        btnLogout.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogout.setAlignmentX(0.0F);
        btnLogout.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        sidebar.add(btnLogout);
        sidebar.add(filler2);
        getContentPane().add(sidebar, java.awt.BorderLayout.WEST);

        rightSide.setLayout(new java.awt.BorderLayout());
        topbar.setBackground(AppTheme.BACKGROUND);
        topbar.setPreferredSize(new java.awt.Dimension(0, AppTheme.TOPBAR_HEIGHT));
        topbar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.INPUT_BORDER));
        topbar.setLayout(new java.awt.BorderLayout());
        topbarLeft.setBackground(AppTheme.BACKGROUND);
        topbarLeft.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        topbarLeft.setLayout(new javax.swing.BoxLayout(topbarLeft, javax.swing.BoxLayout.X_AXIS));
        topbar.add(topbarLeft, java.awt.BorderLayout.WEST);

        userLabel.setText("Xin chào, admin  ");
        userLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        userLabel.setForeground(AppTheme.TEXT);
        userLabel.setFont(userLabel.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
        topbar.add(userLabel, java.awt.BorderLayout.EAST);
        rightSide.add(topbar, java.awt.BorderLayout.NORTH);

        contentContainer.setBackground(AppTheme.BACKGROUND);
        contentContainer.setLayout(new java.awt.BorderLayout());
        rightSide.add(contentContainer, java.awt.BorderLayout.CENTER);

        getContentPane().add(rightSide, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel brand;
    private javax.swing.JPanel brandDivider;
    private javax.swing.JPanel brandPanel;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavAccounts;
    private javax.swing.JButton btnNavAuthor;
    private javax.swing.JButton btnNavBooks;
    private javax.swing.JButton btnNavCategory;
    private javax.swing.JButton btnNavCustomers;
    private javax.swing.JButton btnNavDashboard;
    private javax.swing.JButton btnNavEmployees;
    private javax.swing.JButton btnNavPublisher;
    private javax.swing.JButton btnNavReports;
    private javax.swing.JButton btnNavSupplier;
    private javax.swing.JPanel contentContainer;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel lblSectionManage;
    private javax.swing.JLabel lblSectionReport;
    private javax.swing.JPanel rightSide;
    private javax.swing.JPanel sidebar;
    private javax.swing.JPanel sidebarSeparator1;
    private javax.swing.JPanel sidebarSeparator2;
    private javax.swing.JPanel sidebarSeparator3;
    private javax.swing.JPanel topbar;
    private javax.swing.JPanel topbarLeft;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
}
