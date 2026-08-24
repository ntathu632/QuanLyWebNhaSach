/*
 * Trang Tra cứu khách hàng (FR-SALE-05) — chỉ xem, phục vụ lập hóa đơn hoặc
 * tra cứu nhanh thông tin khách khi tư vấn.
 */
package view.sales;
import view.shared.*;

import dao.CustomerDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Customer;

public class SearchCustomerPage implements SalesPage {

    @Override
    public String navKey() {
        return "customers";
    }

    @Override
    public String title() {
        return "Tra Cứu Khách Hàng";
    }

    @Override
    public JPanel content(SalesShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Tra cứu khách hàng");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));

        JTextField txtKeyword = new JTextField();
        txtKeyword.putClientProperty("JTextField.placeholderText", "Tìm theo tên, số điện thoại hoặc email...");
        JButton btnSearch = new JButton("Tìm kiếm");
        AppTheme.primary(btnSearch);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(AppTheme.BACKGROUND);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchBar.add(txtKeyword, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setBackground(AppTheme.BACKGROUND);
        header.add(heading, BorderLayout.NORTH);
        header.add(searchBar, BorderLayout.CENTER);
        content.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Họ tên", "Điện thoại", "Email", "Địa chỉ"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setGridColor(AppTheme.TABLE_GRID);
        table.getTableHeader().setReorderingAllowed(false);

        Runnable doSearch = () -> {
            Customer[] found = new CustomerDAO().searchCustomer(txtKeyword.getText().trim());
            model.setRowCount(0);
            for (Customer c : found) {
                model.addRow(new Object[]{c.getCustomerID(), c.getFullName(), c.getPhone(), c.getEmail(), c.getAddress()});
            }
        };
        btnSearch.addActionListener(e -> doSearch.run());
        txtKeyword.addActionListener(e -> doSearch.run());

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel resultTitle = new JLabel("Kết quả");
        resultTitle.setFont(resultTitle.getFont().deriveFont(Font.BOLD, 14f));
        resultTitle.setForeground(AppTheme.TEXT);
        tableCard.add(resultTitle, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        doSearch.run();

        return content;
    }
}
