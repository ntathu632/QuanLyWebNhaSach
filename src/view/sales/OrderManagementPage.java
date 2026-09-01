/*
 * Trang Đơn hàng online (FR-SALE-02 Process Online Order + FR-SALE-03 Update
 * Order Status + FR-SALE-04/FR-CUS-10 Cancel Online Order — cùng dùng chung
 * 1 thao tác đổi trạng thái). Chỉ hiện đơn CHƯA có Invoice — đơn đã lập hóa
 * đơn tại quầy (InStoreSalePage) không thuộc kênh online, không cần xử lý lại.
 */
package view.sales;
import view.shared.*;

import dao.OrderDAO;
import dao.PaymentDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Order;
import model.OrderDetail;

public class OrderManagementPage implements SalesPage {

    private static final Color MUTED = new Color(0x8A, 0x83, 0x74);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String[] STATUSES = {"Pending", "Paid", "Cancelled"};
    private static final String ALL_STATUSES = "Tất cả trạng thái";

    @Override
    public String navKey() {
        return "orders";
    }

    @Override
    public String title() {
        return "Đơn Hàng Online";
    }

    @Override
    public JPanel content(SalesShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Đơn hàng online");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] columns = {"Mã đơn", "Khách hàng", "Ngày đặt", "Tổng tiền", "Trạng thái"};
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
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 150, 150);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 130, 130);
        AppTheme.alignTableColumn(table, 4, SwingConstants.CENTER, 110, 110);

        final List<Order> allOrders = new ArrayList<>();
        final List<Order> currentOrders = new ArrayList<>();

        JComboBox<String> cboStatusFilter = new JComboBox<>();
        cboStatusFilter.addItem(ALL_STATUSES);
        for (String s : STATUSES) {
            cboStatusFilter.addItem(s);
        }

        Runnable applyFilter = () -> {
            String selected = (String) cboStatusFilter.getSelectedItem();
            currentOrders.clear();
            for (Order o : allOrders) {
                if (ALL_STATUSES.equals(selected) || o.getStatus().equals(selected)) {
                    currentOrders.add(o);
                }
            }
            model.setRowCount(0);
            for (Order o : currentOrders) {
                model.addRow(new Object[]{o.getOrderID(), o.getCustomer().getFullName(),
                    DATE_FMT.format(o.getOrderDate()), String.format("%,.0f₫", o.getTotalAmount()), o.getStatus()});
            }
        };

        Runnable[] reloadHolder = new Runnable[1];
        Runnable reload = () -> {
            allOrders.clear();
            allOrders.addAll(new OrderDAO().getOrdersWithoutInvoice());
            applyFilter.run();
        };
        reloadHolder[0] = reload;

        cboStatusFilter.addActionListener(e -> applyFilter.run());

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setBackground(AppTheme.BACKGROUND);
        filterRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        filterRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.gridy = 0;
        fgbc.anchor = GridBagConstraints.WEST;
        fgbc.gridx = 0;
        fgbc.insets = new java.awt.Insets(0, 0, 0, 10);
        JLabel filterLabel = new JLabel("Lọc theo trạng thái:");
        filterLabel.setForeground(AppTheme.TEXT);
        filterRow.add(filterLabel, fgbc);
        fgbc.gridx = 1;
        filterRow.add(cboStatusFilter, fgbc);

        JPanel headerWrap = new JPanel();
        headerWrap.setLayout(new BoxLayout(headerWrap, BoxLayout.Y_AXIS));
        headerWrap.setBackground(AppTheme.BACKGROUND);
        headerWrap.add(heading);
        headerWrap.add(filterRow);
        content.add(headerWrap, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);
        JLabel resultTitle = new JLabel("Danh sách đơn hàng online");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JPanel actionButtons = new JPanel();
        actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.X_AXIS));
        actionButtons.setOpaque(false);

        JButton btnView = new JButton("Xem chi tiết");
        AppTheme.secondary(btnView);
        actionButtons.add(btnView);
        actionButtons.add(Box.createHorizontalStrut(10));

        JButton btnUpdate = new JButton("Cập nhật trạng thái");
        AppTheme.primary(btnUpdate);
        actionButtons.add(btnUpdate);
        tableHeader.add(actionButtons, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        btnView.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentOrders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng!");
                return;
            }
            showOrderDetail(shell, currentOrders.get(row));
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentOrders.size()) {
                        showOrderDetail(shell, currentOrders.get(row));
                    }
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentOrders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng!");
                return;
            }
            Order selected = currentOrders.get(row);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Trạng thái mới:"), gbc);
            JComboBox<String> cboStatus = new JComboBox<>(STATUSES);
            cboStatus.setSelectedItem(selected.getStatus());
            cboStatus.setPreferredSize(new Dimension(180, cboStatus.getPreferredSize().height));
            gbc.gridx = 1;
            panel.add(cboStatus, gbc);

            int result = JOptionPane.showConfirmDialog(shell, panel,
                    "Cập nhật trạng thái đơn #" + selected.getOrderID(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            String newStatus = (String) cboStatus.getSelectedItem();
            if (newStatus.equals(selected.getStatus())) {
                return;
            }
            if (new OrderDAO().updateOrderStatus(selected.getOrderID(), newStatus)) {
                if ("Cancelled".equals(newStatus)) {
                    // Đơn đã thanh toán mới có Payment (Pending huỷ thẳng thì UPDATE
                    // này không khớp dòng nào, vô hại) — đánh dấu hoàn tiền cho khớp
                    // với Orders.status, cùng cách InvoiceHistoryPage đang làm.
                    new PaymentDAO().updatePaymentStatus(selected.getOrderID(), "Refunded");
                }
                JOptionPane.showMessageDialog(shell, "Đã cập nhật trạng thái đơn hàng!");
                reloadHolder[0].run();
            } else {
                JOptionPane.showMessageDialog(shell, "Cập nhật thất bại! Vui lòng thử lại.");
            }
        });

        reload.run();

        return content;
    }

    /**
     * FR-SALE-02: xem chi tiết 1 đơn hàng online — thông tin khách hàng + danh
     * sách sách đã đặt (kèm ảnh bìa từng cuốn, cùng phong cách thẻ sách với
     * InStoreSalePage). Dùng BorderLayout cho khung ngoài thay vì BoxLayout —
     * BoxLayout Y_AXIS bên trong JOptionPane để lại khoảng trắng bên trái do
     * JOptionPane không giãn full-width các thành phần con theo alignmentX.
     */
    private void showOrderDetail(java.awt.Component parent, Order order) {
        List<OrderDetail> details = new OrderDAO().getOrderDetails(order.getOrderID());

        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(600, 460));

        JPanel customerCard = new JPanel(new GridBagLayout());
        customerCard.setBackground(new Color(0xF7, 0xF4, 0xEC));
        customerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.INPUT_BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.anchor = GridBagConstraints.WEST;
        cgbc.insets = new java.awt.Insets(2, 0, 2, 24);
        model.Customer c = order.getCustomer();
        addInfoRow(customerCard, cgbc, 0, 0, "Khách hàng:", c.getFullName());
        addInfoRow(customerCard, cgbc, 1, 0, "Điện thoại:", c.getPhone() == null ? "—" : c.getPhone());
        addInfoRow(customerCard, cgbc, 0, 1, "Email:", c.getEmail() == null ? "—" : c.getEmail());
        addInfoRow(customerCard, cgbc, 1, 1, "Địa chỉ giao hàng:", order.getShippingAddress() == null ? "—" : order.getShippingAddress());
        panel.add(customerCard, BorderLayout.NORTH);

        JPanel itemsBlock = new JPanel(new BorderLayout(0, 6));
        itemsBlock.setOpaque(false);

        JLabel itemsTitle = new JLabel("Sách đã đặt");
        itemsTitle.setFont(itemsTitle.getFont().deriveFont(Font.BOLD, 13.5f));
        itemsTitle.setForeground(AppTheme.TEXT);
        itemsBlock.add(itemsTitle, BorderLayout.NORTH);

        JPanel itemsList = new JPanel();
        itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
        itemsList.setBackground(Color.WHITE);
        for (OrderDetail d : details) {
            itemsList.add(orderItemRow(d));
        }
        // JScrollPane không tự giãn view theo bề rộng viewport vì JPanel không
        // implement Scrollable — ép trước bề rộng khớp panel ngoài (600, trừ
        // lề) để không để lại khoảng trắng bên phải trong vùng cuộn.
        itemsList.setPreferredSize(new Dimension(560, itemsList.getPreferredSize().height));
        JScrollPane itemsScroll = new JScrollPane(itemsList);
        itemsScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        itemsScroll.getVerticalScrollBar().setUnitIncrement(16);
        itemsBlock.add(itemsScroll, BorderLayout.CENTER);
        panel.add(itemsBlock, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel(String.format("Tổng cộng: %,.0f₫", order.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        totalLabel.setForeground(AppTheme.ACCENT_TEXT);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, "Chi tiết đơn hàng #" + order.getOrderID(), JOptionPane.PLAIN_MESSAGE);
    }

    /** 1 dòng sách trong đơn: ảnh bìa nhỏ + tên + số lượng × đơn giá = thành tiền. */
    private JPanel orderItemRow(OrderDetail d) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.TABLE_GRID),
                new EmptyBorder(8, 8, 8, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        view.customer.BookCoverPanel cover = new view.customer.BookCoverPanel(
                d.getBook().getTitle(), d.getBook().getBookID(), d.getBook().getImage());
        JPanel coverWrap = new JPanel(new BorderLayout());
        coverWrap.setOpaque(false);
        coverWrap.setPreferredSize(new Dimension(38, 50));
        coverWrap.add(cover, BorderLayout.CENTER);
        row.add(coverWrap, BorderLayout.WEST);

        JLabel title = new JLabel("<html>" + d.getBook().getTitle() + "</html>");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(AppTheme.TEXT);
        row.add(title, BorderLayout.CENTER);

        JPanel amounts = new JPanel();
        amounts.setLayout(new BoxLayout(amounts, BoxLayout.Y_AXIS));
        amounts.setOpaque(false);
        JLabel qtyPrice = new JLabel(d.getQuantity() + " × " + String.format("%,.0f₫", d.getUnitPrice()));
        qtyPrice.setFont(qtyPrice.getFont().deriveFont(11.5f));
        qtyPrice.setForeground(MUTED);
        qtyPrice.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        JLabel subtotal = new JLabel(String.format("%,.0f₫", d.getSubtotal()));
        subtotal.setFont(subtotal.getFont().deriveFont(Font.BOLD, 13f));
        subtotal.setForeground(AppTheme.TEXT);
        subtotal.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        amounts.add(qtyPrice);
        amounts.add(subtotal);
        row.add(amounts, BorderLayout.EAST);

        return row;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int col, int row, String label, String value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(11f));
        lbl.setForeground(MUTED);
        JLabel val = new JLabel(value);
        val.setFont(val.getFont().deriveFont(Font.BOLD, 13f));
        val.setForeground(AppTheme.TEXT);
        cell.add(lbl);
        cell.add(val);
        gbc.gridx = col;
        gbc.gridy = row;
        panel.add(cell, gbc);
    }
}
