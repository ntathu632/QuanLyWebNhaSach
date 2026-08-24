/*
 * Tiện ích style dùng chung cho các panel biểu đồ tự vẽ (BarChartPanel,
 * LineChartPanel, DonutChartPanel, HorizontalBarChartPanel) — gộp lại để
 * không lặp code màu sắc / khung thẻ / định dạng số ở từng file.
 */
package view.admin;
import view.shared.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Stroke;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

final class ChartStyle {

    static final Color GRID = AppTheme.CHART_GRID;
    static final Color TEXT = AppTheme.TEXT;
    static final Color MUTED = AppTheme.CHART_MUTED_TEXT;

    private ChartStyle() {
    }

    static Stroke dashedStroke() {
        return new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4f, 4f}, 0);
    }

    static Color lighten(Color c, float amount) {
        int r = (int) Math.min(255, c.getRed() + (255 - c.getRed()) * amount);
        int g = (int) Math.min(255, c.getGreen() + (255 - c.getGreen()) * amount);
        int b = (int) Math.min(255, c.getBlue() + (255 - c.getBlue()) * amount);
        return new Color(r, g, b);
    }

    /** Rút gọn số cho gọn trên biểu đồ: 1200000 -> "1.2tr", 850 -> "850". */
    static String compact(float value) {
        if (value >= 1_000_000f) {
            return String.format("%.1ftr", value / 1_000_000f);
        }
        if (value >= 1_000f) {
            return String.format("%.0fk", value / 1_000f);
        }
        if (value == Math.floor(value)) {
            return String.format("%.0f", value);
        }
        return String.format("%.1f", value);
    }

    /** Khung thẻ trắng bo góc + viền màu bên trái + tiêu đề, giống statCard — dùng chung cho mọi loại biểu đồ. */
    static JPanel cardWrapper(String title, Color accent, Component chart, int height) {
        JPanel wrapper = new JPanel(new java.awt.BorderLayout(0, 10));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 16)));
        wrapper.setPreferredSize(new java.awt.Dimension(0, height));
        wrapper.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, height));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(TEXT);
        wrapper.add(titleLabel, java.awt.BorderLayout.NORTH);
        wrapper.add(chart, java.awt.BorderLayout.CENTER);
        return wrapper;
    }
}
