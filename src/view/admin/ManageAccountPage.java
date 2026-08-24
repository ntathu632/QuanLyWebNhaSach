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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Account;

public class ManageAccountPage implements AdminPage {

    private static final String[] STAFF_ROLES = {"Admin", "Sales Staff", "Inventory Staff"};
    private static final String[] STATUSES = {"Active", "Locked"};

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
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý tài khoản");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(heading);

        JLabel hint = new JLabel("Chọn 1 tài khoản rồi bấm \"Sửa\" để đổi vai trò (phân quyền) hoặc khóa/mở khóa tài khoản.");
        hint.setFont(hint.getFont().deriveFont(12f));
        hint.setForeground(new Color(0x8A, 0x83, 0x74));
        hint.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(hint);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo username hoặc tên chủ tài khoản...");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        header.add(searchBar);

        content.add(header, BorderLayout.NORTH);

        String[] columns = {"Username", "Thuộc về", "Vai trò", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);

        final List<Account> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
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

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách tài khoản");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JButton btnEdit = new JButton("Sửa");
        AppTheme.primary(btnEdit);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 tài khoản để sửa!");
                return;
            }
            editAccount(shell, currentItems.get(row), doSearch);
        });
        tableHeader.add(btnEdit, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
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
}
