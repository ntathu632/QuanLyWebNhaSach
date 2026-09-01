/*
 * Giả lập màn hình cổng thanh toán bên thứ 3 (VNPay/Momo) — trước đó
 * "Thanh toán ngay" ở PaymentPage tự đánh dấu Paid ngay lập tức, không có
 * bước trung gian nào giống thật. Trang này KHÔNG gọi API VNPay/Momo thật
 * (không có tài khoản merchant), chỉ mô phỏng giao diện + luồng redirect
 * sang cổng thanh toán rồi quay lại, để trải nghiệm bớt "giả trân" hơn.
 */
package view.customer;
import view.shared.*;

import dao.OrderDAO;
import dao.PaymentDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.util.Date;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import model.Order;
import model.Payment;

public class MockPaymentGatewayPage implements CustomerPage {

    private static final Color VNPAY_BLUE = new Color(0x00, 0x66, 0xB3);
    private static final Color MOMO_PINK = new Color(0xD8, 0x2D, 0x8B);

    private final Order order;
    private final String gateway;

    public MockPaymentGatewayPage(Order order, String gateway) {
        this.order = order;
        this.gateway = gateway;
    }

    @Override
    public String navKey() {
        return "cart";
    }

    @Override
    public String title() {
        return gateway + " - Cổng thanh toán";
    }

    @Override
    public CustomerPage backTarget() {
        return new PaymentPage(order);
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        Color brand = "Momo".equals(gateway) ? MOMO_PINK : VNPAY_BLUE;

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(0xEA, 0xEA, 0xEA));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(brand);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel logo = new JLabel(gateway.toUpperCase());
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 20f));
        logo.setForeground(Color.WHITE);
        header.add(logo);
        header.add(Box.createHorizontalGlue());
        JLabel secure = new JLabel("🔒 Kết nối an toàn (giả lập, không phải cổng thật)");
        secure.setForeground(new Color(255, 255, 255, 210));
        secure.setFont(secure.getFont().deriveFont(11.5f));
        header.add(secure);
        content.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(30, 0, 30, 0));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.INPUT_BORDER),
                new EmptyBorder(28, 36, 28, 36)));
        card.setMaximumSize(new Dimension(420, 600));
        card.setPreferredSize(new Dimension(420, 600));

        JLabel merchant = new JLabel("Thanh toán cho Bookshop Management");
        merchant.setFont(merchant.getFont().deriveFont(Font.BOLD, 15f));
        merchant.setForeground(AppTheme.TEXT);
        merchant.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        card.add(merchant);
        card.add(Box.createVerticalStrut(4));

        JLabel orderLine = new JLabel("Mã đơn hàng: #" + order.getOrderID());
        orderLine.setForeground(new Color(0x8A, 0x83, 0x74));
        orderLine.setFont(orderLine.getFont().deriveFont(12.5f));
        orderLine.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        card.add(orderLine);
        card.add(Box.createVerticalStrut(20));

        QrCodePanel qr = new QrCodePanel(order.getOrderID());
        qr.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        card.add(qr);
        card.add(Box.createVerticalStrut(16));

        JLabel scanHint = new JLabel("Mở app " + gateway + " và quét mã để thanh toán");
        scanHint.setFont(scanHint.getFont().deriveFont(12.5f));
        scanHint.setForeground(AppTheme.TEXT);
        scanHint.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        card.add(scanHint);
        card.add(Box.createVerticalStrut(22));

        JLabel amountLabel = new JLabel("Số tiền thanh toán");
        amountLabel.setFont(amountLabel.getFont().deriveFont(11.5f));
        amountLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        amountLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        card.add(amountLabel);

        JLabel amount = new JLabel(String.format("%,.0f₫", order.getTotalAmount()));
        amount.setFont(amount.getFont().deriveFont(Font.BOLD, 28f));
        amount.setForeground(brand);
        amount.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        card.add(amount);
        card.add(Box.createVerticalStrut(28));

        JButton btnConfirm = new JButton("Xác nhận đã thanh toán");
        btnConfirm.setAlignmentX(JButton.CENTER_ALIGNMENT);
        btnConfirm.putClientProperty("FlatLaf.style",
                "background: #" + hex(brand) + "; foreground: #FFFFFF; borderWidth: 0; font: bold;"
                + " hoverBackground: #" + hex(brand.darker()) + "; pressedBackground: #" + hex(brand.darker().darker()));
        card.add(btnConfirm);
        card.add(Box.createVerticalStrut(10));

        JButton btnCancel = new JButton("Hủy giao dịch, quay lại");
        AppTheme.secondary(btnCancel);
        btnCancel.setAlignmentX(JButton.CENTER_ALIGNMENT);
        btnCancel.addActionListener(e -> shell.navigateTo(new PaymentPage(order)));
        card.add(btnCancel);

        btnConfirm.addActionListener(e -> {
            btnConfirm.setEnabled(false);
            btnCancel.setEnabled(false);
            btnConfirm.setText("Đang xử lý giao dịch...");
            Timer timer = new Timer(900, ev -> completePayment(shell));
            timer.setRepeats(false);
            timer.start();
        });

        center.add(card);
        content.add(center, BorderLayout.CENTER);
        return content;
    }

    private static String hex(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private void completePayment(CustomerShellFrm shell) {
        Payment payment = new Payment();
        payment.setPaymentDate(new Date());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(gateway);
        payment.setStatus("Paid");
        payment.setOrderID(order.getOrderID());

        if (new PaymentDAO().addPayment(payment)) {
            new OrderDAO().updateOrderStatus(order.getOrderID(), "Paid");
            JOptionPane.showMessageDialog(shell, gateway + " báo giao dịch thành công! Đơn hàng đã được thanh toán.");
            shell.navigateTo(new CustomerHomePage());
        } else {
            JOptionPane.showMessageDialog(shell, "Giao dịch thất bại! Vui lòng thử lại.");
            shell.navigateTo(new PaymentPage(order));
        }
    }

    /**
     * Mã QR minh họa — pattern tất định theo orderID (KHÔNG phải QR quét được
     * thật, chỉ để giao diện giống cổng thanh toán thật hơn là 1 nút bấm trơn).
     */
    private static class QrCodePanel extends JComponent {

        private final int seed;

        QrCodePanel(int seed) {
            this.seed = seed;
            Dimension size = new Dimension(170, 170);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(0xDD, 0xDD, 0xDD));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

            int cells = 16;
            int cellSize = getWidth() / cells;
            Random rnd = new Random(seed * 7919L);
            g2.setColor(new Color(0x22, 0x22, 0x22));
            for (int y = 0; y < cells; y++) {
                for (int x = 0; x < cells; x++) {
                    boolean isFinder = (x < 3 && y < 3) || (x >= cells - 3 && y < 3) || (x < 3 && y >= cells - 3);
                    if (isFinder || rnd.nextBoolean()) {
                        g2.fillRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);
                    }
                }
            }
            g2.dispose();
        }
    }
}
