/*
 * "Bìa sách" vẽ tay bằng Java2D — dùng thay ảnh bìa thật. Cột "image" trong
 * DB chỉ là tên file (vd "clean-code.jpg") không có file ảnh thật đi kèm dự
 * án, nên không thể hiển thị ảnh thật; thay vào đó vẽ 1 khối gradient bo góc
 * theo mã sách (để mỗi cuốn có màu ổn định, khác nhau) + icon sách lớn ở
 * giữa, giống cách app đã dùng FlatIcon/biểu đồ tự vẽ ở phần Admin.
 */
package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

public class BookCoverPanel extends JComponent {

    private final Color base;

    public BookCoverPanel(String title, int seed) {
        this.base = AppTheme.CHART_PALETTE[Math.floorMod(seed, AppTheme.CHART_PALETTE.length)];
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color light = blend(base, Color.WHITE, 0.18f);
        Color dark = blend(base, Color.BLACK, 0.22f);
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, w, h, 12, 12);
        g2.setPaint(new GradientPaint(0, 0, light, w, h, dark));
        g2.fill(shape);

        // Vài dải chéo mờ trang trí, giống hiệu ứng "gáy sách" cách điệu.
        g2.setColor(new Color(255, 255, 255, 26));
        for (int x = -h; x < w; x += 26) {
            g2.drawLine(x, h, x + h, 0);
        }

        g2.setClip(shape);
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRect(0, h - Math.max(28, h / 4), w, Math.max(28, h / 4));
        g2.setClip(null);

        int iconSize = Math.min(w, h) / 2;
        new FlatIcon(FlatIcon.Kind.BOOK, new Color(255, 255, 255, 235), iconSize)
                .paintIcon(this, g2, (w - iconSize) / 2, (h - iconSize) / 2 - 4);

        g2.dispose();
    }

    private static Color blend(Color a, Color b, float ratio) {
        int r = (int) (a.getRed() * (1 - ratio) + b.getRed() * ratio);
        int gg = (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio);
        int bl = (int) (a.getBlue() * (1 - ratio) + b.getBlue() * ratio);
        return new Color(r, gg, bl);
    }
}
