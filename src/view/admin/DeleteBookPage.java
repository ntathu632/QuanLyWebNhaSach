/*
 * Trang xóa sách (FR-ADM-01, Sub-UC Delete Book — include Search Book).
 * BookDAO.deleteBook() tự kiểm tra dữ liệu liên quan (danh mục, tác giả, tồn
 * kho, giỏ hàng, đơn hàng) và từ chối xóa kèm lý do rõ ràng nếu có.
 */
package view.admin;
import view.shared.*;

import dao.BookDAO;
import dao.CategoryDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Book;
import model.Category;

public class DeleteBookPage extends javax.swing.JPanel implements AdminPage {

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Mã", "ISBN", "Tên sách", "Giá", "Mô tả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final List<Book> currentBooks = new ArrayList<>();

    private final Runnable doSearch;

    /** Các danh mục trong ô lọc, cùng thứ tự với các mục của cboCategory (trừ mục "Tất cả danh mục" ở đầu). */
    private Category[] categories = new Category[0];

    public DeleteBookPage() {
        initComponents();

        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã sách, ISBN hoặc tên sách...");
        AppTheme.primary(btnSearch);
        loadCategories();
        AppTheme.secondary(btnDelete);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 50, 50);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 140, 140);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 110, 110);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 220, 220);

        doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Book[] found = filterByCategory(new BookDAO().searchBook(keyword));
            currentBooks.clear();
            model.setRowCount(0);
            for (Book b : found) {
                currentBooks.add(b);
                model.addRow(new Object[]{b.getBookID(), b.getISBN(), b.getTitle(),
                    String.format("%,.0f₫", b.getPrice()), b.getDescription()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());
        cboCategory.addActionListener(e -> doSearch.run());

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentBooks.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 sách trong bảng để xóa!");
                return;
            }
            Book selected = currentBooks.get(row);
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn xóa sách \"" + selected.getTitle() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            String error = new BookDAO().deleteBook(selected.getBookID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Xóa sách thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
    }

    /** Đổ danh sách danh mục vào ô lọc (mục đầu là "Tất cả danh mục"). */
    private void loadCategories() {
        categories = new CategoryDAO().searchCategory("");
        for (Category category : categories) {
            cboCategory.addItem(category.getCategoryName());
        }
    }

    /** Chỉ giữ các sách thuộc danh mục đang chọn ở ô lọc (chọn "Tất cả danh mục" thì giữ nguyên). */
    private Book[] filterByCategory(Book[] books) {
        int index = cboCategory.getSelectedIndex() - 1;
        if (index < 0 || index >= categories.length) {
            return books;
        }
        Set<Integer> ids = new HashSet<>();
        for (Book book : new BookDAO().searchByCategory(categories[index].getCategoryID())) {
            ids.add(book.getBookID());
        }
        List<Book> kept = new ArrayList<>();
        for (Book book : books) {
            if (ids.contains(book.getBookID())) {
                kept.add(book);
            }
        }
        return kept.toArray(new Book[0]);
    }

    @Override
    public String navKey() {
        return "books";
    }

    @Override
    public String title() {
        return "Xóa Sách";
    }

    @Override
    public AdminPage backTarget() {
        return new ManageBooksPage();
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
        hint = new javax.swing.JLabel();
        searchBar = new javax.swing.JPanel();
        txtKeyword = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        cboCategory = new javax.swing.JComboBox<>();
        tableCard = new javax.swing.JPanel();
        tableHeader = new javax.swing.JPanel();
        resultTitle = new javax.swing.JLabel();
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
        heading.setText("Xóa sách");
        heading.setAlignmentX(0.0F);
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        header.add(heading);
        hint.setFont(hint.getFont().deriveFont(12f));
        hint.setForeground(new java.awt.Color(140, 123, 109));
        hint.setText("Chọn 1 sách trong bảng rồi bấm \"Xóa sách đã chọn\". Sách đang có dữ liệu liên quan (danh mục, tác giả, tồn kho, giỏ hàng, đơn hàng) sẽ không xóa được.");
        hint.setAlignmentX(0.0F);
        hint.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.add(hint);
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setAlignmentX(0.0F);
        searchBar.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        searchBar.setLayout(new java.awt.BorderLayout(10, 0));
        cboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả danh mục" }));
        searchBar.add(cboCategory, java.awt.BorderLayout.WEST);
        searchBar.add(txtKeyword, java.awt.BorderLayout.CENTER);
        btnSearch.setText("Tìm kiếm");
        searchBar.add(btnSearch, java.awt.BorderLayout.EAST);
        header.add(searchBar);

        add(header, java.awt.BorderLayout.NORTH);

        tableCard.setBackground(new java.awt.Color(255, 255, 255));
        tableCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.SECONDARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        tableCard.setLayout(new java.awt.BorderLayout(0, 10));
        tableHeader.setBackground(new java.awt.Color(255, 255, 255));
        tableHeader.setLayout(new java.awt.BorderLayout());
        resultTitle.setFont(resultTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        resultTitle.setText("Kết quả");
        tableHeader.add(resultTitle, java.awt.BorderLayout.WEST);
        btnDelete.setText("Xóa sách đã chọn");
        tableHeader.add(btnDelete, java.awt.BorderLayout.EAST);
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
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cboCategory;
    private javax.swing.JPanel header;
    private javax.swing.JLabel heading;
    private javax.swing.JLabel hint;
    private javax.swing.JLabel resultTitle;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JPanel searchBar;
    private javax.swing.JTable table;
    private javax.swing.JPanel tableCard;
    private javax.swing.JPanel tableHeader;
    private javax.swing.JTextField txtKeyword;
    // End of variables declaration//GEN-END:variables
}
