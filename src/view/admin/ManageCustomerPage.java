/*
 * Trang Quản lý khách hàng — tách riêng khỏi "Quản lý tài khoản" (theo yêu
 * cầu người dùng), cùng cấu trúc với "Quản lý nhân viên" (hồ sơ, không phải
 * đăng nhập/khóa tài khoản). "Quản lý tài khoản" vẫn giữ nguyên vai trò quản
 * lý username/role/khóa-mở cho CẢ Customer lẫn Employee — trang này chỉ quản
 * lý hồ sơ cá nhân khách hàng (tên, SĐT, email, địa chỉ), không đụng vào
 * username/password/trạng thái tài khoản.
 */
package view.admin;
import view.shared.*;

import dao.AccountDAO;
import dao.CustomerDAO;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Account;
import model.Customer;

public class ManageCustomerPage extends javax.swing.JPanel implements AdminPage {

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Họ tên", "Điện thoại", "Email", "Địa chỉ"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final List<Customer> currentItems = new ArrayList<>();

    private final Runnable doSearch;

    public ManageCustomerPage() {
        initComponents();

        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo tên, số điện thoại hoặc email...");
        AppTheme.primary(btnSearch);
        AppTheme.secondary(btnDetail);
        AppTheme.primary(btnEdit);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 60, 60);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 130, 130);
        AppTheme.alignTableColumn(table, 3, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 220, 220);

        doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Customer[] found = new CustomerDAO().searchCustomer(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Customer c : found) {
                currentItems.add(c);
                model.addRow(new Object[]{c.getCustomerID(), c.getFullName(), c.getPhone(), c.getEmail(), c.getAddress()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 khách hàng để xem chi tiết!");
                return;
            }
            Customer selected = currentItems.get(row);
            Account account = new AccountDAO().getAccountForCustomer(selected.getCustomerID());
            shell.navigateTo(new CustomerDetailPage(selected, account, new ManageCustomerPage(), "customers"));
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 khách hàng để sửa!");
                return;
            }
            editCustomer(shell, currentItems.get(row), doSearch);
        });
    }

    @Override
    public String navKey() {
        return "customers";
    }

    @Override
    public String title() {
        return "Quản Lý Khách Hàng";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        this.shell = shell;

        doSearch.run();

        return this;
    }

    private void editCustomer(AdminShellFrm shell, Customer selected, Runnable doSearch) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtFullName = new JTextField(selected.getFullName(), 20);
        JTextField txtPhone = new JTextField(selected.getPhone(), 20);
        JTextField txtEmail = new JTextField(selected.getEmail(), 20);
        JTextField txtAddress = new JTextField(selected.getAddress() == null ? "" : selected.getAddress(), 20);

        addFormRow(panel, gbc, 0, "Họ tên:", txtFullName);
        addFormRow(panel, gbc, 1, "Điện thoại:", txtPhone);
        addFormRow(panel, gbc, 2, "Email:", txtEmail);
        addFormRow(panel, gbc, 3, "Địa chỉ:", txtAddress);

        int result = JOptionPane.showConfirmDialog(shell, panel, "Sửa khách hàng",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập đầy đủ Họ tên, Điện thoại và Email!");
            return;
        }

        selected.setFullName(fullName);
        selected.setPhone(phone);
        selected.setEmail(email);
        selected.setAddress(address.isEmpty() ? null : address);

        String error = new CustomerDAO().updateCustomer(selected);
        if (error == null) {
            JOptionPane.showMessageDialog(shell, "Cập nhật khách hàng thành công!");
            doSearch.run();
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, javax.swing.JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        header = new javax.swing.JPanel();
        heading = new javax.swing.JLabel();
        searchBar = new javax.swing.JPanel();
        txtKeyword = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        tableCard = new javax.swing.JPanel();
        tableHeader = new javax.swing.JPanel();
        resultTitle = new javax.swing.JLabel();
        rowActions = new javax.swing.JPanel();
        btnDetail = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        btnEdit = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setBackground(AppTheme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        setLayout(new java.awt.BorderLayout(0, 16));

        header.setBackground(AppTheme.BACKGROUND);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));

        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setText("Quản lý khách hàng");
        heading.setAlignmentX(0.0F);
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.add(heading);

        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setAlignmentX(0.0F);
        searchBar.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        searchBar.setLayout(new java.awt.BorderLayout(10, 0));
        searchBar.add(txtKeyword, java.awt.BorderLayout.CENTER);

        btnSearch.setText("Tìm kiếm");
        searchBar.add(btnSearch, java.awt.BorderLayout.EAST);

        header.add(searchBar);

        add(header, java.awt.BorderLayout.NORTH);

        tableCard.setBackground(new java.awt.Color(255, 255, 255));
        tableCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        tableCard.setLayout(new java.awt.BorderLayout(0, 10));

        tableHeader.setBackground(new java.awt.Color(255, 255, 255));
        tableHeader.setLayout(new java.awt.BorderLayout());

        resultTitle.setFont(resultTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        resultTitle.setText("Danh sách khách hàng");
        tableHeader.add(resultTitle, java.awt.BorderLayout.WEST);

        rowActions.setOpaque(false);
        rowActions.setLayout(new javax.swing.BoxLayout(rowActions, javax.swing.BoxLayout.LINE_AXIS));

        btnDetail.setText("Xem chi tiết");
        rowActions.add(btnDetail);
        rowActions.add(filler1);

        btnEdit.setText("Sửa");
        rowActions.add(btnEdit);

        tableHeader.add(rowActions, java.awt.BorderLayout.EAST);

        tableCard.add(tableHeader, java.awt.BorderLayout.NORTH);

        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));

        table.setGridColor(AppTheme.TABLE_GRID);
        table.setRowHeight(30);
        table.setShowGrid(true);
        scroll.setViewportView(table);

        tableCard.add(scroll, java.awt.BorderLayout.CENTER);

        add(tableCard, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnSearch;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel header;
    private javax.swing.JLabel heading;
    private javax.swing.JLabel resultTitle;
    private javax.swing.JPanel rowActions;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JPanel searchBar;
    private javax.swing.JTable table;
    private javax.swing.JPanel tableCard;
    private javax.swing.JPanel tableHeader;
    private javax.swing.JTextField txtKeyword;
    // End of variables declaration//GEN-END:variables
}
