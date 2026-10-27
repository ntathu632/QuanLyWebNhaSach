/*
 * Thẻ hiển thị 1 cuốn sách (bìa + tên + giá + nút xem chi tiết) — dùng chung
 * cho các lưới sách lấy từ DB ở CustomerHomeFrm (sách bán chạy, khám phá sách)
 * và SearchBookForPurchaseFrm (kết quả tìm kiếm). Số thẻ phụ thuộc dữ liệu nên
 * phải dựng bằng code, không kéo thả trong Design được. Kiểu phẳng: nền
 * trắng, viền mảnh bo góc — giống các thẻ tĩnh làm trong Design; cả thẻ bắt
 * được click (không chỉ riêng nút) như thẻ sản phẩm trên các trang bán sách.
 */
package view.customer.component;
import view.shared.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.Book;

public final class BookCard {

    private BookCard() {
    }

    public static JPanel build(Book book, Runnable onViewDetail) {
        return build(book, onViewDetail, null);
    }

    /** onAddToCart != null thì cạnh nút "Xem chi tiết" có thêm nút giỏ hàng để thêm sách ngay từ thẻ. */
    public static JPanel build(Book book, Runnable onViewDetail, Runnable onAddToCart) {
        JPanel card = whiteCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        onClick(card, onViewDetail);

        JLabel cover = cover(book);
        cover.setPreferredSize(new Dimension(0, 130));
        cover.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cover.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        card.add(cover);
        card.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("<html>" + escape(book.getTitle()) + "</html>");
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 13f));
        title.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel price = new JLabel(String.format("%,.0f₫", book.getPrice()));
        price.setFont(price.getFont().deriveFont(Font.PLAIN, 16f));
        price.setForeground(AppTheme.ACCENT_TEXT);
        price.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        card.add(price);
        card.add(Box.createVerticalStrut(10));

        JButton btn = new JButton("Xem chi tiết");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        AppTheme.primary(btn);
        btn.addActionListener(e -> onViewDetail.run());

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setOpaque(false);
        actions.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        actions.add(btn);
        if (onAddToCart != null) {
            JButton cart = new JButton(new FlatIcon(FlatIcon.Kind.CART, Color.WHITE, 16));
            cart.setToolTipText("Thêm vào giỏ hàng");
            cart.setPreferredSize(new Dimension(44, 32));
            cart.setMinimumSize(new Dimension(44, 32));
            cart.setMaximumSize(new Dimension(44, 32));
            AppTheme.primary(cart);
            cart.addActionListener(e -> onAddToCart.run());
            actions.add(Box.createHorizontalStrut(8));
            actions.add(cart);
        }
        card.add(actions);
        return card;
    }

    /** Khung thẻ phẳng: nền trắng, viền mảnh bo góc — giống các thẻ tĩnh làm trong Design. */
    public static JPanel whiteCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(AppTheme.TABLE_GRID, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        return card;
    }

    /** Cho cả khung (không chỉ nút bên trong) bắt được click, đổi con trỏ thành bàn tay. */
    public static void onClick(JComponent comp, Runnable action) {
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
    }

    /** Ảnh bìa thật nếu có file trong images/books/, không thì ô màu phẳng có icon sách. */
    private static JLabel cover(Book book) {
        JLabel cover = new JLabel();
        cover.setHorizontalAlignment(SwingConstants.CENTER);
        showCover(cover, book, 130, 40);
        return cover;
    }

    /**
     * Đổ ảnh bìa vào 1 JLabel có sẵn (vd. lblCover kéo thả trong Design ở
     * BookDetailFrm): ảnh thật co theo chiều cao "height", không có ảnh thì
     * ô màu phẳng + icon sách cỡ "iconSize".
     */
    public static void showCover(JLabel cover, Book book, int height, int iconSize) {
        try {
            File file = BookImageStore.resolve(book.getImage());
            if (file != null && file.isFile()) {
                Image img = ImageIO.read(file);
                if (img != null) {
                    cover.setOpaque(false);
                    cover.setIcon(new ImageIcon(img.getScaledInstance(-1, height, Image.SCALE_SMOOTH)));
                    return;
                }
            }
        } catch (Exception ex) {
            // Ảnh hỏng/không đọc được -> dùng ô màu phẳng bên dưới.
        }
        cover.setOpaque(true);
        cover.setBackground(AppTheme.CHART_PALETTE[Math.floorMod(book.getBookID(), AppTheme.CHART_PALETTE.length)]);
        cover.setIcon(new FlatIcon(FlatIcon.Kind.BOOK, Color.WHITE, iconSize));
    }

    public static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
