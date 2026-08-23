/*
 * Trang chi tiết sách — bìa lớn (vẽ tay) + thông tin + chọn số lượng + thêm
 * vào giỏ hàng. Nhận sẵn Book qua constructor, giống EditBookPage bên Admin.
 */
package view;

import dao.ShoppingCartDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Book;
import model.CartItem;
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
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(900, 428));
        card.setPreferredSize(new Dimension(900, 428));

        BookCoverPanel cover = new BookCoverPanel(book.getTitle(), book.getBookID());
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

        JLabel descLabel = new JLabel("<html><div style='width:480px'>"
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

        content.add(card, BorderLayout.NORTH);
        return content;
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
