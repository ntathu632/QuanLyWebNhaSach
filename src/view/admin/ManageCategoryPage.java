/*
 * Trang Quản lý danh mục (FR-ADM-02: Add/Update/Delete Category, Update/Delete
 * include Search Category). Danh mục chỉ có 2 trường (tên + mô tả) nên gộp
 * chung Thêm/Sửa/Xóa vào 1 trang danh sách duy nhất (khác Quản lý sách vốn
 * đã có sẵn 3 màn hình riêng từ code gốc) — tránh dựng dư thừa nhiều trang
 * cho 1 thực thể nhỏ.
 */
package view.admin;
import view.shared.*;

import dao.CategoryDAO;
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
import model.Category;

public class ManageCategoryPage implements AdminPage {

    @Override
    public String navKey() {
        return "category";
    }

    @Override
    public String title() {
        return "Quản Lý Danh Mục";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý danh mục");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên danh mục...");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JButton btnAdd = new JButton("+ Thêm danh mục");
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

        String[] columns = {"ID", "Tên danh mục", "Mô tả"};
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
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 300, 300);

        final List<Category> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Category[] found = new CategoryDAO().searchCategory(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Category c : found) {
                currentItems.add(c);
                model.addRow(new Object[]{c.getCategoryID(), c.getCategoryName(), c.getDescription()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        btnAdd.addActionListener(e -> {
            String[] values = SimpleFormDialog.show(shell, "Thêm danh mục",
                    new String[]{"Tên danh mục:", "Mô tả:"}, null);
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên danh mục!");
                return;
            }
            Category category = new Category();
            category.setCategoryName(values[0]);
            category.setDescription(values[1]);
            String error = new CategoryDAO().addCategory(category);
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Thêm danh mục thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách danh mục");
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
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 danh mục để sửa!");
                return;
            }
            Category selected = currentItems.get(row);
            String[] values = SimpleFormDialog.show(shell, "Sửa danh mục",
                    new String[]{"Tên danh mục:", "Mô tả:"},
                    new String[]{selected.getCategoryName(), selected.getDescription()});
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên danh mục!");
                return;
            }
            selected.setCategoryName(values[0]);
            selected.setDescription(values[1]);
            String error = new CategoryDAO().updateCategory(selected);
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Cập nhật danh mục thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
        rowActions.add(btnEdit);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnDelete = new JButton("Xóa");
        AppTheme.secondary(btnDelete);
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 danh mục để xóa!");
                return;
            }
            Category selected = currentItems.get(row);
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn xóa danh mục \"" + selected.getCategoryName() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            String error = new CategoryDAO().deleteCategory(selected.getCategoryID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Xóa danh mục thành công!");
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
