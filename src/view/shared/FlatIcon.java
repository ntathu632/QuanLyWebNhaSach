/*
 * Icon đơn sắc kiểu font icon (giống <i class="fa-solid fa-..."> trên web):
 * mỗi Kind là 1 ký tự trong font Font Awesome 6 Free Solid (src/fonts/
 * FontAwesome-Solid.ttf, giấy phép trong LICENSE-FontAwesome.txt). Font được
 * nạp thẳng từ file trong gói nên dùng được cả trong Design của NetBeans
 * (thuộc tính icon = new FlatIcon(FlatIcon.Kind.XXX, màu, cỡ)).
 * Muốn thêm icon: thêm 1 Kind kèm mã Unicode tra ở fontawesome.com/icons.
 */
package view.shared;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import javax.swing.Icon;

public final class FlatIcon implements Icon {

    public enum Kind {
        BOOK(''),          // fa-book
        RECEIPT(''),       // fa-receipt
        USERS(''),         // fa-users
        USER(''),          // fa-user
        MONEY(''),         // fa-money-bill
        TAG(''),           // fa-tag
        PEN(''),           // fa-pen
        BUILDING(''),      // fa-building
        CHART(''),         // fa-chart-column
        LOGOUT(''),        // fa-right-from-bracket
        HOME(''),          // fa-house
        PLUS(''),          // fa-plus
        TRASH(''),         // fa-trash
        BACK(''),          // fa-chevron-left
        SEARCH(''),        // fa-magnifying-glass
        LOCK(''),          // fa-lock
        CART(''),          // fa-cart-shopping
        OPEN_BOOK(''),     // fa-book-open
        LEAF(''),          // fa-seedling
        SMILE(''),         // fa-face-smile
        FLASK(''),         // fa-flask
        LAPTOP(''),        // fa-laptop
        BRAIN(''),         // fa-brain
        STAR(''),          // fa-star
        ARROW_RIGHT(''),   // fa-arrow-right
        CIRCLE_USER(''),   // fa-circle-user
        CHEVRON_DOWN(''),  // fa-chevron-down
        BOX('');           // fa-box

        final char glyph;

        Kind(char glyph) {
            this.glyph = glyph;
        }
    }

    private static final Font BASE_FONT = loadFont();

    private final Kind kind;
    private final Color color;
    private final int size;

    public FlatIcon(Kind kind, Color color, int size) {
        this.kind = kind;
        this.color = color;
        this.size = size;
    }

    private static Font loadFont() {
        try (InputStream in = FlatIcon.class.getResourceAsStream("/fonts/FontAwesome-Solid.ttf")) {
            if (in != null) {
                return Font.createFont(Font.TRUETYPE_FONT, in);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (BASE_FONT == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setColor(color);

        // Ký tự Font Awesome có tỉ lệ rộng/cao khác nhau: thu nhỏ để phần to
        // nhất vừa khung size x size, rồi căn giữa theo đúng đường bao hình vẽ.
        Font font = BASE_FONT.deriveFont((float) size);
        GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), String.valueOf(kind.glyph));
        Rectangle2D b = gv.getVisualBounds();
        if (b.getWidth() > 0 && b.getHeight() > 0) {
            double scale = size * 0.9 / Math.max(b.getWidth(), b.getHeight());
            font = BASE_FONT.deriveFont((float) (size * scale));
            gv = font.createGlyphVector(g2.getFontRenderContext(), String.valueOf(kind.glyph));
            b = gv.getVisualBounds();
        }
        float dx = (float) (x + (size - b.getWidth()) / 2 - b.getX());
        float dy = (float) (y + (size - b.getHeight()) / 2 - b.getY());
        g2.drawGlyphVector(gv, dx, dy);
        g2.dispose();
    }
}
