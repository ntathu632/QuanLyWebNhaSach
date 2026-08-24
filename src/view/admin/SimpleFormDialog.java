/*
 * Hộp thoại nhập liệu vài trường đơn giản (nhãn + ô nhập), dùng chung cho các
 * màn Quản lý danh mục/tác giả/nhà xuất bản — thực thể chỉ 2-4 trường, không
 * cần dựng hẳn 1 trang riêng như bên Quản lý sách (đồ án môn học, tránh làm
 * dư thừa cho các thực thể nhỏ).
 */
package view.admin;
import view.shared.*;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class SimpleFormDialog {

    private SimpleFormDialog() {
    }

    /**
     * Hiện hộp thoại OK/Cancel với các trường theo "labels" (điền sẵn theo
     * "initialValues" nếu có, hoặc rỗng nếu null). Trả về mảng giá trị đã
     * nhập theo đúng thứ tự "labels" nếu bấm OK, hoặc null nếu Cancel.
     */
    static String[] show(Component parent, String title, String[] labels, String[] initialValues) {
        JPanel panel = new JPanel(new GridBagLayout());
        JTextField[] fields = new JTextField[labels.length];
        GridBagConstraints gbc = new GridBagConstraints();

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(6, 0, 6, 10);
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            fields[i] = new JTextField(initialValues != null ? initialValues[i] : "", 22);
            panel.add(fields[i], gbc);
        }

        int result = JOptionPane.showConfirmDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String[] values = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            values[i] = fields[i].getText().trim();
        }
        return values;
    }
}
