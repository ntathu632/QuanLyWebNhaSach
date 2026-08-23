/*
 * Icon phẳng, đơn sắc (flat/outline), tự vẽ bằng Java2D — thay cho emoji.
 * Không nhúng font ngoài (kiểu Font Awesome) để tránh rủi ro sai mã Unicode
 * icon cụ thể; hình vẽ đơn giản, đủ nhận diện, style nét mảnh giống outline
 * icon trong các dashboard admin thông dụng (SB Admin 2...).
 */
package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

public final class FlatIcon implements Icon {

    public enum Kind {
        BOOK, RECEIPT, USERS, USER, MONEY, TAG, PEN, BUILDING, CHART, LOGOUT, HOME, PLUS, TRASH, BACK, SEARCH
    }

    private final Kind kind;
    private final Color color;
    private final int size;

    public FlatIcon(Kind kind, Color color, int size) {
        this.kind = kind;
        this.color = color;
        this.size = size;
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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(color);
        float stroke = Math.max(1.4f, size / 11f);
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float s = size;
        switch (kind) {
            case BOOK -> {
                g2.draw(new RoundRectangle2D.Float(s * 0.12f, s * 0.10f, s * 0.76f, s * 0.80f, s * 0.12f, s * 0.12f));
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.40f, s * 0.10f, s * 0.40f, s * 0.90f));
            }
            case RECEIPT -> {
                g2.draw(new RoundRectangle2D.Float(s * 0.18f, s * 0.08f, s * 0.64f, s * 0.84f, s * 0.08f, s * 0.08f));
                for (int i = 0; i < 3; i++) {
                    float ly = s * 0.32f + i * s * 0.18f;
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.30f, ly, s * 0.70f, ly));
                }
            }
            case USERS -> {
                g2.draw(new java.awt.geom.Ellipse2D.Float(s * 0.10f, s * 0.14f, s * 0.34f, s * 0.34f));
                g2.draw(new java.awt.geom.Ellipse2D.Float(s * 0.50f, s * 0.14f, s * 0.34f, s * 0.34f));
                g2.draw(new java.awt.geom.Arc2D.Float(s * 0.02f, s * 0.52f, s * 0.50f, s * 0.46f, 0, 180, java.awt.geom.Arc2D.OPEN));
                g2.draw(new java.awt.geom.Arc2D.Float(s * 0.44f, s * 0.52f, s * 0.50f, s * 0.46f, 0, 180, java.awt.geom.Arc2D.OPEN));
            }
            case USER -> {
                g2.draw(new java.awt.geom.Ellipse2D.Float(s * 0.30f, s * 0.08f, s * 0.40f, s * 0.40f));
                g2.draw(new java.awt.geom.Arc2D.Float(s * 0.08f, s * 0.52f, s * 0.84f, s * 0.46f, 0, 180, java.awt.geom.Arc2D.OPEN));
            }
            case MONEY -> {
                g2.draw(new java.awt.geom.Ellipse2D.Float(s * 0.08f, s * 0.08f, s * 0.84f, s * 0.84f));
                g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, s * 0.42f));
                FontMetrics fm = g2.getFontMetrics();
                String t = "$";
                float tw = fm.stringWidth(t);
                g2.drawString(t, (s - tw) / 2f, s * 0.67f);
            }
            case TAG -> {
                int[] xp = {(int) (s * 0.10f), (int) (s * 0.55f), (int) (s * 0.92f), (int) (s * 0.55f), (int) (s * 0.10f)};
                int[] yp = {(int) (s * 0.30f), (int) (s * 0.08f), (int) (s * 0.50f), (int) (s * 0.92f), (int) (s * 0.70f)};
                g2.drawPolygon(xp, yp, 5);
                g2.fill(new java.awt.geom.Ellipse2D.Float(s * 0.16f, s * 0.42f, s * 0.14f, s * 0.14f));
            }
            case PEN -> {
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.15f, s * 0.85f, s * 0.75f, s * 0.20f));
                int[] xp = {(int) (s * 0.72f), (int) (s * 0.92f), (int) (s * 0.80f)};
                int[] yp = {(int) (s * 0.24f), (int) (s * 0.08f), (int) (s * 0.20f)};
                g2.fillPolygon(xp, yp, 3);
            }
            case BUILDING -> {
                g2.draw(new Rectangle2D.Float(s * 0.14f, s * 0.10f, s * 0.72f, s * 0.80f));
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 2; col++) {
                        float wx = s * 0.26f + col * s * 0.28f;
                        float wy = s * 0.22f + row * s * 0.20f;
                        g2.fill(new Rectangle2D.Float(wx, wy, s * 0.14f, s * 0.12f));
                    }
                }
            }
            case CHART -> {
                g2.fill(new Rectangle2D.Float(s * 0.14f, s * 0.52f, s * 0.18f, s * 0.38f));
                g2.fill(new Rectangle2D.Float(s * 0.42f, s * 0.30f, s * 0.18f, s * 0.60f));
                g2.fill(new Rectangle2D.Float(s * 0.70f, s * 0.12f, s * 0.18f, s * 0.78f));
            }
            case LOGOUT -> {
                g2.draw(new RoundRectangle2D.Float(s * 0.10f, s * 0.10f, s * 0.42f, s * 0.80f, s * 0.08f, s * 0.08f));
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.40f, s * 0.50f, s * 0.92f, s * 0.50f));
                int[] xp = {(int) (s * 0.72f), (int) (s * 0.92f), (int) (s * 0.72f)};
                int[] yp = {(int) (s * 0.34f), (int) (s * 0.50f), (int) (s * 0.66f)};
                g2.fillPolygon(xp, yp, 3);
            }
            case HOME -> {
                int[] xp = {(int) (s * 0.06f), (int) (s * 0.50f), (int) (s * 0.94f)};
                int[] yp = {(int) (s * 0.48f), (int) (s * 0.08f), (int) (s * 0.48f)};
                g2.drawPolyline(xp, yp, 3);
                g2.draw(new Rectangle2D.Float(s * 0.20f, s * 0.46f, s * 0.60f, s * 0.46f));
                g2.draw(new Rectangle2D.Float(s * 0.44f, s * 0.62f, s * 0.14f, s * 0.30f));
            }
            case PLUS -> {
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.50f, s * 0.10f, s * 0.50f, s * 0.90f));
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.10f, s * 0.50f, s * 0.90f, s * 0.50f));
            }
            case BACK -> {
                int[] xp = {(int) (s * 0.64f), (int) (s * 0.28f), (int) (s * 0.64f)};
                int[] yp = {(int) (s * 0.14f), (int) (s * 0.50f), (int) (s * 0.86f)};
                g2.drawPolyline(xp, yp, 3);
            }
            case SEARCH -> {
                g2.draw(new java.awt.geom.Ellipse2D.Float(s * 0.08f, s * 0.08f, s * 0.58f, s * 0.58f));
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.60f, s * 0.60f, s * 0.92f, s * 0.92f));
            }
            case TRASH -> {
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.16f, s * 0.24f, s * 0.84f, s * 0.24f));
                g2.draw(new Rectangle2D.Float(s * 0.24f, s * 0.24f, s * 0.52f, s * 0.66f));
                g2.draw(new Rectangle2D.Float(s * 0.38f, s * 0.10f, s * 0.24f, s * 0.14f));
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.40f, s * 0.36f, s * 0.40f, s * 0.76f));
                g2.draw(new java.awt.geom.Line2D.Float(s * 0.60f, s * 0.36f, s * 0.60f, s * 0.76f));
            }
        }
        g2.dispose();
    }
}
