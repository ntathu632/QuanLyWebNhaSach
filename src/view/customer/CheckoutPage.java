/*
 * Trang xác nhận đơn hàng — nhập địa chỉ giao hàng, chọn phương thức thanh
 * toán, tạo Order (OrderDAO.addOrder ghi Order + OrderDetail + xóa CartItem
 * trong 1 transaction), rồi chuyển sang PaymentPage.
 */
package view.customer;
import view.shared.*;

import dao.OrderDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Order;

public class CheckoutPage implements CustomerPage {

    private final Order order;

    public CheckoutPage(Order order) {
        this.order = order;
    }

    @Override
    public String navKey() {
        return "cart";
    }

    @Override
    public String title() {
        return "Xác nhận đơn hàng";
    }

    @Override
    public CustomerPage backTarget() {
        return new ShoppingCartPage();
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Xác nhận đơn hàng");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        ShadowCard formCard = new ShadowCard(new GridBagLayout(), 16, 24);
        formCard.setMaximumSize(new Dimension(560, 248));
        formCard.setPreferredSize(new Dimension(560, 248));
        formCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblTotal = new JLabel(String.format("%,.0f₫", order.getTotalAmount()));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));
        lblTotal.setForeground(AppTheme.ACCENT_TEXT);
        formRow(formCard, gbc, 0, "Tổng tiền:", lblTotal);

        JTextField txtShippingAddress = new JTextField();
        formRow(formCard, gbc, 1, "Địa chỉ giao hàng:", txtShippingAddress);

        JComboBox<String> cboPaymentMethod = new JComboBox<>(
                new String[]{"Credit Card", "Bank Transfer", "E-Wallet", "Cash on Delivery"});
        formRow(formCard, gbc, 2, "Phương thức thanh toán:", cboPaymentMethod);

        top.add(formCard);
        top.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(560, 40));

        buttonRow.add(Box.createHorizontalGlue());

        JButton btnBack = new JButton("Quay lại giỏ hàng");
        AppTheme.secondary(btnBack);
        btnBack.addActionListener(e -> shell.navigateTo(new ShoppingCartPage()));
        buttonRow.add(btnBack);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnConfirm = new JButton("Xác nhận đặt hàng");
        AppTheme.primary(btnConfirm);
        btnConfirm.addActionListener(e -> onConfirm(shell, txtShippingAddress, cboPaymentMethod));
        buttonRow.add(btnConfirm);

        top.add(buttonRow);

        content.add(top, BorderLayout.NORTH);
        return content;
    }

    private void formRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, 0, 8, 14);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(AppTheme.TEXT);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void onConfirm(CustomerShellFrm shell, JTextField txtShippingAddress, JComboBox<String> cboPaymentMethod) {
        String shippingAddress = txtShippingAddress.getText().trim();
        if (shippingAddress.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập địa chỉ giao hàng!");
            return;
        }

        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod((String) cboPaymentMethod.getSelectedItem());

        OrderDAO orderDAO = new OrderDAO();
        String error = orderDAO.addOrder(order);
        if (error == null) {
            shell.refreshCartBadge();
            shell.navigateTo(new PaymentPage(order));
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }
}
