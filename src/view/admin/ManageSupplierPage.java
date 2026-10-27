/*
 * Trang Quản lý nhà cung cấp — bổ sung theo yêu cầu người dùng (không có
 * trong SRS gốc), cùng mô hình 1-trang với ManagePublisherPage/
 * ManageCategoryPage. Nhà cung cấp dùng để gắn với giao dịch nhập/trả hàng
 * của kho (StockTransaction), khác Publisher (đơn vị xuất bản sách).
 */
package view.admin;
import view.shared.*;

import dao.SupplierDAO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Supplier;

public class ManageSupplierPage extends javax.swing.JPanel implements AdminPage {

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Mã", "Tên nhà cung cấp", "Địa chỉ", "Điện thoại", "Email"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final List<Supplier> currentItems = new ArrayList<>();

    private final Runnable doSearch;

    public ManageSupplierPage() {
        initComponents();

        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên nhà cung cấp...");
        AppTheme.primary(btnSearch);
        AppTheme.secondary(btnAdd);
        AppTheme.primary(btnEdit);
        AppTheme.secondary(btnDelete);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 50, 50);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 120, 120);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 180, 180);

        doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Supplier[] found = new SupplierDAO().searchSupplier(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Supplier s : found) {
                currentItems.add(s);
                model.addRow(new Object[]{s.getSupplierID(), s.getSupplierName(), s.getAddress(), s.getPhone(), s.getEmail()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        btnAdd.addActionListener(e -> {
            String[] values = SimpleFormDialog.show(shell, "Thêm nhà cung cấp",
                    new String[]{"Tên nhà cung cấp:", "Địa chỉ:", "Điện thoại:", "Email:"}, null);
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên nhà cung cấp!");
                return;
            }
            Supplier supplier = new Supplier();
            supplier.setSupplierName(values[0]);
            supplier.setAddress(values[1]);
            supplier.setPhone(values[2]);
            supplier.setEmail(values[3]);
            if (new SupplierDAO().addSupplier(supplier)) {
                JOptionPane.showMessageDialog(shell, "Thêm nhà cung cấp thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Thêm nhà cung cấp thất bại! Vui lòng thử lại.");
            }
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhà cung cấp để sửa!");
                return;
            }
            Supplier selected = currentItems.get(row);
            String[] values = SimpleFormDialog.show(shell, "Sửa nhà cung cấp",
                    new String[]{"Tên nhà cung cấp:", "Địa chỉ:", "Điện thoại:", "Email:"},
                    new String[]{selected.getSupplierName(), selected.getAddress(), selected.getPhone(), selected.getEmail()});
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên nhà cung cấp!");
                return;
            }
            selected.setSupplierName(values[0]);
            selected.setAddress(values[1]);
            selected.setPhone(values[2]);
            selected.setEmail(values[3]);
            if (new SupplierDAO().updateSupplier(selected)) {
                JOptionPane.showMessageDialog(shell, "Cập nhật nhà cung cấp thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Cập nhật nhà cung cấp thất bại! Vui lòng thử lại.");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhà cung cấp để xóa!");
                return;
            }
            Supplier selected = currentItems.get(row);
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn xóa nhà cung cấp \"" + selected.getSupplierName() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            String error = new SupplierDAO().deleteSupplier(selected.getSupplierID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Xóa nhà cung cấp thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
    }

    @Override
    public String navKey() {
        return "supplier";
    }

    @Override
    public String title() {
        return "Quản Lý Nhà Cung Cấp";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        this.shell = shell;

        doSearch.run();

        return this;
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
        heading.setText("Quản lý nhà cung cấp");
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

        btnAdd.setText("+ Thêm nhà cung cấp");
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
        resultTitle.setText("Danh sách nhà cung cấp");
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
