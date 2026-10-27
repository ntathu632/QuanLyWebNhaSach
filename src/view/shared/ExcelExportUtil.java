/*
 * Tiện ích xuất báo cáo ra file Excel thật (.xlsx, qua Apache POI) — dùng
 * chung cho các tab của ReportPage. Trước khi ghi file mở hộp thoại lưu để
 * người dùng chọn thư mục và tên file (gợi ý sẵn tên theo ngày giờ, nhớ thư
 * mục đã chọn lần trước, hỏi trước khi ghi đè file đã có).
 */
package view.shared;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelExportUtil {

    private static final SimpleDateFormat FILE_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");

    /** Thư mục đã chọn ở lần lưu trước, mở lại hộp thoại thì vào thẳng đó. */
    private static File lastDir;

    private ExcelExportUtil() {
    }

    /** Hộp thoại chọn nơi lưu; trả về null nếu bấm Hủy. Tự thêm đuôi .xlsx, hỏi trước khi ghi đè. */
    private static File chooseFile(Component parent, String baseFileName) {
        UIManager.put("FileChooser.saveButtonText", "Lưu");
        UIManager.put("FileChooser.cancelButtonText", "Hủy");
        UIManager.put("FileChooser.saveInLabelText", "Lưu trong:");
        UIManager.put("FileChooser.fileNameLabelText", "Tên tệp:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Loại tệp:");

        File startDir = lastDir != null ? lastDir : FileSystemView.getFileSystemView().getDefaultDirectory();
        JFileChooser chooser = new JFileChooser(startDir);
        chooser.setDialogTitle("Chọn nơi lưu file Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File(startDir, baseFileName + "_" + FILE_TIMESTAMP.format(new Date()) + ".xlsx"));

        while (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getParentFile(), file.getName() + ".xlsx");
            }
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(parent,
                        "File \"" + file.getName() + "\" đã tồn tại. Bạn có muốn ghi đè không?",
                        "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    continue;
                }
            }
            lastDir = file.getParentFile();
            return file;
        }
        return null;
    }

    /**
     * @param sheets tên sheet -> ma trận dữ liệu, dòng đầu tiên là header. Ô kiểu
     * {@link Number} được ghi thành số thật (không phải text), các kiểu khác ghi
     * bằng toString().
     */
    public static void export(Component parent, String baseFileName, Map<String, Object[][]> sheets) {
        File outFile = chooseFile(parent, baseFileName);
        if (outFile == null) {
            return; // người dùng bấm Hủy
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (Map.Entry<String, Object[][]> entry : sheets.entrySet()) {
                Sheet sheet = workbook.createSheet(entry.getKey());
                Object[][] data = entry.getValue();
                for (int r = 0; r < data.length; r++) {
                    Row row = sheet.createRow(r);
                    Object[] rowData = data[r];
                    for (int c = 0; c < rowData.length; c++) {
                        Cell cell = row.createCell(c);
                        Object value = rowData[c];
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value == null ? "" : value.toString());
                        }
                        if (r == 0) {
                            cell.setCellStyle(headerStyle);
                        }
                    }
                }
                int columnCount = data.length > 0 ? data[0].length : 0;
                for (int c = 0; c < columnCount; c++) {
                    sheet.autoSizeColumn(c);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(parent, "Đã xuất file Excel thành công!\n" + outFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Xuất Excel thất bại do lỗi hệ thống, vui lòng thử lại!");
        }
    }
}
