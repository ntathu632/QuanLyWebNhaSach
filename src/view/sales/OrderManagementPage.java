/*
 * Trang Đơn hàng online (FR-SALE-02 Process Online Order + FR-SALE-03 Update
 * Order Status + FR-SALE-04/FR-CUS-10 Cancel Online Order — cùng dùng chung
 * 1 thao tác đổi trạng thái). Chỉ hiện đơn CHƯA có Invoice — đơn đã lập hóa
 * đơn tại quầy (InStoreSalePage) không thuộc kênh online, không cần xử lý lại.
 */
package view.sales;
import view.shared.*;

import dao.OrderDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Order;

public class OrderManagementPage implements SalesPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String[] STATUSES = {"Pending", "Paid", "Cancelled"};

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
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        content.add(heading, BorderLayout.NORTH);

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

        final List<Order> currentOrders = new ArrayList<>();

        Runnable reload = () -> {
            List<Order> orders = new OrderDAO().getOrdersWithoutInvoice();
            currentOrders.clear();
            currentOrders.addAll(orders);
            model.setRowCount(0);
            for (Order o : orders) {
                model.addRow(new Object[]{o.getOrderID(), o.getCustomer().getFullName(),
                    DATE_FMT.format(o.getOrderDate()), String.format("%,.0f₫", o.getTotalAmount()), o.getStatus()});
            }
        };

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

        JButton btnUpdate = new JButton("Cập nhật trạng thái");
        AppTheme.primary(btnUpdate);
        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentOrders.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 đơn hàng!");
                return;
            }
            Order selected = currentOrders.get(row);
            JComboBox<String> cboStatus = new JComboBox<>(STATUSES);
            cboStatus.setSelectedItem(selected.getStatus());
            int result = JOptionPane.showConfirmDialog(shell, cboStatus,
                    "Cập nhật trạng thái đơn #" + selected.getOrderID(), JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            String newStatus = (String) cboStatus.getSelectedItem();
            if (newStatus.equals(selected.getStatus())) {
                return;
            }
            if (new OrderDAO().updateOrderStatus(selected.getOrderID(), newStatus)) {
                JOptionPane.showMessageDialog(shell, "Đã cập nhật trạng thái đơn hàng!");
                reload.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Cập nhật thất bại! Vui lòng thử lại.");
            }
        });
        tableHeader.add(btnUpdate, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        reload.run();

        return content;
    }
}
