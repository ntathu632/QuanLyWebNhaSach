/*
 * Trang Kiểm kê kho (FR-INV-03) — đối chiếu tồn kho hệ thống với số lượng
 * đếm thực tế, chỉ điều chỉnh khi có chênh lệch (extend Update Stock
 * Quantity, không bắt buộc theo mọi lần kiểm kê).
 */
package view.inventory;
import view.shared.*;

import dao.InventoryDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Inventory;

public class InventoryCheckingPage implements InventoryPage {

    @Override
    public String navKey() {
        return "checking";
    }

    @Override
    public String title() {
        return "Kiểm Kê Kho";
    }

    @Override
    public JPanel content(InventoryShellFrm shell) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Kiểm kê kho");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(AppTheme.TEXT);
        heading.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel hint = new JLabel("Chọn 1 sách, nhập số lượng đếm được thực tế — hệ thống chỉ điều chỉnh khi có chênh lệch.");
        hint.setFont(hint.getFont().deriveFont(12f));
        hint.setForeground(new Color(0x8A, 0x83, 0x74));

        JPanel header = new JPanel();
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BACKGROUND);
        header.add(heading);
        header.add(hint);
        header.add(javax.swing.Box.createVerticalStrut(10));
        content.add(header, BorderLayout.NORTH);

        InventoryDAO inventoryDAO = new InventoryDAO();
        final List<Inventory> currentItems = new ArrayList<>();

        String[] columns = {"Sách", "Tồn kho hệ thống"};
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

        Runnable reload = () -> {
            List<Inventory> rows = inventoryDAO.allInventory();
            currentItems.clear();
            currentItems.addAll(rows);
            model.setRowCount(0);
            for (Inventory inv : rows) {
                model.addRow(new Object[]{inv.getBookTitle(), inv.getStockQuantity()});
            }
        };

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);
        JLabel listTitle = new JLabel("Toàn bộ sách trong kho");
        listTitle.setFont(listTitle.getFont().deriveFont(Font.BOLD, 14f));
        listTitle.setForeground(AppTheme.TEXT);
        tableHeader.add(listTitle, BorderLayout.WEST);

        JButton btnCheck = new JButton("Kiểm kê");
        AppTheme.secondary(btnCheck);
        btnCheck.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= currentItems.size()) {
                JOptionPane.showMessageDialog(shell, "Vui lòng chọn 1 sách để kiểm kê!");
                return;
            }
            Inventory selected = currentItems.get(row);
            String input = JOptionPane.showInputDialog(shell,
                    "Số lượng đếm được thực tế cho \"" + selected.getBookTitle() + "\" "
                    + "(hệ thống đang ghi " + selected.getStockQuantity() + "):",
                    String.valueOf(selected.getStockQuantity()));
            if (input == null) {
                return;
            }
            int actual;
            try {
                actual = Integer.parseInt(input.trim());
                if (actual < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(shell, "Số lượng không hợp lệ!");
                return;
            }

            if (actual == selected.getStockQuantity()) {
                JOptionPane.showMessageDialog(shell, "Khớp với hệ thống, không cần điều chỉnh.");
                return;
            }

            int diff = actual - selected.getStockQuantity();
            if (inventoryDAO.adjustStock(selected.getBookID(), actual)) {
                JOptionPane.showMessageDialog(shell,
                        "Đã điều chỉnh tồn kho \"" + selected.getBookTitle() + "\" từ " + selected.getStockQuantity()
                        + " thành " + actual + " (" + (diff > 0 ? "+" : "") + diff + ").");
                reload.run();
            } else {
                JOptionPane.showMessageDialog(shell, "Điều chỉnh tồn kho thất bại! Vui lòng thử lại.");
            }
        });
        tableHeader.add(btnCheck, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        reload.run();

        return content;
    }
}
