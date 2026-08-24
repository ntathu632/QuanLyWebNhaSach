/*
 * Trang chủ Khách hàng — banner chào mừng (vẽ tay Java2D, không có ảnh thật
 * trong dự án nên không dùng ảnh chụp), dải tính năng, và lưới "Sách nổi
 * bật" lấy dữ liệu thật từ BookDAO (không phải sách dựng sẵn để trang trí).
 * Dùng ShadowCard (bo góc + đổ bóng mềm) cho mọi khối, tham khảo phong cách
 * card nổi khối của các trang thương mại điện tử thật (Fahasa, Tiki, Amazon...).
 */
package view.customer;
import view.shared.*;

import dao.BookDAO;
import dao.CategoryDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import model.Book;
import model.Category;

public class CustomerHomePage implements CustomerPage {

    @Override
    public String navKey() {
        return "home";
    }

    @Override
    public String title() {
        return "Trang chủ";
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BACKGROUND);
        body.setBorder(new EmptyBorder(28, 28, 28, 28));
        body.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        body.add(buildHeroBanner(shell));
        body.add(Box.createVerticalStrut(26));
        body.add(buildFeatureStrip());
        body.add(Box.createVerticalStrut(28));
        body.add(buildCategorySection(shell));
        body.add(Box.createVerticalStrut(28));
        body.add(buildBestSellers(shell));
        body.add(Box.createVerticalStrut(28));
        body.add(buildFeaturedBooks(shell));

        return body;
    }

    private JPanel buildHeroBanner(CustomerShellFrm shell) {
        int shadow = 8;
        JPanel hero = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth() - shadow;
                int h = getHeight() - shadow;

                for (int i = shadow; i > 0; i--) {
                    int alpha = Math.min((int) (3.2f * (shadow - i + 1)), 26);
                    g2.setColor(new Color(0x3A, 0x33, 0x28, alpha));
                    g2.fill(new RoundRectangle2D.Float(i * 0.5f, i * 0.6f + 2, w, h, 18, 18));
                }

                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, w, h, 18, 18);
                g2.setPaint(new GradientPaint(0, 0, AppTheme.SECONDARY, w, h, AppTheme.HEADER));
                g2.fill(shape);

                g2.setClip(shape);
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fillOval(w - 260, -110, 360, 360);
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fillOval(w - 120, h - 170, 280, 280);
                g2.setClip(null);

                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(0, 236));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 236));
        hero.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        hero.setBorder(new EmptyBorder(0, 44, shadow + 6, 44));

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);

        JLabel headline = new JLabel("Khám phá thế giới sách");
        headline.setFont(headline.getFont().deriveFont(Font.BOLD, 30f));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        textBox.add(headline);

        textBox.add(Box.createVerticalStrut(10));

        JLabel subtitle = new JLabel("Tìm và đặt mua những cuốn sách bạn yêu thích chỉ trong vài bước.");
        subtitle.setFont(subtitle.getFont().deriveFont(15f));
        subtitle.setForeground(new Color(255, 255, 255, 230));
        subtitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        textBox.add(subtitle);

        textBox.add(Box.createVerticalStrut(22));

        JButton cta = new JButton("Tìm sách ngay");
        cta.setAlignmentX(JButton.LEFT_ALIGNMENT);
        cta.putClientProperty("FlatLaf.style",
                "background: #FFFFFF; foreground: #4F5D42; hoverBackground: #F3EEDD;"
                + " pressedBackground: #DCE3D0; borderWidth: 0; arc: 10; font: bold");
        cta.addActionListener(e -> shell.navigateTo(new SearchBookForPurchasePage()));
        textBox.add(cta);

        hero.add(textBox);
        return hero;
    }

    private JPanel buildFeatureStrip() {
        JPanel row = new JPanel(new GridLayout(1, 3, 18, 0));
        row.setBackground(AppTheme.BACKGROUND);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setPreferredSize(new Dimension(0, 90));

        row.add(featureCard(FlatIcon.Kind.BOOK, "Kho sách đa dạng", "Nhiều thể loại, cập nhật liên tục", AppTheme.CHART_SKY));
        row.add(featureCard(FlatIcon.Kind.RECEIPT, "Đặt hàng nhanh chóng", "Từ giỏ hàng đến xác nhận chỉ vài bước", AppTheme.CHART_GOLD));
        row.add(featureCard(FlatIcon.Kind.MONEY, "Thanh toán an toàn", "Nhiều phương thức thanh toán", AppTheme.CHART_PURPLE));

        return row;
    }

    private JPanel featureCard(FlatIcon.Kind kind, String title, String desc, Color accent) {
        ShadowCard card = new ShadowCard(new BorderLayout(14, 0), 12, 14);

        JLabel icon = new JLabel(new FlatIcon(kind, accent, 26));
        card.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppTheme.TEXT);
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        text.add(titleLabel);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(descLabel.getFont().deriveFont(12f));
        descLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        descLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        text.add(descLabel);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // Duyệt sách theo danh mục — dữ liệu Category/BookCategory đã có thật
    // trong CSDL từ trước nhưng chưa từng được Customer dùng tới; đây gần
    // như là mục bắt buộc phải có trên trang chủ mọi trang bán sách thật
    // (Fahasa, Tiki, Amazon Books...).
    private JPanel buildCategorySection(CustomerShellFrm shell) {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(AppTheme.BACKGROUND);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Danh mục sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        heading.setForeground(AppTheme.TEXT);
        section.add(heading, BorderLayout.NORTH);

        Category[] categories = new CategoryDAO().searchCategory("");

        JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));
        grid.setBackground(AppTheme.BACKGROUND);
        for (Category c : categories) {
            grid.add(categoryCard(shell, c));
        }

        section.add(grid, BorderLayout.CENTER);
        return section;
    }

    private JPanel categoryCard(CustomerShellFrm shell, Category category) {
        ShadowCard card = new ShadowCard(new BorderLayout(12, 0), 12, 14);
        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(0, 60));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                SearchBookForPurchasePage page = new SearchBookForPurchasePage();
                page.presetCategory(category.getCategoryID(), category.getCategoryName());
                shell.navigateTo(page);
            }
        });

        JLabel icon = new JLabel(new FlatIcon(FlatIcon.Kind.TAG, AppTheme.CHART_SKY, 22));
        card.add(icon, BorderLayout.WEST);

        JLabel nameLabel = new JLabel(category.getCategoryName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        nameLabel.setForeground(AppTheme.TEXT);
        card.add(nameLabel, BorderLayout.CENTER);

        return card;
    }

    // Sách bán chạy thật (BookDAO.bestSellers(), tính theo tổng số lượng
    // trong OrderDetail của mọi đơn) — khác "Khám phá sách" bên dưới vốn chỉ
    // lấy N sách đầu tiên trong kho, không dựa trên doanh số thật.
    private JPanel buildBestSellers(CustomerShellFrm shell) {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(AppTheme.BACKGROUND);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Sách bán chạy");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        heading.setForeground(AppTheme.TEXT);
        section.add(heading, BorderLayout.NORTH);

        Book[] books = new BookDAO().bestSellers(4);

        if (books.length == 0) {
            JLabel empty = new JLabel("Chưa có đơn hàng nào để xếp hạng sách bán chạy.");
            empty.setFont(empty.getFont().deriveFont(12f));
            empty.setForeground(new Color(0x8A, 0x83, 0x74));
            section.add(empty, BorderLayout.CENTER);
            return section;
        }

        JPanel grid = new JPanel(new GridLayout(1, 4, 18, 0));
        grid.setBackground(AppTheme.BACKGROUND);
        for (Book book : books) {
            grid.add(BookCard.build(book, () -> shell.navigateTo(new BookDetailPage(book))));
        }
        for (int i = books.length; i < 4; i++) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            grid.add(filler);
        }

        section.add(grid, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildFeaturedBooks(CustomerShellFrm shell) {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(AppTheme.BACKGROUND);
        section.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Khám phá sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        heading.setForeground(AppTheme.TEXT);
        header.add(heading, BorderLayout.WEST);

        JLabel viewAll = new JLabel("Xem tất cả sách →");
        AppTheme.link(viewAll, () -> shell.navigateTo(new SearchBookForPurchasePage()));
        header.add(viewAll, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        Book[] books = new BookDAO().searchBook("");
        int count = Math.min(books.length, 8);

        JPanel grid = new JPanel(new GridLayout(2, 4, 18, 18));
        grid.setBackground(AppTheme.BACKGROUND);
        for (int i = 0; i < count; i++) {
            Book book = books[i];
            grid.add(BookCard.build(book, () -> shell.navigateTo(new BookDetailPage(book))));
        }
        for (int i = count; i < 8; i++) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            grid.add(filler);
        }

        section.add(grid, BorderLayout.CENTER);
        return section;
    }
}
