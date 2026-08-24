/*
 * Biểu đồ cột đứng, tự vẽ bằng Java2D — không dùng thư viện ngoài (dự án
 * chưa có thư viện vẽ biểu đồ, thêm 1 thư viện lớn chỉ để vẽ vài biểu đồ đơn
 * giản là không cần thiết cho phạm vi đồ án môn học).
 */
package view.admin;
import view.shared.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.JComponent;

public class BarChartPanel extends JComponent {

    private final List<Item> items;
    private final Color barColor;

    public static class Item {

        public final String label;
        public final float value;

        public Item(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }

    public BarChartPanel(List<Item> items, Color barColor) {
        this.items = items;
        this.barColor = barColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padLeft = 8;
        int padRight = 8;
        int padTop = 22;
        int padBottom = 26;
        int chartW = w - padLeft - padRight;
        int chartH = h - padTop - padBottom;

        if (items == null || items.isEmpty()) {
            paintEmpty(g2, w, h);
            g2.dispose();
            return;
        }

        float max = 0f;
        for (Item it : items) {
            max = Math.max(max, it.value);
        }
        if (max <= 0f) {
            max = 1f;
        }

        g2.setColor(ChartStyle.GRID);
        g2.setStroke(ChartStyle.dashedStroke());
        for (int i = 1; i <= 3; i++) {
            int gy = padTop + chartH * i / 4;
            g2.drawLine(padLeft, gy, padLeft + chartW, gy);
        }

        int n = items.size();
        float slot = (float) chartW / n;
        float barW = Math.min(slot * 0.5f, 44f);

        g2.setFont(getFont().deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();

        Color top = ChartStyle.lighten(barColor, 0.22f);

        for (int i = 0; i < n; i++) {
            Item it = items.get(i);
            float barH = Math.max(3f, (it.value / max) * (chartH - 8));
            float cx = padLeft + slot * i + slot / 2f;
            float bx = cx - barW / 2f;
            float by = padTop + chartH - barH;

            RoundRectangle2D.Float bar = new RoundRectangle2D.Float(bx, by, barW, barH, 7, 7);
            g2.setPaint(new GradientPaint(bx, by, top, bx, by + barH, barColor));
            g2.fill(bar);

            String valueText = ChartStyle.compact(it.value);
            g2.setColor(ChartStyle.TEXT);
            int vw = fm.stringWidth(valueText);
            g2.drawString(valueText, cx - vw / 2f, by - 6);

            g2.setColor(ChartStyle.MUTED);
            int lw = fm.stringWidth(it.label);
            g2.drawString(it.label, cx - lw / 2f, padTop + chartH + 18);
        }

        g2.dispose();
    }

    private void paintEmpty(Graphics2D g2, int w, int h) {
        g2.setColor(ChartStyle.MUTED);
        g2.setFont(getFont().deriveFont(13f));
        String msg = "Chưa có dữ liệu để vẽ biểu đồ.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
    }

    /** Bọc sẵn 1 tiêu đề nhỏ + biểu đồ trong 1 panel trắng bo góc, đồng bộ style với statCard. */
    public static javax.swing.JPanel card(String title, List<Item> items, Color barColor, int height) {
        return ChartStyle.cardWrapper(title, barColor, new BarChartPanel(items, barColor), height);
    }
}
