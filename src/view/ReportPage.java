/*
 * Trang Báo cáo thống kê — gộp cả 3 loại báo cáo Admin (FR-ADM-08 Doanh thu,
 * FR-ADM-09 Tồn kho, FR-ADM-10 Bán hàng) vào 1 trang có 3 tab, đúng theo mô
 * tả luồng trong SRS: "Admin chọn 'View Reports' -> chọn loại báo cáo".
 */
package view;

import dao.ReportDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReportPage implements AdminPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String navKey() {
        return "reports";
    }

    @Override
    public String title() {
        return "Báo Cáo Thống Kê";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Báo cáo thống kê");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        content.add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(AppTheme.BACKGROUND);
        tabs.addTab("Doanh thu", buildRevenueTab(shell));
        tabs.addTab("Tồn kho", buildInventoryTab());
        tabs.addTab("Bán hàng", buildSalesTab());

        content.add(tabs, BorderLayout.CENTER);
        return content;
    }

    // ==================== FR-ADM-08: Doanh thu ====================
    private JPanel buildRevenueTab(AdminShellFrm shell) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        Calendar cal = Calendar.getInstance();
        String today = DATE_FMT.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        String monthAgo = DATE_FMT.format(cal.getTime());

        JTextField txtStart = new JTextField(monthAgo);
        JTextField txtEnd = new JTextField(today);
        JButton btnView = new JButton("Xem báo cáo");
        AppTheme.primary(btnView);

        JPanel filterBar = new JPanel();
        filterBar.setLayout(new BoxLayout(filterBar, BoxLayout.X_AXIS));
        filterBar.setBackground(AppTheme.BACKGROUND);
        filterBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        filterBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel fromLabel = new JLabel("Từ ngày:");
        fromLabel.setForeground(AppTheme.TEXT);
        filterBar.add(fromLabel);
        filterBar.add(Box.createHorizontalStrut(8));
        txtStart.setMaximumSize(new Dimension(130, 30));
        filterBar.add(txtStart);
        filterBar.add(Box.createHorizontalStrut(16));

        JLabel toLabel = new JLabel("Đến ngày:");
        toLabel.setForeground(AppTheme.TEXT);
        filterBar.add(toLabel);
        filterBar.add(Box.createHorizontalStrut(8));
        txtEnd.setMaximumSize(new Dimension(130, 30));
        filterBar.add(txtEnd);
        filterBar.add(Box.createHorizontalStrut(16));
        filterBar.add(btnView);

        panel.add(filterBar, BorderLayout.NORTH);

        JPanel resultArea = new JPanel(new BorderLayout(0, 16));
        resultArea.setBackground(AppTheme.BACKGROUND);
        panel.add(resultArea, BorderLayout.CENTER);

        Runnable[] runHolder = new Runnable[1];
        runHolder[0] = () -> {
            java.sql.Date start;
            java.sql.Date end;
            try {
                start = new java.sql.Date(DATE_FMT.parse(txtStart.getText().trim()).getTime());
                end = new java.sql.Date(DATE_FMT.parse(txtEnd.getText().trim()).getTime());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(shell, "Ngày không hợp lệ! Vui lòng nhập theo định dạng yyyy-MM-dd.");
                return;
            }

            List<ReportDAO.RevenueOrderRow> rows = new ReportDAO().revenueOrders(start, end);
            float total = 0f;
            for (ReportDAO.RevenueOrderRow r : rows) {
                total += r.totalAmount;
            }

            resultArea.removeAll();

            JPanel statRow = new JPanel(new java.awt.GridLayout(1, 2, 18, 0));
            statRow.setBackground(AppTheme.BACKGROUND);
            statRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            statRow.setPreferredSize(new Dimension(0, 90));
            statRow.add(statCard(String.valueOf(rows.size()), "Tổng đơn hàng đã thanh toán", AppTheme.CHART_SKY));
            statRow.add(statCard(String.format("%,.0f₫", total), "Tổng doanh thu", AppTheme.CHART_GOLD));

            JPanel top = new JPanel(new BorderLayout(0, 16));
            top.setBackground(AppTheme.BACKGROUND);
            top.add(statRow, BorderLayout.NORTH);

            String[] columns = {"Mã đơn", "Ngày đặt", "Khách hàng", "Số tiền"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            for (ReportDAO.RevenueOrderRow r : rows) {
                model.addRow(new Object[]{r.orderID, r.orderDate, r.customerName, String.format("%,.0f₫", r.totalAmount)});
            }
            JTable table = new JTable(model);
            table.setRowHeight(28);
            table.setGridColor(AppTheme.TABLE_GRID);
            table.getTableHeader().setReorderingAllowed(false);

            JPanel tableCard = cardWrap(table, "Chi tiết đơn hàng trong khoảng thời gian");
            top.add(tableCard, BorderLayout.CENTER);

            resultArea.add(top, BorderLayout.CENTER);
            resultArea.revalidate();
            resultArea.repaint();
        };

        btnView.addActionListener(e -> runHolder[0].run());
        runHolder[0].run();

        return panel;
    }

    // ==================== FR-ADM-09: Tồn kho ====================
    private JPanel buildInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        List<ReportDAO.InventoryRow> rows = new ReportDAO().inventoryReport();

        String[] columns = {"Sách", "Đã nhập", "Đã xuất", "Tồn kho hiện tại"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (ReportDAO.InventoryRow r : rows) {
            model.addRow(new Object[]{r.bookTitle, r.importQuantity, r.exportQuantity, r.stockQuantity});
        }
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer lowStockRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                int qty = (Integer) value;
                c.setForeground(qty < 10 ? AppTheme.ACCENT_TEXT : AppTheme.TEXT);
                setHorizontalAlignment(RIGHT);
                return c;
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(lowStockRenderer);

        panel.add(cardWrap(table, "Tồn kho theo sách (đỏ = dưới 10 cuốn)"), BorderLayout.CENTER);
        return panel;
    }

    // ==================== FR-ADM-10: Bán hàng ====================
    private JPanel buildSalesTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        List<ReportDAO.BestSellerRow> bestSellers = new ReportDAO().bestSellers(10);
        String[] bestSellerColumns = {"Sách", "Số lượng đã bán"};
        DefaultTableModel bestSellerModel = new DefaultTableModel(bestSellerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (ReportDAO.BestSellerRow r : bestSellers) {
            bestSellerModel.addRow(new Object[]{r.bookTitle, r.totalSold});
        }
        JTable bestSellerTable = new JTable(bestSellerModel);
        bestSellerTable.setRowHeight(28);
        bestSellerTable.setGridColor(AppTheme.TABLE_GRID);
        bestSellerTable.getTableHeader().setReorderingAllowed(false);

        JPanel bestSellerCard = cardWrap(bestSellerTable, "Sách bán chạy (top 10, tính theo số lượng trong mọi đơn hàng)");
        bestSellerCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        bestSellerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        panel.add(bestSellerCard);

        panel.add(Box.createVerticalStrut(20));

        List<ReportDAO.EmployeeSalesRow> employeeSales = new ReportDAO().salesByEmployee();
        String[] empColumns = {"Nhân viên", "Số đơn xử lý", "Doanh số"};
        DefaultTableModel empModel = new DefaultTableModel(empColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (ReportDAO.EmployeeSalesRow r : employeeSales) {
            empModel.addRow(new Object[]{r.employeeName, r.orderCount, String.format("%,.0f₫", r.totalAmount)});
        }
        JTable empTable = new JTable(empModel);
        empTable.setRowHeight(28);
        empTable.setGridColor(AppTheme.TABLE_GRID);
        empTable.getTableHeader().setReorderingAllowed(false);

        JPanel empCard = cardWrap(empTable, "Doanh số theo nhân viên xử lý (theo hóa đơn — Invoice)");
        empCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        empCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        panel.add(empCard);

        panel.add(Box.createVerticalStrut(8));

        JLabel note = new JLabel("Lưu ý: đơn đặt hàng qua ứng dụng hiện chưa tự tạo hóa đơn (Invoice) gắn nhân viên xử lý — bảng trên chỉ phản ánh dữ liệu hóa đơn đã có sẵn.");
        note.setFont(note.getFont().deriveFont(11f));
        note.setForeground(new Color(0x8A, 0x83, 0x74));
        note.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(note);

        return panel;
    }

    // ==================== Tiện ích dùng chung ====================
    private JPanel statCard(String value, String caption, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(valueLabel);

        card.add(Box.createVerticalStrut(4));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(captionLabel.getFont().deriveFont(12f));
        captionLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        captionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(captionLabel);

        return card;
    }

    private JPanel cardWrap(JTable table, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppTheme.TEXT);
        card.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }
}
