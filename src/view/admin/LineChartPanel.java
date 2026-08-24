/*
 * Biểu đồ đường (line/area chart) — vẽ tay bằng Java2D, có vùng tô gradient
 * mờ dần bên dưới đường và chấm tròn ở từng điểm dữ liệu.
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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;
import javax.swing.JComponent;

public class LineChartPanel extends JComponent {

    private final List<BarChartPanel.Item> items;
    private final Color lineColor;

    public LineChartPanel(List<BarChartPanel.Item> items, Color lineColor) {
        this.items = items;
        this.lineColor = lineColor;
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

        if (items == null || items.size() < 2) {
            g2.setColor(ChartStyle.MUTED);
            g2.setFont(getFont().deriveFont(13f));
            String msg = (items == null || items.isEmpty())
                    ? "Chưa có dữ liệu để vẽ biểu đồ."
                    : "Cần ít nhất 2 điểm dữ liệu để vẽ đường.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        float max = 0f;
        for (BarChartPanel.Item it : items) {
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
        float stepX = (float) chartW / (n - 1);
        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padLeft + stepX * i;
            ys[i] = padTop + chartH - (items.get(i).value / max) * (chartH - 8);
        }

        // Vùng tô mờ dần dưới đường
        Path2D.Float area = new Path2D.Float();
        area.moveTo(xs[0], padTop + chartH);
        for (int i = 0; i < n; i++) {
            area.lineTo(xs[i], ys[i]);
        }
        area.lineTo(xs[n - 1], padTop + chartH);
        area.closePath();
        g2.setPaint(new GradientPaint(0, padTop, ChartStyle.lighten(lineColor, 0.55f),
                0, padTop + chartH, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 20)));
        g2.fill(area);

        // Đường nối các điểm
        Path2D.Float line = new Path2D.Float();
        line.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            line.lineTo(xs[i], ys[i]);
        }
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(line);

        // Chấm tròn tại từng điểm + nhãn trục x
        g2.setFont(getFont().deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < n; i++) {
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Float(xs[i] - 4, ys[i] - 4, 8, 8));
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Float(xs[i] - 4, ys[i] - 4, 8, 8));

            g2.setColor(ChartStyle.MUTED);
            String lbl = items.get(i).label;
            int lw = fm.stringWidth(lbl);
            g2.drawString(lbl, xs[i] - lw / 2f, padTop + chartH + 18);
        }

        g2.dispose();
    }

    public static javax.swing.JPanel card(String title, List<BarChartPanel.Item> items, Color lineColor, int height) {
        return ChartStyle.cardWrapper(title, lineColor, new LineChartPanel(items, lineColor), height);
    }
}
