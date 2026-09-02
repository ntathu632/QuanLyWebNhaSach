/*
 * Trang Sách sắp hết hàng (FR-INV-04). Ngưỡng 10 quyển — SRS ghi rõ đây là
 * điểm "cần nhóm chốt khi triển khai, ví dụ < 10 quyển", nên lấy đúng ví dụ
 * đó làm ngưỡng mặc định (không phải số tự bịa).
 */
package view.inventory;
import view.shared.*;

import dao.InventoryDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Inventory;

public class LowStockPage implements InventoryPage {

    private static final int THRESHOLD = 10;

    @Override
    public String navKey() {
        return "lowstock";
    }

    @Override
    public String title() {
        return "Sách Sắp Hết Hàng";
    }

    @Override
    public JPanel content(InventoryShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        List<Inventory> rows = new InventoryDAO().lowStock(THRESHOLD);

        JLabel heading = new JLabel("Sách sắp hết hàng (dưới " + THRESHOLD + " quyển)");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        content.add(heading, BorderLayout.NORTH);

        String[] columns = {"Sách", "Đã nhập", "Đã xuất", "Tồn kho hiện tại"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Inventory inv : rows) {
            model.addRow(new Object[]{inv.getBookTitle(), inv.getImportQuantity(), inv.getExportQuantity(), inv.getStockQuantity()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 90, 90);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 90, 90);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 140, 140);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground(AppTheme.ACCENT_TEXT);
                setHorizontalAlignment(RIGHT);
                return c;
            }
        });

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.SECONDARY),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel cardTitle = new JLabel(rows.isEmpty() ? "Không có sách nào sắp hết hàng" : "Danh sách sách cần nhập thêm");
        cardTitle.setFont(cardTitle.getFont().deriveFont(Font.BOLD, 14f));
        cardTitle.setForeground(AppTheme.TEXT);
        card.add(cardTitle, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        content.add(card, BorderLayout.CENTER);
        return content;
    }
}
