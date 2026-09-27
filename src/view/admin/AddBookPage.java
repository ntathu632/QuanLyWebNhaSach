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
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Author;
import model.Book;
import model.Category;
import model.Publisher;

public class AddBookPage extends javax.swing.JPanel implements AdminPage {

    private AdminShellFrm shell;

    private final File[] pickedImage = new File[1];

    public AddBookPage() {
        initComponents();

        pageScroll.getVerticalScrollBar().setUnitIncrement(16);

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

        AppTheme.secondary(btnCancel);
        btnCancel.addActionListener(e -> shell.navigateTo(new ManageBooksPage()));

        AppTheme.primary(btnAdd);
        btnAdd.addActionListener(e -> onAdd(shell, txtISBN, txtTitle, txtPrice, cboPublisher, lstCategory, lstAuthor, txtDescription, pickedImage));
    }

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
        this.shell = shell;

        Publisher[] publishers = new PublisherDAO().searchPublisher("");
        cboPublisher.setModel(new DefaultComboBoxModel<>(publishers));

        Category[] categories = new CategoryDAO().searchCategory("");
        lstCategory.setListData(categories);

        Author[] authors = new AuthorDAO().searchAuthor("");
        lstAuthor.setListData(authors);

        return this;
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pageScroll = new javax.swing.JScrollPane();
        top = new javax.swing.JPanel();
        heading = new javax.swing.JLabel();
        formCard = new javax.swing.JPanel();
        lblISBN = new javax.swing.JLabel();
        txtISBN = new javax.swing.JTextField();
        lblTitle = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        lblPrice = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        lblPublisher = new javax.swing.JLabel();
        cboPublisher = new javax.swing.JComboBox<>();
        lblCategory = new javax.swing.JLabel();
        categoryScroll = new javax.swing.JScrollPane();
        lstCategory = new javax.swing.JList<>();
        lblAuthor = new javax.swing.JLabel();
        authorScroll = new javax.swing.JScrollPane();
        lstAuthor = new javax.swing.JList<>();
        rightColumn = new javax.swing.JPanel();
        descBlock = new javax.swing.JPanel();
        descLabel = new javax.swing.JLabel();
        descScroll = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        imageBlock = new javax.swing.JPanel();
        imageLabel = new javax.swing.JLabel();
        imageBlockRow = new javax.swing.JPanel();
        imagePicker = new javax.swing.JPanel();
        lblThumbnail = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(12, 0), new java.awt.Dimension(12, 0), new java.awt.Dimension(12, 32767));
        btnPickImage = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 18), new java.awt.Dimension(0, 18), new java.awt.Dimension(32767, 18));
        buttonRow = new javax.swing.JPanel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        btnCancel = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(12, 0), new java.awt.Dimension(12, 0), new java.awt.Dimension(12, 32767));
        btnAdd = new javax.swing.JButton();

        setBackground(AppTheme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        setLayout(new java.awt.BorderLayout());

        pageScroll.setBorder(null);
        top.setBackground(AppTheme.BACKGROUND);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setText("Thêm sách mới");
        heading.setAlignmentX(0.0F);
        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));
        top.add(heading);
        formCard.setBackground(new java.awt.Color(255, 255, 255));
        formCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY), javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        formCard.setAlignmentX(0.0F);
        formCard.setMaximumSize(new java.awt.Dimension(1160, 480));
        formCard.setPreferredSize(new java.awt.Dimension(1160, 480));
        formCard.setLayout(new java.awt.GridBagLayout());
        lblISBN.setForeground(AppTheme.TEXT);
        lblISBN.setText("ISBN: *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 14);
        formCard.add(lblISBN, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        formCard.add(txtISBN, gridBagConstraints);
        lblTitle.setForeground(AppTheme.TEXT);
        lblTitle.setText("Tên sách: *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 14);
        formCard.add(lblTitle, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        formCard.add(txtTitle, gridBagConstraints);
        lblPrice.setForeground(AppTheme.TEXT);
        lblPrice.setText("Giá bán: *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 14);
        formCard.add(lblPrice, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        formCard.add(txtPrice, gridBagConstraints);
        lblPublisher.setForeground(AppTheme.TEXT);
        lblPublisher.setText("Nhà xuất bản: *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 14);
        formCard.add(lblPublisher, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        formCard.add(cboPublisher, gridBagConstraints);
        lblCategory.setForeground(AppTheme.TEXT);
        lblCategory.setText("Danh mục:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 14);
        formCard.add(lblCategory, gridBagConstraints);
        categoryScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        categoryScroll.setMinimumSize(new java.awt.Dimension(0, 90));
        categoryScroll.setPreferredSize(new java.awt.Dimension(0, 90));
        lstCategory.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstCategory.setVisibleRowCount(4);
        categoryScroll.setViewportView(lstCategory);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        formCard.add(categoryScroll, gridBagConstraints);

        lblAuthor.setForeground(AppTheme.TEXT);
        lblAuthor.setText("Tác giả:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 14);
        formCard.add(lblAuthor, gridBagConstraints);
        authorScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        authorScroll.setMinimumSize(new java.awt.Dimension(0, 90));
        authorScroll.setPreferredSize(new java.awt.Dimension(0, 90));
        lstAuthor.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstAuthor.setVisibleRowCount(4);
        authorScroll.setViewportView(lstAuthor);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        formCard.add(authorScroll, gridBagConstraints);

        rightColumn.setOpaque(false);
        rightColumn.setLayout(new java.awt.BorderLayout(0, 8));
        descBlock.setOpaque(false);
        descBlock.setLayout(new java.awt.BorderLayout(0, 6));
        descLabel.setForeground(AppTheme.TEXT);
        descLabel.setText("Mô tả:");
        descBlock.add(descLabel, java.awt.BorderLayout.NORTH);
        descScroll.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(8);
        txtDescription.setWrapStyleWord(true);
        descScroll.setViewportView(txtDescription);
        descBlock.add(descScroll, java.awt.BorderLayout.CENTER);

        rightColumn.add(descBlock, java.awt.BorderLayout.CENTER);

        imageBlock.setOpaque(false);
        imageBlock.setLayout(new java.awt.BorderLayout(0, 6));
        imageLabel.setForeground(AppTheme.TEXT);
        imageLabel.setText("Hình ảnh:");
        imageBlock.add(imageLabel, java.awt.BorderLayout.NORTH);
        imageBlockRow.setOpaque(false);
        imageBlockRow.setLayout(new java.awt.BorderLayout());
        imagePicker.setOpaque(false);
        imagePicker.setLayout(new javax.swing.BoxLayout(imagePicker, javax.swing.BoxLayout.X_AXIS));
        lblThumbnail.setFont(lblThumbnail.getFont().deriveFont(11f));
        lblThumbnail.setForeground(AppTheme.TEXT);
        lblThumbnail.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblThumbnail.setText("Chưa chọn ảnh");
        lblThumbnail.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        lblThumbnail.setMaximumSize(new java.awt.Dimension(90, 120));
        lblThumbnail.setMinimumSize(new java.awt.Dimension(90, 120));
        lblThumbnail.setPreferredSize(new java.awt.Dimension(90, 120));
        imagePicker.add(lblThumbnail);
        imagePicker.add(filler1);
        btnPickImage.setText("Chọn ảnh...");
        imagePicker.add(btnPickImage);
        imageBlockRow.add(imagePicker, java.awt.BorderLayout.WEST);

        imageBlock.add(imageBlockRow, java.awt.BorderLayout.CENTER);

        rightColumn.add(imageBlock, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 32, 8, 0);
        formCard.add(rightColumn, gridBagConstraints);

        top.add(formCard);

        top.add(filler2);
        buttonRow.setBackground(AppTheme.BACKGROUND);
        buttonRow.setAlignmentX(0.0F);
        buttonRow.setMaximumSize(new java.awt.Dimension(1160, 40));
        buttonRow.setLayout(new javax.swing.BoxLayout(buttonRow, javax.swing.BoxLayout.X_AXIS));
        buttonRow.add(filler3);
        btnCancel.setText("Hủy");
        buttonRow.add(btnCancel);
        buttonRow.add(filler4);
        btnAdd.setText("Thêm sách");
        buttonRow.add(btnAdd);
        top.add(buttonRow);

        pageScroll.setViewportView(top);

        add(pageScroll, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane authorScroll;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnPickImage;
    private javax.swing.JPanel buttonRow;
    private javax.swing.JScrollPane categoryScroll;
    private javax.swing.JComboBox<Publisher> cboPublisher;
    private javax.swing.JPanel descBlock;
    private javax.swing.JLabel descLabel;
    private javax.swing.JScrollPane descScroll;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JPanel formCard;
    private javax.swing.JLabel heading;
    private javax.swing.JPanel imageBlock;
    private javax.swing.JPanel imageBlockRow;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JPanel imagePicker;
    private javax.swing.JLabel lblAuthor;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblISBN;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblPublisher;
    private javax.swing.JLabel lblThumbnail;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JList<Author> lstAuthor;
    private javax.swing.JList<Category> lstCategory;
    private javax.swing.JScrollPane pageScroll;
    private javax.swing.JPanel rightColumn;
    private javax.swing.JPanel top;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtISBN;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}
