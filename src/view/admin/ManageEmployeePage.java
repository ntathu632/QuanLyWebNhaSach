/*
 * Trang Quản lý nhân viên (FR-ADM-06: Add/Update/Delete Employee, Update/Delete
 * include Search Employee; Add Employee include Add Account — bắt buộc, vì
 * Employee cần 1 Account để đăng nhập, Employee entity không tự chứa
 * username/password). Xóa nhân viên khi còn tài khoản sẽ bị EmployeeDAO từ
 * chối, hướng dẫn khóa tài khoản ở "Quản lý tài khoản" thay vì xóa hẳn.
 */
package view.admin;
import view.shared.*;

import dao.EmployeeDAO;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Employee;

public class ManageEmployeePage extends javax.swing.JPanel implements AdminPage {

    private static final String[] POSITIONS = {"Admin", "Sales Staff", "Inventory Staff"};

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Họ tên", "Vị trí", "Điện thoại", "Email"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final List<Employee> currentItems = new ArrayList<>();

    private final Runnable doSearch;

    public ManageEmployeePage() {
        initComponents();

        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên nhân viên...");
        AppTheme.primary(btnSearch);
        AppTheme.secondary(btnAdd);
        AppTheme.primary(btnEdit);
        AppTheme.secondary(btnDelete);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 60, 60);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 130, 130);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 130, 130);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 200, 200);

        doSearch = () -> {
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

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhân viên để sửa!");
                return;
            }
            editEmployee(shell, currentItems.get(row), doSearch);
        });

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
    }

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
        this.shell = shell;

        doSearch.run();

        return this;
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
        btnAdd = new javax.swing.JButton();
        tableCard = new javax.swing.JPanel();
        tableHeader = new javax.swing.JPanel();
        resultTitle = new javax.swing.JLabel();
        rowActions = new javax.swing.JPanel();
        btnEdit = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        btnDelete = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setBackground(AppTheme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        setLayout(new java.awt.BorderLayout(0, 16));

        header.setBackground(AppTheme.BACKGROUND);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));

        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setText("Quản lý nhân viên");
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

        btnAdd.setText("+ Thêm nhân viên");
        searchBar.add(btnAdd, java.awt.BorderLayout.WEST);

        header.add(searchBar);

        add(header, java.awt.BorderLayout.NORTH);

        tableCard.setBackground(new java.awt.Color(255, 255, 255));
        tableCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        tableCard.setLayout(new java.awt.BorderLayout(0, 10));

        tableHeader.setBackground(new java.awt.Color(255, 255, 255));
        tableHeader.setLayout(new java.awt.BorderLayout());

        resultTitle.setFont(resultTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        resultTitle.setText("Danh sách nhân viên");
        tableHeader.add(resultTitle, java.awt.BorderLayout.WEST);

        rowActions.setOpaque(false);
        rowActions.setLayout(new javax.swing.BoxLayout(rowActions, javax.swing.BoxLayout.LINE_AXIS));

        btnEdit.setText("Sửa");
        rowActions.add(btnEdit);
        rowActions.add(filler1);

        btnDelete.setText("Xóa");
        rowActions.add(btnDelete);

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
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
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
