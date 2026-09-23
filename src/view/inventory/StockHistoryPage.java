/*
 * Trang Lịch sử nhập/trả kho — xem lại nhật ký StockTransaction (cả nhập lẫn
 * trả hàng nhà cung cấp), lọc theo loại giao dịch. Trước đây ImportBooksPage
 * chỉ cộng dồn số liệu, không có cách nào tra lại "ai nhập/trả, ngày nào,
 * bao nhiêu, từ/cho nhà cung cấp nào".
 */
package view.inventory;
import view.shared.*;

import dao.StockTransactionDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.StockTransaction;

public class StockHistoryPage implements InventoryPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String ALL_TYPES = "Tất cả";
    private static final String[] TYPE_OPTIONS = {ALL_TYPES, "Nhập kho", "Trả hàng"};

    @Override
    public String navKey() {
        return "history";
    }

    @Override
    public String title() {
        return "Lịch Sử Nhập/Trả Kho";
    }

    @Override
    public JPanel content(InventoryShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Lịch sử nhập/trả kho");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 8, 0));

        JComboBox<String> cboType = new JComboBox<>(TYPE_OPTIONS);

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setBackground(AppTheme.BACKGROUND);
        filterRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.gridy = 0;
        fgbc.anchor = GridBagConstraints.WEST;
        fgbc.gridx = 0;
        fgbc.insets = new java.awt.Insets(0, 0, 0, 10);
        JLabel filterLabel = new JLabel("Loại giao dịch:");
        filterLabel.setForeground(AppTheme.TEXT);
        filterRow.add(filterLabel, fgbc);
        fgbc.gridx = 1;
        filterRow.add(cboType, fgbc);

        JPanel headerWrap = new JPanel();
        headerWrap.setLayout(new BoxLayout(headerWrap, BoxLayout.Y_AXIS));
        headerWrap.setBackground(AppTheme.BACKGROUND);
        headerWrap.add(heading);
        headerWrap.add(filterRow);
        content.add(headerWrap, BorderLayout.NORTH);

        String[] columns = {"Ngày", "Sách", "Loại", "Số lượng", "Nhà cung cấp", "Nhân viên", "Ghi chú"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 140, 140);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.CENTER, 100, 100);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 90, 90);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 5, SwingConstants.LEFT, 150, 150);
        AppTheme.alignTableColumn(table, 6, SwingConstants.LEFT, 180, 180);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground("Nhập kho".equals(value) ? AppTheme.PRIMARY_HOVER : AppTheme.ACCENT_TEXT);
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        Runnable reload = () -> {
            String selected = (String) cboType.getSelectedItem();
            String typeFilter = ALL_TYPES.equals(selected) ? null : ("Nhập kho".equals(selected) ? "Import" : "Return");
            List<StockTransaction> rows = new StockTransactionDAO().getHistory(typeFilter);
            model.setRowCount(0);
            for (StockTransaction t : rows) {
                model.addRow(new Object[]{DATE_FMT.format(t.getTransactionDate()), t.getBookTitle(),
                    "Import".equals(t.getType()) ? "Nhập kho" : "Trả hàng", t.getQuantity(),
                    t.getSupplierName(), t.getEmployeeName(), t.getNote() == null ? "" : t.getNote()});
            }
        };
        cboType.addActionListener(e -> reload.run());

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel resultTitle = new JLabel("Nhật ký giao dịch kho");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableCard.add(resultTitle, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        reload.run();

        return content;
    }
}
