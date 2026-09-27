/*
 * Trang Quản lý sách. Chỉ "Sửa thông tin sách" có trang thật phía sau
 * (SearchBookForEditPage -> EditBookPage); "Thêm sách" và "Xóa sách" hiển thị
 * đúng khung theo FR-ADM-02..04 nhưng chưa có trang thật nên bấm vào báo
 * "chưa được hỗ trợ".
 */
package view.admin;
import view.shared.*;

import dao.BookDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Book;

public class ManageBooksPage extends javax.swing.JPanel implements AdminPage {

    private AdminShellFrm shell;

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "ISBN", "Tên sách", "Giá"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    public ManageBooksPage() {
        initComponents();

        pageScroll.getVerticalScrollBar().setUnitIncrement(16);

        AppTheme.primary(btnOpen1);
        AppTheme.primary(btnOpen2);
        AppTheme.primary(btnOpen3);
        btnOpen1.addActionListener(e -> shell.navigateTo(new AddBookPage()));
        btnOpen2.addActionListener(e -> shell.navigateTo(new SearchBookForEditPage()));
        btnOpen3.addActionListener(e -> shell.navigateTo(new DeleteBookPage()));

        AppTheme.link(viewAll, () -> shell.navigateTo(new SearchBookForEditPage()));

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 50, 50);
        AppTheme.alignTableColumn(table, 1, SwingConstants.CENTER, 140, 140);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 3, SwingConstants.RIGHT, 110, 110);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    shell.navigateTo(new SearchBookForEditPage());
                }
            }
        });
    }

    @Override
    public String navKey() {
        return "books";
    }

    @Override
    public String title() {
        return "Quản Lý Sách";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        this.shell = shell;

        // Dữ liệu sách thật, lấy 1 lần duy nhất từ DB (BookDAO.searchBook("")
        // khớp mọi bản ghi vì so khớp LIKE '%%') rồi dùng lại cho cả 3 thẻ
        // thống kê lẫn bảng xem trước bên dưới — không phải số liệu dựng sẵn.
        Book[] books = new BookDAO().searchBook("");

        fillStatRow(books);
        fillPreviewTable(books);

        return this;
    }

    // 3 thẻ thống kê nhanh tính trực tiếp từ mảng sách thật vừa truy vấn —
    // không gọi thêm DAO nào khác, không có số nào là số bịa.
    private void fillStatRow(Book[] books) {
        float avg = 0f, min = 0f, max = 0f;
        if (books.length > 0) {
            float sum = 0f;
            min = books[0].getPrice();
            max = books[0].getPrice();
            for (Book b : books) {
                sum += b.getPrice();
                min = Math.min(min, b.getPrice());
                max = Math.max(max, b.getPrice());
            }
            avg = sum / books.length;
        }

        statValue1.setText(String.valueOf(books.length));
        statValue2.setText(String.format("%,.0f₫", avg));
        statValue3.setText(books.length == 0 ? "—" : String.format("%,.0f₫ - %,.0f₫", min, max));
    }

    /** Huy hiệu tròn nền màu nhạt + icon đậm chính giữa — thay cho icon trần đặt trực tiếp lên nền trắng. */
    private JPanel iconBadge(FlatIcon.Kind kind, Color accent) {
        int size = 44;
        Icon icon = new FlatIcon(kind, accent, 22);
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ChartStyle.lighten(accent, 0.78f));
                g2.fillOval(0, 0, size, size);
                g2.dispose();
                icon.paintIcon(this, g, (size - icon.getIconWidth()) / 2, (size - icon.getIconHeight()) / 2);
            }
        };
        badge.setPreferredSize(new Dimension(size, size));
        badge.setMaximumSize(new Dimension(size, size));
        badge.setMinimumSize(new Dimension(size, size));
        badge.setOpaque(false);
        return badge;
    }

    // Bảng xem trước 1 phần kho sách thật (không phân trang/tìm kiếm — đó là
    // việc của SearchBookForEditPage), giúp trang này không trống trải; bấm
    // "Xem tất cả" hoặc double-click 1 dòng đều dẫn sang đúng luồng tìm & sửa
    // sách đã có sẵn (SearchBookForEditPage -> EditBookPage).
    private void fillPreviewTable(Book[] books) {
        previewTitle.setText("Tổng quan kho sách (" + books.length + " đầu sách)");

        model.setRowCount(0);
        int previewCount = Math.min(books.length, 6);
        for (int i = 0; i < previewCount; i++) {
            Book b = books[i];
            model.addRow(new Object[]{b.getBookID(), b.getISBN(), b.getTitle(), String.format("%,.0f₫", b.getPrice())});
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pageScroll = new javax.swing.JScrollPane();
        top = new javax.swing.JPanel();
        heading = new javax.swing.JLabel();
        statRow = new javax.swing.JPanel();
        statCard1 = new javax.swing.JPanel();
        statIcon1 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        statValue1 = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        statCaption1 = new javax.swing.JLabel();
        statCard2 = new javax.swing.JPanel();
        statIcon2 = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        statValue2 = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        statCaption2 = new javax.swing.JLabel();
        statCard3 = new javax.swing.JPanel();
        statIcon3 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        statValue3 = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        statCaption3 = new javax.swing.JLabel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 18), new java.awt.Dimension(0, 18), new java.awt.Dimension(32767, 18));
        actionRow = new javax.swing.JPanel();
        actionCard1 = new javax.swing.JPanel();
        badge1 = iconBadge(FlatIcon.Kind.PLUS, AppTheme.CHART_SKY);
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 12), new java.awt.Dimension(0, 12), new java.awt.Dimension(32767, 12));
        actionTitle1 = new javax.swing.JLabel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        actionDesc1 = new javax.swing.JLabel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        btnOpen1 = new javax.swing.JButton();
        actionCard2 = new javax.swing.JPanel();
        badge2 = iconBadge(FlatIcon.Kind.BOOK, AppTheme.CHART_GOLD);
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 12), new java.awt.Dimension(0, 12), new java.awt.Dimension(32767, 12));
        actionTitle2 = new javax.swing.JLabel();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        actionDesc2 = new javax.swing.JLabel();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        btnOpen2 = new javax.swing.JButton();
        actionCard3 = new javax.swing.JPanel();
        badge3 = iconBadge(FlatIcon.Kind.TRASH, AppTheme.CHART_RED);
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 12), new java.awt.Dimension(0, 12), new java.awt.Dimension(32767, 12));
        actionTitle3 = new javax.swing.JLabel();
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        actionDesc3 = new javax.swing.JLabel();
        filler18 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler19 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        btnOpen3 = new javax.swing.JButton();
        filler20 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 18), new java.awt.Dimension(0, 18), new java.awt.Dimension(32767, 18));
        previewCard = new javax.swing.JPanel();
        previewHeader = new javax.swing.JPanel();
        previewTitle = new javax.swing.JLabel();
        viewAll = new javax.swing.JLabel();
        previewScroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setBackground(AppTheme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        setLayout(new java.awt.BorderLayout());

        pageScroll.setBorder(null);
        top.setBackground(AppTheme.BACKGROUND);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setText("Quản lý sách");
        heading.setAlignmentX(0.0F);
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));
        top.add(heading);
        statRow.setBackground(AppTheme.BACKGROUND);
        statRow.setAlignmentX(0.0F);
        statRow.setMaximumSize(new java.awt.Dimension(2147483647, 96));
        statRow.setPreferredSize(new java.awt.Dimension(0, 96));
        statRow.setLayout(new java.awt.GridLayout(1, 3, 18, 0));
        statCard1.setBackground(new java.awt.Color(255, 255, 255));
        statCard1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_SKY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        statCard1.setLayout(new javax.swing.BoxLayout(statCard1, javax.swing.BoxLayout.Y_AXIS));
        statIcon1.setIcon(new FlatIcon(FlatIcon.Kind.BOOK, AppTheme.CHART_SKY, 22));
        statIcon1.setAlignmentX(0.0F);
        statCard1.add(statIcon1);
        statCard1.add(filler1);
        statValue1.setFont(statValue1.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        statValue1.setForeground(AppTheme.TEXT);
        statValue1.setText("0");
        statValue1.setAlignmentX(0.0F);
        statCard1.add(statValue1);
        statCard1.add(filler2);
        statCaption1.setFont(statCaption1.getFont().deriveFont(12f));
        statCaption1.setForeground(new java.awt.Color(140, 123, 109));
        statCaption1.setText("Tổng số sách");
        statCaption1.setAlignmentX(0.0F);
        statCard1.add(statCaption1);
        statRow.add(statCard1);

        statCard2.setBackground(new java.awt.Color(255, 255, 255));
        statCard2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_GOLD), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        statCard2.setLayout(new javax.swing.BoxLayout(statCard2, javax.swing.BoxLayout.Y_AXIS));
        statIcon2.setIcon(new FlatIcon(FlatIcon.Kind.MONEY, AppTheme.CHART_GOLD, 22));
        statIcon2.setAlignmentX(0.0F);
        statCard2.add(statIcon2);
        statCard2.add(filler3);
        statValue2.setFont(statValue2.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        statValue2.setForeground(AppTheme.TEXT);
        statValue2.setText("0₫");
        statValue2.setAlignmentX(0.0F);
        statCard2.add(statValue2);
        statCard2.add(filler4);
        statCaption2.setFont(statCaption2.getFont().deriveFont(12f));
        statCaption2.setForeground(new java.awt.Color(140, 123, 109));
        statCaption2.setText("Giá trung bình");
        statCaption2.setAlignmentX(0.0F);
        statCard2.add(statCaption2);
        statRow.add(statCard2);

        statCard3.setBackground(new java.awt.Color(255, 255, 255));
        statCard3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_PURPLE), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        statCard3.setLayout(new javax.swing.BoxLayout(statCard3, javax.swing.BoxLayout.Y_AXIS));
        statIcon3.setIcon(new FlatIcon(FlatIcon.Kind.CHART, AppTheme.CHART_PURPLE, 22));
        statIcon3.setAlignmentX(0.0F);
        statCard3.add(statIcon3);
        statCard3.add(filler5);
        statValue3.setFont(statValue3.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        statValue3.setForeground(AppTheme.TEXT);
        statValue3.setText("—");
        statValue3.setAlignmentX(0.0F);
        statCard3.add(statValue3);
        statCard3.add(filler6);
        statCaption3.setFont(statCaption3.getFont().deriveFont(12f));
        statCaption3.setForeground(new java.awt.Color(140, 123, 109));
        statCaption3.setText("Khoảng giá");
        statCaption3.setAlignmentX(0.0F);
        statCard3.add(statCaption3);
        statRow.add(statCard3);

        top.add(statRow);

        top.add(filler7);
        actionRow.setBackground(AppTheme.BACKGROUND);
        actionRow.setAlignmentX(0.0F);
        actionRow.setMaximumSize(new java.awt.Dimension(2147483647, 190));
        actionRow.setPreferredSize(new java.awt.Dimension(0, 190));
        actionRow.setLayout(new java.awt.GridLayout(1, 3, 18, 0));
        actionCard1.setBackground(new java.awt.Color(255, 255, 255));
        actionCard1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_SKY), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        actionCard1.setLayout(new javax.swing.BoxLayout(actionCard1, javax.swing.BoxLayout.Y_AXIS));
        badge1.setAlignmentX(0.0F);
        actionCard1.add(badge1);
        actionCard1.add(filler8);
        actionTitle1.setFont(actionTitle1.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        actionTitle1.setForeground(AppTheme.TEXT);
        actionTitle1.setText("Thêm sách");
        actionTitle1.setAlignmentX(0.0F);
        actionCard1.add(actionTitle1);
        actionCard1.add(filler9);
        actionDesc1.setFont(actionDesc1.getFont().deriveFont(12f));
        actionDesc1.setForeground(new java.awt.Color(140, 123, 109));
        actionDesc1.setText("<html>Thêm một đầu sách mới vào kho.</html>");
        actionDesc1.setAlignmentX(0.0F);
        actionCard1.add(actionDesc1);
        actionCard1.add(filler10);
        actionCard1.add(filler11);
        btnOpen1.setText("Mở");
        btnOpen1.setAlignmentX(0.0F);
        actionCard1.add(btnOpen1);
        actionRow.add(actionCard1);

        actionCard2.setBackground(new java.awt.Color(255, 255, 255));
        actionCard2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_GOLD), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        actionCard2.setLayout(new javax.swing.BoxLayout(actionCard2, javax.swing.BoxLayout.Y_AXIS));
        badge2.setAlignmentX(0.0F);
        actionCard2.add(badge2);
        actionCard2.add(filler12);
        actionTitle2.setFont(actionTitle2.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        actionTitle2.setForeground(AppTheme.TEXT);
        actionTitle2.setText("Sửa thông tin sách");
        actionTitle2.setAlignmentX(0.0F);
        actionCard2.add(actionTitle2);
        actionCard2.add(filler13);
        actionDesc2.setFont(actionDesc2.getFont().deriveFont(12f));
        actionDesc2.setForeground(new java.awt.Color(140, 123, 109));
        actionDesc2.setText("<html>Tìm và chỉnh sửa thông tin sách đã có.</html>");
        actionDesc2.setAlignmentX(0.0F);
        actionCard2.add(actionDesc2);
        actionCard2.add(filler14);
        actionCard2.add(filler15);
        btnOpen2.setText("Mở");
        btnOpen2.setAlignmentX(0.0F);
        actionCard2.add(btnOpen2);
        actionRow.add(actionCard2);

        actionCard3.setBackground(new java.awt.Color(255, 255, 255));
        actionCard3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.CHART_RED), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        actionCard3.setLayout(new javax.swing.BoxLayout(actionCard3, javax.swing.BoxLayout.Y_AXIS));
        badge3.setAlignmentX(0.0F);
        actionCard3.add(badge3);
        actionCard3.add(filler16);
        actionTitle3.setFont(actionTitle3.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        actionTitle3.setForeground(AppTheme.TEXT);
        actionTitle3.setText("Xóa sách");
        actionTitle3.setAlignmentX(0.0F);
        actionCard3.add(actionTitle3);
        actionCard3.add(filler17);
        actionDesc3.setFont(actionDesc3.getFont().deriveFont(12f));
        actionDesc3.setForeground(new java.awt.Color(140, 123, 109));
        actionDesc3.setText("<html>Xóa một đầu sách khỏi kho.</html>");
        actionDesc3.setAlignmentX(0.0F);
        actionCard3.add(actionDesc3);
        actionCard3.add(filler18);
        actionCard3.add(filler19);
        btnOpen3.setText("Mở");
        btnOpen3.setAlignmentX(0.0F);
        actionCard3.add(btnOpen3);
        actionRow.add(actionCard3);

        top.add(actionRow);

        top.add(filler20);
        previewCard.setBackground(new java.awt.Color(255, 255, 255));
        previewCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        previewCard.setAlignmentX(0.0F);
        previewCard.setMaximumSize(new java.awt.Dimension(2147483647, 300));
        previewCard.setPreferredSize(new java.awt.Dimension(0, 300));
        previewCard.setLayout(new java.awt.BorderLayout(0, 10));
        previewHeader.setBackground(new java.awt.Color(255, 255, 255));
        previewHeader.setLayout(new java.awt.BorderLayout());
        previewTitle.setFont(previewTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        previewTitle.setForeground(AppTheme.TEXT);
        previewTitle.setText("Tổng quan kho sách (0 đầu sách)");
        previewHeader.add(previewTitle, java.awt.BorderLayout.WEST);
        viewAll.setText("Xem tất cả / Sửa →");
        previewHeader.add(viewAll, java.awt.BorderLayout.EAST);
        previewCard.add(previewHeader, java.awt.BorderLayout.NORTH);

        previewScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        table.setGridColor(AppTheme.TABLE_GRID);
        table.setRowHeight(28);
        table.setShowGrid(true);
        previewScroll.setViewportView(table);
        previewCard.add(previewScroll, java.awt.BorderLayout.CENTER);

        top.add(previewCard);

        pageScroll.setViewportView(top);

        add(pageScroll, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionCard1;
    private javax.swing.JPanel actionCard2;
    private javax.swing.JPanel actionCard3;
    private javax.swing.JLabel actionDesc1;
    private javax.swing.JLabel actionDesc2;
    private javax.swing.JLabel actionDesc3;
    private javax.swing.JPanel actionRow;
    private javax.swing.JLabel actionTitle1;
    private javax.swing.JLabel actionTitle2;
    private javax.swing.JLabel actionTitle3;
    private javax.swing.JPanel badge1;
    private javax.swing.JPanel badge2;
    private javax.swing.JPanel badge3;
    private javax.swing.JButton btnOpen1;
    private javax.swing.JButton btnOpen2;
    private javax.swing.JButton btnOpen3;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler18;
    private javax.swing.Box.Filler filler19;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler20;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel heading;
    private javax.swing.JScrollPane pageScroll;
    private javax.swing.JPanel previewCard;
    private javax.swing.JPanel previewHeader;
    private javax.swing.JScrollPane previewScroll;
    private javax.swing.JLabel previewTitle;
    private javax.swing.JLabel statCaption1;
    private javax.swing.JLabel statCaption2;
    private javax.swing.JLabel statCaption3;
    private javax.swing.JPanel statCard1;
    private javax.swing.JPanel statCard2;
    private javax.swing.JPanel statCard3;
    private javax.swing.JLabel statIcon1;
    private javax.swing.JLabel statIcon2;
    private javax.swing.JLabel statIcon3;
    private javax.swing.JPanel statRow;
    private javax.swing.JLabel statValue1;
    private javax.swing.JLabel statValue2;
    private javax.swing.JLabel statValue3;
    private javax.swing.JTable table;
    private javax.swing.JPanel top;
    private javax.swing.JLabel viewAll;
    // End of variables declaration//GEN-END:variables
}
