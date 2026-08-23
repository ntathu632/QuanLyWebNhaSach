/*
 * Trang Quản lý nhân viên (FR-ADM-06: Add/Update/Delete Employee, Update/Delete
 * include Search Employee; Add Employee include Add Account — bắt buộc, vì
 * Employee cần 1 Account để đăng nhập, Employee entity không tự chứa
 * username/password). Xóa nhân viên khi còn tài khoản sẽ bị EmployeeDAO từ
 * chối, hướng dẫn khóa tài khoản ở "Quản lý tài khoản" thay vì xóa hẳn.
 */
package view;

import dao.EmployeeDAO;
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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Employee;

public class ManageEmployeePage implements AdminPage {

    private static final String[] POSITIONS = {"Admin", "Sales Staff", "Inventory Staff"};

    @Override
    public String navKey() {
        return "employees";
    }

    @Override
    public String title() {
        return "Quản Lý Nhân Viên";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý nhân viên");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên nhân viên...");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JButton btnAdd = new JButton("+ Thêm nhân viên");
        AppTheme.secondary(btnAdd);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        searchBar.add(btnAdd, BorderLayout.WEST);
        header.add(searchBar);

        content.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Họ tên", "Vị trí", "Điện thoại", "Email"};
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

        final List<Employee> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Employee[] found = new EmployeeDAO().searchEmployee(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Employee emp : found) {
                currentItems.add(emp);
                model.addRow(new Object[]{emp.getEmployeeID(), emp.getFullName(), emp.getPosition(), emp.getPhone(), emp.getEmail()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());
        btnAdd.addActionListener(e -> addEmployee(shell, doSearch));

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách nhân viên");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JPanel rowActions = new JPanel();
        rowActions.setLayout(new BoxLayout(rowActions, BoxLayout.X_AXIS));
        rowActions.setOpaque(false);

        JButton btnEdit = new JButton("Sửa");
        AppTheme.primary(btnEdit);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhân viên để sửa!");
                return;
            }
            editEmployee(shell, currentItems.get(row), doSearch);
        });
        rowActions.add(btnEdit);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnDelete = new JButton("Xóa");
        AppTheme.secondary(btnDelete);
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhân viên để xóa!");
                return;
            }
            Employee selected = currentItems.get(row);
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn xóa nhân viên \"" + selected.getFullName() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            String error = new EmployeeDAO().deleteEmployee(selected.getEmployeeID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Xóa nhân viên thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
        rowActions.add(btnDelete);

        tableHeader.add(rowActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
    }

    private void addEmployee(AdminShellFrm shell, Runnable doSearch) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtFullName = new JTextField(20);
        JComboBox<String> cboPosition = new JComboBox<>(POSITIONS);
        JTextField txtPhone = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);

        addFormRow(panel, gbc, 0, "Họ tên: *", txtFullName);
        addFormRow(panel, gbc, 1, "Vị trí: *", cboPosition);
        addFormRow(panel, gbc, 2, "Điện thoại: *", txtPhone);
        addFormRow(panel, gbc, 3, "Email: *", txtEmail);
        addFormRow(panel, gbc, 4, "Username: *", txtUsername);
        addFormRow(panel, gbc, 5, "Password: *", txtPassword);

        int result = JOptionPane.showConfirmDialog(shell, panel, "Thêm nhân viên",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập đầy đủ các trường bắt buộc (*)!");
            return;
        }

        Employee employee = new Employee();
        employee.setFullName(fullName);
        employee.setPosition((String) cboPosition.getSelectedItem());
        employee.setPhone(phone);
        employee.setEmail(email);

        String error = new EmployeeDAO().addEmployee(employee, username, password);
        if (error == null) {
            JOptionPane.showMessageDialog(shell, "Thêm nhân viên thành công!");
            doSearch.run();
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }

    private void editEmployee(AdminShellFrm shell, Employee selected, Runnable doSearch) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtFullName = new JTextField(selected.getFullName(), 20);
        JComboBox<String> cboPosition = new JComboBox<>(POSITIONS);
        cboPosition.setSelectedItem(selected.getPosition());
        JTextField txtPhone = new JTextField(selected.getPhone(), 20);
        JTextField txtEmail = new JTextField(selected.getEmail(), 20);

        addFormRow(panel, gbc, 0, "Họ tên:", txtFullName);
        addFormRow(panel, gbc, 1, "Vị trí:", cboPosition);
        addFormRow(panel, gbc, 2, "Điện thoại:", txtPhone);
        addFormRow(panel, gbc, 3, "Email:", txtEmail);

        int result = JOptionPane.showConfirmDialog(shell, panel, "Sửa nhân viên",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        selected.setFullName(fullName);
        selected.setPosition((String) cboPosition.getSelectedItem());
        selected.setPhone(phone);
        selected.setEmail(email);

        String error = new EmployeeDAO().updateEmployee(selected);
        if (error == null) {
            JOptionPane.showMessageDialog(shell, "Cập nhật nhân viên thành công!");
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
