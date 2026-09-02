/*
 * Trang xóa sách (FR-ADM-01, Sub-UC Delete Book — include Search Book).
 * BookDAO.deleteBook() tự kiểm tra dữ liệu liên quan (danh mục, tác giả, tồn
 * kho, giỏ hàng, đơn hàng) và từ chối xóa kèm lý do rõ ràng nếu có.
 */
package view.admin;
import view.shared.*;

import dao.BookDAO;
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
import model.Book;

public class DeleteBookPage implements AdminPage {

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
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Xóa sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(heading);

        JLabel hint = new JLabel("Chọn 1 sách trong bảng rồi bấm \"Xóa sách đã chọn\". Sách đang có dữ liệu liên quan (danh mục, tác giả, tồn kho, giỏ hàng, đơn hàng) sẽ không xóa được.");
        hint.setFont(hint.getFont().deriveFont(12f));
        hint.setForeground(new Color(0x8A, 0x83, 0x74));
        hint.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(hint);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã sách, ISBN hoặc tên sách...");

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

        String[] columns = {"ID", "ISBN", "Tên sách", "Giá", "Mô tả"};
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
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 140, 140);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 110, 110);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 220, 220);

        final List<Book> currentBooks = new ArrayList<>();

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Book[] found = new BookDAO().searchBook(keyword);
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

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.SECONDARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Kết quả");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JButton btnDelete = new JButton("Xóa sách đã chọn");
        AppTheme.secondary(btnDelete);
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
        tableHeader.add(btnDelete, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
    }
}
