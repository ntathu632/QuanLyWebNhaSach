/*
 * Khung Khách hàng DUY NHẤT (1 JFrame sống suốt phiên) — navbar trên cùng cố
 * định, chỉ phần nội dung được thay khi điều hướng (navigateTo), cùng mô
 * hình với AdminShellFrm bên phần Admin. Dùng navbar ngang (kiểu website bán
 * hàng thật: logo, menu, giỏ hàng, tài khoản) thay vì sidebar dọc — phù hợp
 * hơn với trang khách mua hàng so với kiểu dashboard quản trị.
 */
package view;

import dao.DAO;
import dao.ShoppingCartDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Customer;

public class CustomerShellFrm extends javax.swing.JFrame {

    final Customer customer;

    private JPanel contentContainer;
    private JPanel subHeaderLeft;
    private JLabel cartBadge;

    private JButton navHome;
    private JButton navSearch;

    public CustomerShellFrm(Customer c) {
        this.customer = c;
        initShell();
        navigateTo(new CustomerHomePage());
    }

    /** Thay nội dung trang hiện tại bằng "page", cập nhật navbar/sub-header — không mở cửa sổ mới. */
    public void navigateTo(CustomerPage page) {
        contentContainer.removeAll();
        contentContainer.add(page.content(this), BorderLayout.CENTER);
        contentContainer.revalidate();
        contentContainer.repaint();

        updateSubHeader(page);
        setActiveNav(page.navKey());
        refreshCartBadge();
        setTitle("Bookshop - " + page.title());
    }

    /** Cập nhật lại số trong badge giỏ hàng — gọi lại sau mỗi lần thêm/xóa sách trong giỏ. */
    public void refreshCartBadge() {
        int count = new ShoppingCartDAO().countItemsIfCartExists(customer);
        cartBadge.setText(String.valueOf(count));
        cartBadge.setVisible(count > 0);
    }

    private void initShell() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        getContentPane().setLayout(new BorderLayout());

        contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(AppTheme.BACKGROUND);

        JPanel subHeader = buildSubHeader();

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AppTheme.BACKGROUND);
        body.add(subHeader, BorderLayout.NORTH);
        body.add(contentContainer, BorderLayout.CENTER);

        getContentPane().add(buildNavbar(), BorderLayout.NORTH);
        getContentPane().add(body, BorderLayout.CENTER);

        setSize(1100, 700);
        setLocationRelativeTo(null);
        AppTheme.maximize(this);
    }

    private JPanel buildNavbar() {
        JPanel navbarWrap = new JPanel(new BorderLayout());
        navbarWrap.setBackground(AppTheme.HEADER);

        JPanel navbar = new JPanel(new BorderLayout(24, 0));
        navbar.setOpaque(false);
        navbar.setPreferredSize(new Dimension(0, 68));
        navbar.setBorder(new EmptyBorder(0, 24, 0, 24));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setOpaque(false);

        JComponent brandBadge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                new FlatIcon(FlatIcon.Kind.BOOK, AppTheme.HEADER, 18).paintIcon(this, g,
                        (getWidth() - 18) / 2, (getHeight() - 18) / 2);
            }
        };
        brandBadge.setPreferredSize(new Dimension(34, 34));
        brandBadge.setMaximumSize(new Dimension(34, 34));
        brandBadge.setOpaque(false);
        left.add(brandBadge);
        left.add(Box.createHorizontalStrut(10));

        JLabel brand = new JLabel("Bookshop");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 19f));
        left.add(brand);
        left.add(Box.createHorizontalStrut(30));

        navHome = navLink("Trang chủ");
        navHome.addActionListener(e -> navigateTo(new CustomerHomePage()));
        left.add(navHome);

        navSearch = navLink("Tìm sách");
        navSearch.addActionListener(e -> navigateTo(new SearchBookForPurchasePage()));
        left.add(navSearch);

        navbar.add(left, BorderLayout.WEST);
        navbar.add(buildNavSearchBox(), BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);

        JButton cartButton = new JButton("Giỏ hàng");
        cartButton.setIcon(new FlatIcon(FlatIcon.Kind.RECEIPT, Color.WHITE, 16));
        cartButton.setIconTextGap(8);
        AppTheme.iconButton(cartButton);
        cartButton.putClientProperty("FlatLaf.style",
                "background: #4F5D42; foreground: #FFFFFF; hoverBackground: #5C6E4F; pressedBackground: #3F4B35; borderWidth: 0; arc: 8");
        cartButton.addActionListener(e -> navigateTo(new ShoppingCartPage()));
        right.add(cartButton);

        cartBadge = new JLabel("0");
        cartBadge.setOpaque(true);
        cartBadge.setBackground(AppTheme.SECONDARY);
        cartBadge.setForeground(Color.WHITE);
        cartBadge.setFont(cartBadge.getFont().deriveFont(Font.BOLD, 11f));
        cartBadge.setHorizontalAlignment(SwingConstants.CENTER);
        cartBadge.setBorder(new EmptyBorder(1, 6, 1, 6));
        cartBadge.putClientProperty("FlatLaf.style", "arc: 999");
        right.add(Box.createHorizontalStrut(6));
        right.add(cartBadge);

        right.add(Box.createHorizontalStrut(20));

        String name = (customer != null && customer.getFullName() != null) ? customer.getFullName() : "khách hàng";
        JLabel userLabel = new JLabel("Xin chào, " + name);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.PLAIN, 13f));
        right.add(userLabel);
        right.add(Box.createHorizontalStrut(16));

        JButton btnLogout = new JButton();
        btnLogout.setIcon(new FlatIcon(FlatIcon.Kind.LOGOUT, Color.WHITE, 16));
        btnLogout.setToolTipText("Đăng xuất");
        btnLogout.putClientProperty("FlatLaf.style",
                "background: #4F5D42; hoverBackground: #5C6E4F; pressedBackground: #3F4B35; borderWidth: 0; arc: 8; margin: 6,10,6,10");
        btnLogout.addActionListener(e -> {
            new DAO();
            (new LoginFrm()).setVisible(true);
            this.dispose();
        });
        right.add(btnLogout);

        navbar.add(right, BorderLayout.EAST);

        navbarWrap.add(navbar, BorderLayout.CENTER);
        navbarWrap.add(navbarShadow(), BorderLayout.SOUTH);
        return navbarWrap;
    }

    /** Dải bóng đổ mờ dần ngay dưới navbar, tạo chiều sâu thay vì cắt phẳng với nền. */
    private JComponent navbarShadow() {
        JComponent shadow = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int h = getHeight();
                for (int i = 0; i < h; i++) {
                    int alpha = (int) (60 * (1f - (float) i / h));
                    g2.setColor(new Color(0, 0, 0, Math.max(alpha, 0)));
                    g2.drawLine(0, i, getWidth(), i);
                }
                g2.dispose();
            }
        };
        shadow.setPreferredSize(new Dimension(0, 5));
        shadow.setOpaque(false);
        return shadow;
    }

    private JPanel buildNavSearchBox() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(15, 0, 15, 0));

        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", "Tìm sách theo tên, ISBN...");
        field.putClientProperty("FlatLaf.style", "arc: 999; borderWidth: 0; background: #FFFFFF; margin: 2,14,2,14");

        JButton searchBtn = new JButton();
        searchBtn.setIcon(new FlatIcon(FlatIcon.Kind.SEARCH, Color.WHITE, 15));
        searchBtn.putClientProperty("FlatLaf.style",
                "background: #C0604A; hoverBackground: #AD533F; pressedBackground: #9A4735; borderWidth: 0; arc: 999; margin: 6,10,6,10");

        Runnable go = () -> {
            SearchBookForPurchasePage page = new SearchBookForPurchasePage();
            page.presetKeyword(field.getText().trim());
            navigateTo(page);
        };
        searchBtn.addActionListener(e -> go.run());
        field.addActionListener(e -> go.run());

        JPanel pill = new JPanel(new BorderLayout());
        pill.setOpaque(false);
        pill.add(field, BorderLayout.CENTER);
        pill.add(searchBtn, BorderLayout.EAST);

        wrap.add(pill, BorderLayout.CENTER);
        return wrap;
    }

    private JButton navLink(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #4F5D42; foreground: #DDE2D3;"
                + " hoverBackground: #5C6E4F; pressedBackground: #3F4B35;"
                + " focusedBackground: #4F5D42; borderWidth: 0; arc: 8; margin: 8,14,8,14; font: +0");
        return btn;
    }

    private void setActiveNav(String key) {
        boolean home = "home".equals(key);
        boolean search = "search".equals(key);
        navHome.putClientProperty("FlatLaf.style", navLinkStyle(home));
        navHome.setForeground(home ? Color.WHITE : AppTheme.SIDEBAR_TEXT_MUTED);
        navSearch.putClientProperty("FlatLaf.style", navLinkStyle(search));
        navSearch.setForeground(search ? Color.WHITE : AppTheme.SIDEBAR_TEXT_MUTED);
    }

    private String navLinkStyle(boolean active) {
        String bg = active ? "5C6E4F" : "4F5D42";
        return "background: #" + bg + "; hoverBackground: #5C6E4F; pressedBackground: #3F4B35;"
                + " focusedBackground: #" + bg + "; borderWidth: 0; arc: 8; margin: 8,14,8,14;"
                + (active ? " font: bold" : "");
    }

    private JPanel buildSubHeader() {
        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setBackground(AppTheme.BACKGROUND);
        subHeader.setPreferredSize(new Dimension(0, 44));
        subHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.INPUT_BORDER));

        subHeaderLeft = new JPanel();
        subHeaderLeft.setLayout(new BoxLayout(subHeaderLeft, BoxLayout.X_AXIS));
        subHeaderLeft.setBackground(AppTheme.BACKGROUND);
        subHeaderLeft.setBorder(new EmptyBorder(0, 20, 0, 0));
        subHeader.add(subHeaderLeft, BorderLayout.WEST);

        return subHeader;
    }

    private void updateSubHeader(CustomerPage page) {
        subHeaderLeft.removeAll();

        CustomerPage back = page.backTarget();
        if (back != null) {
            JButton btnBack = new JButton();
            btnBack.setIcon(new FlatIcon(FlatIcon.Kind.BACK, AppTheme.TEXT, 16));
            btnBack.setToolTipText("Quay lại");
            AppTheme.iconButton(btnBack);
            btnBack.addActionListener(e -> navigateTo(back));
            subHeaderLeft.add(btnBack);
        }

        subHeaderLeft.revalidate();
        subHeaderLeft.repaint();
    }
}
