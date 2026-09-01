/*
 * Trang chi tiết sách — bìa lớn (vẽ tay) + thông tin + chọn số lượng + thêm
 * vào giỏ hàng. Nhận sẵn Book qua constructor, giống EditBookPage bên Admin.
 */
package view.customer;
import view.shared.*;

import dao.ReviewDAO;
import dao.ShoppingCartDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Book;
import model.CartItem;
import model.Review;
import model.ShoppingCart;

public class BookDetailPage implements CustomerPage {

    private final Book book;

    public BookDetailPage(Book book) {
        this.book = book;
    }

    @Override
    public String navKey() {
        return "search";
    }

    @Override
    public String title() {
        return "Chi tiết sách";
    }

    @Override
    public CustomerPage backTarget() {
        return new SearchBookForPurchasePage();
    }

    @Override
    public JPanel content(CustomerShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        ShadowCard card = new ShadowCard(new BorderLayout(28, 0), 16, 24);
        card.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(1000, 428));
        card.setPreferredSize(new Dimension(1000, 428));

        BookCoverPanel cover = new BookCoverPanel(book.getTitle(), book.getBookID(), book.getImage());
        cover.setPreferredSize(new Dimension(260, 0));
        card.add(cover, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titleLabel = new JLabel(book.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(AppTheme.TEXT);
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(titleLabel);

        info.add(Box.createVerticalStrut(8));

        JLabel isbnLabel = new JLabel("ISBN: " + book.getISBN());
        isbnLabel.setFont(isbnLabel.getFont().deriveFont(12f));
        isbnLabel.setForeground(new Color(0x8A, 0x83, 0x74));
        isbnLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(isbnLabel);

        info.add(Box.createVerticalStrut(14));

        JLabel priceLabel = new JLabel(String.format("%,.0f₫", book.getPrice()));
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 22f));
        priceLabel.setForeground(AppTheme.ACCENT_TEXT);
        priceLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(priceLabel);

        info.add(Box.createVerticalStrut(14));

        JLabel descLabel = new JLabel("<html><div style='width:560px'>"
                + (book.getDescription() == null ? "" : book.getDescription().replace("&", "&amp;").replace("<", "&lt;"))
                + "</div></html>");
        descLabel.setFont(descLabel.getFont().deriveFont(13f));
        descLabel.setForeground(AppTheme.TEXT);
        descLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        info.add(descLabel);

        info.add(Box.createVerticalStrut(20));

        JPanel qtyRow = new JPanel();
        qtyRow.setLayout(new BoxLayout(qtyRow, BoxLayout.X_AXIS));
        qtyRow.setOpaque(false);
        qtyRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        qtyRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel qtyLabel = new JLabel("Số lượng:");
        qtyLabel.setForeground(AppTheme.TEXT);
        qtyRow.add(qtyLabel);
        qtyRow.add(Box.createHorizontalStrut(10));

        JTextField txtQuantity = new JTextField("1");
        txtQuantity.setMaximumSize(new Dimension(60, 30));
        txtQuantity.setPreferredSize(new Dimension(60, 30));
        qtyRow.add(txtQuantity);

        info.add(qtyRow);
        info.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JButton btnAddToCart = new JButton("Thêm vào giỏ hàng");
        AppTheme.primary(btnAddToCart);
        btnAddToCart.addActionListener(e -> addToCart(shell, txtQuantity));
        buttonRow.add(btnAddToCart);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnViewCart = new JButton("Xem giỏ hàng");
        AppTheme.secondary(btnViewCart);
        btnViewCart.addActionListener(e -> shell.navigateTo(new ShoppingCartPage()));
        buttonRow.add(btnViewCart);

        info.add(buttonRow);

        card.add(info, BorderLayout.CENTER);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);
        top.add(card);
        top.add(Box.createVerticalStrut(20));
        top.add(buildReviewSection(shell));

        content.add(top, BorderLayout.NORTH);
        return content;
    }

    // FR-CUS-09 (Rate Book): danh sách đánh giá thật (Review, bảng mới bổ
    // sung theo SRS) + form gửi đánh giá nếu khách đủ điều kiện (AS-01: đã
    // mua sách này và đơn đã "Paid") và chưa từng đánh giá cuốn này.
    private JPanel buildReviewSection(CustomerShellFrm shell) {
        ReviewDAO reviewDAO = new ReviewDAO();
        List<Review> reviews = reviewDAO.getReviewsForBook(book.getBookID());
        float avg = reviewDAO.averageRating(book.getBookID());

        ShadowCard sectionCard = new ShadowCard(new BorderLayout(0, 14), 16, 20);
        sectionCard.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        sectionCard.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));

        String heading = reviews.isEmpty()
                ? "Đánh giá sách (chưa có đánh giá nào)"
                : String.format("Đánh giá sách — trung bình %.1f/5 (%d lượt)", avg, reviews.size());
        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(headingLabel.getFont().deriveFont(Font.BOLD, 16f));
        headingLabel.setForeground(AppTheme.TEXT);
        sectionCard.add(headingLabel, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        if (!reviews.isEmpty()) {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setOpaque(false);
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            for (Review r : reviews) {
                list.add(reviewRow(r, fmt));
                list.add(Box.createVerticalStrut(10));
            }
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            scroll.setPreferredSize(new Dimension(0, Math.min(220, reviews.size() * 70 + 10)));
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            body.add(scroll);
            body.add(Box.createVerticalStrut(14));
        }

        boolean purchased = reviewDAO.hasPurchased(shell.customer.getCustomerID(), book.getBookID());
        boolean alreadyReviewed = reviewDAO.hasReviewed(shell.customer.getCustomerID(), book.getBookID());

        if (alreadyReviewed) {
            JLabel note = new JLabel("Bạn đã đánh giá cuốn sách này.");
            note.setFont(note.getFont().deriveFont(12f));
            note.setForeground(new Color(0x8A, 0x83, 0x74));
            body.add(note);
        } else if (!purchased) {
            JLabel note = new JLabel("Chỉ khách hàng đã mua và thanh toán thành công cuốn sách này mới có thể đánh giá.");
            note.setFont(note.getFont().deriveFont(12f));
            note.setForeground(new Color(0x8A, 0x83, 0x74));
            body.add(note);
        } else {
            body.add(buildReviewForm(shell, reviewDAO));
        }

        sectionCard.add(body, BorderLayout.CENTER);
        return sectionCard;
    }

    private JPanel reviewRow(Review r, SimpleDateFormat fmt) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel header = new JLabel(r.getCustomerName() + " — " + r.getRating() + "/5 · " + fmt.format(r.getReviewDate()));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12.5f));
        header.setForeground(AppTheme.TEXT);
        header.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        row.add(header);

        if (r.getComment() != null && !r.getComment().isEmpty()) {
            JLabel comment = new JLabel("<html><div style='width:800px'>"
                    + r.getComment().replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
            comment.setFont(comment.getFont().deriveFont(12.5f));
            comment.setForeground(AppTheme.TEXT);
            comment.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            row.add(comment);
        }

        return row;
    }

    private JPanel buildReviewForm(CustomerShellFrm shell, ReviewDAO reviewDAO) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel ratingRow = new JPanel();
        ratingRow.setLayout(new BoxLayout(ratingRow, BoxLayout.X_AXIS));
        ratingRow.setOpaque(false);
        ratingRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel ratingLabel = new JLabel("Điểm đánh giá:");
        ratingLabel.setForeground(AppTheme.TEXT);
        ratingRow.add(ratingLabel);
        ratingRow.add(Box.createHorizontalStrut(10));

        JComboBox<Integer> cboRating = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        ratingRow.add(cboRating);
        form.add(ratingRow);

        form.add(Box.createVerticalStrut(8));

        JTextArea txtComment = new JTextArea(3, 40);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(txtComment);
        commentScroll.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        commentScroll.setMaximumSize(new Dimension(600, 80));
        form.add(commentScroll);

        form.add(Box.createVerticalStrut(10));

        JButton btnSubmit = new JButton("Gửi đánh giá");
        btnSubmit.setAlignmentX(JButton.LEFT_ALIGNMENT);
        AppTheme.primary(btnSubmit);
        btnSubmit.addActionListener(e -> {
            Review review = new Review();
            review.setBookID(book.getBookID());
            review.setCustomerID(shell.customer.getCustomerID());
            review.setRating((Integer) cboRating.getSelectedItem());
            review.setComment(txtComment.getText().trim());

            String error = reviewDAO.addReview(review);
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Cảm ơn bạn đã đánh giá!");
                shell.navigateTo(new BookDetailPage(book));
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
        form.add(btnSubmit);

        return form;
    }

    private void addToCart(CustomerShellFrm shell, JTextField txtQuantity) {
        int quantity;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(shell, "Số lượng không hợp lệ!");
            return;
        }

        float unitPrice = book.getPrice();
        float subtotal = unitPrice * quantity;

        CartItem item = new CartItem(quantity, unitPrice, subtotal, book);

        ShoppingCartDAO shoppingCartDAO = new ShoppingCartDAO();
        ShoppingCart cart = shoppingCartDAO.getOrCreateCart(shell.customer);
        item.setCartID(cart.getCartID());

        if (shoppingCartDAO.addCartItem(item)) {
            shell.refreshCartBadge();
            JOptionPane.showMessageDialog(shell, "Đã thêm sách vào giỏ hàng!");
        } else {
            JOptionPane.showMessageDialog(shell, "Thêm vào giỏ hàng thất bại! Vui lòng thử lại.");
        }
    }
}
