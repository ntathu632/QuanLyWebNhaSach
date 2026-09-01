/*
 * Trang giỏ hàng — bảng các sách đã thêm, xóa từng dòng, và chuyển sang
 * CheckoutPage. Tự lấy/tạo giỏ hàng của khách qua ShoppingCartDAO.getOrCreateCart().
 */
package view.customer;
import view.shared.*;

import dao.ShoppingCartDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.CartItem;
import model.Order;
import model.OrderDetail;
import model.ShoppingCart;

public class ShoppingCartPage implements CustomerPage {

    @Override
    public String navKey() {
        return "cart";
    }

    @Override
    public String title() {
        return "Giỏ hàng";
    }

    @Override
    public CustomerPage backTarget() {
        return new CustomerHomePage();
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Giỏ hàng");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        content.add(heading, BorderLayout.NORTH);

        ShoppingCart cart = new ShoppingCartDAO().getOrCreateCart(shell.customer);

        String[] columns = {"Sách", "Số lượng", "Đơn giá", "Thành tiền"};
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
        AppTheme.alignTableColumn(table, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 100, 100);
        AppTheme.alignTableColumn(table, 2, SwingConstants.RIGHT, 130, 130);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 130, 130);

        final ArrayList<CartItem> currentItems = new ArrayList<>();

        Runnable reload = () -> {
            CartItem[] items = new ShoppingCartDAO().getCartItems(cart);
            cart.setItems(items);
            currentItems.clear();
            model.setRowCount(0);
            float total = 0f;
            for (CartItem item : items) {
                currentItems.add(item);
                total += item.getSubtotal();
                model.addRow(new Object[]{item.getBook().getTitle(), item.getQuantity(),
                    String.format("%,.0f₫", item.getUnitPrice()), String.format("%,.0f₫", item.getSubtotal())});
            }
        };
        reload.run();

        ShadowCard tableCard = new ShadowCard(new BorderLayout(0, 10), 14, 16);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 0, 0, 0));

        JLabel totalLabel = new JLabel();
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
        totalLabel.setForeground(AppTheme.TEXT);
        footer.add(totalLabel, BorderLayout.WEST);

        Runnable refreshTotal = () -> {
            float total = 0f;
            for (CartItem item : currentItems) {
                total += item.getSubtotal();
            }
            totalLabel.setText("Tổng cộng: " + String.format("%,.0f₫", total));
        };
        refreshTotal.run();

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setOpaque(false);

        JButton btnRemove = new JButton("Xóa sách đã chọn");
        AppTheme.secondary(btnRemove);
        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 sách trong giỏ hàng để xóa!");
                return;
            }
            if (new ShoppingCartDAO().removeCartItem(currentItems.get(row))) {
                reload.run();
                refreshTotal.run();
                shell.refreshCartBadge();
            } else {
                JOptionPane.showMessageDialog(shell, "Xóa sách khỏi giỏ hàng thất bại!");
            }
        });
        actions.add(btnRemove);
        actions.add(Box.createHorizontalStrut(12));

        JButton btnCheckout = new JButton("Tiến hành thanh toán");
        AppTheme.primary(btnCheckout);
        btnCheckout.addActionListener(e -> {
            if (currentItems.isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Giỏ hàng đang trống!");
                return;
            }

            OrderDetail[] details = new OrderDetail[currentItems.size()];
            float total = 0f;
            for (int i = 0; i < currentItems.size(); i++) {
                CartItem item = currentItems.get(i);
                details[i] = new OrderDetail(item.getQuantity(), item.getUnitPrice(), item.getSubtotal(), item.getBook());
                total += item.getSubtotal();
            }

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setTotalAmount(total);
            order.setStatus("Pending");
            order.setCustomer(cart.getCustomer());
            order.setDetails(details);
            order.setCartID(cart.getCartID());

            shell.navigateTo(new CheckoutPage(order));
        });
        actions.add(btnCheckout);

        footer.add(actions, BorderLayout.EAST);
        tableCard.add(footer, BorderLayout.SOUTH);

        content.add(tableCard, BorderLayout.CENTER);
        return content;
    }
}
