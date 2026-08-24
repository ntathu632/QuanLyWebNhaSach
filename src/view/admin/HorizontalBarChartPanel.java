/*
 * Biểu đồ cột ngang — vẽ tay bằng Java2D, phù hợp khi nhãn (label) dài hoặc
 * số lượng mục ít, dễ đọc hơn cột đứng trong trường hợp đó.
 */
package view.admin;
import view.shared.*;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.JComponent;

public class HorizontalBarChartPanel extends JComponent {

    private final List<dao.DashboardDAO.LabelCount> items;

    public HorizontalBarChartPanel(List<dao.DashboardDAO.LabelCount> items) {
        this.items = items;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (items == null || items.isEmpty()) {
            g2.setColor(ChartStyle.MUTED);
            g2.setFont(getFont().deriveFont(13f));
            String msg = "Chưa có dữ liệu để vẽ biểu đồ.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        g2.setFont(getFont().deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();

        int labelW = 0;
        for (dao.DashboardDAO.LabelCount it : items) {
            labelW = Math.max(labelW, fm.stringWidth(it.label));
        }
        labelW += 10;

        float max = 0f;
        for (dao.DashboardDAO.LabelCount it : items) {
            max = Math.max(max, it.count);
        }
        if (max <= 0f) {
            max = 1f;
        }

        int n = items.size();
        float rowH = (float) h / n;
        float barH = Math.min(rowH * 0.5f, 22f);
        int trackW = w - labelW - 44;

        for (int i = 0; i < n; i++) {
            dao.DashboardDAO.LabelCount it = items.get(i);
            float cy = rowH * i + rowH / 2f;
            Color color = AppTheme.CHART_PALETTE[i % AppTheme.CHART_PALETTE.length];

            g2.setColor(ChartStyle.TEXT);
            int lw = fm.stringWidth(it.label);
            g2.drawString(it.label, labelW - lw - 10, cy + fm.getAscent() / 2f - 2);

            float barW = Math.max(4f, (it.count / max) * trackW);
            RoundRectangle2D.Float bar = new RoundRectangle2D.Float(labelW, cy - barH / 2f, barW, barH, 8, 8);
            g2.setPaint(new GradientPaint(labelW, 0, ChartStyle.lighten(color, 0.15f), labelW + barW, 0, color));
            g2.fill(bar);

            g2.setColor(ChartStyle.TEXT);
            String valueText = ChartStyle.compact(it.count);
            g2.drawString(valueText, labelW + barW + 8, cy + fm.getAscent() / 2f - 2);
        }

        g2.dispose();
    }

    public static javax.swing.JPanel card(String title, List<dao.DashboardDAO.LabelCount> items, Color accent, int height) {
        return ChartStyle.cardWrapper(title, accent, new HorizontalBarChartPanel(items), height);
    }
}
