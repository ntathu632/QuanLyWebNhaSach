/*
 * Trang sửa thông tin sách. Nhận sẵn Book qua constructor — khác với kiểu
 * JFrame trước đây, content(shell) chỉ chạy SAU KHI trang đã được new() đầy
 * đủ nên "book" luôn có sẵn giá trị, không còn cần workaround thứ tự khởi
 * tạo như bản EditBookFrm cũ.
 */
package view.admin;
import view.shared.*;

import dao.AuthorDAO;
import dao.BookDAO;
import dao.CategoryDAO;
import dao.PublisherDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
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

public class EditBookPage implements AdminPage {

    private static final DecimalFormat PRICE_FMT = new DecimalFormat("0.##");

    private final Book book;

    public EditBookPage(Book book) {
        this.book = book;
    }

    @Override
    public String navKey() {
        return "books";
    }

    @Override
    public String title() {
        return "Sửa Thông Tin Sách";
    }

    @Override
    public AdminPage backTarget() {
        return new SearchBookForEditPage();
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Sửa thông tin sách");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        top.add(heading);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(24, 24, 24, 24)));
        formCard.setMaximumSize(new Dimension(620, 680));
        formCard.setPreferredSize(new Dimension(620, 680));
        formCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblBookID = new JLabel(String.valueOf(book.getBookID()));
        lblBookID.setForeground(AppTheme.TEXT);
        lblBookID.setFont(lblBookID.getFont().deriveFont(Font.BOLD));
        formRow(formCard, gbc, 0, "Mã sách:", lblBookID);

        JTextField txtISBN = new JTextField(book.getISBN());
        formRow(formCard, gbc, 1, "ISBN:", txtISBN);

        JTextField txtTitle = new JTextField(book.getTitle());
        formRow(formCard, gbc, 2, "Tên sách:", txtTitle);

        JTextField txtPrice = new JTextField(PRICE_FMT.format(book.getPrice()));
        formRow(formCard, gbc, 3, "Giá bán:", txtPrice);

        BookDAO bookDAOForLoad = new BookDAO();
        int currentPublisherID = bookDAOForLoad.getPublisherID(book.getBookID());
        List<Integer> currentCategoryIDs = bookDAOForLoad.getCategoryIDs(book.getBookID());
        List<Integer> currentAuthorIDs = bookDAOForLoad.getAuthorIDs(book.getBookID());

        Publisher[] publishers = new PublisherDAO().searchPublisher("");
        JComboBox<Publisher> cboPublisher = new JComboBox<>(publishers);
        for (Publisher p : publishers) {
            if (p.getPublisherID() == currentPublisherID) {
                cboPublisher.setSelectedItem(p);
                break;
            }
        }
        formRow(formCard, gbc, 4, "Nhà xuất bản:", cboPublisher);

        Category[] categories = new CategoryDAO().searchCategory("");
        JList<Category> lstCategory = new JList<>(categories);
        lstCategory.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstCategory.setVisibleRowCount(4);
        List<Integer> selectedCategoryIndexes = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            if (currentCategoryIDs.contains(categories[i].getCategoryID())) {
                selectedCategoryIndexes.add(i);
            }
        }
        lstCategory.setSelectedIndices(selectedCategoryIndexes.stream().mapToInt(Integer::intValue).toArray());
        JScrollPane categoryScroll = new JScrollPane(lstCategory);
        categoryScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        formRow(formCard, gbc, 5, "Danh mục:", categoryScroll);

        Author[] authors = new AuthorDAO().searchAuthor("");
        JList<Author> lstAuthor = new JList<>(authors);
        lstAuthor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstAuthor.setVisibleRowCount(4);
        List<Integer> selectedAuthorIndexes = new ArrayList<>();
        for (int i = 0; i < authors.length; i++) {
            if (currentAuthorIDs.contains(authors[i].getAuthorID())) {
                selectedAuthorIndexes.add(i);
            }
        }
        lstAuthor.setSelectedIndices(selectedAuthorIndexes.stream().mapToInt(Integer::intValue).toArray());
        JScrollPane authorScroll = new JScrollPane(lstAuthor);
        authorScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        formRow(formCard, gbc, 6, "Tác giả:", authorScroll);

        JTextArea txtDescription = new JTextArea(book.getDescription(), 4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        formRow(formCard, gbc, 7, "Mô tả:", descScroll);

        JTextField txtImage = new JTextField(book.getImage());
        formRow(formCard, gbc, 8, "Hình ảnh:", txtImage);

        top.add(formCard);
        top.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(620, 40));

        buttonRow.add(Box.createHorizontalGlue());

        JButton btnReset = new JButton("Reset");
        AppTheme.secondary(btnReset);
        btnReset.addActionListener(e -> {
            txtISBN.setText(book.getISBN());
            txtTitle.setText(book.getTitle());
            txtPrice.setText(PRICE_FMT.format(book.getPrice()));
            for (Publisher p : publishers) {
                if (p.getPublisherID() == currentPublisherID) {
                    cboPublisher.setSelectedItem(p);
                    break;
                }
            }
            lstCategory.setSelectedIndices(selectedCategoryIndexes.stream().mapToInt(Integer::intValue).toArray());
            lstAuthor.setSelectedIndices(selectedAuthorIndexes.stream().mapToInt(Integer::intValue).toArray());
            txtDescription.setText(book.getDescription());
            txtImage.setText(book.getImage());
        });
        buttonRow.add(btnReset);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnSave = new JButton("Lưu");
        AppTheme.primary(btnSave);
        btnSave.addActionListener(e -> onSave(shell, txtISBN, txtTitle, txtPrice, cboPublisher, lstCategory, lstAuthor, txtDescription, txtImage));
        buttonRow.add(btnSave);

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

    private void onSave(AdminShellFrm shell, JTextField txtISBN, JTextField txtTitle,
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

        Publisher selectedPublisher = (Publisher) cboPublisher.getSelectedItem();
        if (selectedPublisher == null) {
            JOptionPane.showMessageDialog(shell, "Vui lòng chọn Nhà xuất bản!");
            return;
        }

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

        BookDAO bookDAO = new BookDAO();
        if (bookDAO.updateBook(book, selectedPublisher.getPublisherID(), categoryIDs, authorIDs)) {
            JOptionPane.showMessageDialog(shell, "Cập nhật sách thành công!");
            shell.navigateTo(new ManageBooksPage());
        } else {
            JOptionPane.showMessageDialog(shell, "Cập nhật thất bại! Vui lòng thử lại.");
        }
    }
}
