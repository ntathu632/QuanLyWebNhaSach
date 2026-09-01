/*
 * Theme dùng chung cho toàn bộ ứng dụng, theo đúng phong cách trong
 * "UI Design Mockup.png" (nền kem, header/nút chính xanh olive, nút phụ
 * cam đất). Gọi AppTheme.install() một lần duy nhất ở đầu mỗi main(),
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

    public static final Color BACKGROUND = new Color(0xF3, 0xEE, 0xDD);
    public static final Color HEADER = new Color(0x4F, 0x5D, 0x42);
    public static final Color PRIMARY = new Color(0x6B, 0x7F, 0x5C);
    public static final Color PRIMARY_HOVER = new Color(0x5C, 0x6E, 0x4F);
    public static final Color PRIMARY_PRESSED = new Color(0x4F, 0x5D, 0x42);
    public static final Color SECONDARY = new Color(0xC0, 0x60, 0x4A);
    public static final Color SECONDARY_HOVER = new Color(0xAD, 0x53, 0x3F);
    public static final Color SECONDARY_PRESSED = new Color(0x9A, 0x47, 0x35);
    public static final Color TABLE_HEADER = new Color(0xDC, 0xE3, 0xD0);
    public static final Color TABLE_GRID = new Color(0xD8, 0xD3, 0xC4);
    public static final Color ACCENT_TEXT = new Color(0xC0, 0x39, 0x2B);
    public static final Color TEXT = new Color(0x3A, 0x33, 0x28);
    public static final Color INPUT_BORDER = new Color(0xC9, 0xC2, 0xAE);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0xDD, 0xE2, 0xD3);
    public static final int SIDEBAR_WIDTH = 240;
    public static final int TOPBAR_HEIGHT = 56;

    // Bảng màu RIÊNG cho biểu đồ — cố tình KHÔNG dùng lại PRIMARY/SECONDARY/
    // HEADER (màu nút, sidebar...) để nội dung không lặp đúng 1 màu nhìn đơn
    // điệu; tông tươi/sáng/rực hơn hẳn bảng màu trầm ấm của UI. Có 1 màu xanh
    // dương (CHART_SKY) nhưng là xanh da trời sáng, khác hẳn xanh olive của
    // PRIMARY/HEADER và khác luôn xanh dương đậm dùng ở bản trước.
    public static final Color CHART_PINK = new Color(0xFF, 0x4F, 0xA3);
    public static final Color CHART_ORANGE = new Color(0xFF, 0xA0, 0x33);
    public static final Color CHART_GOLD = new Color(0xFF, 0xC8, 0x00);
    public static final Color CHART_SKY = new Color(0x29, 0xB6, 0xF6);
    public static final Color CHART_PURPLE = new Color(0xAB, 0x47, 0xBC);
    public static final Color CHART_RED = new Color(0xFF, 0x5C, 0x5C);
    public static final Color[] CHART_PALETTE = {CHART_PINK, CHART_ORANGE, CHART_SKY, CHART_GOLD, CHART_PURPLE, CHART_RED};
    public static final Color CHART_GRID = new Color(0xE3, 0xDD, 0xC9);
    public static final Color CHART_MUTED_TEXT = new Color(0x8A, 0x83, 0x74);

    private AppTheme() {
    }

    /**
     * Cài đặt FlatLaf + theme màu riêng. Gọi 1 lần duy nhất, trước khi
     * tạo component Swing nào (đầu mỗi main()).
     */
    public static void install() {
        FlatLightLaf.setup();

        // Tăng cỡ chữ mặc định toàn app thêm 2pt (FlatLaf mặc định khá nhỏ,
        // ~13px) — mọi component (label, field, button, table...) đều tự
        // scale theo "defaultFont" này nên chỉ cần đặt 1 chỗ duy nhất.
        Font base = UIManager.getFont("defaultFont");
        if (base != null) {
            UIManager.put("defaultFont", base.deriveFont(base.getSize2D() + 2f));
        }

        // Từ FlatLaf 2.0+, "unified background" bật mặc định khiến title bar tự lấy
        // màu nền chung (Panel.background), ghi đè TitlePane.background — phải tắt
        // trước thì mới đặt được màu title bar riêng khác màu nền form.
        UIManager.put("TitlePane.unifiedBackground", false);
        UIManager.put("TitlePane.background", HEADER);
        UIManager.put("TitlePane.foreground", Color.WHITE);
        UIManager.put("TitlePane.inactiveBackground", HEADER);
        UIManager.put("TitlePane.inactiveForeground", new Color(0xE8, 0xE8, 0xE0));
        UIManager.put("TitlePane.buttonHoverBackground", PRIMARY_HOVER);
        UIManager.put("TitlePane.buttonPressedBackground", PRIMARY_PRESSED);

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("RootPane.background", BACKGROUND);

        UIManager.put("Component.borderColor", INPUT_BORDER);
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 6);

        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.gridColor", TABLE_GRID);
        UIManager.put("Table.selectionBackground", new Color(0xE3, 0xE9, 0xD9));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", TABLE_HEADER);
        UIManager.put("TableHeader.foreground", TEXT);

        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", new Color(0xCF, 0xE0, 0xF2));
    }

    /**
     * Áp style nút chính (xanh olive) — dùng cho hành động xác nhận: Login, Save,
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
                + " borderWidth: 0; font: bold");
    }

    /** Áp style nút phụ/hủy (cam đất) — dùng cho: Reset, Remove, Cancel, Back... */
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
                + " borderWidth: 0; font: bold");
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
     * Style 1 mục điều hướng trong sidebar (nền tối liền với sidebar, chữ trắng,
     * bo tròn 0, căn trái, sáng lên khi hover) — dùng cho các trang có khung kiểu
     * "SB Admin 2" (sidebar trái + topbar + nội dung): AdminShellFrm.
     */
    public static void sidebarNav(JButton btn) {
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(HEADER) + "; foreground: #FFFFFF;"
                + " hoverBackground: #" + hex(PRIMARY_HOVER) + ";"
                + " pressedBackground: #" + hex(PRIMARY_PRESSED) + ";"
                + " focusedBackground: #" + hex(HEADER) + ";"
                + " borderWidth: 0; arc: 0; margin: 10,18,10,12; font: +1");
    }

    /** Như {@link #sidebarNav}, nhưng chữ mờ hơn — dùng cho mục chưa hiện thực (bấm vẫn báo thông báo). */
    public static void sidebarNavMuted(JButton btn) {
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.putClientProperty("FlatLaf.style",
                "background: #" + hex(HEADER) + "; foreground: #" + hex(SIDEBAR_TEXT_MUTED) + ";"
                + " hoverBackground: #" + hex(PRIMARY_HOVER) + ";"
                + " pressedBackground: #" + hex(PRIMARY_PRESSED) + ";"
                + " focusedBackground: #" + hex(HEADER) + ";"
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
