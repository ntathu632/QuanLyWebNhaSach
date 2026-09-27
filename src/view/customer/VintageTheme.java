/*
 * Bảng màu "Vintage Books" cho khu vực Khách hàng (khung CustomerShellFrm +
 * Trang chủ): nền kem ấm, màu nhấn nâu gỗ, thẻ trắng ngà viền mảnh bo góc.
 * Font vẫn giữ theo AppTheme (Cormorant Garamond cho tiêu đề, Raleway cho
 * phần còn lại) — ở đây chỉ gom màu + vài style dùng lại nhiều lần.
 */
package view.customer;

import com.formdev.flatlaf.ui.FlatLineBorder;
import java.awt.Color;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;
import view.shared.FlatIcon;

public final class VintageTheme {

    public static final Color PAGE_BG = new Color(0xF7, 0xF2, 0xEA);
    public static final Color SIDEBAR_BG = new Color(0xF1, 0xE8, 0xDC);
    public static final Color CARD_BG = new Color(0xFC, 0xF9, 0xF4);
    public static final Color CARD_BORDER = new Color(0xEA, 0xDF, 0xD1);
    public static final Color LINE = new Color(0xE3, 0xD8, 0xCA);
    public static final Color BROWN = new Color(0x7A, 0x4A, 0x33);
    public static final Color BROWN_HOVER = new Color(0x69, 0x3E, 0x2A);
    public static final Color BROWN_PRESSED = new Color(0x58, 0x33, 0x22);
    public static final Color BROWN_TEXT = new Color(0x8A, 0x57, 0x3B);
    public static final Color TEXT = new Color(0x3B, 0x2A, 0x20);
    public static final Color MUTED = new Color(0x8C, 0x7B, 0x6D);
    public static final Color NAV_HOVER = new Color(0xE8, 0xDC, 0xCD);
    public static final Color CIRCLE_BG = new Color(0xF2, 0xE7, 0xDB);
    public static final Color PLACEHOLDER = new Color(0xEF, 0xE5, 0xD8);
    public static final Color STAR = new Color(0xE0, 0xA5, 0x26);

    private VintageTheme() {
    }

    /** Nút nâu đặc (Đăng ký, tìm kiếm, thêm vào giỏ). */
    public static void brownButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(BROWN) + "; foreground: #FFFFFF;"
                + " hoverBackground: #" + hex(BROWN_HOVER) + ";"
                + " pressedBackground: #" + hex(BROWN_PRESSED) + ";"
                + " focusedBackground: #" + hex(BROWN) + ";"
                + " borderWidth: 0; arc: 10");
    }

    /** Nút viền (Đăng nhập): nền trắng ngà, viền mảnh, chữ nâu đậm. */
    public static void outlineButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(CARD_BG) + "; foreground: #" + hex(TEXT) + ";"
                + " hoverBackground: #" + hex(NAV_HOVER) + ";"
                + " pressedBackground: #" + hex(CARD_BORDER) + ";"
                + " focusedBackground: #" + hex(CARD_BG) + ";"
                + " borderColor: #" + hex(CARD_BORDER) + "; borderWidth: 1; arc: 10");
    }

    /** Nút chỉ icon/chữ, nền trong suốt theo nền trang (Giỏ hàng, các icon tài khoản). */
    public static void flatButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(PAGE_BG) + "; foreground: #" + hex(TEXT) + ";"
                + " hoverBackground: #" + hex(NAV_HOVER) + ";"
                + " pressedBackground: #" + hex(CARD_BORDER) + ";"
                + " focusedBackground: #" + hex(PAGE_BG) + ";"
                + " borderWidth: 0; arc: 10");
    }

    /** Mục điều hướng ở thanh bên: đang chọn thì nền nâu chữ trắng, còn lại nền trong suốt. */
    public static void sidebarNav(JButton btn, boolean active) {
        Color bg = active ? BROWN : SIDEBAR_BG;
        Color fg = active ? Color.WHITE : TEXT;
        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(bg) + "; foreground: #" + hex(fg) + ";"
                + " hoverBackground: #" + hex(active ? BROWN_HOVER : NAV_HOVER) + ";"
                + " pressedBackground: #" + hex(active ? BROWN_PRESSED : CARD_BORDER) + ";"
                + " focusedBackground: #" + hex(bg) + ";"
                + " borderWidth: 0; arc: 10; margin: 10,16,10,12");
    }

    /** Khung thẻ trắng ngà, viền mảnh bo góc "arc". */
    public static JPanel card(int arc, int padding) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.putClientProperty("FlatLaf.style", "arc: " + arc);
        card.setBorder(new FlatLineBorder(new Insets(padding, padding, padding, padding), CARD_BORDER, 1f, arc));
        return card;
    }

    /** Icon theo tên danh mục (so khớp từ khóa), mặc định là quyển sách mở. */
    public static FlatIcon.Kind categoryIcon(String name) {
        String n = name == null ? "" : name.toLowerCase();
        if (n.contains("công nghệ") || n.contains("tin học") || n.contains("lập trình")) {
            return FlatIcon.Kind.LAPTOP;
        }
        if (n.contains("kinh doanh") || n.contains("kinh tế")) {
            return FlatIcon.Kind.CHART;
        }
        if (n.contains("phát triển") || n.contains("kỹ năng")) {
            return FlatIcon.Kind.LEAF;
        }
        if (n.contains("thiếu nhi") || n.contains("trẻ em")) {
            return FlatIcon.Kind.SMILE;
        }
        if (n.contains("khoa học")) {
            return FlatIcon.Kind.FLASK;
        }
        if (n.contains("tâm lý")) {
            return FlatIcon.Kind.BRAIN;
        }
        if (n.contains("trinh thám")) {
            return FlatIcon.Kind.SEARCH;
        }
        return FlatIcon.Kind.OPEN_BOOK;
    }

    /** Icon nâu đặt giữa 1 hình tròn nền kem đậm — dùng cho ô danh mục ở Trang chủ. */
    public static javax.swing.Icon circled(FlatIcon.Kind kind, int diameter, int iconSize) {
        FlatIcon inner = new FlatIcon(kind, BROWN, iconSize);
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CIRCLE_BG);
                g2.fillOval(x, y, diameter, diameter);
                g2.dispose();
                int offset = (diameter - iconSize) / 2;
                inner.paintIcon(c, g, x + offset, y + offset);
            }

            @Override
            public int getIconWidth() {
                return diameter;
            }

            @Override
            public int getIconHeight() {
                return diameter;
            }
        };
    }

    public static String hex(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
