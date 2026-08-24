/*
 * Màn hình Đăng ký tài khoản — hiện thực FR-CUS-01 (Register Account) theo
 * SRS: họ tên, sđt, email, địa chỉ, username, password -> tạo đồng thời
 * Customer + Account (role="Customer").
 */
package view.customer;
import view.shared.*;

import dao.AccountDAO;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.Account;
import model.Customer;

/**
 *
 * @author anhthu632
 */
public class RegisterFrm extends javax.swing.JFrame implements ActionListener {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegisterFrm.class.getName());

    public RegisterFrm() {
        initComponents();
        setTitle("Bookshop Management - Đăng Ký");
        btnRegister.addActionListener(this);
        getRootPane().setDefaultButton(btnRegister);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        lblAppName = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblFullName = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        lblPhone = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblConfirmPassword = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JPasswordField();
        lblBackToLogin = new javax.swing.JLabel();
        btnRegister = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);

        lblAppName.setFont(lblAppName.getFont().deriveFont(Font.BOLD, 22f));
        lblAppName.setForeground(AppTheme.HEADER);
        lblAppName.setText("Bookshop Management");
        lblAppName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lblSubtitle.setFont(lblSubtitle.getFont().deriveFont(14f));
        lblSubtitle.setForeground(AppTheme.TEXT);
        lblSubtitle.setText("Tạo tài khoản khách hàng mới");
        lblSubtitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lblFullName.setForeground(AppTheme.TEXT);
        lblFullName.setText("Họ tên: *");
        lblPhone.setForeground(AppTheme.TEXT);
        lblPhone.setText("Số điện thoại: *");
        lblEmail.setForeground(AppTheme.TEXT);
        lblEmail.setText("Email: *");
        lblAddress.setForeground(AppTheme.TEXT);
        lblAddress.setText("Địa chỉ:");
        lblUsername.setForeground(AppTheme.TEXT);
        lblUsername.setText("Username: *");
        lblPassword.setForeground(AppTheme.TEXT);
        lblPassword.setText("Password: *");
        lblConfirmPassword.setForeground(AppTheme.TEXT);
        lblConfirmPassword.setText("Xác nhận mật khẩu: *");

        lblBackToLogin.setText("Đã có tài khoản? Đăng nhập");
        AppTheme.link(lblBackToLogin, () -> {
            (new LoginFrm()).setVisible(true);
            this.dispose();
        });

        btnRegister.setText("Đăng ký");
        AppTheme.primary(btnRegister);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        javax.swing.GroupLayout.ParallelGroup labelCol = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFullName).addComponent(lblPhone).addComponent(lblEmail)
            .addComponent(lblAddress).addComponent(lblUsername).addComponent(lblPassword)
            .addComponent(lblConfirmPassword);

        javax.swing.GroupLayout.ParallelGroup fieldCol = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblAppName, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitle, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(labelCol)
                        .addGap(12, 12, 12)
                        .addGroup(fieldCol))
                    .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBackToLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblAppName)
                .addGap(6, 6, 6)
                .addComponent(lblSubtitle)
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFullName).addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhone).addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmail).addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAddress).addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername).addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword).addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConfirmPassword).addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblBackToLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        pack();
        setLocationRelativeTo(null);
    }

    private javax.swing.JButton btnRegister;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblAppName;
    private javax.swing.JLabel lblBackToLogin;
    private javax.swing.JLabel lblConfirmPassword;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFullName;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtUsername;

    public static void main(String args[]) {
        AppTheme.install();
        new dao.DAO();
        java.awt.EventQueue.invokeLater(() -> new RegisterFrm().setVisible(true));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường bắt buộc (*)!");
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Xác nhận mật khẩu không khớp!");
            return;
        }

        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address.isEmpty() ? null : address);

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);

        AccountDAO accountDAO = new AccountDAO();
        String error = accountDAO.register(customer, account);

        if (error == null) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
            (new LoginFrm()).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, error);
        }
    }
}
