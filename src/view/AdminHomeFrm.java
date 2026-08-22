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
import java.awt.BorderLayout;
import java.awt.Font;
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

        JLabel brand = new JLabel("📚  Bookshop Admin");
        brand.setForeground(java.awt.Color.WHITE);
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
        btnManageBooks = sidebarButton("📖  Quản lý sách", AppTheme::sidebarNav);
        sidebar.add(btnManageBooks);
        btnManageCategory = sidebarButton("🗂️  Quản lý danh mục", AppTheme::sidebarNavMuted);
        sidebar.add(btnManageCategory);
        btnManageAuthor = sidebarButton("✍️  Quản lý tác giả", AppTheme::sidebarNavMuted);
        sidebar.add(btnManageAuthor);
        btnManagePublisher = sidebarButton("🏢  Quản lý nhà xuất bản", AppTheme::sidebarNavMuted);
        sidebar.add(btnManagePublisher);
        btnManageAccounts = sidebarButton("👤  Quản lý tài khoản", AppTheme::sidebarNavMuted);
        sidebar.add(btnManageAccounts);
        btnManageEmployees = sidebarButton("👥  Quản lý nhân viên", AppTheme::sidebarNavMuted);
        sidebar.add(btnManageEmployees);

        sidebar.add(sidebarSeparator());
        sidebar.add(sidebarSectionLabel("BÁO CÁO"));
        btnReports = sidebarButton("📊  Báo cáo thống kê", AppTheme::sidebarNavMuted);
        sidebar.add(btnReports);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarSeparator());
        btnLogout = sidebarButton("🚪  Đăng xuất", AppTheme::sidebarNavMuted);
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

    private JButton sidebarButton(String text, java.util.function.Consumer<JButton> style) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 42));
        style.accept(btn);
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

        JLabel welcome = new JLabel("<html>Chào mừng quay lại, <b>" + escapeHtml(displayName()) + "</b>."
                + "<br/><br/>Chọn 1 mục ở thanh bên trái để bắt đầu quản lý.</html>");
        welcome.setFont(welcome.getFont().deriveFont(15f));
        welcome.setForeground(AppTheme.TEXT);
        content.add(welcome, BorderLayout.NORTH);

        return content;
    }

    private String displayName() {
        return (account != null && account.getUsername() != null) ? account.getUsername() : "Admin";
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
