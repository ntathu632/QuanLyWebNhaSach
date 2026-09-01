/*
 * Trang thêm sách mới (FR-ADM-01, Sub-UC Add Book). Validate ISBN/Tên sách
 * bắt buộc và giá > 0 (Book.price CHECK) trước khi gọi BookDAO.addBook() —
 * trùng ISBN (Book.ISBN UNIQUE) được BookDAO tự kiểm tra và trả thông báo.
 */
package view.admin;
import view.shared.*;

import dao.AuthorDAO;
import dao.BookDAO;
import dao.CategoryDAO;
import dao.PublisherDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import model.Author;
import model.Book;
import model.Category;
import model.Publisher;

public class AddBookPage implements AdminPage {

    @Override
    public String navKey() {
        return "books";
    }

    @Override
    public String title() {
        return "Thêm Sách";
    }

    @Override
    public AdminPage backTarget() {
        return new ManageBooksPage();
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Thêm sách mới");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(java.awt.Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(24, 24, 24, 24)));
        formCard.setMaximumSize(new Dimension(620, 640));
        formCard.setPreferredSize(new Dimension(620, 640));
        formCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JTextField txtISBN = new JTextField();
        formRow(formCard, gbc, 0, "ISBN: *", txtISBN);

        JTextField txtTitle = new JTextField();
        formRow(formCard, gbc, 1, "Tên sách: *", txtTitle);

        JTextField txtPrice = new JTextField();
        formRow(formCard, gbc, 2, "Giá bán: *", txtPrice);

        Publisher[] publishers = new PublisherDAO().searchPublisher("");
        JComboBox<Publisher> cboPublisher = new JComboBox<>(publishers);
        formRow(formCard, gbc, 3, "Nhà xuất bản: *", cboPublisher);

        Category[] categories = new CategoryDAO().searchCategory("");
        JList<Category> lstCategory = new JList<>(categories);
        lstCategory.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstCategory.setVisibleRowCount(4);
        JScrollPane categoryScroll = new JScrollPane(lstCategory);
        categoryScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        formRow(formCard, gbc, 4, "Danh mục:", categoryScroll);

        Author[] authors = new AuthorDAO().searchAuthor("");
        JList<Author> lstAuthor = new JList<>(authors);
        lstAuthor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstAuthor.setVisibleRowCount(4);
        JScrollPane authorScroll = new JScrollPane(lstAuthor);
        authorScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        formRow(formCard, gbc, 5, "Tác giả:", authorScroll);

        JTextArea txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        formRow(formCard, gbc, 6, "Mô tả:", descScroll);

        JTextField txtImage = new JTextField();
        formRow(formCard, gbc, 7, "Hình ảnh:", txtImage);

        top.add(formCard);
        top.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(620, 40));

        buttonRow.add(Box.createHorizontalGlue());

        JButton btnCancel = new JButton("Hủy");
        AppTheme.secondary(btnCancel);
        btnCancel.addActionListener(e -> shell.navigateTo(new ManageBooksPage()));
        buttonRow.add(btnCancel);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnAdd = new JButton("Thêm sách");
        AppTheme.primary(btnAdd);
        btnAdd.addActionListener(e -> onAdd(shell, txtISBN, txtTitle, txtPrice, cboPublisher, lstCategory, lstAuthor, txtDescription, txtImage));
        buttonRow.add(btnAdd);

        top.add(buttonRow);

        JScrollPane pageScroll = new JScrollPane(top);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(pageScroll, BorderLayout.CENTER);
        return content;
    }

    private void formRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, 0, 8, 14);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(AppTheme.TEXT);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void onAdd(AdminShellFrm shell, JTextField txtISBN, JTextField txtTitle,
            JTextField txtPrice, JComboBox<Publisher> cboPublisher, JList<Category> lstCategory,
            JList<Author> lstAuthor, JTextArea txtDescription, JTextField txtImage) {
        String isbn = txtISBN.getText().trim();
        String title = txtTitle.getText().trim();

        if (isbn.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(shell, "Vui lòng nhập đầy đủ ISBN và Tên sách!");
            return;
        }

        float price;
        try {
            price = Float.parseFloat(txtPrice.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(shell, "Giá bán không hợp lệ!");
            return;
        }
        if (price <= 0) {
            JOptionPane.showMessageDialog(shell, "Giá bán phải lớn hơn 0!");
            return;
        }

        Publisher selectedPublisher = (Publisher) cboPublisher.getSelectedItem();
        if (selectedPublisher == null) {
            JOptionPane.showMessageDialog(shell, "Vui lòng chọn Nhà xuất bản!");
            return;
        }

        Book book = new Book();
        book.setISBN(isbn);
        book.setTitle(title);
        book.setPrice(price);
        book.setDescription(txtDescription.getText().trim());
        book.setImage(txtImage.getText().trim());

        List<Integer> categoryIDs = new ArrayList<>();
        for (Category c : lstCategory.getSelectedValuesList()) {
            categoryIDs.add(c.getCategoryID());
        }
        List<Integer> authorIDs = new ArrayList<>();
        for (Author a : lstAuthor.getSelectedValuesList()) {
            authorIDs.add(a.getAuthorID());
        }

        String error = new BookDAO().addBook(book, selectedPublisher.getPublisherID(), categoryIDs, authorIDs);
        if (error == null) {
            JOptionPane.showMessageDialog(shell, "Thêm sách thành công!");
            shell.navigateTo(new ManageBooksPage());
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }
}
