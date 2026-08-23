/*
 * Thẻ trắng bo góc + đổ bóng mềm (vẽ tay bằng nhiều lớp hình chữ nhật bo góc
 * mờ dần, do Swing không có box-shadow sẵn) — thay cho các thẻ viền phẳng
 * (BorderFactory.createLineBorder) trước đó ở phần Khách hàng, để có chiều
 * sâu/nổi khối giống các trang bán hàng thật (Fahasa, Tiki, Amazon...) thay
 * vì phẳng lì.
 */
package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

class ShadowCard extends JPanel {

    private static final int SHADOW = 8;
    private final int arc;

    ShadowCard(LayoutManager layout, int arc, int padding) {
        super(layout);
        this.arc = arc;
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding + SHADOW, padding + SHADOW));
    }

    ShadowCard(LayoutManager layout) {
        this(layout, 14, 16);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - SHADOW;
        int h = getHeight() - SHADOW;

        for (int i = SHADOW; i > 0; i--) {
            int alpha = (int) (3.2f * (SHADOW - i + 1));
            g2.setColor(new Color(0x3A, 0x33, 0x28, Math.min(alpha, 26)));
            g2.fill(new RoundRectangle2D.Float(i * 0.5f, i * 0.6f + 2, w, h, arc, arc));
        }

        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        g2.dispose();

        super.paintComponent(g);
    }
}
