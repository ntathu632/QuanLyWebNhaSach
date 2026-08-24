/*
 * Trang Quản lý tác giả (FR-ADM-03: Add/Update/Delete Author, Update/Delete
 * include Search Author). Cùng mô hình 1-trang với ManageCategoryPage.
 */
package view.admin;
import view.shared.*;

import dao.AuthorDAO;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Author;

public class ManageAuthorPage implements AdminPage {

    @Override
    public String navKey() {
        return "author";
    }

    @Override
    public String title() {
        return "Quản Lý Tác Giả";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý tác giả");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên tác giả...");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JButton btnAdd = new JButton("+ Thêm tác giả");
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

        String[] columns = {"ID", "Tên tác giả", "Quốc tịch", "Mô tả"};
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

        final List<Author> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Author[] found = new AuthorDAO().searchAuthor(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Author a : found) {
                currentItems.add(a);
                model.addRow(new Object[]{a.getAuthorID(), a.getAuthorName(), a.getNationality(), a.getDescription()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        btnAdd.addActionListener(e -> {
            String[] values = SimpleFormDialog.show(shell, "Thêm tác giả",
                    new String[]{"Tên tác giả:", "Quốc tịch:", "Mô tả:"}, null);
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên tác giả!");
                return;
            }
            Author author = new Author();
            author.setAuthorName(values[0]);
            author.setNationality(values[1]);
            author.setDescription(values[2]);
            if (new AuthorDAO().addAuthor(author)) {
                JOptionPane.showMessageDialog(shell, "Thêm tác giả thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Thêm tác giả thất bại! Vui lòng thử lại.");
            }
        });

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách tác giả");
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
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 tác giả để sửa!");
                return;
            }
            Author selected = currentItems.get(row);
            String[] values = SimpleFormDialog.show(shell, "Sửa tác giả",
                    new String[]{"Tên tác giả:", "Quốc tịch:", "Mô tả:"},
                    new String[]{selected.getAuthorName(), selected.getNationality(), selected.getDescription()});
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên tác giả!");
                return;
            }
            selected.setAuthorName(values[0]);
            selected.setNationality(values[1]);
            selected.setDescription(values[2]);
            if (new AuthorDAO().updateAuthor(selected)) {
                JOptionPane.showMessageDialog(shell, "Cập nhật tác giả thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Cập nhật tác giả thất bại! Vui lòng thử lại.");
            }
        });
        rowActions.add(btnEdit);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnDelete = new JButton("Xóa");
        AppTheme.secondary(btnDelete);
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 tác giả để xóa!");
                return;
            }
            Author selected = currentItems.get(row);
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn xóa tác giả \"" + selected.getAuthorName() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            String error = new AuthorDAO().deleteAuthor(selected.getAuthorID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Xóa tác giả thành công!");
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
