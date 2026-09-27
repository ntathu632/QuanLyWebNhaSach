/*
 * Khung Khách hàng DUY NHẤT (1 JFrame sống suốt phiên làm việc) — navbar trên
 * cùng cố định, chỉ phần nội dung bên dưới (pnlContent) được thay khi điều
 * hướng (navigateTo), giống AdminShellFrm bên Admin. Giữ luôn trạng thái đăng
 * nhập của khách (customer = null là khách vãng lai).
 */
package view.customer;

import dao.ShoppingCartDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JButton;
import model.Customer;
import view.shared.AppTheme;
import view.shared.ChangePasswordDialog;

public class CustomerShellFrm extends javax.swing.JFrame {

    private static final Color NAV_ACTIVE_BG = new Color(92, 110, 79);
    private static final Color NAV_BG = new Color(79, 93, 66);
    private static final Color NAV_MUTED_FG = new Color(221, 226, 211);

    /** null = khách vãng lai. */
    private Customer customer;

    private CustomerPage currentPage;

    /** Mục navbar có thể tô sáng: khóa điều hướng và nút tương ứng (cùng thứ tự). */
    private final String[] navKeys = {"home", "search", "orders"};
    private final JButton[] navButtons;

    /**
     * Creates new form CustomerShellFrm
     */
    public CustomerShellFrm(Customer customer) {
        this.customer = customer;
        initComponents();
        navButtons = new JButton[]{btnHome, btnSearchNav, btnOrders};
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm sách theo tên, ISBN...");
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

    /** Thay nội dung trang hiện tại bằng "page", tô sáng đúng mục navbar — không mở cửa sổ mới. */
    public void navigateTo(CustomerPage page) {
        currentPage = page;
        pnlContent.removeAll();
        pnlContent.add(page.content(this), BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();

        setActiveNav(page.navKey());
        setTitle("Bookshop - " + page.title());
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

    /** Đăng xuất: về khách vãng lai và mở Trang chủ (dùng cho navbar và trang Tài khoản). */
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

    /** Mở trang ứng với mục navbar "key" (home, orders, cart, account). */
    void go(String key) {
        switch (key) {
            case "home" -> navigateTo(new CustomerHomeFrm(customer));
            case "orders" -> navigateTo(new OrderHistoryFrm(customer));
            case "cart" -> navigateTo(new ShoppingCartFrm(customer));
            case "account" -> navigateTo(new AccountFrm(customer));
            default -> throw new IllegalArgumentException("Mục navbar không tồn tại: " + key);
        }
    }

    /** Mở trang Tìm kiếm sách với từ khóa "keyword". */
    void openSearch(String keyword) {
        navigateTo(new SearchBookForPurchaseFrm(customer, keyword, null, null));
    }

    /** Mục navbar của trang đang mở nền sáng hơn + chữ trắng, các mục còn lại chữ mờ. */
    private void setActiveNav(String key) {
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = navKeys[i].equals(key);
            navButtons[i].setBackground(active ? NAV_ACTIVE_BG : NAV_BG);
            navButtons[i].setForeground(active ? Color.WHITE : NAV_MUTED_FG);
        }
    }

    /** Cập nhật navbar theo trạng thái đăng nhập + số lượng trong giỏ (trang gọi lại sau khi đổi giỏ). */
    public void showLoginState() {
        boolean guest = customer == null;
        btnLogin.setVisible(guest);
        btnRegister.setVisible(guest);
        lblUser.setVisible(!guest);
        btnAccount.setVisible(!guest);
        btnChangePassword.setVisible(!guest);
        btnLogout.setVisible(!guest);

        if (guest) {
            lblCartBadge.setVisible(false);
        } else {
            String name = customer.getFullName() != null ? customer.getFullName() : "khách hàng";
            lblUser.setText("Xin chào, " + name);
            int count = new ShoppingCartDAO().countItemsIfCartExists(customer);
            lblCartBadge.setText(String.valueOf(count));
            lblCartBadge.setVisible(count > 0);
        }
        pnlNavRight.revalidate();
        pnlNavRight.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlNavbar = new javax.swing.JPanel();
        pnlNavLeft = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        btnHome = new javax.swing.JButton();
        btnSearchNav = new javax.swing.JButton();
        btnOrders = new javax.swing.JButton();
        pnlNavSearch = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        pnlNavRight = new javax.swing.JPanel();
        btnCart = new javax.swing.JButton();
        lblCartBadge = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        lblUser = new javax.swing.JLabel();
        btnAccount = new javax.swing.JButton();
        btnChangePassword = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlNavbar.setBackground(new java.awt.Color(79, 93, 66));
        pnlNavbar.setPreferredSize(new java.awt.Dimension(0, 68));
        pnlNavbar.setLayout(new java.awt.BorderLayout());

        pnlNavLeft.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        pnlNavLeft.setOpaque(false);
        pnlNavLeft.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 16));

        lblBrand.setFont(new java.awt.Font("Cormorant Garamond", 1, 26)); // NOI18N
        lblBrand.setForeground(new java.awt.Color(255, 255, 255));
        lblBrand.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.BOOK, java.awt.Color.WHITE, 22));
        lblBrand.setText("Bookshop");
        lblBrand.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 24));
        lblBrand.setIconTextGap(10);
        pnlNavLeft.add(lblBrand);

        btnHome.setBackground(new java.awt.Color(92, 110, 79));
        btnHome.setFont(new java.awt.Font("Raleway Medium", 0, 14)); // NOI18N
        btnHome.setForeground(new java.awt.Color(255, 255, 255));
        btnHome.setText("Trang chủ");
        btnHome.setBorderPainted(false);
        btnHome.setFocusPainted(false);
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });
        pnlNavLeft.add(btnHome);

        btnSearchNav.setBackground(new java.awt.Color(79, 93, 66));
        btnSearchNav.setForeground(new java.awt.Color(221, 226, 211));
        btnSearchNav.setText("Tìm sách");
        btnSearchNav.setBorderPainted(false);
        btnSearchNav.setFocusPainted(false);
        btnSearchNav.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchNavActionPerformed(evt);
            }
        });
        pnlNavLeft.add(btnSearchNav);

        btnOrders.setBackground(new java.awt.Color(79, 93, 66));
        btnOrders.setForeground(new java.awt.Color(221, 226, 211));
        btnOrders.setText("Đơn hàng của tôi");
        btnOrders.setBorderPainted(false);
        btnOrders.setFocusPainted(false);
        btnOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOrdersActionPerformed(evt);
            }
        });
        pnlNavLeft.add(btnOrders);

        pnlNavbar.add(pnlNavLeft, java.awt.BorderLayout.WEST);

        pnlNavSearch.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 16, 24));
        pnlNavSearch.setOpaque(false);
        pnlNavSearch.setLayout(new java.awt.BorderLayout(6, 0));

        txtSearch.setToolTipText("Tìm sách theo tên, ISBN...");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        pnlNavSearch.add(txtSearch, java.awt.BorderLayout.CENTER);

        btnSearch.setBackground(new java.awt.Color(192, 96, 74));
        btnSearch.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.SEARCH, java.awt.Color.WHITE, 15));
        btnSearch.setToolTipText("Tìm kiếm");
        btnSearch.setBorderPainted(false);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        pnlNavSearch.add(btnSearch, java.awt.BorderLayout.EAST);

        pnlNavbar.add(pnlNavSearch, java.awt.BorderLayout.CENTER);

        pnlNavRight.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 18));
        pnlNavRight.setOpaque(false);
        pnlNavRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 16));

        btnCart.setBackground(new java.awt.Color(79, 93, 66));
        btnCart.setForeground(new java.awt.Color(255, 255, 255));
        btnCart.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.RECEIPT, java.awt.Color.WHITE, 16));
        btnCart.setText("Giỏ hàng");
        btnCart.setBorderPainted(false);
        btnCart.setFocusPainted(false);
        btnCart.setIconTextGap(8);
        btnCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCartActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnCart);

        lblCartBadge.setBackground(new java.awt.Color(192, 96, 74));
        lblCartBadge.setFont(new java.awt.Font("Raleway Medium", 0, 11)); // NOI18N
        lblCartBadge.setForeground(new java.awt.Color(255, 255, 255));
        lblCartBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCartBadge.setText("0");
        lblCartBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 6, 1, 6));
        lblCartBadge.setOpaque(true);
        pnlNavRight.add(lblCartBadge);

        btnLogin.setFont(new java.awt.Font("Raleway Medium", 0, 14)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(79, 93, 66));
        btnLogin.setText("Đăng nhập");
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnLogin);

        btnRegister.setBackground(new java.awt.Color(192, 96, 74));
        btnRegister.setFont(new java.awt.Font("Raleway Medium", 0, 14)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("Đăng ký");
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnRegister);

        lblUser.setForeground(new java.awt.Color(255, 255, 255));
        lblUser.setText("Xin chào, khách hàng");
        lblUser.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 8));
        pnlNavRight.add(lblUser);

        btnAccount.setBackground(new java.awt.Color(79, 93, 66));
        btnAccount.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.PEN, java.awt.Color.WHITE, 16));
        btnAccount.setToolTipText("Tài khoản của tôi");
        btnAccount.setBorderPainted(false);
        btnAccount.setFocusPainted(false);
        btnAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAccountActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnAccount);

        btnChangePassword.setBackground(new java.awt.Color(79, 93, 66));
        btnChangePassword.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.USER, java.awt.Color.WHITE, 16));
        btnChangePassword.setToolTipText("Đổi mật khẩu");
        btnChangePassword.setBorderPainted(false);
        btnChangePassword.setFocusPainted(false);
        btnChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangePasswordActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnChangePassword);

        btnLogout.setBackground(new java.awt.Color(79, 93, 66));
        btnLogout.setIcon(new view.shared.FlatIcon(view.shared.FlatIcon.Kind.LOGOUT, java.awt.Color.WHITE, 16));
        btnLogout.setToolTipText("Đăng xuất");
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        pnlNavRight.add(btnLogout);

        pnlNavbar.add(pnlNavRight, java.awt.BorderLayout.EAST);

        getContentPane().add(pnlNavbar, java.awt.BorderLayout.NORTH);

        pnlContent.setBackground(new java.awt.Color(243, 238, 221));
        pnlContent.setLayout(new java.awt.BorderLayout());
        getContentPane().add(pnlContent, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccountActionPerformed
        if (ensureLoggedIn()) {
            go("account");
        }
    }//GEN-LAST:event_btnAccountActionPerformed

    private void btnCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCartActionPerformed
        if (ensureLoggedIn()) {
            go("cart");
        }
    }//GEN-LAST:event_btnCartActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        ChangePasswordDialog.show(this, customer != null ? customer.getUsername() : null);
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        go("home");
    }//GEN-LAST:event_btnHomeActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        login(false);
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        logout();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrdersActionPerformed
        if (ensureLoggedIn()) {
            go("orders");
        }
    }//GEN-LAST:event_btnOrdersActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        login(true);
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchFromNavbar();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnSearchNavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchNavActionPerformed
        openSearch("");
    }//GEN-LAST:event_btnSearchNavActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        searchFromNavbar();
    }//GEN-LAST:event_txtSearchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAccount;
    private javax.swing.JButton btnCart;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnOrders;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSearchNav;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblCartBadge;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlNavLeft;
    private javax.swing.JPanel pnlNavRight;
    private javax.swing.JPanel pnlNavSearch;
    private javax.swing.JPanel pnlNavbar;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
