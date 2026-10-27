/*
 * Trang Báo cáo thống kê — gộp cả 3 loại báo cáo Admin (FR-ADM-08 Doanh thu,
 * FR-ADM-09 Tồn kho, FR-ADM-10 Bán hàng) vào 1 trang có 3 tab, đúng theo mô
 * tả luồng trong SRS: "Admin chọn 'View Reports' -> chọn loại báo cáo".
 */
package view.admin;
import view.shared.*;

import dao.ReportDAO;
import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReportPage extends javax.swing.JPanel implements AdminPage {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    private AdminShellFrm shell;

    private final String[] revenueColumns = {"Mã đơn", "Ngày đặt", "Khách hàng", "Số tiền"};
    private final DefaultTableModel revenueModel = readOnlyModel(revenueColumns);
    private Object[][] revenueExport = new Object[][]{revenueColumns};

    private static final java.awt.Color LOW_STOCK_RED = new java.awt.Color(0xC0, 0x1F, 0x1F);

    private final String[] inventoryColumns = {"Sách", "Đã nhập", "Đã xuất", "Tồn kho hiện tại"};
    private final DefaultTableModel inventoryModel = readOnlyModel(inventoryColumns);
    private Object[][] inventoryExport = new Object[][]{inventoryColumns};

    private final String[] bestSellerColumns = {"Sách", "Số lượng đã bán"};
    private final DefaultTableModel bestSellerModel = readOnlyModel(bestSellerColumns);
    private Object[][] bestSellerExport = new Object[][]{bestSellerColumns};

    private final String[] empColumns = {"Nhân viên", "Số đơn xử lý", "Doanh số"};
    private final DefaultTableModel empModel = readOnlyModel(empColumns);
    private Object[][] empExport = new Object[][]{empColumns};

    public ReportPage() {
        initComponents();

        // ---- Tab Doanh thu ----
        AppTheme.primary(btnView);
        btnView.addActionListener(e -> runRevenueReport());

        revenueTable.setModel(revenueModel);
        revenueTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(revenueTable, 0, SwingConstants.CENTER, 80, 80);
        AppTheme.alignTableColumn(revenueTable, 1, SwingConstants.CENTER, 150, 150);
        AppTheme.alignTableColumn(revenueTable, 2, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(revenueTable, 3, SwingConstants.RIGHT, 130, 130);

        AppTheme.secondary(btnExportRevenue);
        btnExportRevenue.addActionListener(ev -> {
            LinkedHashMap<String, Object[][]> sheets = new LinkedHashMap<>();
            sheets.put("Doanh thu", revenueExport);
            ExcelExportUtil.export(shell, "BaoCao_DoanhThu", sheets);
        });

        // ---- Tab Tồn kho ----
        inventoryTable.setModel(inventoryModel);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(inventoryTable, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(inventoryTable, 1, SwingConstants.CENTER, 90, 90);
        AppTheme.alignTableColumn(inventoryTable, 2, SwingConstants.CENTER, 90, 90);
        AppTheme.alignTableColumn(inventoryTable, 3, SwingConstants.RIGHT, 140, 140);

        // Sách dưới 10 cuốn: tô đỏ cả dòng, không chỉ ô số lượng.
        int[] alignments = {SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.RIGHT};
        for (int col = 0; col < alignments.length; col++) {
            int alignment = alignments[col];
            DefaultTableCellRenderer lowStockRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    int qty = (Integer) t.getValueAt(row, 3);
                    c.setForeground(qty < 10 ? LOW_STOCK_RED : AppTheme.TEXT);
                    setHorizontalAlignment(alignment);
                    return c;
                }
            };
            inventoryTable.getColumnModel().getColumn(col).setCellRenderer(lowStockRenderer);
        }

        AppTheme.secondary(btnExportInventory);
        btnExportInventory.addActionListener(ev -> {
            LinkedHashMap<String, Object[][]> sheets = new LinkedHashMap<>();
            sheets.put("Tồn kho", inventoryExport);
            ExcelExportUtil.export(inventoryTab, "BaoCao_TonKho", sheets);
        });

        // ---- Tab Bán hàng ----
        bestSellerTable.setModel(bestSellerModel);
        bestSellerTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(bestSellerTable, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(bestSellerTable, 1, SwingConstants.CENTER, 160, 160);

        empTable.setModel(empModel);
        empTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(empTable, 0, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(empTable, 1, SwingConstants.CENTER, 130, 130);
        AppTheme.alignTableColumn(empTable, 2, SwingConstants.RIGHT, 150, 150);

        AppTheme.secondary(btnExportSales);
        btnExportSales.addActionListener(ev -> {
            LinkedHashMap<String, Object[][]> sheets = new LinkedHashMap<>();
            sheets.put("Sach ban chay", bestSellerExport);
            sheets.put("Doanh so nhan vien", empExport);
            ExcelExportUtil.export(salesTab, "BaoCao_BanHang", sheets);
        });
    }

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
        this.shell = shell;

        Calendar cal = Calendar.getInstance();
        String today = DATE_FMT.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        String monthAgo = DATE_FMT.format(cal.getTime());
        txtStart.setText(monthAgo);
        txtEnd.setText(today);
        runRevenueReport();

        loadInventoryReport();
        loadSalesReport();

        return this;
    }

    // ==================== FR-ADM-08: Doanh thu ====================
    private void runRevenueReport() {
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

        revenueStatValue1.setText(String.valueOf(rows.size()));
        revenueStatValue2.setText(String.format("%,.0f₫", total));

        revenueModel.setRowCount(0);
        Object[][] exportData = new Object[rows.size() + 1][4];
        exportData[0] = revenueColumns;
        for (int i = 0; i < rows.size(); i++) {
            ReportDAO.RevenueOrderRow r = rows.get(i);
            revenueModel.addRow(new Object[]{r.orderID, r.orderDate, r.customerName, String.format("%,.0f₫", r.totalAmount)});
            exportData[i + 1] = new Object[]{r.orderID, r.orderDate, r.customerName, r.totalAmount};
        }
        revenueExport = exportData;
    }

    // ==================== FR-ADM-09: Tồn kho ====================
    private void loadInventoryReport() {
        List<ReportDAO.InventoryRow> rows = new ReportDAO().inventoryReport();

        inventoryModel.setRowCount(0);
        Object[][] exportData = new Object[rows.size() + 1][4];
        exportData[0] = inventoryColumns;
        for (int i = 0; i < rows.size(); i++) {
            ReportDAO.InventoryRow r = rows.get(i);
            inventoryModel.addRow(new Object[]{r.bookTitle, r.importQuantity, r.exportQuantity, r.stockQuantity});
            exportData[i + 1] = new Object[]{r.bookTitle, r.importQuantity, r.exportQuantity, r.stockQuantity};
        }
        inventoryExport = exportData;
    }

    // ==================== FR-ADM-10: Bán hàng ====================
    private void loadSalesReport() {
        List<ReportDAO.BestSellerRow> bestSellers = new ReportDAO().bestSellers(10);
        bestSellerModel.setRowCount(0);
        bestSellerExport = new Object[bestSellers.size() + 1][2];
        bestSellerExport[0] = bestSellerColumns;
        for (int i = 0; i < bestSellers.size(); i++) {
            ReportDAO.BestSellerRow r = bestSellers.get(i);
            bestSellerModel.addRow(new Object[]{r.bookTitle, r.totalSold});
            bestSellerExport[i + 1] = new Object[]{r.bookTitle, r.totalSold};
        }

        List<ReportDAO.EmployeeSalesRow> employeeSales = new ReportDAO().salesByEmployee();
        empModel.setRowCount(0);
        empExport = new Object[employeeSales.size() + 1][3];
        empExport[0] = empColumns;
        for (int i = 0; i < employeeSales.size(); i++) {
            ReportDAO.EmployeeSalesRow r = employeeSales.get(i);
            empModel.addRow(new Object[]{r.employeeName, r.orderCount, String.format("%,.0f₫", r.totalAmount)});
            empExport[i + 1] = new Object[]{r.employeeName, r.orderCount, r.totalAmount};
        }
    }

    // ==================== Tiện ích dùng chung ====================
    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        heading = new javax.swing.JLabel();
        tabs = new javax.swing.JTabbedPane();
        revenueTab = new javax.swing.JPanel();
        filterBar = new javax.swing.JPanel();
        fromLabel = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        txtStart = new javax.swing.JTextField();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 32767));
        toLabel = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        txtEnd = new javax.swing.JTextField();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 32767));
        btnView = new javax.swing.JButton();
        resultArea = new javax.swing.JPanel();
        revenueTop = new javax.swing.JPanel();
        revenueCard = new javax.swing.JPanel();
        revenueTitle = new javax.swing.JLabel();
        revenueScroll = new javax.swing.JScrollPane();
        revenueTable = new javax.swing.JTable();
        revenueNorthArea = new javax.swing.JPanel();
        revenueStatRow = new javax.swing.JPanel();
        revenueStat1 = new javax.swing.JPanel();
        revenueStatValue1 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        revenueStatCaption1 = new javax.swing.JLabel();
        revenueStat2 = new javax.swing.JPanel();
        revenueStatValue2 = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        revenueStatCaption2 = new javax.swing.JLabel();
        revenueExportRow = new javax.swing.JPanel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        btnExportRevenue = new javax.swing.JButton();
        inventoryTab = new javax.swing.JPanel();
        inventoryCard = new javax.swing.JPanel();
        inventoryTitle = new javax.swing.JLabel();
        inventoryScroll = new javax.swing.JScrollPane();
        inventoryTable = new javax.swing.JTable();
        inventoryExportRow = new javax.swing.JPanel();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        btnExportInventory = new javax.swing.JButton();
        salesTab = new javax.swing.JPanel();
        bestSellerCard = new javax.swing.JPanel();
        bestSellerTitle = new javax.swing.JLabel();
        bestSellerScroll = new javax.swing.JScrollPane();
        bestSellerTable = new javax.swing.JTable();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        empCard = new javax.swing.JPanel();
        empTitle = new javax.swing.JLabel();
        empScroll = new javax.swing.JScrollPane();
        empTable = new javax.swing.JTable();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 12), new java.awt.Dimension(0, 12), new java.awt.Dimension(32767, 12));
        salesExportRow = new javax.swing.JPanel();
        btnExportSales = new javax.swing.JButton();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));

        setBackground(AppTheme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        setLayout(new java.awt.BorderLayout());

        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setText("Báo cáo thống kê");
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(heading, java.awt.BorderLayout.NORTH);

        tabs.setBackground(AppTheme.BACKGROUND);
        revenueTab.setBackground(AppTheme.BACKGROUND);
        revenueTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 0, 0));
        revenueTab.setLayout(new java.awt.BorderLayout(0, 16));
        filterBar.setBackground(AppTheme.BACKGROUND);
        filterBar.setAlignmentX(0.0F);
        filterBar.setMaximumSize(new java.awt.Dimension(2147483647, 36));
        filterBar.setLayout(new javax.swing.BoxLayout(filterBar, javax.swing.BoxLayout.X_AXIS));
        fromLabel.setForeground(AppTheme.TEXT);
        fromLabel.setText("Từ ngày:");
        filterBar.add(fromLabel);
        filterBar.add(filler1);
        txtStart.setMaximumSize(new java.awt.Dimension(130, 30));
        filterBar.add(txtStart);
        filterBar.add(filler2);
        toLabel.setForeground(AppTheme.TEXT);
        toLabel.setText("Đến ngày:");
        filterBar.add(toLabel);
        filterBar.add(filler3);
        txtEnd.setMaximumSize(new java.awt.Dimension(130, 30));
        filterBar.add(txtEnd);
        filterBar.add(filler4);
        btnView.setText("Xem báo cáo");
        filterBar.add(btnView);
        revenueTab.add(filterBar, java.awt.BorderLayout.NORTH);

        resultArea.setBackground(AppTheme.BACKGROUND);
        resultArea.setLayout(new java.awt.BorderLayout(0, 16));
        revenueTop.setBackground(AppTheme.BACKGROUND);
        revenueTop.setLayout(new java.awt.BorderLayout(0, 16));
        revenueCard.setBackground(new java.awt.Color(255, 255, 255));
        revenueCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        revenueCard.setLayout(new java.awt.BorderLayout(0, 10));
        revenueTitle.setFont(revenueTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        revenueTitle.setForeground(AppTheme.TEXT);
        revenueTitle.setText("Chi tiết đơn hàng trong khoảng thời gian");
        revenueCard.add(revenueTitle, java.awt.BorderLayout.NORTH);
        revenueScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        revenueTable.setGridColor(AppTheme.TABLE_GRID);
        revenueTable.setRowHeight(28);
        revenueScroll.setViewportView(revenueTable);
        revenueCard.add(revenueScroll, java.awt.BorderLayout.CENTER);

        revenueTop.add(revenueCard, java.awt.BorderLayout.CENTER);

        revenueNorthArea.setBackground(AppTheme.BACKGROUND);
        revenueNorthArea.setLayout(new java.awt.BorderLayout(0, 10));
        revenueStatRow.setBackground(AppTheme.BACKGROUND);
        revenueStatRow.setMaximumSize(new java.awt.Dimension(2147483647, 90));
        revenueStatRow.setPreferredSize(new java.awt.Dimension(0, 90));
        revenueStatRow.setLayout(new java.awt.GridLayout(1, 2, 18, 0));
        revenueStat1.setBackground(new java.awt.Color(255, 255, 255));
        revenueStat1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_SKY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        revenueStat1.setLayout(new javax.swing.BoxLayout(revenueStat1, javax.swing.BoxLayout.Y_AXIS));
        revenueStatValue1.setFont(revenueStatValue1.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        revenueStatValue1.setForeground(AppTheme.TEXT);
        revenueStatValue1.setText("0");
        revenueStatValue1.setAlignmentX(0.0F);
        revenueStat1.add(revenueStatValue1);
        revenueStat1.add(filler5);
        revenueStatCaption1.setFont(revenueStatCaption1.getFont().deriveFont(12f));
        revenueStatCaption1.setForeground(new java.awt.Color(140, 123, 109));
        revenueStatCaption1.setText("Tổng đơn hàng đã thanh toán");
        revenueStatCaption1.setAlignmentX(0.0F);
        revenueStat1.add(revenueStatCaption1);
        revenueStatRow.add(revenueStat1);

        revenueStat2.setBackground(new java.awt.Color(255, 255, 255));
        revenueStat2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_GOLD), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        revenueStat2.setLayout(new javax.swing.BoxLayout(revenueStat2, javax.swing.BoxLayout.Y_AXIS));
        revenueStatValue2.setFont(revenueStatValue2.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        revenueStatValue2.setForeground(AppTheme.TEXT);
        revenueStatValue2.setText("0");
        revenueStatValue2.setAlignmentX(0.0F);
        revenueStat2.add(revenueStatValue2);
        revenueStat2.add(filler6);
        revenueStatCaption2.setFont(revenueStatCaption2.getFont().deriveFont(12f));
        revenueStatCaption2.setForeground(new java.awt.Color(140, 123, 109));
        revenueStatCaption2.setText("Tổng doanh thu");
        revenueStatCaption2.setAlignmentX(0.0F);
        revenueStat2.add(revenueStatCaption2);
        revenueStatRow.add(revenueStat2);

        revenueNorthArea.add(revenueStatRow, java.awt.BorderLayout.NORTH);

        revenueExportRow.setBackground(AppTheme.BACKGROUND);
        revenueExportRow.setLayout(new javax.swing.BoxLayout(revenueExportRow, javax.swing.BoxLayout.X_AXIS));
        revenueExportRow.add(filler7);
        btnExportRevenue.setText("Xuất Excel");
        revenueExportRow.add(btnExportRevenue);
        revenueNorthArea.add(revenueExportRow, java.awt.BorderLayout.SOUTH);

        revenueTop.add(revenueNorthArea, java.awt.BorderLayout.NORTH);

        resultArea.add(revenueTop, java.awt.BorderLayout.CENTER);

        revenueTab.add(resultArea, java.awt.BorderLayout.CENTER);

        tabs.addTab("Doanh thu", revenueTab);

        inventoryTab.setBackground(AppTheme.BACKGROUND);
        inventoryTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 0, 0));
        inventoryTab.setLayout(new java.awt.BorderLayout());
        inventoryCard.setBackground(new java.awt.Color(255, 255, 255));
        inventoryCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        inventoryCard.setLayout(new java.awt.BorderLayout(0, 10));
        inventoryTitle.setFont(inventoryTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        inventoryTitle.setForeground(AppTheme.TEXT);
        inventoryTitle.setText("Tồn kho theo sách (đỏ = dưới 10 cuốn)");
        inventoryCard.add(inventoryTitle, java.awt.BorderLayout.NORTH);
        inventoryScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        inventoryTable.setGridColor(AppTheme.TABLE_GRID);
        inventoryTable.setRowHeight(28);
        inventoryScroll.setViewportView(inventoryTable);
        inventoryCard.add(inventoryScroll, java.awt.BorderLayout.CENTER);

        inventoryTab.add(inventoryCard, java.awt.BorderLayout.CENTER);

        inventoryExportRow.setBackground(AppTheme.BACKGROUND);
        inventoryExportRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 12, 0));
        inventoryExportRow.setLayout(new javax.swing.BoxLayout(inventoryExportRow, javax.swing.BoxLayout.X_AXIS));
        inventoryExportRow.add(filler8);
        btnExportInventory.setText("Xuất Excel");
        inventoryExportRow.add(btnExportInventory);
        inventoryTab.add(inventoryExportRow, java.awt.BorderLayout.NORTH);

        tabs.addTab("Tồn kho", inventoryTab);

        salesTab.setBackground(AppTheme.BACKGROUND);
        salesTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 0, 0));
        salesTab.setLayout(new javax.swing.BoxLayout(salesTab, javax.swing.BoxLayout.Y_AXIS));
        bestSellerCard.setBackground(new java.awt.Color(255, 255, 255));
        bestSellerCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        bestSellerCard.setAlignmentX(0.0F);
        bestSellerCard.setMaximumSize(new java.awt.Dimension(2147483647, 320));
        bestSellerCard.setLayout(new java.awt.BorderLayout(0, 10));
        bestSellerTitle.setFont(bestSellerTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        bestSellerTitle.setForeground(AppTheme.TEXT);
        bestSellerTitle.setText("Sách bán chạy (top 10, tính theo số lượng trong mọi đơn hàng)");
        bestSellerCard.add(bestSellerTitle, java.awt.BorderLayout.NORTH);
        bestSellerScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        bestSellerTable.setGridColor(AppTheme.TABLE_GRID);
        bestSellerTable.setRowHeight(28);
        bestSellerScroll.setViewportView(bestSellerTable);
        bestSellerCard.add(bestSellerScroll, java.awt.BorderLayout.CENTER);

        salesTab.add(bestSellerCard);

        salesTab.add(filler9);
        empCard.setBackground(new java.awt.Color(255, 255, 255));
        empCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        empCard.setAlignmentX(0.0F);
        empCard.setMaximumSize(new java.awt.Dimension(2147483647, 260));
        empCard.setLayout(new java.awt.BorderLayout(0, 10));
        empTitle.setFont(empTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        empTitle.setForeground(AppTheme.TEXT);
        empTitle.setText("Doanh số theo nhân viên xử lý (theo hóa đơn)");
        empCard.add(empTitle, java.awt.BorderLayout.NORTH);
        empScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        empTable.setGridColor(AppTheme.TABLE_GRID);
        empTable.setRowHeight(28);
        empScroll.setViewportView(empTable);
        empCard.add(empScroll, java.awt.BorderLayout.CENTER);

        salesTab.add(empCard);

        salesTab.add(filler10);
        salesExportRow.setBackground(AppTheme.BACKGROUND);
        salesExportRow.setAlignmentX(0.0F);
        salesExportRow.setMaximumSize(new java.awt.Dimension(2147483647, 32));
        salesExportRow.setLayout(new javax.swing.BoxLayout(salesExportRow, javax.swing.BoxLayout.X_AXIS));
        btnExportSales.setText("Xuất Excel");
        salesExportRow.add(btnExportSales);
        salesExportRow.add(filler11);
        salesTab.add(salesExportRow);

        tabs.addTab("Bán hàng", salesTab);

        add(tabs, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bestSellerCard;
    private javax.swing.JScrollPane bestSellerScroll;
    private javax.swing.JTable bestSellerTable;
    private javax.swing.JLabel bestSellerTitle;
    private javax.swing.JButton btnExportInventory;
    private javax.swing.JButton btnExportRevenue;
    private javax.swing.JButton btnExportSales;
    private javax.swing.JButton btnView;
    private javax.swing.JPanel empCard;
    private javax.swing.JScrollPane empScroll;
    private javax.swing.JTable empTable;
    private javax.swing.JLabel empTitle;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JPanel filterBar;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel heading;
    private javax.swing.JPanel inventoryCard;
    private javax.swing.JPanel inventoryExportRow;
    private javax.swing.JScrollPane inventoryScroll;
    private javax.swing.JPanel inventoryTab;
    private javax.swing.JTable inventoryTable;
    private javax.swing.JLabel inventoryTitle;
    private javax.swing.JPanel resultArea;
    private javax.swing.JPanel revenueCard;
    private javax.swing.JPanel revenueExportRow;
    private javax.swing.JPanel revenueNorthArea;
    private javax.swing.JScrollPane revenueScroll;
    private javax.swing.JPanel revenueStat1;
    private javax.swing.JPanel revenueStat2;
    private javax.swing.JLabel revenueStatCaption1;
    private javax.swing.JLabel revenueStatCaption2;
    private javax.swing.JPanel revenueStatRow;
    private javax.swing.JLabel revenueStatValue1;
    private javax.swing.JLabel revenueStatValue2;
    private javax.swing.JPanel revenueTab;
    private javax.swing.JTable revenueTable;
    private javax.swing.JLabel revenueTitle;
    private javax.swing.JPanel revenueTop;
    private javax.swing.JPanel salesExportRow;
    private javax.swing.JPanel salesTab;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JLabel toLabel;
    private javax.swing.JTextField txtEnd;
    private javax.swing.JTextField txtStart;
    // End of variables declaration//GEN-END:variables
}
