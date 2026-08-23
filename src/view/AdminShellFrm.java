/*
 * Khung Admin DUY NHẤT (1 JFrame sống suốt phiên làm việc) — sidebar trái +
 * topbar cố định, chỉ có phần nội dung ở giữa được thay khi điều hướng
 * (navigateTo). Trước đây mỗi trang là 1 JFrame riêng (dispose() rồi mở
 * JFrame mới) khiến cả cửa sổ "nháy" lại từ đầu mỗi lần chuyển trang, giống
 * mở tab mới hơn là 1 ứng dụng thật — AdminShellFrm thay thế cách làm đó.
 */
package view;

import dao.DAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Account;

public class AdminShellFrm extends javax.swing.JFrame {

    final Account account;

    private JPanel contentContainer;
    private JPanel topbarLeft;

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
        setTitle("Bookshop Management - " + page.title());
    }

    private void initShell() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        getContentPane().setLayout(new BorderLayout());
        // Title bar (FlatLaf vẽ riêng, cũng màu HEADER) và khối "Bookshop Admin"
        // ngay bên dưới cùng là màu xanh olive — nếu không có đường kẻ phân tách,
        // 2 khối sẽ nhìn dính liền thành 1. Thêm viền sáng màu nền để tách rõ.
        ((JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, AppTheme.BACKGROUND));

        contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(AppTheme.BACKGROUND);

        // Sidebar phải nằm ở WEST của layout NGOÀI CÙNG để chiếm trọn chiều cao
        // cửa sổ. Topbar + content được gộp vào 1 panel con riêng (layout riêng)
        // rồi mới đặt panel đó vào CENTER — nếu đặt topbar thẳng vào NORTH của
        // layout ngoài cùng thì nó sẽ tràn full chiều ngang, đẩy sidebar tụt
        // xuống dưới nó thay vì nằm cạnh nó.
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.add(buildTopbar(), BorderLayout.NORTH);
        rightSide.add(contentContainer, BorderLayout.CENTER);

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
        sidebar.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));

        // Panel cao đúng bằng TOPBAR_HEIGHT để hàng "Bookshop Admin" luôn ngang
        // hàng với topbar bên phải, không bị lệch cao/thấp giữa 2 bên.
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(AppTheme.HEADER);
        brandPanel.setBorder(new EmptyBorder(0, 18, 0, 12));
        brandPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        brandPanel.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));

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
        brandDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        brandDivider.setPreferredSize(new Dimension(0, 2));
        brandDivider.setBackground(AppTheme.PRIMARY_HOVER);
        sidebar.add(brandDivider);

        addNavItem(sidebar, "dashboard", FlatIcon.Kind.HOME, "Dashboard", () -> navigateTo(new DashboardPage()));

        sidebar.add(sidebarSeparator());
        sidebar.add(sidebarSectionLabel("QUẢN LÝ"));

        addNavItem(sidebar, "books", FlatIcon.Kind.BOOK, "Quản lý sách", () -> navigateTo(new ManageBooksPage()));

        addNavItem(sidebar, "category", FlatIcon.Kind.TAG, "Quản lý danh mục", () -> navigateTo(new ManageCategoryPage()));
        addNavItem(sidebar, "author", FlatIcon.Kind.PEN, "Quản lý tác giả", () -> navigateTo(new ManageAuthorPage()));
        addNavItem(sidebar, "publisher", FlatIcon.Kind.BUILDING, "Quản lý nhà xuất bản", () -> navigateTo(new ManagePublisherPage()));
        addNavItem(sidebar, "accounts", FlatIcon.Kind.USER, "Quản lý tài khoản", () -> navigateTo(new ManageAccountPage()));
        addNavItem(sidebar, "employees", FlatIcon.Kind.USERS, "Quản lý nhân viên", () -> navigateTo(new ManageEmployeePage()));

        sidebar.add(sidebarSeparator());
        sidebar.add(sidebarSectionLabel("BÁO CÁO"));
        addNavItem(sidebar, "reports", FlatIcon.Kind.CHART, "Báo cáo thống kê", () -> navigateTo(new ReportPage()));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarSeparator());

        JButton btnLogout = sidebarButton(FlatIcon.Kind.LOGOUT, "Đăng xuất");
        btnLogout.addActionListener(e -> {
            new DAO();
            (new LoginFrm()).setVisible(true);
            this.dispose();
        });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(8));

        return sidebar;
    }

    private void addNavItem(JPanel sidebar, String key, FlatIcon.Kind kind, String text, Runnable onClick) {
        JButton btn = sidebarButton(kind, text);
        btn.addActionListener(e -> onClick.run());
        sidebar.add(btn);
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
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBackground(AppTheme.PRIMARY_HOVER);
        return sep;
    }

    private JPanel buildTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppTheme.BACKGROUND);
        topbar.setPreferredSize(new Dimension(0, AppTheme.TOPBAR_HEIGHT));
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.INPUT_BORDER));

        topbarLeft = new JPanel();
        topbarLeft.setLayout(new BoxLayout(topbarLeft, BoxLayout.X_AXIS));
        topbarLeft.setBackground(AppTheme.BACKGROUND);
        topbarLeft.setBorder(new EmptyBorder(0, 18, 0, 0));
        topbar.add(topbarLeft, BorderLayout.WEST);

        String username = (account != null && account.getUsername() != null) ? account.getUsername() : "admin";
        JLabel userLabel = new JLabel("Xin chào, " + username + "  ");
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setForeground(AppTheme.TEXT);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.PLAIN, 13f));
        topbar.add(userLabel, BorderLayout.EAST);

        return topbar;
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
}
