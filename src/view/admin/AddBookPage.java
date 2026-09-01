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
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
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
        formCard.setMaximumSize(new Dimension(1160, 480));
        formCard.setPreferredSize(new Dimension(1160, 480));
        formCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        JTextField txtISBN = new JTextField();
        formRow(formCard, gbc, 0, 0, "ISBN: *", txtISBN);

        JTextField txtTitle = new JTextField();
        formRow(formCard, gbc, 0, 1, "Tên sách: *", txtTitle);

        JTextField txtPrice = new JTextField();
        formRow(formCard, gbc, 0, 2, "Giá bán: *", txtPrice);

        Publisher[] publishers = new PublisherDAO().searchPublisher("");
        JComboBox<Publisher> cboPublisher = new JComboBox<>(publishers);
        formRow(formCard, gbc, 0, 3, "Nhà xuất bản: *", cboPublisher);

        Category[] categories = new CategoryDAO().searchCategory("");
        JList<Category> lstCategory = new JList<>(categories);
        lstCategory.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstCategory.setVisibleRowCount(4);
        JScrollPane categoryScroll = new JScrollPane(lstCategory);
        categoryScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        categoryScroll.setPreferredSize(new Dimension(0, 90));
        categoryScroll.setMinimumSize(new Dimension(0, 90));
        formRow(formCard, gbc, 0, 4, "Danh mục:", categoryScroll);

        Author[] authors = new AuthorDAO().searchAuthor("");
        JList<Author> lstAuthor = new JList<>(authors);
        lstAuthor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstAuthor.setVisibleRowCount(4);
        JScrollPane authorScroll = new JScrollPane(lstAuthor);
        authorScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        authorScroll.setPreferredSize(new Dimension(0, 90));
        authorScroll.setMinimumSize(new Dimension(0, 90));
        formRow(formCard, gbc, 0, 5, "Tác giả:", authorScroll);

        JTextArea txtDescription = new JTextArea(8, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));

        File[] pickedImage = new File[1];
        JLabel lblThumbnail = new JLabel("Chưa chọn ảnh");
        lblThumbnail.setHorizontalAlignment(SwingConstants.CENTER);
        lblThumbnail.setPreferredSize(new Dimension(90, 120));
        lblThumbnail.setMinimumSize(new Dimension(90, 120));
        lblThumbnail.setMaximumSize(new Dimension(90, 120));
        lblThumbnail.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        lblThumbnail.setForeground(AppTheme.TEXT);
        lblThumbnail.setFont(lblThumbnail.getFont().deriveFont(11f));

        JButton btnPickImage = new JButton("Chọn ảnh...");
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
        // MỘT ô duy nhất chiếm hết chiều cao cột phải (gridheight=6) — để BorderLayout
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
        gbc.gridheight = 6;
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

        JButton btnCancel = new JButton("Hủy");
        AppTheme.secondary(btnCancel);
        btnCancel.addActionListener(e -> shell.navigateTo(new ManageBooksPage()));
        buttonRow.add(btnCancel);
        buttonRow.add(Box.createHorizontalStrut(12));

        JButton btnAdd = new JButton("Thêm sách");
        AppTheme.primary(btnAdd);
        btnAdd.addActionListener(e -> onAdd(shell, txtISBN, txtTitle, txtPrice, cboPublisher, lstCategory, lstAuthor, txtDescription, pickedImage));
        buttonRow.add(btnAdd);

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

    private void onAdd(AdminShellFrm shell, JTextField txtISBN, JTextField txtTitle,
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

        String error = new BookDAO().addBook(book, selectedPublisher.getPublisherID(), categoryIDs, authorIDs);
        if (error == null) {
            JOptionPane.showMessageDialog(shell, "Thêm sách thành công!");
            shell.navigateTo(new ManageBooksPage());
        } else {
            JOptionPane.showMessageDialog(shell, error);
        }
    }
}
