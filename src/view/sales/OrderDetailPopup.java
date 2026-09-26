/*
 * Hộp thoại xem chi tiết 1 đơn/hóa đơn — dùng chung cho InvoiceHistoryFrm
 * (hóa đơn tại quầy) và OrderManagementFrm (đơn hàng online, FR-SALE-02):
 * khối 4 ô thông tin + danh sách sách (kèm ảnh bìa từng cuốn) + tổng cộng.
 * Số dòng sách phụ thuộc dữ liệu nên dựng bằng code, hiện qua JOptionPane.
 * Dùng BorderLayout cho khung ngoài thay vì BoxLayout — BoxLayout Y_AXIS bên
 * trong JOptionPane để lại khoảng trắng bên trái do JOptionPane không giãn
 * full-width các thành phần con theo alignmentX.
 */
package view.sales;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.OrderDetail;
import view.customer.BookCoverPanel;
import view.shared.AppTheme;

final class OrderDetailPopup {

    private static final Color MUTED = new Color(0x8A, 0x83, 0x74);

    private OrderDetailPopup() {
    }

    /**
     * @param info 4 cặp {nhãn, giá trị}, xếp thành lưới 2 cột x 2 hàng theo
     *             thứ tự (cột 0 hàng 0), (cột 1 hàng 0), (cột 0 hàng 1), (cột 1 hàng 1)
     */
    static void show(Component parent, String title, String[][] info, String itemsHeading,
            List<OrderDetail> details, float total) {
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
        for (int i = 0; i < info.length; i++) {
            addInfoRow(infoCard, cgbc, i % 2, i / 2, info[i][0], info[i][1]);
        }
        panel.add(infoCard, BorderLayout.NORTH);

        JPanel itemsBlock = new JPanel(new BorderLayout(0, 6));
        itemsBlock.setOpaque(false);

        JLabel itemsTitle = new JLabel(itemsHeading);
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

        JLabel totalLabel = new JLabel(String.format("Tổng cộng: %,.0f₫", total));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        totalLabel.setForeground(AppTheme.ACCENT_TEXT);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    /** 1 dòng sách: ảnh bìa nhỏ + tên + số lượng × đơn giá = thành tiền. */
    private static JPanel orderItemRow(OrderDetail d) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.TABLE_GRID),
                new EmptyBorder(8, 8, 8, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        BookCoverPanel cover = new BookCoverPanel(d.getBook().getTitle(), d.getBook().getBookID(), d.getBook().getImage());
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

    private static void addInfoRow(JPanel panel, GridBagConstraints gbc, int col, int row, String label, String value) {
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
