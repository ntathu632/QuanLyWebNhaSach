/*
 * Tiện ích xuất báo cáo ra file Excel thật (.xlsx, qua Apache POI) — dùng
 * chung cho các tab của ReportPage. Lưu thẳng vào thư mục làm việc hiện tại
 * (không mở JFileChooser) để giữ đơn giản, nhất quán với cách các màn khác
 * trong app tránh dùng dialog hệ điều hành.
 */
package view.shared;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelExportUtil {

    private static final SimpleDateFormat FILE_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private ExcelExportUtil() {
    }

    /**
     * @param sheets tên sheet -> ma trận dữ liệu, dòng đầu tiên là header. Ô kiểu
     * {@link Number} được ghi thành số thật (không phải text), các kiểu khác ghi
     * bằng toString().
     */
    public static void export(Component parent, String baseFileName, Map<String, Object[][]> sheets) {
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

            String fileName = baseFileName + "_" + FILE_TIMESTAMP.format(new Date()) + ".xlsx";
            File outFile = new File(fileName);
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
