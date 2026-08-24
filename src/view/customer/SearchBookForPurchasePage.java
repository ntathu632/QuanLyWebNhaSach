/*
 * Trang tìm sách để mua — lưới thẻ sách (kiểu cửa hàng, khác bảng dữ liệu bên
 * Admin) thay vì JTable, phù hợp hơn cho trải nghiệm mua sắm. Mở lên hiện sẵn
 * toàn bộ sách thật, gõ từ khóa rồi Tìm kiếm (hoặc Enter) để lọc. Có thể mở
 * sẵn với 1 từ khóa (vd. gõ từ ô tìm kiếm ở navbar) qua presetKeyword(), hoặc
 * lọc sẵn theo 1 danh mục (vd. bấm từ lưới danh mục ở trang chủ) qua presetCategory().
 */
package view.customer;
import view.shared.*;

import dao.BookDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Book;

public class SearchBookForPurchasePage implements CustomerPage {

    private String initialKeyword = "";
    private Integer presetCategoryId;
    private String presetCategoryName;

    /** Đặt sẵn từ khóa để tìm ngay khi trang được mở (dùng khi tìm từ ô search ở navbar). */
    public void presetKeyword(String keyword) {
        this.initialKeyword = keyword == null ? "" : keyword;
    }

    /** Lọc sẵn theo 1 danh mục khi trang được mở (dùng khi bấm 1 danh mục ở trang chủ). */
    public void presetCategory(int categoryId, String categoryName) {
        this.presetCategoryId = categoryId;
        this.presetCategoryName = categoryName;
    }

    @Override
    public String navKey() {
        return "search";
    }

    @Override
    public String title() {
        return "Tìm kiếm sách";
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Tìm kiếm sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField(initialKeyword);
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã sách, ISBN hoặc tên sách...");
        txtKeyword.putClientProperty("FlatLaf.style", "arc: 10");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        header.add(searchBar);

        header.add(Box.createVerticalStrut(8));

        JLabel categoryBadge = new JLabel();
        categoryBadge.setFont(categoryBadge.getFont().deriveFont(Font.BOLD, 12f));
        categoryBadge.setForeground(AppTheme.PRIMARY_HOVER);
        categoryBadge.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        categoryBadge.setVisible(false);
        header.add(categoryBadge);

        JLabel resultCount = new JLabel();
        resultCount.setFont(resultCount.getFont().deriveFont(12f));
        resultCount.setForeground(new Color(0x8A, 0x83, 0x74));
        resultCount.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        header.add(resultCount);

        content.add(header, BorderLayout.NORTH);

        JPanel resultGrid = new JPanel(new GridLayout(0, 4, 16, 16));
        resultGrid.setBackground(AppTheme.BACKGROUND);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(AppTheme.BACKGROUND);
        gridWrapper.setBorder(new EmptyBorder(0, 0, 20, 0));
        gridWrapper.add(resultGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        final boolean[] categoryMode = {presetCategoryId != null};
        if (categoryMode[0]) {
            categoryBadge.setText("Danh mục: " + presetCategoryName + "   (gõ tìm kiếm để xem tất cả sách)");
            categoryBadge.setVisible(true);
        }

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Book[] found;
            if (categoryMode[0] && keyword.isEmpty()) {
                found = new BookDAO().searchByCategory(presetCategoryId);
            } else {
                categoryMode[0] = false;
                categoryBadge.setVisible(false);
                found = new BookDAO().searchBook(keyword);
            }
            resultGrid.removeAll();
            for (Book b : found) {
                resultGrid.add(BookCard.build(b, () -> shell.navigateTo(new BookDetailPage(b))));
            }
            resultCount.setText("Tìm thấy " + found.length + " cuốn sách");
            resultGrid.revalidate();
            resultGrid.repaint();
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        doSearch.run();

        return content;
    }
}
