/*
 * Bìa sách — vẽ ảnh thật nếu Book.image trỏ tới 1 file có thật trong
 * images/books/ (xem BookImageStore); nếu không (sách seed cũ chỉ có tên
 * file giả như "clean-code.jpg", không có file thật) thì vẽ tay bằng
 * Java2D thay thế: khối gradient bo góc theo mã sách (để mỗi cuốn có màu
 * ổn định, khác nhau) + icon sách lớn ở giữa, giống cách app đã dùng
 * FlatIcon/biểu đồ tự vẽ ở phần Admin.
 */
package view.shared;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class BookCoverPanel extends JComponent {

    private final Color base;
    private final Image realImage;

    public BookCoverPanel(String title, int seed, String imageFilename) {
        this.base = AppTheme.CHART_PALETTE[Math.floorMod(seed, AppTheme.CHART_PALETTE.length)];
        this.realImage = loadImage(imageFilename);
        setOpaque(false);
    }

    private static Image loadImage(String filename) {
        try {
            File file = BookImageStore.resolve(filename);
            if (file != null && file.isFile()) {
                return ImageIO.read(file);
            }
        } catch (Exception ex) {
            // Ảnh hỏng/không đọc được -> rơi về bìa vẽ tay bên dưới, không cần báo lỗi.
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, w, h, 12, 12);

        if (realImage != null) {
            g2.setClip(shape);
            drawCoverFit(g2, realImage, w, h);
            g2.setClip(null);
            g2.dispose();
            return;
        }

        Color light = blend(base, Color.WHITE, 0.18f);
        Color dark = blend(base, Color.BLACK, 0.22f);
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

    /** Vẽ ảnh phủ kín w x h, giữ tỉ lệ gốc, cắt phần thừa (giống CSS object-fit: cover). */
    private static void drawCoverFit(Graphics2D g2, Image img, int w, int h) {
        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) {
            return;
        }
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) Math.ceil(iw * scale);
        int dh = (int) Math.ceil(ih * scale);
        int dx = (w - dw) / 2;
        int dy = (h - dh) / 2;
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    private static Color blend(Color a, Color b, float ratio) {
        int r = (int) (a.getRed() * (1 - ratio) + b.getRed() * ratio);
        int gg = (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio);
        int bl = (int) (a.getBlue() * (1 - ratio) + b.getBlue() * ratio);
        return new Color(r, gg, bl);
    }
}
