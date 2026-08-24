/*
 * Trang Lập hóa đơn bán tại quầy (FR-SALE-01) — include Search Book, Check
 * Stock Availability (BR-04), Update Stock Quantity (BR-03); extend Search
 * Customer (tùy chọn, mặc định "Khách vãng lai" nếu không tra cứu).
 */
package view.sales;
import view.shared.*;

import dao.BookDAO;
import dao.CustomerDAO;
import dao.InvoiceDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Book;
import model.Customer;
import model.OrderDetail;

public class InStoreSalePage implements SalesPage {

    @Override
    public String navKey() {
        return "instore";
    }

    @Override
    public String title() {
        return "Lập Hóa Đơn Bán Hàng";
    }

    @Override
    public JPanel content(SalesShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Lập hóa đơn bán hàng");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel customerLabel = new JLabel("Khách hàng: Khách vãng lai");
        customerLabel.setFont(customerLabel.getFont().deriveFont(13f));
        customerLabel.setForeground(AppTheme.TEXT);

        final Customer[] selectedCustomer = new Customer[1];

        JButton btnPickCustomer = new JButton("Chọn khách quen...");
        AppTheme.secondary(btnPickCustomer);
        btnPickCustomer.addActionListener(e -> {
            Customer picked = pickCustomer(shell);
            if (picked != null) {
                selectedCustomer[0] = picked;
                customerLabel.setText("Khách hàng: " + picked.getFullName() + " (" + picked.getPhone() + ")");
            }
        });

        JPanel customerRow = new JPanel(new BorderLayout(10, 0));
        customerRow.setBackground(AppTheme.BACKGROUND);
        customerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        customerRow.add(customerLabel, BorderLayout.CENTER);
        customerRow.add(btnPickCustomer, BorderLayout.EAST);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);
        header.add(heading);
        header.add(Box.createVerticalStrut(8));
        header.add(customerRow);
        content.add(header, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 20, 0));
        columns.setBackground(AppTheme.BACKGROUND);

        // ---- Cột trái: tìm sách ----
        JPanel searchPanel = new JPanel(new BorderLayout(0, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm sách theo tên, ISBN...");
        JButton btnSearch = new JButton("Tìm");
        AppTheme.primary(btnSearch);
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(Color.WHITE);
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        JPanel searchTop = new JPanel(new BorderLayout());
        searchTop.setBackground(Color.WHITE);
        JLabel searchTitle = new JLabel("Tìm sách");
        searchTitle.setFont(searchTitle.getFont().deriveFont(Font.BOLD, 14f));
        searchTitle.setForeground(AppTheme.TEXT);
        searchTop.add(searchTitle, BorderLayout.NORTH);
        searchTop.add(searchBar, BorderLayout.SOUTH);
        searchTop.setBorder(new EmptyBorder(0, 0, 10, 0));
        searchPanel.add(searchTop, BorderLayout.NORTH);

        String[] bookColumns = {"Sách", "Giá", "Tồn kho"};
        DefaultTableModel bookModel = new DefaultTableModel(bookColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable bookTable = new JTable(bookModel);
        bookTable.setRowHeight(28);
        bookTable.setGridColor(AppTheme.TABLE_GRID);
        final List<Book> foundBooks = new ArrayList<>();
        dao.InventoryDAO inventoryDAO = new dao.InventoryDAO();

        Runnable doSearchBook = () -> {
            Book[] found = new BookDAO().searchBook(txtKeyword.getText().trim());
            foundBooks.clear();
            bookModel.setRowCount(0);
            for (Book b : found) {
                foundBooks.add(b);
                int stock = inventoryDAO.getInventoryForBook(b.getBookID()).getStockQuantity();
                bookModel.addRow(new Object[]{b.getTitle(), String.format("%,.0f₫", b.getPrice()), stock});
            }
        };
        btnSearch.addActionListener(e -> doSearchBook.run());
        txtKeyword.addActionListener(e -> doSearchBook.run());
        searchPanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // ---- Cột phải: hóa đơn hiện tại ----
        JPanel invoicePanel = new JPanel(new BorderLayout(0, 10));
        invoicePanel.setBackground(Color.WHITE);
        invoicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.SECONDARY),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel invoiceTitle = new JLabel("Hóa đơn hiện tại");
        invoiceTitle.setFont(invoiceTitle.getFont().deriveFont(Font.BOLD, 14f));
        invoiceTitle.setForeground(AppTheme.TEXT);
        invoicePanel.add(invoiceTitle, BorderLayout.NORTH);

        String[] cartColumns = {"Sách", "Số lượng", "Thành tiền"};
        DefaultTableModel cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable cartTable = new JTable(cartModel);
        cartTable.setRowHeight(28);
        cartTable.setGridColor(AppTheme.TABLE_GRID);
        final List<OrderDetail> cartItems = new ArrayList<>();

        JLabel totalLabel = new JLabel("Tổng cộng: 0₫");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        totalLabel.setForeground(AppTheme.ACCENT_TEXT);

        Runnable refreshCart = () -> {
            cartModel.setRowCount(0);
            float total = 0f;
            for (OrderDetail d : cartItems) {
                cartModel.addRow(new Object[]{d.getBook().getTitle(), d.getQuantity(), String.format("%,.0f₫", d.getSubtotal())});
                total += d.getSubtotal();
            }
            totalLabel.setText("Tổng cộng: " + String.format("%,.0f₫", total));
        };

        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = bookTable.getSelectedRow();
                    if (row < 0 || row >= foundBooks.size()) {
                        return;
                    }
                    Book selected = foundBooks.get(row);
                    String input = JOptionPane.showInputDialog(shell,
                            "Số lượng cho \"" + selected.getTitle() + "\":", "1");
                    if (input == null) {
                        return;
                    }
                    int qty;
                    try {
                        qty = Integer.parseInt(input.trim());
                        if (qty <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(shell, "Số lượng không hợp lệ!");
                        return;
                    }
                    cartItems.add(new OrderDetail(qty, selected.getPrice(), selected.getPrice() * qty, selected));
                    refreshCart.run();
                }
            }
        });

        JScrollPane cartScroll = new JScrollPane(cartTable);
        invoicePanel.add(cartScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        JButton btnRemoveLine = new JButton("Xóa dòng đã chọn");
        AppTheme.secondary(btnRemoveLine);
        btnRemoveLine.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btnRemoveLine.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0 && row < cartItems.size()) {
                cartItems.remove(row);
                refreshCart.run();
            }
        });
        footer.add(btnRemoveLine);
        footer.add(Box.createVerticalStrut(10));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        totalRow.add(totalLabel, BorderLayout.WEST);

        JComboBox<String> cboPaymentMethod = new JComboBox<>(
                new String[]{"Cash on Delivery", "Credit Card", "Bank Transfer", "E-Wallet"});
        totalRow.add(cboPaymentMethod, BorderLayout.EAST);
        footer.add(totalRow);
        footer.add(Box.createVerticalStrut(10));

        JButton btnSubmit = new JButton("Lập hóa đơn");
        btnSubmit.setAlignmentX(JButton.LEFT_ALIGNMENT);
        AppTheme.primary(btnSubmit);
        btnSubmit.addActionListener(e -> {
            if (cartItems.isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Hóa đơn chưa có sách nào!");
                return;
            }
            Customer customer = selectedCustomer[0];
            if (customer == null) {
                customer = new CustomerDAO().getOrCreateWalkInCustomer();
            }
            String paymentMethod = (String) cboPaymentMethod.getSelectedItem();
            String error = new InvoiceDAO().createInStoreSale(customer.getCustomerID(), cartItems, paymentMethod, shell.employee.getEmployeeID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Lập hóa đơn thành công! Tổng tiền: " + totalLabel.getText().replace("Tổng cộng: ", ""));
                shell.navigateTo(new InStoreSalePage());
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
        footer.add(btnSubmit);

        invoicePanel.add(footer, BorderLayout.SOUTH);

        columns.add(searchPanel);
        columns.add(invoicePanel);
        content.add(columns, BorderLayout.CENTER);

        return content;
    }

    private Customer pickCustomer(SalesShellFrm shell) {
        String keyword = JOptionPane.showInputDialog(shell, "Nhập tên/SĐT/email khách hàng:");
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        Customer[] found = new CustomerDAO().searchCustomer(keyword.trim());
        if (found.length == 0) {
            JOptionPane.showMessageDialog(shell, "Không tìm thấy khách hàng nào!");
            return null;
        }
        String[] options = new String[found.length];
        for (int i = 0; i < found.length; i++) {
            options[i] = found[i].getFullName() + " - " + found[i].getPhone();
        }
        String choice = (String) JOptionPane.showInputDialog(shell, "Chọn khách hàng:", "Kết quả",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == null) {
            return null;
        }
        int idx = java.util.Arrays.asList(options).indexOf(choice);
        return found[idx];
    }
}
