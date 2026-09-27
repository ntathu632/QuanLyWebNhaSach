/*
 * Trang Quản lý tác giả (FR-ADM-03: Add/Update/Delete Author, Update/Delete
 * include Search Author). Cùng mô hình 1-trang với ManageCategoryPage.
 */
package view.admin;
import view.shared.*;

import dao.AuthorDAO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Author;

public class ManageAuthorPage extends javax.swing.JPanel implements AdminPage {

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Tên tác giả", "Quốc tịch", "Mô tả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final List<Author> currentItems = new ArrayList<>();

    private final Runnable doSearch;

    public ManageAuthorPage() {
        initComponents();

        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên tác giả...");
        AppTheme.primary(btnSearch);
        AppTheme.secondary(btnAdd);
        AppTheme.primary(btnEdit);
        AppTheme.secondary(btnDelete);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 50, 50);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 140, 140);
        AppTheme.alignTableColumn(table, 3, SwingConstants.LEFT, 260, 260);

        doSearch = () -> {
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
    }

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
        heading.setText("Quản lý tác giả");
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

        btnAdd.setText("+ Thêm tác giả");
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
        resultTitle.setText("Danh sách tác giả");
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
