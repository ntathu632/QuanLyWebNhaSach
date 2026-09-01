/*
 * Khung Nhân viên bán hàng DUY NHẤT (1 JFrame sống suốt phiên) — cùng mô
 * hình điều hướng 1-cửa-sổ như AdminShellFrm/CustomerShellFrm/InventoryShellFrm.
 */
package view.sales;
import view.shared.*;

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
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Employee;

public class SalesShellFrm extends javax.swing.JFrame {

    final Employee employee;

    private JPanel contentContainer;

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

    public SalesShellFrm(Employee employee) {
        this.employee = employee;
        initShell();
        navigateTo(new InStoreSalePage());
    }

    public void navigateTo(SalesPage page) {
        contentContainer.removeAll();
        contentContainer.add(page.content(this), BorderLayout.CENTER);
        contentContainer.revalidate();
        contentContainer.repaint();

        setActiveNav(page.navKey());
        setTitle("Bookshop Management - " + page.title());
    }

    private void initShell() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        getContentPane().setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, AppTheme.BACKGROUND));

        contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(AppTheme.BACKGROUND);

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

        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(AppTheme.HEADER);
        brandPanel.setBorder(new EmptyBorder(0, 18, 0, 12));
        brandPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        brandPanel.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppTheme.TOPBAR_HEIGHT));
        brandPanel.setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.TOPBAR_HEIGHT));

        JLabel brand = new JLabel("Bookshop Bán hàng");
        brand.setIcon(new FlatIcon(FlatIcon.Kind.RECEIPT, Color.WHITE, 20));
        brand.setIconTextGap(10);
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        brandPanel.add(brand, BorderLayout.CENTER);
        sidebar.add(brandPanel);

        JPanel brandDivider = new JPanel();
        brandDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        brandDivider.setPreferredSize(new Dimension(0, 2));
        brandDivider.setBackground(AppTheme.PRIMARY_HOVER);
        sidebar.add(brandDivider);

        sidebar.add(sidebarSectionLabel("BÁN HÀNG"));

        addNavItem(sidebar, "instore", FlatIcon.Kind.RECEIPT, "Lập hóa đơn", () -> navigateTo(new InStoreSalePage()));
        addNavItem(sidebar, "invoices", FlatIcon.Kind.MONEY, "Hóa đơn đã lập", () -> navigateTo(new InvoiceHistoryPage()));
        addNavItem(sidebar, "orders", FlatIcon.Kind.CHART, "Đơn hàng online", () -> navigateTo(new OrderManagementPage()));

        sidebar.add(sidebarSeparator());
        sidebar.add(sidebarSectionLabel("TRA CỨU"));

        addNavItem(sidebar, "customers", FlatIcon.Kind.USER, "Tra cứu khách hàng", () -> navigateTo(new SearchCustomerPage()));
        addNavItem(sidebar, "books", FlatIcon.Kind.BOOK, "Tra cứu sách", () -> navigateTo(new SalesSearchBookPage()));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarSeparator());

        JButton btnChangePassword = sidebarButton(FlatIcon.Kind.USER, "Đổi mật khẩu");
        btnChangePassword.addActionListener(e -> ChangePasswordDialog.show(this, employee != null ? employee.getUsername() : null));
        sidebar.add(btnChangePassword);

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

        String name = (employee != null && employee.getFullName() != null) ? employee.getFullName() : "nhân viên";
        JLabel userLabel = new JLabel("Xin chào, " + name + "  ");
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setForeground(AppTheme.TEXT);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.PLAIN, 13f));
        topbar.add(userLabel, BorderLayout.EAST);

        return topbar;
    }
}
