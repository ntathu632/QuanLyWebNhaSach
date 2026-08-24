/*
 * Thẻ hiển thị 1 cuốn sách (bìa vẽ tay + tên + giá + nút xem chi tiết) — dùng
 * chung giữa CustomerHomePage (lưới "Sách nổi bật") và SearchBookForPurchasePage
 * (lưới kết quả tìm kiếm) để không lặp code. Dùng ShadowCard (bo góc + đổ
 * bóng) thay viền phẳng, và cả thẻ bắt được click (không chỉ riêng nút) —
 * giống thẻ sản phẩm trên các trang bán sách thật.
 */
package view.customer;
import view.shared.*;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Book;

final class BookCard {

    private BookCard() {
    }

    static JPanel build(Book book, Runnable onViewDetail) {
        ShadowCard card = new ShadowCard(null, 14, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onViewDetail.run();
            }
        });

        BookCoverPanel cover = new BookCoverPanel(book.getTitle(), book.getBookID());
        cover.setPreferredSize(new Dimension(0, 130));
        cover.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cover.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(cover);

        card.add(Box.createVerticalStrut(10));

        JLabel titleLabel = new JLabel("<html>" + escape(book.getTitle()) + "</html>");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        titleLabel.setForeground(AppTheme.TEXT);
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(6));

        JLabel priceLabel = new JLabel(String.format("%,.0f₫", book.getPrice()));
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 16f));
        priceLabel.setForeground(AppTheme.ACCENT_TEXT);
        priceLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(priceLabel);

        card.add(Box.createVerticalStrut(10));

        JButton btn = new JButton("Xem chi tiết");
        btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        AppTheme.primary(btn);
        btn.addActionListener(e -> onViewDetail.run());
        card.add(btn);

        return card;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
