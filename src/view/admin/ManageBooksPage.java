/*
 * Trang Quản lý sách. Chỉ "Sửa thông tin sách" có trang thật phía sau
 * (SearchBookForEditPage -> EditBookPage); "Thêm sách" và "Xóa sách" hiển thị
 * đúng khung theo FR-ADM-02..04 nhưng chưa có trang thật nên bấm vào báo
 * "chưa được hỗ trợ".
 */
package view.admin;
import view.shared.*;

import dao.BookDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Book;

public class ManageBooksPage implements AdminPage {

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
        // Dữ liệu sách thật, lấy 1 lần duy nhất từ DB (BookDAO.searchBook("")
        // khớp mọi bản ghi vì so khớp LIKE '%%') rồi dùng lại cho cả 3 thẻ
        // thống kê lẫn bảng xem trước bên dưới — không phải số liệu dựng sẵn.
        Book[] books = new BookDAO().searchBook("");

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        top.add(buildStatRow(books));
        top.add(Box.createVerticalStrut(18));

        JPanel actionRow = new JPanel(new GridLayout(1, 3, 18, 0));
        actionRow.setBackground(AppTheme.BACKGROUND);
        actionRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        actionRow.setPreferredSize(new Dimension(0, 190));

        actionRow.add(actionCard(FlatIcon.Kind.PLUS, "Thêm sách", "Thêm một đầu sách mới vào kho.",
                AppTheme.CHART_SKY, () -> shell.navigateTo(new AddBookPage())));
        actionRow.add(actionCard(FlatIcon.Kind.BOOK, "Sửa thông tin sách", "Tìm và chỉnh sửa thông tin sách đã có.",
                AppTheme.CHART_GOLD, () -> shell.navigateTo(new SearchBookForEditPage())));
        actionRow.add(actionCard(FlatIcon.Kind.TRASH, "Xóa sách", "Xóa một đầu sách khỏi kho.",
                AppTheme.CHART_RED, () -> shell.navigateTo(new DeleteBookPage())));

        top.add(actionRow);
        top.add(Box.createVerticalStrut(18));
        top.add(buildPreviewTableCard(shell, books));

        JScrollPane pageScroll = new JScrollPane(top);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(pageScroll, BorderLayout.CENTER);
        return content;
    }

    // 3 thẻ thống kê nhanh tính trực tiếp từ mảng sách thật vừa truy vấn —
    // không gọi thêm DAO nào khác, không có số nào là số bịa.
    private JPanel buildStatRow(Book[] books) {
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

        JPanel row = new JPanel(new GridLayout(1, 3, 18, 0));
        row.setBackground(AppTheme.BACKGROUND);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        row.setPreferredSize(new Dimension(0, 96));

        row.add(statCard(FlatIcon.Kind.BOOK, String.valueOf(books.length), "Tổng số sách", AppTheme.CHART_SKY));
        row.add(statCard(FlatIcon.Kind.MONEY, String.format("%,.0f₫", avg), "Giá trung bình", AppTheme.CHART_GOLD));
        row.add(statCard(FlatIcon.Kind.CHART,
                books.length == 0 ? "—" : String.format("%,.0f₫ - %,.0f₫", min, max),
                "Khoảng giá", AppTheme.CHART_PURPLE));

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
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
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

    private JPanel actionCard(FlatIcon.Kind kind, String title, String description, Color accent, Runnable onClick) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(20, 20, 20, 20)));

        JComponent badge = iconBadge(kind, accent);
        badge.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        card.add(badge);

        card.add(Box.createVerticalStrut(12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppTheme.TEXT);
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(4));

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(descLabel.getFont().deriveFont(12f));
        descLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        descLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(descLabel);

        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(10));

        JButton btn = new JButton("Mở");
        btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
        AppTheme.primary(btn);
        btn.addActionListener(e -> onClick.run());
        card.add(btn);

        return card;
    }

    /** Huy hiệu tròn nền màu nhạt + icon đậm chính giữa — thay cho icon trần đặt trực tiếp lên nền trắng. */
    private JComponent iconBadge(FlatIcon.Kind kind, Color accent) {
        int size = 44;
        Icon icon = new FlatIcon(kind, accent, 22);
        JComponent badge = new JComponent() {
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
    private JPanel buildPreviewTableCard(AdminShellFrm shell, Book[] books) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(0, 300));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Tổng quan kho sách (" + books.length + " đầu sách)");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppTheme.TEXT);
        header.add(titleLabel, BorderLayout.WEST);

        JLabel viewAll = new JLabel("Xem tất cả / Sửa →");
        AppTheme.link(viewAll, () -> shell.navigateTo(new SearchBookForEditPage()));
        header.add(viewAll, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "ISBN", "Tên sách", "Giá"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        int previewCount = Math.min(books.length, 6);
        for (int i = 0; i < previewCount; i++) {
            Book b = books[i];
            model.addRow(new Object[]{b.getBookID(), b.getISBN(), b.getTitle(), String.format("%,.0f₫", b.getPrice())});
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(AppTheme.TABLE_GRID);
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }
}
