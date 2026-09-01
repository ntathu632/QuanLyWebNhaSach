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
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
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
        formCard.setMaximumSize(new Dimension(1160, 520));
        formCard.setPreferredSize(new Dimension(1160, 520));
        formCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblBookID = new JLabel(String.valueOf(book.getBookID()));
        lblBookID.setForeground(AppTheme.TEXT);
        lblBookID.setFont(lblBookID.getFont().deriveFont(Font.BOLD));
        formRow(formCard, gbc, 0, 0, "Mã sách:", lblBookID);

        JTextField txtISBN = new JTextField(book.getISBN());
        formRow(formCard, gbc, 0, 1, "ISBN:", txtISBN);

        JTextField txtTitle = new JTextField(book.getTitle());
        formRow(formCard, gbc, 0, 2, "Tên sách:", txtTitle);

        JTextField txtPrice = new JTextField(PRICE_FMT.format(book.getPrice()));
        formRow(formCard, gbc, 0, 3, "Giá bán:", txtPrice);

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
        formRow(formCard, gbc, 0, 4, "Nhà xuất bản:", cboPublisher);

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
        categoryScroll.setPreferredSize(new Dimension(0, 90));
        categoryScroll.setMinimumSize(new Dimension(0, 90));
        formRow(formCard, gbc, 0, 5, "Danh mục:", categoryScroll);

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
        authorScroll.setPreferredSize(new Dimension(0, 90));
        authorScroll.setMinimumSize(new Dimension(0, 90));
        formRow(formCard, gbc, 0, 6, "Tác giả:", authorScroll);

        JTextArea txtDescription = new JTextArea(book.getDescription(), 8, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));

        File[] pickedImage = new File[1];
        JLabel lblThumbnail = new JLabel("Chưa có ảnh");
        lblThumbnail.setHorizontalAlignment(SwingConstants.CENTER);
        lblThumbnail.setPreferredSize(new Dimension(90, 120));
        lblThumbnail.setMinimumSize(new Dimension(90, 120));
        lblThumbnail.setMaximumSize(new Dimension(90, 120));
        lblThumbnail.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        lblThumbnail.setForeground(AppTheme.TEXT);
        lblThumbnail.setFont(lblThumbnail.getFont().deriveFont(11f));
        File existingImageFile = BookImageStore.resolve(book.getImage());
        if (existingImageFile != null && existingImageFile.isFile()) {
            Image scaled = new ImageIcon(existingImageFile.getPath()).getImage()
                    .getScaledInstance(90, 120, Image.SCALE_SMOOTH);
            lblThumbnail.setIcon(new ImageIcon(scaled));
            lblThumbnail.setText(null);
        }

        JButton btnPickImage = new JButton("Đổi ảnh...");
        AppTheme.secondary(btnPickImage);
        btnPickImage.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (jpg, jpeg, png, gif)", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(shell) == JFileChooser.APPROVE_OPTION) {
                pickedImage[0] = chooser.getSelectedFile();
                Image scaled = new ImageIcon(pickedImage[0].getPath()).getImage()
                        .getScaledInstance(90, 120, Image.SCALE_SMOOTH);
                lblThumbnail.setIcon(new ImageIcon(scaled));
                lblThumbnail.setText(null);
            }
        });

        JPanel imagePicker = new JPanel();
        imagePicker.setLayout(new BoxLayout(imagePicker, BoxLayout.X_AXIS));
        imagePicker.setOpaque(false);
        imagePicker.add(lblThumbnail);
        imagePicker.add(Box.createHorizontalStrut(12));
        imagePicker.add(btnPickImage);

        // Mô tả + Hình ảnh gộp vào 1 panel con (BorderLayout riêng) rồi gán làm
        // MỘT ô duy nhất chiếm hết chiều cao cột phải (gridheight=7) — để BorderLayout
        // tự chia không gian (Mô tả giãn hết phần còn lại, Hình ảnh giữ đúng kích
        // thước cố định), tránh GridBagLayout tính sai chiều cao khi chia theo hàng
        // dùng chung với cột trái (bug từng làm khung ảnh 90x120 bị bóp còn ~20px).
        JPanel rightColumn = new JPanel(new BorderLayout(0, 8));
        rightColumn.setOpaque(false);

        JPanel descBlock = new JPanel(new BorderLayout(0, 6));
        descBlock.setOpaque(false);
        JLabel descLabel = new JLabel("Mô tả:");
        descLabel.setForeground(AppTheme.TEXT);
        descBlock.add(descLabel, BorderLayout.NORTH);
        descBlock.add(descScroll, BorderLayout.CENTER);
        rightColumn.add(descBlock, BorderLayout.CENTER);

        JPanel imageBlock = new JPanel(new BorderLayout(0, 6));
        imageBlock.setOpaque(false);
        JLabel imageLabel = new JLabel("Hình ảnh:");
        imageLabel.setForeground(AppTheme.TEXT);
        imageBlock.add(imageLabel, BorderLayout.NORTH);
        JPanel imageBlockRow = new JPanel(new BorderLayout());
        imageBlockRow.setOpaque(false);
        imageBlockRow.add(imagePicker, BorderLayout.WEST);
        imageBlock.add(imageBlockRow, BorderLayout.CENTER);
        rightColumn.add(imageBlock, BorderLayout.SOUTH);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 7;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, 32, 8, 0);
        formCard.add(rightColumn, gbc);

        top.add(formCard);
        top.add(Box.createVerticalStrut(18));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(1160, 40));

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
            pickedImage[0] = null;
            if (existingImageFile != null && existingImageFile.isFile()) {
                Image scaled = new ImageIcon(existingImageFile.getPath()).getImage()
                        .getScaledInstance(90, 120, Image.SCALE_SMOOTH);
                lblThumbnail.setIcon(new ImageIcon(scaled));
                lblThumbnail.setText(null);
            } else {
                lblThumbnail.setIcon(null);
                lblThumbnail.setText("Chưa có ảnh");
            }
        });
        buttonRow.add(btnReset);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnSave = new JButton("Lưu");
        AppTheme.primary(btnSave);
        btnSave.addActionListener(e -> onSave(shell, txtISBN, txtTitle, txtPrice, cboPublisher, lstCategory, lstAuthor, txtDescription, pickedImage));
        buttonRow.add(btnSave);

        top.add(buttonRow);

        JScrollPane pageScroll = new JScrollPane(top);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(pageScroll, BorderLayout.CENTER);
        return content;
    }

    /** col 0 = cột trái (gridx 0/1), col 1 = cột phải (gridx 2/3). */
    private void formRow(JPanel panel, GridBagConstraints gbc, int col, int row, String label, JComponent field) {
        int baseX = col * 2;
        gbc.gridy = row;
        gbc.gridheight = 1;
        gbc.weighty = 0;

        gbc.gridx = baseX;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, col == 0 ? 0 : 32, 8, 14);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(AppTheme.TEXT);
        panel.add(lbl, gbc);

        gbc.gridx = baseX + 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(field, gbc);
    }

    private void onSave(AdminShellFrm shell, JTextField txtISBN, JTextField txtTitle,
            JTextField txtPrice, JComboBox<Publisher> cboPublisher, JList<Category> lstCategory,
            JList<Author> lstAuthor, JTextArea txtDescription, File[] pickedImage) {
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

        if (pickedImage[0] != null) {
            try {
                book.setImage(BookImageStore.save(pickedImage[0], isbn));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(shell, "Lưu ảnh thất bại, vui lòng thử lại!");
                return;
            }
        }

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
