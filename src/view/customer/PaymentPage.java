/*
 * Trang thanh toán — tạo Payment (status "Paid") và cập nhật Order sang
 * "Paid", hoặc hủy đơn (status "Cancelled") nếu khách bấm Hủy.
 */
package view.customer;
import view.shared.*;

import dao.OrderDAO;
import dao.PaymentDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import model.Order;
import model.Payment;

public class PaymentPage implements CustomerPage {

    private final Order order;

    public PaymentPage(Order order) {
        this.order = order;
    }

    @Override
    public String navKey() {
        return "cart";
    }

    @Override
    public String title() {
        return "Thanh toán";
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Thanh toán");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        ShadowCard formCard = new ShadowCard(new GridBagLayout(), 16, 24);
        formCard.setMaximumSize(new Dimension(560, 168));
        formCard.setPreferredSize(new Dimension(560, 168));
        formCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblAmount = new JLabel(String.format("%,.0f₫", order.getTotalAmount()));
        lblAmount.setFont(lblAmount.getFont().deriveFont(Font.BOLD, 18f));
        lblAmount.setForeground(AppTheme.ACCENT_TEXT);
        formRow(formCard, gbc, 0, "Số tiền cần thanh toán:", lblAmount);

        JLabel lblMethod = new JLabel(order.getPaymentMethod());
        lblMethod.setForeground(AppTheme.TEXT);
        formRow(formCard, gbc, 1, "Phương thức thanh toán:", lblMethod);

        top.add(formCard);
        top.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(560, 40));

        buttonRow.add(Box.createHorizontalGlue());

        JButton btnCancel = new JButton("Hủy đơn hàng");
        AppTheme.secondary(btnCancel);
        btnCancel.addActionListener(e -> {
            new OrderDAO().updateOrderStatus(order.getOrderID(), "Cancelled");
            shell.navigateTo(new CustomerHomePage());
        });
        buttonRow.add(btnCancel);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnPay = new JButton("Thanh toán ngay");
        AppTheme.primary(btnPay);
        btnPay.addActionListener(e -> onPay(shell));
        buttonRow.add(btnPay);

        top.add(buttonRow);

        JScrollPane pageScroll = new JScrollPane(top);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(pageScroll, BorderLayout.CENTER);
        return content;
    }

    private void formRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel field) {
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

    private void onPay(CustomerShellFrm shell) {
        String method = order.getPaymentMethod();
        if ("VNPay".equals(method) || "Momo".equals(method)) {
            shell.navigateTo(new MockPaymentGatewayPage(order, method));
            return;
        }

        Payment payment = new Payment();
        payment.setPaymentDate(new Date());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(order.getPaymentMethod());
        payment.setStatus("Paid");
        payment.setOrderID(order.getOrderID());

        PaymentDAO paymentDAO = new PaymentDAO();
        if (paymentDAO.addPayment(payment)) {
            new OrderDAO().updateOrderStatus(order.getOrderID(), "Paid");
            JOptionPane.showMessageDialog(shell, "Thanh toán và đặt hàng thành công!");
            shell.navigateTo(new CustomerHomePage());
        } else {
            JOptionPane.showMessageDialog(shell, "Thanh toán thất bại! Vui lòng thử lại.");
        }
    }
}
