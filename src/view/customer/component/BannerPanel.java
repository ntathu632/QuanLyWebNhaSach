/*
 * Banner Trang chủ: vẽ ảnh images/banner/home-banner.png (hoặc .jpg) ở thư
 * mục làm việc của app, phủ kín khung và bo góc (giống CSS object-fit:
 * cover). Chưa có file ảnh thì để trống — chỉ tô nền placeholder bo góc.
 *
 * Co giãn ảnh chất lượng cao rất tốn CPU, nên chỉ làm 1 lần cho mỗi kích
 * thước khung rồi lưu lại (cache); các lần vẽ sau (cuộn trang, rê chuột...)
 * chỉ chép nguyên ảnh đã co giãn ra màn hình.
 */
package view.customer.component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import view.customer.VintageTheme;

public class BannerPanel extends JPanel {

    private static final String[] FILES = {"images/banner/home-banner.png", "images/banner/home-banner.jpg"};
    private static final int ARC = 18;

    private final Image image;

    /** Banner đã vẽ sẵn theo đúng số điểm ảnh thật của màn hình (tính cả tỉ lệ HiDPI). */
    private BufferedImage cache;

    public BannerPanel() {
        setOpaque(false);
        image = loadImage();
    }

    private static Image loadImage() {
        for (String path : FILES) {
            File file = new File(path.replace('/', File.separatorChar));
            if (file.isFile()) {
                try {
                    return ImageIO.read(file);
                } catch (Exception ex) {
                    // Ảnh hỏng -> thử file kế tiếp, cuối cùng để trống.
                }
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AffineTransform at = g2.getTransform();
        double sx = at.getScaleX();
        double sy = at.getScaleY();
        int pw = (int) Math.ceil(getWidth() * sx);
        int ph = (int) Math.ceil(getHeight() * sy);
        if (pw <= 0 || ph <= 0) {
            g2.dispose();
            return;
        }
        if (cache == null || cache.getWidth() != pw || cache.getHeight() != ph) {
            cache = render(pw, ph, sx, sy);
        }
        // Chép thẳng theo điểm ảnh thật (bỏ phép co giãn HiDPI) -> không phải co giãn lại mỗi lần vẽ.
        g2.setTransform(new AffineTransform(1, 0, 0, 1, at.getTranslateX(), at.getTranslateY()));
        g2.drawImage(cache, 0, 0, null);
        g2.dispose();
    }

    private BufferedImage render(int pw, int ph, double sx, double sy) {
        BufferedImage img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.scale(sx, sy);
        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, ARC, ARC);

        g2.setColor(VintageTheme.PLACEHOLDER);
        g2.fill(shape);
        if (image != null) {
            int iw = image.getWidth(null);
            int ih = image.getHeight(null);
            if (iw > 0 && ih > 0) {
                double scale = Math.max((double) w / iw, (double) h / ih);
                int dw = (int) Math.ceil(iw * scale);
                int dh = (int) Math.ceil(ih * scale);
                g2.setClip(shape);
                g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                g2.setClip(null);
            }
        }
        g2.setColor(VintageTheme.CARD_BORDER);
        g2.draw(shape);
        g2.dispose();
        return img;
    }
}
