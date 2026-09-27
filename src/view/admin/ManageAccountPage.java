/*
 * Trang Quản lý tài khoản (FR-ADM-05: Add/Update/Delete-Lock Account,
 * Update/Delete include Search Account; FR-ADM-07 Assign Permissions gộp
 * vào đây vì SRS mô tả nó chỉ là đổi trường "role" khi Update Account).
 * Không có "+ Thêm tài khoản" riêng: Account luôn phải gắn với 1 Customer
 * hoặc 1 Employee có sẵn (CK_Account_Owner — AS-04), nên tài khoản mới được
 * tạo qua đúng luồng của nó (Đăng ký cho khách hàng, "Thêm nhân viên" cho
 * nhân viên) thay vì tạo tài khoản "trôi nổi" không thuộc về ai ở đây.
 * "Delete-Lock" = khóa tài khoản qua status="Locked" thay vì xóa hẳn, để
 * không mất lịch sử đơn hàng/dữ liệu gắn với chủ tài khoản.
 */
package view.admin;
import view.shared.*;

import dao.AccountDAO;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Account;
import model.Customer;

public class ManageAccountPage extends javax.swing.JPanel implements AdminPage {

    private static final String[] STAFF_ROLES = {"Admin", "Sales Staff", "Inventory Staff"};
    private static final String[] STATUSES = {"Active", "Locked"};

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Username", "Thuộc về", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final List<Account> currentItems = new ArrayList<>();

    private final Runnable doSearch;

    public ManageAccountPage() {
        initComponents();

        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo username hoặc tên chủ tài khoản...");
        AppTheme.primary(btnSearch);
        AppTheme.secondary(btnDetail);
        AppTheme.primary(btnEdit);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 140, 140);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 110, 110);

        doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Account[] found = new AccountDAO().searchAccounts(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Account a : found) {
                currentItems.add(a);
                model.addRow(new Object[]{a.getUsername(), a.getOwnerName(), a.getRole(), a.getStatus()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 tài khoản để xem chi tiết!");
                return;
            }
            Account selected = currentItems.get(row);
            if (!"Customer".equals(selected.getRole())) {
                JOptionPane.showMessageDialog(shell, "Chỉ xem chi tiết được với tài khoản khách hàng!");
                return;
            }
            Customer customer = new AccountDAO().getCustomerByUsername(selected.getUsername());
            if (customer == null) {
                JOptionPane.showMessageDialog(shell, "Không tìm thấy thông tin khách hàng cho tài khoản này!");
                return;
            }
            shell.navigateTo(new CustomerDetailPage(customer, selected));
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 tài khoản để sửa!");
                return;
            }
            editAccount(shell, currentItems.get(row), doSearch);
        });
    }

    @Override
    public String navKey() {
        return "accounts";
    }

    @Override
    public String title() {
        return "Quản Lý Tài Khoản";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        this.shell = shell;

        doSearch.run();

        return this;
    }

    private void editAccount(AdminShellFrm shell, Account selected, Runnable doSearch) {
        boolean isCustomerAccount = "Customer".equals(selected.getRole());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(selected.getUsername()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Vai trò:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cboRole;
        if (isCustomerAccount) {
            cboRole = new JComboBox<>(new String[]{"Customer"});
            cboRole.setEnabled(false);
        } else {
            cboRole = new JComboBox<>(STAFF_ROLES);
            cboRole.setSelectedItem(selected.getRole());
        }
        panel.add(cboRole, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cboStatus = new JComboBox<>(STATUSES);
        cboStatus.setSelectedItem(selected.getStatus());
        panel.add(cboStatus, gbc);

        int result = JOptionPane.showConfirmDialog(shell, panel, "Sửa tài khoản \"" + selected.getUsername() + "\"",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newRole = (String) cboRole.getSelectedItem();
        String newStatus = (String) cboStatus.getSelectedItem();

        boolean lockingSelf = "Locked".equals(newStatus)
                && shell.account != null && shell.account.getUsername().equals(selected.getUsername());
        if (lockingSelf) {
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn đang khóa chính tài khoản đang đăng nhập. Tiếp tục?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (new AccountDAO().updateAccountRoleStatus(selected.getUsername(), newRole, newStatus)) {
            JOptionPane.showMessageDialog(shell, "Cập nhật tài khoản thành công!");
            doSearch.run();
        } else {
            JOptionPane.showMessageDialog(shell, "Cập nhật tài khoản thất bại! Vui lòng thử lại.");
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

        header = new javax.swing.JPanel();
        heading = new javax.swing.JLabel();
        hint = new javax.swing.JLabel();
        searchBar = new javax.swing.JPanel();
        txtKeyword = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        tableCard = new javax.swing.JPanel();
        tableHeader = new javax.swing.JPanel();
        resultTitle = new javax.swing.JLabel();
        headerActions = new javax.swing.JPanel();
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
        heading.setText("Quản lý tài khoản");
        heading.setAlignmentX(0.0F);
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        header.add(heading);

        hint.setFont(hint.getFont().deriveFont(12f));
        hint.setForeground(new java.awt.Color(138, 131, 116));
        hint.setText("Chọn 1 tài khoản rồi bấm \"Sửa\" để đổi vai trò (phân quyền) hoặc khóa/mở khóa tài khoản.");
        hint.setAlignmentX(0.0F);
        hint.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.add(hint);

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
        resultTitle.setText("Danh sách tài khoản");
        tableHeader.add(resultTitle, java.awt.BorderLayout.WEST);

        headerActions.setOpaque(false);
        headerActions.setLayout(new javax.swing.BoxLayout(headerActions, javax.swing.BoxLayout.LINE_AXIS));

        btnDetail.setText("Xem chi tiết");
        headerActions.add(btnDetail);
        headerActions.add(filler1);

        btnEdit.setText("Sửa");
        headerActions.add(btnEdit);

        tableHeader.add(headerActions, java.awt.BorderLayout.EAST);

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
    private javax.swing.JLabel hint;
    private javax.swing.JLabel resultTitle;
    private javax.swing.JPanel headerActions;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JPanel searchBar;
    private javax.swing.JTable table;
    private javax.swing.JPanel tableCard;
    private javax.swing.JPanel tableHeader;
    private javax.swing.JTextField txtKeyword;
    // End of variables declaration//GEN-END:variables
}
