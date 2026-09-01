/*
 * Trang Hóa đơn đã lập (FR-SALE-07 Invoice History + FR-SALE-08 Cancel
 * In-Store Invoice) — liệt kê toàn bộ hóa đơn lập tại quầy (InStoreSalePage),
 * cho phép tra cứu lại và hủy hóa đơn đã thanh toán. Hủy dùng lại
 * OrderDAO.updateOrderStatus("Cancelled") (đã tự hoàn kho, BR-03) và đổi
 * Payment sang "Refunded" cho khớp — đối xứng với luồng hủy đơn online ở
 * OrderManagementPage.
 */
package view.sales;
import view.shared.*;

import dao.InvoiceDAO;
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
import model.Invoice;
import model.OrderDetail;

public class InvoiceHistoryPage implements SalesPage {

    private static final Color MUTED = new Color(0x8A, 0x83, 0x74);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String[] STATUSES = {"Paid", "Cancelled"};
    private static final String ALL_STATUSES = "Tất cả trạng thái";

    @Override
    public String navKey() {
        return "invoices";
    }

    @Override
    public String title() {
        return "Hóa Đơn Đã Lập";
    }

    @Override
    public JPanel content(SalesShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Hóa đơn đã lập");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] columns = {"Mã HĐ", "Ngày lập", "Khách hàng", "Nhân viên lập", "Tổng tiền", "Trạng thái"};
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
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 70, 70);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 140, 140);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 3, SwingConstants.LEFT, 140, 140);
        AppTheme.alignTableColumn(table, 4, SwingConstants.RIGHT, 130, 130);
        AppTheme.alignTableColumn(table, 5, SwingConstants.CENTER, 110, 110);

        final List<Invoice> allInvoices = new ArrayList<>();
        final List<Invoice> currentInvoices = new ArrayList<>();

        JComboBox<String> cboStatusFilter = new JComboBox<>();
        cboStatusFilter.addItem(ALL_STATUSES);
        for (String s : STATUSES) {
            cboStatusFilter.addItem(s);
        }

        Runnable applyFilter = () -> {
            String selected = (String) cboStatusFilter.getSelectedItem();
            currentInvoices.clear();
            for (Invoice iv : allInvoices) {
                if (ALL_STATUSES.equals(selected) || iv.getOrderStatus().equals(selected)) {
                    currentInvoices.add(iv);
                }
            }
            model.setRowCount(0);
            for (Invoice iv : currentInvoices) {
                model.addRow(new Object[]{iv.getInvoiceID(), DATE_FMT.format(iv.getInvoiceDate()),
                    iv.getCustomerName(), iv.getEmployeeName(),
                    String.format("%,.0f₫", iv.getTotalAmount()), iv.getOrderStatus()});
            }
        };

        Runnable[] reloadHolder = new Runnable[1];
        Runnable reload = () -> {
            allInvoices.clear();
            allInvoices.addAll(new InvoiceDAO().getInStoreInvoices());
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
        JLabel resultTitle = new JLabel("Danh sách hóa đơn tại quầy");
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

        JButton btnCancel = new JButton("Hủy hóa đơn");
        AppTheme.secondary(btnCancel);
        actionButtons.add(btnCancel);
        tableHeader.add(actionButtons, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        btnView.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentInvoices.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 hóa đơn!");
                return;
            }
            showInvoiceDetail(shell, currentInvoices.get(row));
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentInvoices.size()) {
                        showInvoiceDetail(shell, currentInvoices.get(row));
                    }
                }
            }
        });

        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentInvoices.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 hóa đơn!");
                return;
            }
            Invoice selected = currentInvoices.get(row);
            if ("Cancelled".equals(selected.getOrderStatus())) {
                JOptionPane.showMessageDialog(shell, "Hóa đơn này đã bị hủy trước đó!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Hủy hóa đơn #" + selected.getInvoiceID() + " của khách \"" + selected.getCustomerName()
                    + "\"? Số sách trong hóa đơn sẽ được hoàn lại vào tồn kho.",
                    "Xác nhận hủy hóa đơn", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            boolean ok = new OrderDAO().updateOrderStatus(selected.getOrderID(), "Cancelled");
            if (ok) {
                new PaymentDAO().updatePaymentStatus(selected.getOrderID(), "Refunded");
                JOptionPane.showMessageDialog(shell, "Đã hủy hóa đơn và hoàn tồn kho!");
                reloadHolder[0].run();
            } else {
                JOptionPane.showMessageDialog(shell, "Hủy hóa đơn thất bại! Vui lòng thử lại.");
            }
        });

        reload.run();

        return content;
    }

    /** Xem chi tiết 1 hóa đơn — thông tin thanh toán + danh sách sách đã bán. */
    private void showInvoiceDetail(java.awt.Component parent, Invoice invoice) {
        List<OrderDetail> details = new OrderDAO().getOrderDetails(invoice.getOrderID());

        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(600, 460));

        JPanel infoCard = new JPanel(new GridBagLayout());
        infoCard.setBackground(new Color(0xF7, 0xF4, 0xEC));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.INPUT_BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.anchor = GridBagConstraints.WEST;
        cgbc.insets = new java.awt.Insets(2, 0, 2, 24);
        addInfoRow(infoCard, cgbc, 0, 0, "Khách hàng:", invoice.getCustomerName());
        addInfoRow(infoCard, cgbc, 1, 0, "Nhân viên lập:", invoice.getEmployeeName());
        addInfoRow(infoCard, cgbc, 0, 1, "Phương thức thanh toán:", invoice.getPaymentMethod());
        addInfoRow(infoCard, cgbc, 1, 1, "Trạng thái:", invoice.getOrderStatus());
        panel.add(infoCard, BorderLayout.NORTH);

        JPanel itemsBlock = new JPanel(new BorderLayout(0, 6));
        itemsBlock.setOpaque(false);

        JLabel itemsTitle = new JLabel("Sách đã bán");
        itemsTitle.setFont(itemsTitle.getFont().deriveFont(Font.BOLD, 13.5f));
        itemsTitle.setForeground(AppTheme.TEXT);
        itemsBlock.add(itemsTitle, BorderLayout.NORTH);

        JPanel itemsList = new JPanel();
        itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
        itemsList.setBackground(Color.WHITE);
        for (OrderDetail d : details) {
            itemsList.add(orderItemRow(d));
        }
        itemsList.setPreferredSize(new Dimension(560, itemsList.getPreferredSize().height));
        JScrollPane itemsScroll = new JScrollPane(itemsList);
        itemsScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        itemsScroll.getVerticalScrollBar().setUnitIncrement(16);
        itemsBlock.add(itemsScroll, BorderLayout.CENTER);
        panel.add(itemsBlock, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel(String.format("Tổng cộng: %,.0f₫", invoice.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        totalLabel.setForeground(AppTheme.ACCENT_TEXT);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, "Chi tiết hóa đơn #" + invoice.getInvoiceID(), JOptionPane.PLAIN_MESSAGE);
    }

    /** 1 dòng sách trong hóa đơn: ảnh bìa nhỏ + tên + số lượng × đơn giá = thành tiền. */
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
