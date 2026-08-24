/*
 * Trang Nhập sách (FR-INV-01) — include Search Book (chỉ nhập thêm số lượng
 * cho sách ĐÃ TỒN TẠI; tạo mới đầu sách là việc của Admin — FR-ADM-01) và
 * Update Stock Quantity (FR-INV-02).
 */
package view.inventory;
import view.shared.*;

import dao.BookDAO;
import dao.InventoryDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Book;

public class ImportBooksPage implements InventoryPage {

    @Override
    public String navKey() {
        return "import";
    }

    @Override
    public String title() {
        return "Nhập Sách";
    }

    @Override
    public JPanel content(InventoryShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Nhập sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã sách, ISBN hoặc tên sách...");
        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setBackground(AppTheme.BACKGROUND);
        header.add(heading, BorderLayout.NORTH);
        header.add(searchBar, BorderLayout.CENTER);
        content.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "ISBN", "Tên sách", "Tồn kho hiện tại"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);

        final List<Book> currentBooks = new ArrayList<>();
        InventoryDAO inventoryDAO = new InventoryDAO();

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Book[] found = new BookDAO().searchBook(keyword);
            currentBooks.clear();
            model.setRowCount(0);
            for (Book b : found) {
                currentBooks.add(b);
                int stock = inventoryDAO.getInventoryForBook(b.getBookID()).getStockQuantity();
                model.addRow(new Object[]{b.getBookID(), b.getISBN(), b.getTitle(), stock});
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
        JLabel resultTitle = new JLabel("Kết quả");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JButton btnImport = new JButton("Nhập kho");
        AppTheme.secondary(btnImport);
        btnImport.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentBooks.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 sách để nhập kho!");
                return;
            }
            Book selected = currentBooks.get(row);
            String input = JOptionPane.showInputDialog(shell,
                    "Số lượng nhập kho cho \"" + selected.getTitle() + "\":", "1");
            if (input == null) {
                return;
            }
            int quantity;
            try {
                quantity = Integer.parseInt(input.trim());
                if (quantity <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(shell, "Số lượng không hợp lệ!");
                return;
            }
            if (inventoryDAO.importStock(selected.getBookID(), quantity)) {
                JOptionPane.showMessageDialog(shell, "Đã nhập " + quantity + " quyển \"" + selected.getTitle() + "\" vào kho!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Nhập kho thất bại! Vui lòng thử lại.");
            }
        });
        tableHeader.add(btnImport, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
    }
}
