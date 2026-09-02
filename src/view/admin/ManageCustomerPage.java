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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Account;
import model.Customer;

public class ManageCustomerPage implements AdminPage {

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
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý khách hàng");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo tên, số điện thoại hoặc email...");

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

        String[] columns = {"ID", "Họ tên", "Điện thoại", "Email", "Địa chỉ"};
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
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 60, 60);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 130, 130);
        AppTheme.alignTableColumn(table, 3, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 220, 220);

        final List<Customer> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
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

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách khách hàng");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JPanel rowActions = new JPanel();
        rowActions.setLayout(new BoxLayout(rowActions, BoxLayout.X_AXIS));
        rowActions.setOpaque(false);

        JButton btnDetail = new JButton("Xem chi tiết");
        AppTheme.secondary(btnDetail);
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
        rowActions.add(btnDetail);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnEdit = new JButton("Sửa");
        AppTheme.primary(btnEdit);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 khách hàng để sửa!");
                return;
            }
            editCustomer(shell, currentItems.get(row), doSearch);
        });
        rowActions.add(btnEdit);

        tableHeader.add(rowActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
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
}
