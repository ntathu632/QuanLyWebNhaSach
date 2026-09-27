/*
 * Theme dùng chung cho toàn bộ ứng dụng, theo đúng phong cách trong
 * tông "Vintage Books" của Trang chủ khách hàng (nền trắng, thanh bên và
 * title bar màu kem, mọi nút bấm cùng màu nâu gỗ). Gọi AppTheme.install() một lần duy nhất ở đầu mỗi main(),
 * trước khi tạo bất kỳ component Swing nào.
 */
package view.shared;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public final class AppTheme {

    // Tông "Vintage Books" dùng chung toàn app (giống Trang chủ khách hàng):
    // nội dung nền trắng, thanh bên/title bar màu kem, màu nhấn nâu gỗ.
    public static final Color BACKGROUND = Color.WHITE;
    /** Màu thương hiệu (nâu đậm) — cho logo/tên app, KHÔNG dùng làm nền có chữ trắng nhạt. */
    public static final Color HEADER = new Color(0x7A, 0x4A, 0x33);
    public static final Color SIDEBAR_BG = new Color(0xF1, 0xE8, 0xDC);
    public static final Color NAV_HOVER = new Color(0xE8, 0xDC, 0xCD);
    public static final Color LINE = new Color(0xE3, 0xD8, 0xCA);
    public static final Color PRIMARY = new Color(0x7A, 0x4A, 0x33);
    public static final Color PRIMARY_HOVER = new Color(0x69, 0x3E, 0x2A);
    public static final Color PRIMARY_PRESSED = new Color(0x58, 0x33, 0x22);
    public static final Color SECONDARY = new Color(0x7A, 0x4A, 0x33);
    public static final Color SECONDARY_HOVER = new Color(0x69, 0x3E, 0x2A);
    public static final Color SECONDARY_PRESSED = new Color(0x58, 0x33, 0x22);
    public static final Color TABLE_HEADER = new Color(0xF1, 0xE8, 0xDC);
    public static final Color TABLE_GRID = new Color(0xEA, 0xDF, 0xD1);
    public static final Color ACCENT_TEXT = new Color(0x8A, 0x57, 0x3B);
    public static final Color TEXT = new Color(0x3B, 0x2A, 0x20);
    public static final Color MUTED_TEXT = new Color(0x8C, 0x7B, 0x6D);
    public static final Color INPUT_BORDER = new Color(0xDC, 0xCF, 0xBF);
    /** Chữ/icon mục thanh bên chưa chọn (thanh bên nền kem nên là màu chữ đậm). */
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x3B, 0x2A, 0x20);
    public static final int SIDEBAR_WIDTH = 240;
    public static final int TOPBAR_HEIGHT = 56;

    // Bảng màu RIÊNG cho biểu đồ và bìa sách vẽ tay — các tông đất trầm (hồng
    // phấn, cam đất, xanh xám, vàng đồng, tím khói, nâu đỏ) để hợp tông vintage
    // nhưng vẫn đủ khác nhau giữa các cột/lát biểu đồ.
    public static final Color CHART_PINK = new Color(0xB5, 0x83, 0x8D);
    public static final Color CHART_ORANGE = new Color(0xC9, 0x8B, 0x5B);
    public static final Color CHART_GOLD = new Color(0xC9, 0xA1, 0x5A);
    public static final Color CHART_SKY = new Color(0x6B, 0x8F, 0xA3);
    public static final Color CHART_PURPLE = new Color(0x8C, 0x6A, 0x8D);
    public static final Color CHART_RED = new Color(0xA0, 0x66, 0x4A);
    public static final Color[] CHART_PALETTE = {CHART_PINK, CHART_ORANGE, CHART_SKY, CHART_GOLD, CHART_PURPLE, CHART_RED};
    public static final Color CHART_GRID = new Color(0xEA, 0xDF, 0xD1);
    public static final Color CHART_MUTED_TEXT = new Color(0x8C, 0x7B, 0x6D);

    // Typography: Cormorant Garamond lo phần cảm xúc/thẩm mỹ (logo, tiêu đề
    // lớn, heading, tiêu đề section — Bold; chữ cần nhấn mạnh — Medium
    // Italic), Raleway lo phần chức năng/thông tin (menu, nội dung, mô tả,
    // button, input, giá tiền... — Medium). File .ttf đóng gói trong
    // src/fonts/ (giấy phép SIL OFL) và được nạp trong install(), nên máy
    // chạy app không cần cài font. Trong Form Editor chọn đúng tên dưới đây.
    public static final String FONT_HEADING = "Cormorant Garamond";
    public static final String FONT_EMPHASIS = "Cormorant Garamond Medium Italic";
    public static final String FONT_UI = "Raleway Medium";
    private static final String[] FONT_FILES = {
        "CormorantGaramond-Bold.ttf", "CormorantGaramond-MediumItalic.ttf", "Raleway-Medium.ttf"
    };

    private AppTheme() {
    }

    /** Font tiêu đề (Cormorant Garamond Bold) — cho tiêu đề dựng bằng code, vd. trong hộp thoại. */
    public static Font headingFont(float size) {
        return new Font(FONT_HEADING, Font.BOLD, Math.round(size));
    }

    private static void registerFonts() {
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String file : FONT_FILES) {
            try (java.io.InputStream in = AppTheme.class.getResourceAsStream("/fonts/" + file)) {
                if (in != null) {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, in));
                }
            } catch (Exception ex) {
                // Thiếu/hỏng file font -> Swing tự rơi về font mặc định, app vẫn chạy.
                ex.printStackTrace();
            }
        }
    }

    /**
     * Cài đặt FlatLaf + theme màu riêng. Gọi 1 lần duy nhất, trước khi
     * tạo component Swing nào (đầu mỗi main()).
     */
    public static void install() {
        FlatLightLaf.setup();
        registerFonts();

        // Font mặc định toàn app = Raleway Medium, lớn hơn cỡ mặc định của
        // FlatLaf 2pt (FlatLaf khá nhỏ, ~13px) — mọi component (label, field,
        // button, table...) đều lấy theo "defaultFont" này nên chỉ cần đặt 1 chỗ.
        Font base = UIManager.getFont("defaultFont");
        if (base != null) {
            UIManager.put("defaultFont", new javax.swing.plaf.FontUIResource(
                    FONT_UI, Font.PLAIN, Math.round(base.getSize2D() + 2f)));
        }

        // Từ FlatLaf 2.0+, "unified background" bật mặc định khiến title bar tự lấy
        // màu nền chung (Panel.background), ghi đè TitlePane.background — phải tắt
        // trước thì mới đặt được màu title bar riêng khác màu nền form.
        UIManager.put("TitlePane.unifiedBackground", false);
        UIManager.put("TitlePane.background", SIDEBAR_BG);
        UIManager.put("TitlePane.foreground", TEXT);
        UIManager.put("TitlePane.inactiveBackground", SIDEBAR_BG);
        UIManager.put("TitlePane.inactiveForeground", MUTED_TEXT);
        UIManager.put("TitlePane.buttonHoverBackground", NAV_HOVER);
        UIManager.put("TitlePane.buttonPressedBackground", TABLE_GRID);

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("RootPane.background", BACKGROUND);

        UIManager.put("Component.borderColor", INPUT_BORDER);
        UIManager.put("Component.focusColor", ACCENT_TEXT);
        UIManager.put("Component.focusedBorderColor", ACCENT_TEXT);
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 6);

        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.gridColor", TABLE_GRID);
        UIManager.put("Table.selectionBackground", new Color(0xF2, 0xE7, 0xDB));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", TABLE_HEADER);
        UIManager.put("TableHeader.foreground", TEXT);

        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", new Color(0xF2, 0xE7, 0xDB));
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", new Color(0xF2, 0xE7, 0xDB));
        UIManager.put("List.selectionForeground", TEXT);
    }

    /**
     * Áp style nút chính (nâu gỗ) — dùng cho hành động xác nhận: Login, Save,
     * Confirm, Pay... Đặt cả 2 bộ key ("background..." và "default.background...")
     * vì nếu nút này là default button của JRootPane (bấm Enter), FlatLaf sẽ tô
     * bằng bộ key "default.*" riêng, bỏ qua "background" thường.
     */
    public static void primary(JButton btn) {
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(PRIMARY) + "; foreground: #FFFFFF;"
                + " hoverBackground: #" + hex(PRIMARY_HOVER) + ";"
                + " pressedBackground: #" + hex(PRIMARY_PRESSED) + ";"
                + " focusedBackground: #" + hex(PRIMARY) + ";"
                + " default.background: #" + hex(PRIMARY) + ";"
                + " default.foreground: #FFFFFF;"
                + " default.hoverBackground: #" + hex(PRIMARY_HOVER) + ";"
                + " default.pressedBackground: #" + hex(PRIMARY_PRESSED) + ";"
                + " default.focusedBackground: #" + hex(PRIMARY) + ";"
                + " default.borderColor: #" + hex(PRIMARY) + ";"
                + " default.focusedBorderColor: #" + hex(PRIMARY_HOVER) + ";"
                + " borderWidth: 0");
    }

    /** Áp style nút phụ/hủy (Reset, Remove, Cancel, Back...) — cùng màu nâu gỗ với nút chính cho đồng nhất. */
    public static void secondary(JButton btn) {
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(SECONDARY) + "; foreground: #FFFFFF;"
                + " hoverBackground: #" + hex(SECONDARY_HOVER) + ";"
                + " pressedBackground: #" + hex(SECONDARY_PRESSED) + ";"
                + " focusedBackground: #" + hex(SECONDARY) + ";"
                + " default.background: #" + hex(SECONDARY) + ";"
                + " default.foreground: #FFFFFF;"
                + " default.hoverBackground: #" + hex(SECONDARY_HOVER) + ";"
                + " default.pressedBackground: #" + hex(SECONDARY_PRESSED) + ";"
                + " default.focusedBackground: #" + hex(SECONDARY) + ";"
                + " default.borderColor: #" + hex(SECONDARY) + ";"
                + " default.focusedBorderColor: #" + hex(SECONDARY_HOVER) + ";"
                + " borderWidth: 0");
    }

    /**
     * Biến 1 JLabel thành đường link chữ (gạch chân, đổi con trỏ khi rê chuột) —
     * dùng cho các dòng dạng "Chưa có tài khoản? Đăng ký ngay" thay vì làm nút.
     * Giữ nguyên text gốc đã setText() trước đó, chỉ bọc thêm HTML gạch chân.
     */
    public static void link(JLabel lbl, Runnable onClick) {
        lbl.setText("<html><u>" + lbl.getText() + "</u></html>");
        lbl.setForeground(PRIMARY_HOVER);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 13f));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
    }

    /**
     * Style mục điều hướng ĐANG CHỌN trong thanh bên (nền nâu, chữ trắng, căn
     * trái) — dùng cho các khung có thanh bên: AdminShellFrm, SalesShellFrm,
     * InventoryShellFrm (giống thanh bên khu Khách hàng).
     */
    public static void sidebarNav(JButton btn) {
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(PRIMARY) + "; foreground: #FFFFFF;"
                + " hoverBackground: #" + hex(PRIMARY_HOVER) + ";"
                + " pressedBackground: #" + hex(PRIMARY_PRESSED) + ";"
                + " focusedBackground: #" + hex(PRIMARY) + ";"
                + " borderWidth: 0; arc: 0; margin: 10,18,10,12; font: +1");
    }

    /** Mục thanh bên CHƯA chọn: nền kem liền với thanh bên, chữ nâu đậm, sẫm nhẹ khi hover. */
    public static void sidebarNavMuted(JButton btn) {
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(SIDEBAR_BG) + "; foreground: #" + hex(SIDEBAR_TEXT_MUTED) + ";"
                + " hoverBackground: #" + hex(NAV_HOVER) + ";"
                + " pressedBackground: #" + hex(TABLE_GRID) + ";"
                + " focusedBackground: #" + hex(SIDEBAR_BG) + ";"
                + " borderWidth: 0; arc: 0; margin: 10,18,10,12; font: +1");
    }

    /**
     * Nút chỉ có icon, nền trong suốt, sáng nhẹ khi hover — dùng cho nút mũi
     * tên "quay lại" ở topbar các trang Admin con (SearchBookForEditPage,
     * EditBookPage...).
     */
    public static void iconButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(BACKGROUND) + "; foreground: #" + hex(TEXT) + ";"
                + " hoverBackground: #" + hex(TABLE_HEADER) + ";"
                + " pressedBackground: #" + hex(INPUT_BORDER) + ";"
                + " focusedBackground: #" + hex(BACKGROUND) + ";"
                + " borderWidth: 0; arc: 8; margin: 6,10,6,10");
    }

    /**
     * Mở 1 JFrame ở chế độ full màn hình (maximized, vẫn còn thanh tiêu đề +
     * nút minimize/maximize/close, không phải kiosk mode). Áp dụng cho mọi
     * trang TRỪ Login/Register (2 trang đó cố tình để nhỏ, dạng form đăng nhập).
     */
    public static void maximize(javax.swing.JFrame frame) {
        frame.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Chuyển sang JFrame "to" ở đúng vị trí/kích thước của "from" (đang phóng
     * to thì vẫn phóng to) rồi đóng "from" — để chuyển trang giữa các JFrame
     * Form nhìn liền mạch, không bị nhảy cửa sổ.
     */
    public static void switchFrame(javax.swing.JFrame from, javax.swing.JFrame to) {
        to.setExtendedState(javax.swing.JFrame.NORMAL);
        to.setBounds(from.getBounds());
        to.setExtendedState(from.getExtendedState());
        to.setVisible(true);
        from.dispose();
    }

    /** Cửa sổ chứa "c" (hoặc chính "c" nếu là cửa sổ) — làm owner cho các JDialog Form. */
    public static java.awt.Window windowOf(Component c) {
        if (c == null || c instanceof java.awt.Window) {
            return (java.awt.Window) c;
        }
        return javax.swing.SwingUtilities.getWindowAncestor(c);
    }

    /**
     * Căn lề cell và header của 1 cột JTable theo cùng 1 kiểu, và (tùy chọn)
     * ghim độ rộng cột. Mặc định Swing/FlatLaf căn giữa header nhưng căn trái
     * nội dung cell, khiến các cột nhìn bị lệch dù biên cột thẳng hàng thật —
     * dùng hàm này để header và cell luôn cùng 1 alignment.
     */
    public static void alignTableColumn(JTable table, int columnIndex, int alignment, int minWidth, int maxWidth) {
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(alignment);
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setCellRenderer(cellRenderer);
        if (minWidth > 0) {
            column.setMinWidth(minWidth);
            column.setMaxWidth(maxWidth);
        }
        javax.swing.table.TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
        column.setHeaderRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            Component c = defaultHeaderRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (c instanceof JLabel) {
                ((JLabel) c).setHorizontalAlignment(alignment);
            }
            return c;
        });
    }

    private static String hex(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
