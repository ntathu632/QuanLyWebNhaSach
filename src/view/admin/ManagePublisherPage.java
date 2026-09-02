/*
 * Trang Quản lý nhà xuất bản (FR-ADM-04: Add/Update/Delete Publisher,
 * Update/Delete include Search Publisher). Cùng mô hình 1-trang với
 * ManageCategoryPage/ManageAuthorPage.
 */
package view.admin;
import view.shared.*;

import dao.PublisherDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Publisher;

public class ManagePublisherPage implements AdminPage {

    @Override
    public String navKey() {
        return "publisher";
    }

    @Override
    public String title() {
        return "Quản Lý Nhà Xuất Bản";
    }

    @Override
    public JPanel content(AdminShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);

        JLabel heading = new JLabel("Quản lý nhà xuất bản");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(heading);

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên nhà xuất bản...");

        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JButton btnAdd = new JButton("+ Thêm nhà xuất bản");
        AppTheme.secondary(btnAdd);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        searchBar.add(btnAdd, BorderLayout.WEST);
        header.add(searchBar);

        content.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Tên nhà xuất bản", "Địa chỉ", "Điện thoại", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);
        AppTheme.alignTableColumn(table, 0, SwingConstants.CENTER, 50, 50);
        AppTheme.alignTableColumn(table, 1, SwingConstants.LEFT, 0, 0);
        AppTheme.alignTableColumn(table, 2, SwingConstants.LEFT, 200, 200);
        AppTheme.alignTableColumn(table, 3, SwingConstants.CENTER, 120, 120);
        AppTheme.alignTableColumn(table, 4, SwingConstants.LEFT, 180, 180);

        final List<Publisher> currentItems = new ArrayList<>();

        Runnable doSearch = () -> {
            String keyword = txtKeyword.getText().trim();
            Publisher[] found = new PublisherDAO().searchPublisher(keyword);
            currentItems.clear();
            model.setRowCount(0);
            for (Publisher p : found) {
                currentItems.add(p);
                model.addRow(new Object[]{p.getPublisherID(), p.getPublisherName(), p.getAddress(), p.getPhone(), p.getEmail()});
            }
        };

        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        btnAdd.addActionListener(e -> {
            String[] values = SimpleFormDialog.show(shell, "Thêm nhà xuất bản",
                    new String[]{"Tên nhà xuất bản:", "Địa chỉ:", "Điện thoại:", "Email:"}, null);
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên nhà xuất bản!");
                return;
            }
            Publisher publisher = new Publisher();
            publisher.setPublisherName(values[0]);
            publisher.setAddress(values[1]);
            publisher.setPhone(values[2]);
            publisher.setEmail(values[3]);
            if (new PublisherDAO().addPublisher(publisher)) {
                JOptionPane.showMessageDialog(shell, "Thêm nhà xuất bản thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Thêm nhà xuất bản thất bại! Vui lòng thử lại.");
            }
        });

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Danh sách nhà xuất bản");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(resultTitle, BorderLayout.WEST);

        JPanel rowActions = new JPanel();
        rowActions.setLayout(new BoxLayout(rowActions, BoxLayout.X_AXIS));
        rowActions.setOpaque(false);

        JButton btnEdit = new JButton("Sửa");
        AppTheme.primary(btnEdit);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhà xuất bản để sửa!");
                return;
            }
            Publisher selected = currentItems.get(row);
            String[] values = SimpleFormDialog.show(shell, "Sửa nhà xuất bản",
                    new String[]{"Tên nhà xuất bản:", "Địa chỉ:", "Điện thoại:", "Email:"},
                    new String[]{selected.getPublisherName(), selected.getAddress(), selected.getPhone(), selected.getEmail()});
            if (values == null) {
                return;
            }
            if (values[0].isEmpty()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng nhập tên nhà xuất bản!");
                return;
            }
            selected.setPublisherName(values[0]);
            selected.setAddress(values[1]);
            selected.setPhone(values[2]);
            selected.setEmail(values[3]);
            if (new PublisherDAO().updatePublisher(selected)) {
                JOptionPane.showMessageDialog(shell, "Cập nhật nhà xuất bản thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Cập nhật nhà xuất bản thất bại! Vui lòng thử lại.");
            }
        });
        rowActions.add(btnEdit);
        rowActions.add(Box.createHorizontalStrut(10));

        JButton btnDelete = new JButton("Xóa");
        AppTheme.secondary(btnDelete);
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 nhà xuất bản để xóa!");
                return;
            }
            Publisher selected = currentItems.get(row);
            int confirm = JOptionPane.showConfirmDialog(shell,
                    "Bạn có chắc muốn xóa nhà xuất bản \"" + selected.getPublisherName() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            String error = new PublisherDAO().deletePublisher(selected.getPublisherID());
            if (error == null) {
                JOptionPane.showMessageDialog(shell, "Xóa nhà xuất bản thành công!");
                doSearch.run();
            } else {
                JOptionPane.showMessageDialog(shell, error);
            }
        });
        rowActions.add(btnDelete);

        tableHeader.add(rowActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
    }
}
