/*
 * Trang Dashboard — thẻ thống kê + 4 biểu đồ, dữ liệu thật lấy qua DashboardDAO.
 */
package view;

import dao.DashboardDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class DashboardPage implements AdminPage {

    @Override
    public String navKey() {
        return "dashboard";
    }

    @Override
    public String title() {
        return "Trang Quản Trị";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        top.add(buildStatCards());

        top.add(Box.createVerticalStrut(18));
        top.add(buildChartsGrid());

        content.add(top, BorderLayout.NORTH);
        return content;
    }

    // 4 biểu đồ khác dạng nhau, mỗi cái dùng 1 loại số liệu thật khác nhau từ
    // DB qua DashboardDAO — không phải dữ liệu dựng sẵn để trang trí:
    //   - Cột đứng   : doanh thu theo ngày (đơn "Paid")
    //   - Đường/vùng : số lượng đơn hàng theo ngày (mọi trạng thái)
    //   - Donut      : tỉ lệ đơn hàng theo trạng thái (Pending/Paid/Cancelled)
    //   - Cột ngang  : số đơn theo từng phương thức thanh toán
    private JPanel buildChartsGrid() {
        DashboardDAO dashboardDAO = new DashboardDAO();

        java.util.List<BarChartPanel.Item> revenueItems = new java.util.ArrayList<>();
        for (DashboardDAO.RevenuePoint p : dashboardDAO.revenueByDay()) {
            revenueItems.add(new BarChartPanel.Item(p.label, p.amount));
        }

        java.util.List<BarChartPanel.Item> orderCountItems = new java.util.ArrayList<>();
        for (DashboardDAO.RevenuePoint p : dashboardDAO.orderCountByDay()) {
            orderCountItems.add(new BarChartPanel.Item(p.label, p.amount));
        }

        java.util.List<DashboardDAO.LabelCount> statusItems = dashboardDAO.orderStatusBreakdown();
        java.util.List<DashboardDAO.LabelCount> paymentItems = dashboardDAO.paymentMethodBreakdown();

        // Không đặt maximumSize/preferredSize theo chiều rộng cố định — để
        // BoxLayout giãn full chiều ngang giống hệt hàng thẻ thống kê phía
        // trên (row cũng dùng Integer.MAX_VALUE cho width), nhờ đó 2 hàng
        // luôn thẳng mép nhau thay vì hàng dưới bị bó hẹp hơn hàng trên.
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setBackground(AppTheme.BACKGROUND);
        grid.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        grid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 520));
        grid.setPreferredSize(new java.awt.Dimension(0, 520));

        grid.add(BarChartPanel.card("Doanh thu theo ngày (đơn đã thanh toán)", revenueItems, AppTheme.CHART_PINK, 250));
        grid.add(LineChartPanel.card("Số đơn hàng theo ngày", orderCountItems, AppTheme.CHART_SKY, 250));
        grid.add(DonutChartPanel.card("Tỉ lệ đơn hàng theo trạng thái", statusItems, AppTheme.CHART_GOLD, 250));
        grid.add(HorizontalBarChartPanel.card("Đơn hàng theo phương thức thanh toán", paymentItems, AppTheme.CHART_ORANGE, 250));

        return grid;
    }

    // Số liệu thật lấy từ DB qua DashboardDAO (COUNT/SUM) — không phải số giả
    // định để trang trí. Doanh thu chỉ tính đơn đã "Paid" (xem AS-03 trong SRS).
    private JPanel buildStatCards() {
        DashboardDAO dashboardDAO = new DashboardDAO();

        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setBackground(AppTheme.BACKGROUND);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 96));
        row.setPreferredSize(new java.awt.Dimension(0, 96));

        row.add(statCard(FlatIcon.Kind.BOOK, String.valueOf(dashboardDAO.countBooks()), "Tổng số sách", AppTheme.CHART_SKY));
        row.add(statCard(FlatIcon.Kind.RECEIPT, String.valueOf(dashboardDAO.countOrders()), "Tổng đơn hàng", AppTheme.CHART_ORANGE));
        row.add(statCard(FlatIcon.Kind.USERS, String.valueOf(dashboardDAO.countCustomers()), "Tổng khách hàng", AppTheme.CHART_PURPLE));
        row.add(statCard(FlatIcon.Kind.MONEY, String.format("%,.0f₫", dashboardDAO.totalRevenue()), "Doanh thu đã thu", AppTheme.CHART_GOLD));

        return row;
    }

    private JPanel statCard(FlatIcon.Kind kind, String value, String caption, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel iconLabel = new JLabel(new FlatIcon(kind, accent, 22));
        iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(iconLabel);

        card.add(Box.createVerticalStrut(6));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(valueLabel);

        card.add(Box.createVerticalStrut(2));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(captionLabel.getFont().deriveFont(12f));
        captionLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        captionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(captionLabel);

        return card;
    }
}
