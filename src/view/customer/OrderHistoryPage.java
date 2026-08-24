/*
 * Trang "Đơn hàng của tôi" — gộp FR-CUS-08 (View Purchase History, danh sách
 * đơn) + FR-CUS-07 (Track Order Status, "extend của View Purchase History" —
 * chọn 1 đơn xem chi tiết) + FR-CUS-10 (Cancel Online Order) vào 1 trang duy
 * nhất, vì cả 3 vốn là cùng 1 luồng thao tác trên cùng 1 danh sách.
 *
 * Hủy đơn ở đây gọi OrderDAO.updateOrderStatus(orderID, "Cancelled") — hàm
 * này tự hoàn lại tồn kho tương ứng OrderDetail của đơn (BR-03), đối xứng
 * với phần trừ kho đã thêm vào OrderDAO.addOrder() khi đặt hàng.
 */
package view.customer;
import view.shared.*;

import dao.OrderDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Order;
import model.OrderDetail;

public class OrderHistoryPage implements CustomerPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public String navKey() {
        return "orders";
    }

    @Override
    public String title() {
        return "Đơn hàng của tôi";
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Đơn hàng của tôi");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        content.add(heading, BorderLayout.NORTH);

        String[] columns = {"Mã đơn", "Ngày đặt", "Tổng tiền", "Trạng thái"};
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
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground(statusColor(String.valueOf(value)));
                return c;
            }
        });

        final List<Order> currentOrders = new ArrayList<>();

        Runnable reload = () -> {
            List<Order> orders = new OrderDAO().getOrdersByCustomer(shell.customer.getCustomerID());
            currentOrders.clear();
            currentOrders.addAll(orders);
            model.setRowCount(0);
            for (Order o : orders) {
                model.addRow(new Object[]{o.getOrderID(), DATE_FMT.format(o.getOrderDate()),
                    String.format("%,.0f₫", o.getTotalAmount()), statusLabel(o.getStatus())});
            }
        };

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Lịch sử đơn hàng");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JPanel rowActions = new JPanel();
        rowActions.setLayout(new BoxLayout(rowActions, BoxLayout.X_AXIS));
        rowActions.setOpaque(false);

        JButton btnDetail = new JButton("Xem chi tiết");
        AppTheme.primary(btnDetail);
        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentOrders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng để xem chi tiết!");
                return;
            }
            showOrderDetail(shell, currentOrders.get(row));
        });
        rowActions.add(btnDetail);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnCancel = new JButton("Hủy đơn");
        AppTheme.secondary(btnCancel);
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentOrders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng để hủy!");
                return;
            }
            Order selected = currentOrders.get(row);
            if (!"Pending".equals(selected.getStatus())) {
                JOptionPane.showMessageDialog(shell, "Chỉ có thể hủy đơn đang ở trạng thái \"Chờ xử lý\"!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn hủy đơn hàng #" + selected.getOrderID() + "?",
                    "Xác nhận hủy đơn", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            if (new OrderDAO().updateOrderStatus(selected.getOrderID(), "Cancelled")) {
                JOptionPane.showMessageDialog(shell, "Đã hủy đơn hàng!");
                reload.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Hủy đơn hàng thất bại! Vui lòng thử lại.");
            }
        });
        rowActions.add(btnCancel);

        tableHeader.add(rowActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        reload.run();

        return content;
    }

    private void showOrderDetail(CustomerShellFrm shell, Order order) {
        List<OrderDetail> details = new OrderDAO().getOrderDetails(order.getOrderID());

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new java.awt.Dimension(480, 300));

        JLabel info = new JLabel("<html>Trạng thái: <b>" + statusLabel(order.getStatus()) + "</b><br>"
                + "Địa chỉ giao hàng: " + (order.getShippingAddress() == null ? "" : order.getShippingAddress()) + "<br>"
                + "Tổng tiền: <b>" + String.format("%,.0f₫", order.getTotalAmount()) + "</b></html>");
        panel.add(info, BorderLayout.NORTH);

        String[] columns = {"Sách", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (OrderDetail d : details) {
            model.addRow(new Object[]{d.getBook().getTitle(), d.getQuantity(),
                String.format("%,.0f₫", d.getUnitPrice()), String.format("%,.0f₫", d.getSubtotal())});
        }
        JTable table = new JTable(model);
        table.setRowHeight(26);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(shell, panel, "Chi tiết đơn hàng #" + order.getOrderID(), JOptionPane.PLAIN_MESSAGE);
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "Pending" -> "Chờ xử lý";
            case "Paid" -> "Đã thanh toán";
            case "Cancelled" -> "Đã hủy";
            default -> status;
        };
    }

    private Color statusColor(String label) {
        if ("Đã thanh toán".equals(label)) {
            return AppTheme.PRIMARY_HOVER;
        }
        if ("Đã hủy".equals(label)) {
            return AppTheme.ACCENT_TEXT;
        }
        return AppTheme.CHART_ORANGE;
    }
}
