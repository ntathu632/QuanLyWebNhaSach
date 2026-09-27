/*
 * Khung Khách hàng DUY NHẤT (1 JFrame sống suốt phiên làm việc) — thanh bên
 * trái (logo, điều hướng, danh mục sách) + thanh trên (tìm kiếm, giỏ hàng,
 * tài khoản) cố định, chỉ phần nội dung (pnlContent) được thay khi điều hướng
 * (navigateTo), giống AdminShellFrm bên Admin. Giữ luôn trạng thái đăng nhập
 * của khách (customer = null là khách vãng lai). Màu theo VintageTheme.
 */
package view.customer;

import com.formdev.flatlaf.FlatClientProperties;
import dao.CategoryDAO;
import dao.ShoppingCartDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import model.Category;
import model.Customer;
import view.shared.AppTheme;
import view.shared.ChangePasswordDialog;
import view.shared.FlatIcon;

public class CustomerShellFrm extends javax.swing.JFrame {

    /** null = khách vãng lai. */
    private Customer customer;

    private CustomerPage currentPage;

    /** Mục thanh bên có thể tô sáng: khóa điều hướng, nút và icon tương ứng (cùng thứ tự). */
    private final String[] navKeys = {"home", "orders", "cart"};
    private final FlatIcon.Kind[] navIcons = {FlatIcon.Kind.HOME, FlatIcon.Kind.RECEIPT, FlatIcon.Kind.CART};
    private final JButton[] navButtons;

    /**
     * Creates new form CustomerShellFrm
     */
    public CustomerShellFrm(Customer customer) {
        this.customer = customer;
        initComponents();
        navButtons = new JButton[]{btnNavHome, btnNavOrders, btnNavCart};
        styleComponents();
        loadSidebarCategories();
        showLoginState();
        setSize(1100, 700);
        setLocationRelativeTo(null);
        AppTheme.maximize(this);
    }

    /** Tạo khung Khách hàng đang mở sẵn trang "page" (chưa hiện lên màn hình). */
    public static CustomerShellFrm open(Customer customer, CustomerPage page) {
        CustomerShellFrm shell = new CustomerShellFrm(customer);
        shell.navigateTo(page);
        return shell;
    }

    /** Tạo khung Khách hàng đang mở sẵn Trang chủ (chưa hiện lên màn hình). */
    public static CustomerShellFrm home(Customer customer) {
        return open(customer, new CustomerHomeFrm(customer));
    }

    /** Khách đang đăng nhập (null = khách vãng lai). */
    public Customer getCustomer() {
        return customer;
    }

    /** Thay nội dung trang hiện tại bằng "page", tô sáng đúng mục thanh bên — không mở cửa sổ mới. */
    public void navigateTo(CustomerPage page) {
        currentPage = page;
        pnlContent.removeAll();
        pnlContent.add(page.content(this), BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();

        setActiveNav(page.navKey());
        setTitle("Vintage Books - " + page.title());
    }

    /**
     * Mở hộp thoại đăng nhập / đăng ký. Đăng nhập bằng tài khoản Khách hàng thì
     * ở lại trang hiện tại và báo cho trang biết; tài khoản nhân viên thì
     * CustomerLogin tự mở khung theo vai trò và đóng cửa sổ này.
     */
    public Customer login(boolean startWithRegister) {
        Customer c = CustomerLogin.show(this, startWithRegister);
        if (c != null) {
            customer = c;
            showLoginState();
            if (currentPage != null) {
                currentPage.customerLoggedIn(c);
            }
        }
        return c;
    }

    /** Đăng xuất: về khách vãng lai và mở Trang chủ (dùng cho thanh trên và trang Tài khoản). */
    public void logout() {
        customer = null;
        showLoginState();
        navigateTo(new CustomerHomeFrm(null));
    }

    /** Chưa đăng nhập thì mở hộp thoại đăng nhập; trả về true nếu đã có khách và cửa sổ còn mở. */
    public boolean ensureLoggedIn() {
        if (customer == null) {
            login(false);
        }
        return customer != null && isDisplayable();
    }

    private void searchFromNavbar() {
        openSearch(txtSearch.getText().trim());
    }

    /** Mở trang ứng với mục điều hướng "key" (home, orders, cart, account). */
    void go(String key) {
        switch (key) {
            case "home" -> navigateTo(new CustomerHomeFrm(customer));
            case "orders" -> navigateTo(new OrderHistoryFrm(customer));
            case "cart" -> navigateTo(new ShoppingCartFrm(customer));
            case "account" -> navigateTo(new AccountFrm(customer));
            default -> throw new IllegalArgumentException("Mục điều hướng không tồn tại: " + key);
        }
    }

    /** Mở trang Tìm kiếm sách với từ khóa "keyword". */
    void openSearch(String keyword) {
        navigateTo(new SearchBookForPurchaseFrm(customer, keyword, null, null));
    }

    /** Mở trang Tìm kiếm sách lọc sẵn theo 1 danh mục. */
    void openCategory(Category category) {
        navigateTo(new SearchBookForPurchaseFrm(customer, "", category.getCategoryID(), category.getCategoryName()));
    }

    /** Màu/icon không đặt được trong Design (FlatLaf style, icon đổi màu theo trạng thái). */
    private void styleComponents() {
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, VintageTheme.SIDEBAR_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, VintageTheme.TEXT);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm sách theo tên, ISBN...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                new FlatIcon(FlatIcon.Kind.SEARCH, VintageTheme.TEXT, 16));
        txtSearch.putClientProperty("FlatLaf.style", "arc: 10; background: #FFFFFF"
                + "; borderColor: #" + VintageTheme.hex(VintageTheme.CARD_BORDER) + "; margin: 4,10,4,10");
        VintageTheme.brownButton(btnSearch);

        VintageTheme.flatButton(btnCart);
        VintageTheme.outlineButton(btnLogin);
        VintageTheme.brownButton(btnRegister);
        VintageTheme.flatButton(btnUserMenu);
    }

    /** Danh mục sách ở thanh bên lấy từ CSDL — bấm vào thì mở trang Tìm sách lọc theo danh mục đó. */
    private void loadSidebarCategories() {
        for (Category category : new CategoryDAO().searchCategory("")) {
            JButton btn = new JButton(category.getCategoryName());
            btn.setIcon(new FlatIcon(VintageTheme.categoryIcon(category.getCategoryName()), VintageTheme.TEXT, 18));
            btn.setIconTextGap(14);
            VintageTheme.sidebarNav(btn, false);
            // Cao tối đa = cao thật của nút: nếu nhỏ hơn thì BoxLayout không co được
            // (min = preferred) và đẩy mục cuối ra ngoài khung.
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
            btn.addActionListener(e -> openCategory(category));
            pnlSidebarCategories.add(btn);
        }
    }

    /** Mục thanh bên của trang đang mở nền nâu chữ trắng, các mục còn lại nền trong suốt. */
    private void setActiveNav(String key) {
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = navKeys[i].equals(key);
            VintageTheme.sidebarNav(navButtons[i], active);
            navButtons[i].setIcon(new FlatIcon(navIcons[i], active ? Color.WHITE : VintageTheme.TEXT, 20));
        }
    }

    /**
     * Cập nhật thanh trên theo trạng thái đăng nhập + số lượng trong giỏ (trang
     * gọi lại sau khi đổi giỏ): khách vãng lai thấy Đăng nhập/Đăng ký, đã đăng
     * nhập thì thấy nút tên khách (bấm mở menu tài khoản); số sách trong giỏ
     * hiện ngay trên nút Giỏ hàng.
     */
    public void showLoginState() {
        boolean guest = customer == null;
        btnLogin.setVisible(guest);
        btnRegister.setVisible(guest);
        btnUserMenu.setVisible(!guest);

        int count = 0;
        if (!guest) {
            btnUserMenu.setText(customer.getFullName() != null ? customer.getFullName() : "Tài khoản");
            count = new ShoppingCartDAO().countItemsIfCartExists(customer);
        }
        btnCart.setText(count > 0 ? "Giỏ hàng (" + count + ")" : "Giỏ hàng");
        pnlNavRight.revalidate();
        pnlNavRight.repaint();
    }

    private void openCart() {
        if (ensureLoggedIn()) {
            go("cart");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popUserMenu = new javax.swing.JPopupMenu();
        mniAccount = new javax.swing.JMenuItem();
        mniOrders = new javax.swing.JMenuItem();
        mniChangePassword = new javax.swing.JMenuItem();
        sepUserMenu = new javax.swing.JPopupMenu.Separator();
        mniLogout = new javax.swing.JMenuItem();
        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        btnNavHome = new javax.swing.JButton();
        btnNavOrders = new javax.swing.JButton();
        btnNavCart = new javax.swing.JButton();
        pnlSidebarLine = new javax.swing.JPanel();
        sepSidebar = new javax.swing.JSeparator();
        lblCategoryHeader = new javax.swing.JLabel();
        pnlSidebarCategories = new javax.swing.JPanel();
        pnlMain = new javax.swing.JPanel();
        pnlTopbar = new javax.swing.JPanel();
        pnlSearchWrap = new javax.swing.JPanel();
        pnlSearch = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        pnlNavRight = new javax.swing.JPanel();
        btnCart = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        btnUserMenu = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        mniAccount.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.USER, new java.awt.Color(122, 74, 51), 16));
        mniAccount.setText("Tài khoản của tôi");
        mniAccount.setIconTextGap(12);
        mniAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniAccountActionPerformed(evt);
            }
        });
        popUserMenu.add(mniAccount);

        mniOrders.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.RECEIPT, new java.awt.Color(122, 74, 51), 16));
        mniOrders.setText("Đơn hàng của tôi");
        mniOrders.setIconTextGap(12);
        mniOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniOrdersActionPerformed(evt);
            }
        });
        popUserMenu.add(mniOrders);

        mniChangePassword.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.LOCK, new java.awt.Color(122, 74, 51), 16));
        mniChangePassword.setText("Đổi mật khẩu");
        mniChangePassword.setIconTextGap(12);
        mniChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniChangePasswordActionPerformed(evt);
            }
        });
        popUserMenu.add(mniChangePassword);
        popUserMenu.add(sepUserMenu);

        mniLogout.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.LOGOUT, new java.awt.Color(122, 74, 51), 16));
        mniLogout.setText("Đăng xuất");
        mniLogout.setIconTextGap(12);
        mniLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLogoutActionPerformed(evt);
            }
        });
        popUserMenu.add(mniLogout);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlSidebar.setBackground(new java.awt.Color(241, 232, 220));
        pnlSidebar.setBorder(javax.swing.BorderFactory.createEmptyBorder(26, 18, 18, 18));
        pnlSidebar.setPreferredSize(new java.awt.Dimension(264, 0));
        pnlSidebar.setLayout(new javax.swing.BoxLayout(pnlSidebar, javax.swing.BoxLayout.Y_AXIS));

        lblBrand.setFont(new java.awt.Font("Cormorant Garamond", 1, 32)); // NOI18N
        lblBrand.setForeground(new java.awt.Color(122, 74, 51));
        lblBrand.setText("Vintage Books");
        lblBrand.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 28, 0));
        lblBrand.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblBrand.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblBrandMouseClicked(evt);
            }
        });
        pnlSidebar.add(lblBrand);

        btnNavHome.setText("Trang chủ");
        btnNavHome.setIconTextGap(14);
        btnNavHome.setMaximumSize(new java.awt.Dimension(32767, 46));
        btnNavHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavHomeActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnNavHome);

        btnNavOrders.setText("Đơn hàng của tôi");
        btnNavOrders.setIconTextGap(14);
        btnNavOrders.setMaximumSize(new java.awt.Dimension(32767, 46));
        btnNavOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavOrdersActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnNavOrders);

        btnNavCart.setText("Giỏ hàng");
        btnNavCart.setIconTextGap(14);
        btnNavCart.setMaximumSize(new java.awt.Dimension(32767, 46));
        btnNavCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNavCartActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnNavCart);

        pnlSidebarLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 12, 14, 12));
        pnlSidebarLine.setOpaque(false);
        pnlSidebarLine.setAlignmentX(0.0F);
        pnlSidebarLine.setMaximumSize(new java.awt.Dimension(32767, 34));
        pnlSidebarLine.setPreferredSize(new java.awt.Dimension(0, 34));
        pnlSidebarLine.setLayout(new java.awt.BorderLayout());

        sepSidebar.setForeground(new java.awt.Color(227, 216, 202));
        pnlSidebarLine.add(sepSidebar, java.awt.BorderLayout.CENTER);

        pnlSidebar.add(pnlSidebarLine);

        lblCategoryHeader.setForeground(new java.awt.Color(140, 123, 109));
        lblCategoryHeader.setText("Danh mục sách");
        lblCategoryHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 10, 0));
        pnlSidebar.add(lblCategoryHeader);

        pnlSidebarCategories.setOpaque(false);
        pnlSidebarCategories.setAlignmentX(0.0F);
        pnlSidebarCategories.setLayout(new javax.swing.BoxLayout(pnlSidebarCategories, javax.swing.BoxLayout.Y_AXIS));
        pnlSidebar.add(pnlSidebarCategories);

        getContentPane().add(pnlSidebar, java.awt.BorderLayout.WEST);

        pnlMain.setBackground(new java.awt.Color(247, 242, 234));
        pnlMain.setLayout(new java.awt.BorderLayout());

        pnlTopbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 28, 14, 24));
        pnlTopbar.setOpaque(false);
        pnlTopbar.setLayout(new java.awt.BorderLayout());

        pnlSearchWrap.setOpaque(false);
        pnlSearchWrap.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        pnlSearch.setOpaque(false);
        pnlSearch.setPreferredSize(new java.awt.Dimension(560, 44));
        pnlSearch.setLayout(new java.awt.BorderLayout());

        txtSearch.setToolTipText("Tìm sách theo tên, ISBN...");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        pnlSearch.add(txtSearch, java.awt.BorderLayout.CENTER);

        btnSearch.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.SEARCH, java.awt.Color.WHITE, 18));
        btnSearch.setToolTipText("Tìm kiếm");
        btnSearch.setPreferredSize(new java.awt.Dimension(56, 44));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        pnlSearch.add(btnSearch, java.awt.BorderLayout.EAST);

        pnlSearchWrap.add(pnlSearch);

        pnlTopbar.add(pnlSearchWrap, java.awt.BorderLayout.CENTER);

        pnlNavRight.setOpaque(false);
        pnlNavRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));

        btnCart.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.CART, new java.awt.Color(59, 42, 32), 20));
        btnCart.setText("Giỏ hàng");
        btnCart.setIconTextGap(10);
        btnCart.setPreferredSize(new java.awt.Dimension(156, 44));
        btnCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCartActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnCart);

        btnLogin.setText("Đăng nhập");
        btnLogin.setPreferredSize(new java.awt.Dimension(124, 44));
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnLogin);

        btnRegister.setText("Đăng ký");
        btnRegister.setPreferredSize(new java.awt.Dimension(124, 44));
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnRegister);

        btnUserMenu.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.CIRCLE_USER, new java.awt.Color(122, 74, 51), 22));
        btnUserMenu.setText("Tài khoản");
        btnUserMenu.setToolTipText("Tài khoản của tôi");
        btnUserMenu.setIconTextGap(10);
        btnUserMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserMenuActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnUserMenu);

        pnlTopbar.add(pnlNavRight, java.awt.BorderLayout.EAST);

        pnlMain.add(pnlTopbar, java.awt.BorderLayout.NORTH);

        pnlContent.setBackground(new java.awt.Color(247, 242, 234));
        pnlContent.setLayout(new java.awt.BorderLayout());
        pnlMain.add(pnlContent, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlMain, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblBrandMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBrandMouseClicked
        go("home");
    }//GEN-LAST:event_lblBrandMouseClicked

    private void btnNavHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavHomeActionPerformed
        go("home");
    }//GEN-LAST:event_btnNavHomeActionPerformed

    private void btnNavOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavOrdersActionPerformed
        if (ensureLoggedIn()) {
            go("orders");
        }
    }//GEN-LAST:event_btnNavOrdersActionPerformed

    private void btnNavCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavCartActionPerformed
        openCart();
    }//GEN-LAST:event_btnNavCartActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        searchFromNavbar();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchFromNavbar();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCartActionPerformed
        openCart();
    }//GEN-LAST:event_btnCartActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        login(false);
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        login(true);
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnUserMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserMenuActionPerformed
        popUserMenu.show(btnUserMenu, 0, btnUserMenu.getHeight());
    }//GEN-LAST:event_btnUserMenuActionPerformed

    private void mniAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniAccountActionPerformed
        if (ensureLoggedIn()) {
            go("account");
        }
    }//GEN-LAST:event_mniAccountActionPerformed

    private void mniOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniOrdersActionPerformed
        if (ensureLoggedIn()) {
            go("orders");
        }
    }//GEN-LAST:event_mniOrdersActionPerformed

    private void mniChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniChangePasswordActionPerformed
        ChangePasswordDialog.show(this, customer != null ? customer.getUsername() : null);
    }//GEN-LAST:event_mniChangePasswordActionPerformed

    private void mniLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLogoutActionPerformed
        logout();
    }//GEN-LAST:event_mniLogoutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCart;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnNavCart;
    private javax.swing.JButton btnNavHome;
    private javax.swing.JButton btnNavOrders;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUserMenu;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblCategoryHeader;
    private javax.swing.JMenuItem mniAccount;
    private javax.swing.JMenuItem mniChangePassword;
    private javax.swing.JMenuItem mniLogout;
    private javax.swing.JMenuItem mniOrders;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlNavRight;
    private javax.swing.JPanel pnlSearch;
    private javax.swing.JPanel pnlSearchWrap;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JPanel pnlSidebarCategories;
    private javax.swing.JPanel pnlSidebarLine;
    private javax.swing.JPanel pnlTopbar;
    private javax.swing.JPopupMenu popUserMenu;
    private javax.swing.JPopupMenu.Separator sepUserMenu;
    private javax.swing.JSeparator sepSidebar;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
