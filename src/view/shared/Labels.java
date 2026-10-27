package view.shared;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Tên hiển thị tiếng Việt cho các giá trị mã hóa bằng tiếng Anh lưu trong DB
 * (trạng thái đơn/thanh toán/tài khoản, phương thức thanh toán, vai trò, loại
 * giao dịch kho). DB và logic nghiệp vụ vẫn dùng giá trị gốc; chỉ phần hiển
 * thị trên giao diện, báo cáo và hóa đơn đi qua lớp này.
 */
public final class Labels {

    private Labels() {
    }

    /** Trạng thái đơn hàng, thanh toán và tài khoản. */
    public static String status(String value) {
        if (value == null) {
            return "";
        }
        switch (value) {
            case "Pending":
                return "Chờ xử lý";
            case "Paid":
                return "Đã thanh toán";
            case "Cancelled":
                return "Đã hủy";
            case "Refunded":
                return "Đã hoàn tiền";
            case "Active":
                return "Đang hoạt động";
            case "Locked":
                return "Đã khóa";
            default:
                return value;
        }
    }

    public static String paymentMethod(String value) {
        if (value == null) {
            return "";
        }
        switch (value) {
            case "Cash on Delivery":
                return "Thanh toán khi nhận hàng";
            case "Credit Card":
                return "Thẻ tín dụng";
            case "Bank Transfer":
                return "Chuyển khoản ngân hàng";
            case "E-Wallet":
                return "Ví điện tử";
            case "Online":
                return "Thanh toán trực tuyến";
            default:
                return value;
        }
    }

    public static String role(String value) {
        if (value == null) {
            return "";
        }
        switch (value) {
            case "Admin":
                return "Quản trị viên";
            case "Sales Staff":
                return "Nhân viên bán hàng";
            case "Inventory Staff":
                return "Nhân viên kho";
            case "Customer":
                return "Khách hàng";
            default:
                return value;
        }
    }

    public static String stockType(String value) {
        if (value == null) {
            return "";
        }
        switch (value) {
            case "Import":
                return "Nhập kho";
            case "Return":
                return "Trả hàng";
            default:
                return value;
        }
    }

    /** Dịch bất kỳ giá trị mã hóa nào ở trên (dùng cho nhãn biểu đồ gộp nhiều loại). */
    public static String any(String value) {
        String s = status(value);
        if (s.equals(value)) {
            s = paymentMethod(value);
        }
        if (s.equals(value)) {
            s = role(value);
        }
        return s;
    }

    /** Renderer cho JComboBox: item vẫn là giá trị gốc, chỉ chữ hiển thị đổi sang tiếng Việt. */
    public static ListCellRenderer<Object> comboRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Object shown = value instanceof String ? any((String) value) : value;
                return super.getListCellRendererComponent(list, shown, index, isSelected, cellHasFocus);
            }
        };
    }
}
