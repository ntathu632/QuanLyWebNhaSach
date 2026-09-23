/*
 * Trang Quản lý nhà cung cấp — bổ sung theo yêu cầu người dùng (không có
 * trong SRS gốc), cùng mô hình 1-trang với ManagePublisherPage/
 * ManageCategoryPage. Nhà cung cấp dùng để gắn với giao dịch nhập/trả hàng
 * của kho (StockTransaction), khác Publisher (đơn vị xuất bản sách).
 */
package view.admin;
import view.shared.*;

import dao.SupplierDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import model.Supplier;

public class ManageSupplierPage implements AdminPage {

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
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý nhà cung cấp");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên nhà cung cấp...");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JButton btnAdd = new JButton("+ Thêm nhà cung cấp");
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

        String[] columns = {"ID", "Tên nhà cung cấp", "Địa chỉ", "Điện thoại", "Email"};
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
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 50, 50);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 120, 120);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 180, 180);

        final List<Supplier> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
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

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách nhà cung cấp");
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
        rowActions.add(btnEdit);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnDelete = new JButton("Xóa");
        AppTheme.secondary(btnDelete);
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
}
