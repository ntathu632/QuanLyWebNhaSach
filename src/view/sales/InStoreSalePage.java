/*
 * Trang Lập hóa đơn bán tại quầy (FR-SALE-01) — include Search Book, Check
 * Stock Availability (BR-04), Update Stock Quantity (BR-03); extend Search
 * Customer (tùy chọn, mặc định "Khách vãng lai" nếu không tra cứu).
 *
 * Kết quả tìm sách hiển thị dạng thẻ (ảnh bìa + tên + giá + tồn kho) thay vì
 * bảng chữ trần, kèm bộ lọc nhanh theo Danh mục — theo góp ý thực tế: nhân
 * viên bán hàng cần tìm sách nhanh bằng mắt (giống lối tắt "duyệt theo danh
 * mục" bên Customer) thay vì chỉ gõ từ khóa.
 */
package view.sales;
import view.shared.*;

import dao.BookDAO;
import dao.CategoryDAO;
import dao.CustomerDAO;
import dao.InvoiceDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Book;
import model.Category;
import model.Customer;
import model.OrderDetail;
import view.customer.BookCoverPanel;

public class InStoreSalePage implements SalesPage {

    private static final Color MUTED = new Color(0x8A, 0x83, 0x74);
    private static final String ALL_CATEGORIES = "Tất cả danh mục";

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

        JButton btnAddCustomer = new JButton("+ Khách hàng mới...");
        AppTheme.secondary(btnAddCustomer);
        btnAddCustomer.addActionListener(e -> {
            Customer created = addNewCustomer(shell);
            if (created != null) {
                selectedCustomer[0] = created;
                customerLabel.setText("Khách hàng: " + created.getFullName() + " (" + created.getPhone() + ")");
            }
        });

        JPanel customerButtons = new JPanel();
        customerButtons.setLayout(new BoxLayout(customerButtons, BoxLayout.X_AXIS));
        customerButtons.setOpaque(false);
        customerButtons.add(btnPickCustomer);
        customerButtons.add(Box.createHorizontalStrut(8));
        customerButtons.add(btnAddCustomer);

        JPanel customerRow = new JPanel(new BorderLayout(10, 0));
        customerRow.setBackground(AppTheme.BACKGROUND);
        customerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        customerRow.add(customerLabel, BorderLayout.CENTER);
        customerRow.add(customerButtons, BorderLayout.EAST);

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

        JPanel searchTop = new JPanel();
        searchTop.setLayout(new BoxLayout(searchTop, BoxLayout.Y_AXIS));
        searchTop.setBackground(Color.WHITE);
        searchTop.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel searchTitle = new JLabel("Tìm sách");
        searchTitle.setFont(searchTitle.getFont().deriveFont(Font.BOLD, 14f));
        searchTitle.setForeground(AppTheme.TEXT);
        searchTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        searchTop.add(searchTitle);
        searchTop.add(Box.createVerticalStrut(8));

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm sách theo tên, ISBN...");
        JButton btnSearch = new JButton("Tìm");
        AppTheme.primary(btnSearch);
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(Color.WHITE);
        searchBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        searchTop.add(searchBar);
        searchTop.add(Box.createVerticalStrut(8));

        // Lối tắt lọc nhanh theo Danh mục — không cần gõ chữ, bấm chọn là ra
        // ngay sách thuộc danh mục đó, giống cách duyệt theo danh mục bên
        // trang chủ Customer.
        JPanel categoryRow = new JPanel(new GridBagLayout());
        categoryRow.setBackground(Color.WHITE);
        categoryRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        categoryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.gridy = 0;
        cgbc.anchor = GridBagConstraints.WEST;

        JLabel categoryLabel = new JLabel("Danh mục:");
        categoryLabel.setForeground(AppTheme.TEXT);
        cgbc.gridx = 0;
        cgbc.insets = new java.awt.Insets(0, 0, 0, 10);
        categoryRow.add(categoryLabel, cgbc);

        Category[] categories = new CategoryDAO().searchCategory("");
        String[] categoryOptions = new String[categories.length + 1];
        categoryOptions[0] = ALL_CATEGORIES;
        for (int i = 0; i < categories.length; i++) {
            categoryOptions[i + 1] = categories[i].getCategoryName();
        }
        JComboBox<String> cboCategory = new JComboBox<>(categoryOptions);
        cgbc.gridx = 1;
        cgbc.weightx = 1;
        cgbc.fill = GridBagConstraints.HORIZONTAL;
        cgbc.insets = new java.awt.Insets(0, 0, 0, 0);
        categoryRow.add(cboCategory, cgbc);
        searchTop.add(categoryRow);

        searchPanel.add(searchTop, BorderLayout.NORTH);

        final List<Book> foundBooks = new ArrayList<>();
        dao.InventoryDAO inventoryDAO = new dao.InventoryDAO();

        JPanel cardGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        cardGrid.setBackground(Color.WHITE);
        cardGrid.setAlignmentY(JPanel.TOP_ALIGNMENT);

        JPanel cardGridWrap = new JPanel(new BorderLayout());
        cardGridWrap.setOpaque(false);
        cardGridWrap.add(cardGrid, BorderLayout.NORTH);

        JScrollPane cardScroll = new JScrollPane(cardGridWrap);
        cardScroll.setBorder(null);
        cardScroll.getVerticalScrollBar().setUnitIncrement(16);
        searchPanel.add(cardScroll, BorderLayout.CENTER);

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
        AppTheme.alignTableColumn(cartTable, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(cartTable, 1, SwingConstants.CENTER, 90, 90);
        AppTheme.alignTableColumn(cartTable, 2, SwingConstants.RIGHT, 120, 120);
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

        java.util.function.BiConsumer<Book, Integer> addBookToCart = (book, stock) -> {
            String input = JOptionPane.showInputDialog(shell,
                    "Số lượng cho \"" + book.getTitle() + "\" (còn " + stock + "):", "1");
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
            cartItems.add(new OrderDetail(qty, book.getPrice(), book.getPrice() * qty, book));
            refreshCart.run();
        };

        Runnable[] doSearchHolder = new Runnable[1];

        Runnable renderCards = () -> {
            cardGrid.removeAll();
            for (Book b : foundBooks) {
                int stock = inventoryDAO.getInventoryForBook(b.getBookID()).getStockQuantity();
                cardGrid.add(bookCard(b, stock, () -> addBookToCart.accept(b, stock)));
            }
            cardGrid.revalidate();
            cardGrid.repaint();
        };

        Runnable doSearchByKeyword = () -> {
            cboCategory.setSelectedItem(ALL_CATEGORIES);
            Book[] found = new BookDAO().searchBook(txtKeyword.getText().trim());
            foundBooks.clear();
            foundBooks.addAll(java.util.Arrays.asList(found));
            renderCards.run();
        };
        doSearchHolder[0] = doSearchByKeyword;

        btnSearch.addActionListener(e -> doSearchHolder[0].run());
        txtKeyword.addActionListener(e -> doSearchHolder[0].run());

        cboCategory.addActionListener(e -> {
            String selected = (String) cboCategory.getSelectedItem();
            foundBooks.clear();
            if (ALL_CATEGORIES.equals(selected)) {
                if (!txtKeyword.getText().trim().isEmpty()) {
                    foundBooks.addAll(java.util.Arrays.asList(new BookDAO().searchBook(txtKeyword.getText().trim())));
                }
            } else {
                txtKeyword.setText("");
                for (Category c : categories) {
                    if (c.getCategoryName().equals(selected)) {
                        foundBooks.addAll(java.util.Arrays.asList(new BookDAO().searchByCategory(c.getCategoryID())));
                        break;
                    }
                }
            }
            renderCards.run();
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
            int[] outOrderId = new int[1];
            String error = new InvoiceDAO().createInStoreSale(customer.getCustomerID(), cartItems, paymentMethod, shell.employee.getEmployeeID(), outOrderId);
            if (error == null) {
                showReceiptDialog(shell, outOrderId[0], customer, shell.employee.getFullName(), paymentMethod, cartItems);
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

        doSearchByKeyword.run();

        return content;
    }

    /** Thẻ 1 cuốn sách: ảnh bìa + tên + giá/tồn kho + nút thêm vào hóa đơn — dễ tìm bằng mắt hơn bảng chữ trần. */
    private JPanel bookCard(Book book, int stock, Runnable onAdd) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));

        BookCoverPanel cover = new BookCoverPanel(book.getTitle(), book.getBookID(), book.getImage());
        JPanel coverWrap = new JPanel(new BorderLayout());
        coverWrap.setOpaque(false);
        coverWrap.setPreferredSize(new Dimension(58, 100));
        coverWrap.setBorder(new EmptyBorder(8, 8, 8, 0));
        coverWrap.add(cover, BorderLayout.CENTER);
        card.add(coverWrap, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel titleLabel = new JLabel("<html>" + escape(book.getTitle()) + "</html>");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12.5f));
        titleLabel.setForeground(AppTheme.TEXT);
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(titleLabel);

        info.add(Box.createVerticalStrut(4));

        JLabel priceLabel = new JLabel(String.format("%,.0f₫", book.getPrice()));
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 12.5f));
        priceLabel.setForeground(AppTheme.ACCENT_TEXT);
        priceLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(priceLabel);

        JLabel stockLabel = new JLabel(stock > 0 ? "Tồn kho: " + stock : "Hết hàng");
        stockLabel.setFont(stockLabel.getFont().deriveFont(11f));
        stockLabel.setForeground(stock > 0 ? MUTED : AppTheme.PRIMARY_HOVER);
        stockLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(stockLabel);

        info.add(Box.createVerticalStrut(6));

        JButton btnAdd = new JButton("+ Thêm vào hóa đơn");
        btnAdd.setFont(btnAdd.getFont().deriveFont(11f));
        AppTheme.secondary(btnAdd);
        btnAdd.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btnAdd.setEnabled(stock > 0);
        btnAdd.addActionListener(e -> onAdd.run());
        info.add(btnAdd);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Hiện biên lai vừa lập (số hóa đơn = orderID) trong 1 khung chữ đơn cách
     * (monospace) để cột thẳng hàng, kèm nút in trực tiếp bằng
     * JTextArea.print() — mở hộp thoại máy in hệ thống, không cần thư viện
     * PDF/print riêng.
     */
    private void showReceiptDialog(SalesShellFrm shell, int orderID, Customer customer, String employeeName,
            String paymentMethod, List<OrderDetail> items) {
        JTextArea receipt = new JTextArea(buildReceiptText(orderID, customer, employeeName, paymentMethod, items));
        receipt.setEditable(false);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        receipt.setBackground(Color.WHITE);
        receipt.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(receipt);
        scroll.setPreferredSize(new Dimension(420, 380));
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));

        Object[] options = {"In hóa đơn", "Đóng"};
        int choice = JOptionPane.showOptionDialog(shell, scroll, "Lập hóa đơn thành công! Hóa đơn #" + orderID,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            try {
                receipt.print();
            } catch (java.awt.print.PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(shell, "In hóa đơn thất bại: " + ex.getMessage());
            }
        }
    }

    private String buildReceiptText(int orderID, Customer customer, String employeeName,
            String paymentMethod, List<OrderDetail> items) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("          BOOKSHOP - HÓA ĐƠN BÁN HÀNG\n");
        sb.append("========================================\n");
        sb.append("Hóa đơn số: #").append(orderID).append("\n");
        sb.append("Ngày lập  : ").append(fmt.format(new Date())).append("\n");
        sb.append("Nhân viên : ").append(employeeName == null ? "-" : employeeName).append("\n");
        sb.append("Khách hàng: ").append(customer.getFullName())
                .append(customer.getPhone() == null ? "" : " (" + customer.getPhone() + ")").append("\n");
        sb.append("----------------------------------------\n");
        float total = 0f;
        for (OrderDetail d : items) {
            sb.append(String.format("%-22.22s %3d %12s%n",
                    d.getBook().getTitle(), d.getQuantity(), String.format("%,.0f", d.getSubtotal())));
            total += d.getSubtotal();
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-26s %12s%n", "Tổng cộng:", String.format("%,.0f₫", total)));
        sb.append("Thanh toán: ").append(paymentMethod).append("\n");
        sb.append("========================================\n");
        sb.append("           Cảm ơn quý khách!\n");
        return sb.toString();
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

    /**
     * FR-SALE-09: tạo nhanh 1 khách hàng thật ngay tại quầy khi khách chưa có
     * trong hệ thống — thay vì bắt buộc dùng chung "Khách vãng lai". Email
     * là NOT NULL + UNIQUE trong CSDL nhưng nhân viên thường không hỏi email
     * khi bán tại quầy, nên để trống thì tự sinh 1 email placeholder theo số
     * điện thoại (cùng kiểu placeholder @bookshop.local đã dùng cho khách
     * vãng lai) thay vì chặn luôn nếu nhân viên không có email khách.
     */
    private Customer addNewCustomer(SalesShellFrm shell) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtName = new JTextField(18);
        JTextField txtPhone = new JTextField(18);
        JTextField txtEmail = new JTextField(18);
        JTextField txtAddress = new JTextField(18);

        String[] labels = {"Họ tên *:", "Số điện thoại *:", "Email (tùy chọn):", "Địa chỉ (tùy chọn):"};
        JTextField[] fields = {txtName, txtPhone, txtEmail, txtAddress};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(fields[i], gbc);
        }

        int result = JOptionPane.showConfirmDialog(shell, panel, "Thêm khách hàng mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập Họ tên và Số điện thoại!");
            return null;
        }
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            email = "khach" + phone + "@bookshop.local";
        }

        Customer customer = new Customer();
        customer.setFullName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        String address = txtAddress.getText().trim();
        customer.setAddress(address.isEmpty() ? null : address);

        String error = new CustomerDAO().addCustomer(customer);
        if (error != null) {
            JOptionPane.showMessageDialog(shell, error);
            return null;
        }
        return customer;
    }
}
