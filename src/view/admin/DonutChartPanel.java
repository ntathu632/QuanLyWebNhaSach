/*
 * Biểu đồ tròn dạng donut (vành khuyên) — vẽ tay bằng Java2D, kèm chú giải
 * (legend) màu + nhãn + số lượng bên cạnh.
 */
package view.admin;
import view.shared.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DonutChartPanel extends javax.swing.JPanel {

    // Dữ liệu nạp trước initComponents() vì vành khuyên (ring) được tạo ngay
    // trong initComponents() bằng custom creation code "new Ring(items, total)".
    private final List<dao.DashboardDAO.LabelCount> items;
    private final float total;

    public DonutChartPanel(List<dao.DashboardDAO.LabelCount> items) {
        float total = 0f;
        for (dao.DashboardDAO.LabelCount it : items) {
            total += it.count;
        }
        this.items = items;
        this.total = total;

        initComponents();
        fillLegend(items, total);
    }

    private void fillLegend(List<dao.DashboardDAO.LabelCount> items, float total) {
        for (int i = 0; i < items.size(); i++) {
            dao.DashboardDAO.LabelCount it = items.get(i);
            Color color = AppTheme.CHART_PALETTE[i % AppTheme.CHART_PALETTE.length];
            int percent = total > 0 ? Math.round(it.count * 100 / total) : 0;

            JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 3));
            row.setOpaque(false);

            JLabel dot = new JLabel("●");
            dot.setForeground(color);
            row.add(dot);

            JLabel text = new JLabel(" " + it.label + " (" + percent + "%)");
            text.setForeground(ChartStyle.TEXT);
            text.setFont(text.getFont().deriveFont(12f));
            row.add(text);

            legend.add(row);
        }
    }

    /** Phần vành khuyên tự vẽ. */
    private static class Ring extends JPanel {

        private final List<dao.DashboardDAO.LabelCount> items;
        private final float total;

        Ring(List<dao.DashboardDAO.LabelCount> items, float total) {
            this.items = items;
            this.total = total;
            setPreferredSize(new Dimension(140, 140));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight()) - 8;
            int x = (getWidth() - side) / 2;
            int y = (getHeight() - side) / 2;

            if (items == null || items.isEmpty() || total <= 0f) {
                g2.setColor(ChartStyle.MUTED);
                g2.setFont(getFont().deriveFont(12f));
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Chưa có dữ liệu";
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }

            float strokeWidth = side * 0.22f;
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

            Arc2D.Float bounds = new Arc2D.Float(x + strokeWidth / 2, y + strokeWidth / 2,
                    side - strokeWidth, side - strokeWidth, 0, 0, Arc2D.OPEN);

            float startAngle = 90f;
            for (int i = 0; i < items.size(); i++) {
                float sweep = -(items.get(i).count / total) * 360f;
                bounds.setAngleStart(startAngle);
                bounds.setAngleExtent(sweep);
                g2.setColor(AppTheme.CHART_PALETTE[i % AppTheme.CHART_PALETTE.length]);
                g2.draw(bounds);
                startAngle += sweep;
            }

            // Vòng trắng ở giữa để tạo hiệu ứng "donut" (rỗng ở tâm)
            g2.setColor(java.awt.Color.WHITE);
            float holeSize = side - strokeWidth * 2 - 2;
            g2.fill(new Ellipse2D.Float(x + strokeWidth + 1, y + strokeWidth + 1, holeSize, holeSize));

            g2.setColor(ChartStyle.TEXT);
            g2.setFont(getFont().deriveFont(java.awt.Font.BOLD, side * 0.14f));
            String totalText = ChartStyle.compact(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalText, getWidth() / 2f - fm.stringWidth(totalText) / 2f, getHeight() / 2f + fm.getAscent() / 2f - 3);

            g2.dispose();
        }
    }

    public static javax.swing.JPanel card(String title, List<dao.DashboardDAO.LabelCount> items, Color accent, int height) {
        return ChartStyle.cardWrapper(title, accent, new DonutChartPanel(items), height);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ring = new Ring(items, total);
        legend = new javax.swing.JPanel();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout(14, 0));

        ring.setLayout(new java.awt.FlowLayout());
        add(ring, java.awt.BorderLayout.CENTER);

        legend.setOpaque(false);
        legend.setLayout(new javax.swing.BoxLayout(legend, javax.swing.BoxLayout.Y_AXIS));
        add(legend, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel legend;
    private javax.swing.JPanel ring;
    // End of variables declaration//GEN-END:variables
}
