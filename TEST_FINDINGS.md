# Test Findings — QuanLyWebNhaSach

Ghi lại lỗi phát hiện khi test thực tế app (build từ `src/`, chạy qua GUI, kết nối SQL Server thật). Cập nhật dần khi test thêm các luồng khác.

Trạng thái: 🔴 Lỗi | 🟡 Cần chú ý | 🟢 OK

---

## Đăng nhập (Admin)

🟢 OK — Đăng nhập với `admin` / `123456` (seed data), điều hướng đúng vào khu vực Admin, không crash.

## Admin > Dashboard

🟢 OK — Load đầy đủ số liệu thật (20 sách, 11 đơn, 10 khách, doanh thu 5,565,000đ) và các biểu đồ.

## Admin > Quản lý sách > Xem danh sách

🟢 OK — Bảng danh sách 20 sách hiển thị đúng dữ liệu từ DB.

## Admin > Quản lý sách > Thêm sách

🟢 **ĐÃ FIX** (trước đó 🔴 lỗi nghiêm trọng: Thêm sách luôn thất bại do `Book.publisherID` NOT NULL nhưng `BookDAO.addBook()` không insert cột này).

**Đã sửa:**
- `src/model/Book.java` — thêm field `publisherID`.
- `src/dao/BookDAO.java` — `addBook()`/`updateBook()` giờ nhận thêm `publisherID`, `categoryIDs`, `authorIDs`; chạy trong 1 transaction (INSERT/UPDATE Book + đồng bộ `BookCategory`/`BookAuthor`). Thêm `getPublisherID/getCategoryIDs/getAuthorIDs(bookID)` để load lại khi Sửa.
- `src/view/admin/AddBookPage.java` + `EditBookPage.java` — thêm combobox **Nhà xuất bản** (bắt buộc) và 2 `JList` multi-select **Danh mục**/**Tác giả** (tùy chọn), đều load dữ liệu thật từ `PublisherDAO`/`CategoryDAO`/`AuthorDAO`. Trước đó 2 trang này hoàn toàn không có cách nào gán Category/Author cho sách dù DB đã có sẵn bảng `BookCategory`/`BookAuthor` — không chỉ thiếu Nhà xuất bản.
- `model/Publisher.java`, `Category.java`, `Author.java` — thêm `toString()` để hiển thị tên trong combobox/list.

**Test lại (sau fix):** Thêm sách "Test Full Book Claude" với NXB + 2 danh mục + 1 tác giả → "Thêm sách thành công!", tổng số sách 20→21. Mở lại qua Sửa: NXB/danh mục/tác giả prefill đúng 100%, giá hiển thị sạch "199000" (không còn ".0"). Không exception trong `app_err.log` suốt quá trình.

🟡 **Bug tự phát hiện & tự sửa trong lúc test:** form dài ra do thêm 3 field mới làm nút "Thêm sách"/"Lưu" bị đẩy khỏi màn hình (AdminShellFrm không bọc trang trong `JScrollPane`). Đã sửa bằng cách bọc nội dung `AddBookPage`/`EditBookPage` trong `JScrollPane` — xác nhận cuộn được bằng chuột (wheel) và bấm nút vẫn hoạt động bình thường.

⏳ Chưa test lại: dọn dữ liệu test "Test Full Book Claude" (ID 22) — sách này giờ có gán Danh mục/Tác giả nên xóa sẽ bị chặn đúng như thiết kế (giống mọi sách khác có liên kết), cố ý giữ lại làm dữ liệu minh chứng.

## Admin > Quản lý danh mục

🟢 OK — Cả 4 thao tác đều hoạt động đúng, không exception, không crash:
- Xem danh sách: hiển thị đúng 8 danh mục seed.
- Thêm danh mục: dialog "Thêm danh mục" (OK/Cancel), thêm "Test Danh Muc Claude" thành công, báo "Thêm danh mục thành công!", danh sách tự refresh (do `doSearch.run()` sau khi thêm).
- Sửa danh mục: chọn dòng → "Sửa" → dialog prefill đúng dữ liệu → sửa tên → lưu thành công, báo đúng, danh sách refresh.
- Xóa danh mục: có dialog xác nhận riêng ("Xác nhận xóa") trước khi xóa thật.
  - Xóa danh mục KHÔNG có sách: thành công, báo "Xóa danh mục thành công!".
  - Xóa danh mục ĐANG có sách (vd "Công nghệ thông tin"): bị chặn đúng, báo "Không thể xóa danh mục này vì đang có sách thuộc về nó!".

🟡 Ghi chú (test tooling, không phải bug): 2 lần đầu tưởng "Thêm danh mục" bị lỗi (danh mục không xuất hiện sau khi bấm OK) — hóa ra do click chuột tự động của tôi trượt nút OK (dialog `JOptionPane` vẫn còn mở, bị che khuất). Xác nhận lại bằng phím Enter thì chạy đúng. Không phải lỗi ứng dụng.

## Admin > Quản lý tác giả

🟢 OK — Giống hệt Quản lý danh mục, cả 4 thao tác chạy đúng, không exception, không crash:
- Xem danh sách: 10 tác giả seed hiển thị đúng.
- Thêm tác giả (3 trường: Tên, Quốc tịch, Mô tả): thêm "Claude Test Author" thành công, báo "Thêm tác giả thành công!", list tự refresh.
- Sửa tác giả: prefill đúng 3 trường, sửa tên, lưu thành công, báo "Cập nhật tác giả thành công!".
- Xóa tác giả: có confirm riêng.
  - Xóa tác giả ĐANG có sách (vd "Robert C. Martin" — tác giả Clean Code): bị chặn đúng, báo "Không thể xóa tác giả này vì đang có sách gắn với tác giả này!".
  - Xóa tác giả KHÔNG có sách (tác giả test vừa tạo): thành công, báo "Xóa tác giả thành công!". Đã dọn sạch dữ liệu test.

## Admin > Quản lý nhà xuất bản

🟢 OK — Giống Quản lý danh mục/tác giả, cả 4 thao tác chạy đúng, không exception, không crash:
- Xem danh sách: 5 NXB seed hiển thị đúng.
- Thêm NXB (4 trường: Tên, Địa chỉ, Điện thoại, Email): thêm thành công, báo "Thêm nhà xuất bản thành công!", list tự refresh.
- Sửa NXB: prefill đúng 4 trường, sửa tên, lưu thành công, báo "Cập nhật nhà xuất bản thành công!".
- Xóa NXB: có confirm riêng.
  - Xóa NXB ĐANG có sách (vd "Nhà xuất bản Trẻ"): bị chặn đúng, báo "Không thể xóa nhà xuất bản này vì đang có sách thuộc về nhà xuất bản này!".
  - Xóa NXB KHÔNG có sách (NXB test vừa tạo): thành công, báo "Xóa nhà xuất bản thành công!". Đã dọn sạch dữ liệu test.

🟡 Ghi chú (môi trường test, không phải bug): trong lúc test trang này, một ứng dụng khác đang chạy trên máy (game) liên tục giành lại focus cửa sổ, khiến vài lần click/gõ phím tự động bị trượt (dialog "Thêm nhà xuất bản" phải chuyển hẳn sang điều khiển bằng Tab/phím thay vì click chuột mới ổn định). Không liên quan đến chất lượng code của app.

## Admin > Quản lý tài khoản

🟢 OK — Không có Thêm/Xóa (thiết kế có chủ đích: tài khoản luôn gắn với Customer/Employee có sẵn), chỉ có Sửa (đổi vai trò + khóa/mở khóa qua status). Không exception, không crash:
- Xem danh sách: 15 tài khoản (admin, 10 customer, inventory01, sales01, sales02) hiển thị đúng, kèm tên chủ tài khoản thật (cột "Thuộc về").
- Sửa tài khoản người khác (sales01): đổi Trạng thái Active → Locked qua dropdown, lưu thành công, báo "Cập nhật tài khoản thành công!", list refresh đúng. Đổi lại Locked → Active cũng thành công (đã dọn dữ liệu test).
- **Bảo vệ tự khóa tài khoản đang đăng nhập:** chọn sửa chính tài khoản "admin" (đang đăng nhập) và đổi Trạng thái sang Locked → hiện thêm dialog xác nhận riêng "Bạn đang khóa chính tài khoản đang đăng nhập. Tiếp tục?". Chọn "No" → hủy đúng, không cập nhật DB, admin vẫn Active. Logic chặn tự khóa hoạt động chính xác.

## Admin > Quản lý nhân viên

🟢 OK — Cả Thêm/Sửa/Xóa chạy đúng, không exception, không crash:
- Xem danh sách: 4 nhân viên seed hiển thị đúng.
- Thêm nhân viên (6 trường: Họ tên, Vị trí, Điện thoại, Email, Username, Password — tạo đồng thời Employee + Account trong 1 transaction): thêm "Claude Test Employee" (Sales Staff) thành công, báo "Thêm nhân viên thành công!", list refresh đúng.
- Sửa nhân viên: prefill đúng 4 trường (không có username/password vì đó là của Account, sửa qua "Quản lý tài khoản"), sửa tên, lưu thành công.
- Xóa nhân viên: có confirm riêng, đúng như thiết kế — nhân viên vừa tạo (đang có Account đi kèm) bị chặn xóa, báo "Nhân viên này đang có tài khoản đăng nhập — vui lòng khóa tài khoản ở mục 'Quản lý tài khoản' thay vì xóa nhân viên."

🟡 Lưu ý dọn dẹp: nhân viên test "Claude Test Employee Edited" (ID 5, username `claudetest01`) **không xóa được qua UI** vì luôn có Account đi kèm (đúng thiết kế — Employee có Account thì không cho xóa hẳn, chỉ có thể khóa Account ở "Quản lý tài khoản"). Chưa dọn dữ liệu test này; nếu muốn sạch hoàn toàn cần xóa trực tiếp trong DB hoặc chấp nhận giữ lại.

## Đổi mật khẩu (chung mọi vai trò)

🟢 OK — Test trên tài khoản admin, cả 3 tình huống đều đúng, không exception, không crash:
- Mật khẩu hiện tại sai: báo "Mật khẩu hiện tại không đúng!", không đổi gì.
- Mật khẩu mới và xác nhận không khớp: báo "Xác nhận mật khẩu mới không khớp!", không đổi gì.
- Đổi đúng (current=123456 → new=ClaudeTest789): báo "Đổi mật khẩu thành công!". **Xác minh thật**: đăng xuất → đăng nhập lại bằng mật khẩu mới `ClaudeTest789` → vào được đúng Trang Quản Trị, xác nhận DB đã cập nhật thật (không chỉ là UI). Sau đó đổi lại về `123456` để khôi phục trạng thái ban đầu — đã dọn sạch.

## Admin > Báo cáo thống kê

🟢 OK — Cả 3 tab hoạt động đúng, không exception, không crash:
- **Doanh thu**: chọn khoảng ngày (Từ ngày/Đến ngày) + "Xem báo cáo" → đúng số liệu, khớp với Dashboard (mở rộng khoảng 01/07–01/09 ra 8 đơn đã thanh toán, tổng 5,565,000đ — đúng bằng "Doanh thu đã thu" trên Dashboard).
- **Tồn kho**: bảng "Tồn kho theo sách" đúng, số liệu nhất quán (Tồn kho hiện tại = Đã nhập − Đã xuất, kiểm tra khớp nhiều dòng). Có quy ước "đỏ = dưới 10 cuốn" (không sách nào rơi vào diện này lúc test nên chưa thấy highlight đỏ thực tế).
- **Bán hàng**: gồm "Sách bán chạy" (top 10 theo số lượng bán) và "Doanh số theo nhân viên xử lý" (theo Invoice) — cả 2 đều có dữ liệu thật, hiển thị đúng.

🟢 Điểm hay: tab "Bán hàng" tự hiển thị chú thích minh bạch ngay trên UI: *"Lưu ý: đơn đặt hàng qua ứng dụng hiện chưa tự tạo hóa đơn (Invoice) gắn nhân viên xử lý — bảng trên chỉ phản ánh dữ liệu hóa đơn đã có sẵn."* — khớp đúng với hạn chế đã ghi trong code (`ReportDAO.salesByEmployee()`), không phải bug mà là giới hạn được khai báo rõ ràng, chủ động cho người dùng biết.

### 💡 Đã bổ sung: Xuất Excel (tính năng thiếu so với logic thực tế)

🟢 **ĐÃ THÊM MỚI** — trang báo cáo trước đó không có cách nào xuất dữ liệu ra ngoài, trong khi đây là nhu cầu thực tế cơ bản của mọi màn báo cáo. Đã thêm:
- Thư viện Apache POI 5.5.1 (+ 11 jar phụ thuộc bắt buộc: poi-ooxml, xmlbeans, commons-compress/io/codec/collections4/math3, log4j-api, curvesapi, SparseBitSet — đã lược bỏ các phụ thuộc optional như pdfbox/batik/xmlsec vì không cần cho ghi .xlsx cơ bản) vào `lib/` + khai báo trong `nbproject/project.properties`.
- `src/view/shared/ExcelExportUtil.java` — tiện ích dùng chung, ghi file `.xlsx` thật (không phải CSV giả danh), có định dạng header in đậm, tự co giãn cột. Lưu thẳng vào thư mục chạy app kèm timestamp (`BaoCao_DoanhThu_20260901_190540.xlsx`) thay vì mở hộp thoại chọn nơi lưu, để tránh phụ thuộc dialog hệ điều hành.
- Nút "Xuất Excel" ở cả 3 tab (`ReportPage.java`): Doanh thu (1 sheet), Tồn kho (1 sheet), Bán hàng (2 sheet: "Sach ban chay" + "Doanh so nhan vien").

**Đã test cả 3 nút:** đều xuất thành công, mở kiểm tra XML bên trong file (`jar tf`/giải nén) xác nhận đúng cấu trúc `.xlsx` hợp lệ, header + dữ liệu tiếng Việt (dấu, ký tự Unicode) hiển thị đúng, các cột số tiền/số lượng là số thật (không phải text) để dùng được cho tính toán tiếp trong Excel. Không exception trong `app_err.log`. Đã xóa các file test sau khi xác minh.

## Admin > Quản lý sách > Sửa thông tin sách

🟢 OK — Mở từ "Tìm kiếm sách" (double-click 1 dòng), form load đúng dữ liệu hiện có (Mã sách, ISBN, Tên sách, Giá bán, Mô tả, Hình ảnh). Sửa giá `Clean Code` 420,000 → 425,000, bấm "Lưu": lưu thành công, tự quay về "Quản lý sách", bảng danh sách + "Giá trung bình" cập nhật đúng ngay lập tức. Không lỗi, không exception.

🟢 **ĐÃ FIX:** trường "Giá bán" trước đây hiển thị số thô kiểu float (`420000.0`) — đã sửa bằng `DecimalFormat("0.##")` trong `EditBookPage.java`, giờ hiển thị sạch (`420000`). Cùng đợt sửa còn thêm combobox Nhà xuất bản + multi-select Danh mục/Tác giả (xem mục "Thêm sách" ở trên) vì trang Sửa sách trước đó cũng không có cách nào đổi các trường này.

## Admin > Quản lý sách > Xóa sách

🟢 OK — Chọn 1 dòng có liên kết (danh mục/tác giả/tồn kho/giỏ hàng/đơn hàng), bấm "Xóa sách đã chọn" → dialog xác nhận "Bạn có chắc muốn xóa..." → chọn Yes → app đúng như thiết kế: chặn xóa, báo "Không thể xóa sách này vì đang có dữ liệu liên quan (danh mục, giỏ hàng hoặc đơn hàng)!". Không crash, không xóa nhầm dữ liệu có liên kết.

⏳ Chưa test: xóa 1 sách KHÔNG có dữ liệu liên quan (đường thành công thật sự của tính năng xóa).

💡 **Gợi ý thiết kế (từ chị thu, chưa code):** vì hầu hết sách thực tế đều có danh mục/tác giả/tồn kho/giỏ hàng/đơn hàng liên quan, tính năng "xóa cứng" gần như luôn bị chặn → nên cân nhắc đổi sang **ẩn sách (soft delete)** thay vì `DELETE` thật: thêm cột trạng thái (vd `isActive`/`status`) trên `Book`, "Xóa sách" chỉ set ẩn khỏi danh sách bán/storefront, vẫn giữ nguyên dữ liệu cho lịch sử đơn hàng. Cần bàn với anhthu vì đổi schema + nhiều chỗ query Book phải lọc theo trạng thái.

## Admin > Quản lý sách > Thêm/Sửa sách > Ảnh bìa thật + layout full màn hình

🟢 **ĐÃ FIX (2 phần):**

1. **Ảnh bìa sách giờ là dữ liệu thật.** Trước đó `Book.image` chỉ là 1 chuỗi text không có file thật đi kèm, không hiển thị ở đâu cả. Giờ:
   - `src/view/shared/BookImageStore.java` (mới) — lưu file ảnh admin chọn vào `images/books/<ISBN>.<ext>`, `Book.image` lưu tên file đã lưu.
   - `AddBookPage`/`EditBookPage` — trường "Hình ảnh" đổi từ ô nhập text sang nút "Chọn ảnh..."/"Đổi ảnh..." mở `JFileChooser` thật + xem trước ảnh (thumbnail 90x120).
   - `src/view/customer/BookCoverPanel.java` — vẽ ảnh thật nếu có file, nếu không thì fallback về bìa vẽ tay như cũ (sách seed cũ không có file thật vẫn hiển thị bình thường, không lỗi).

2. **Layout Thêm/Sửa sách trước đó chỉ chiếm ~620px bên trái, bỏ trống gần nửa màn hình dù cửa sổ đã maximize.** Đổi sang layout 2 cột (trái: ISBN/Tên sách/Giá/NXB/Danh mục/Tác giả, phải: Mô tả to + Hình ảnh) dùng hết chiều rộng cửa sổ (~1160px form card thay vì 620px).
   - Gặp lại đúng bug từng gặp trước đây (GridBagLayout bóp chiều cao hàng dùng chung giữa 2 cột) — sửa triệt để bằng cách gộp Mô tả + Hình ảnh vào 1 panel con `BorderLayout` riêng (Mô tả ở CENTER giãn hết, Hình ảnh ở SOUTH giữ cố định), thay vì để GridBagLayout tính chiều cao từng hàng dùng chung với cột trái.
   - Bug phụ phát hiện khi debug: khung thumbnail (90x120) bị bóp còn ~20px dù đã set `setPreferredSize`/`setMinimumSize` — nguyên nhân là **chưa set `setMaximumSize`**, nên `BoxLayout` (chiều ngang, chứa thumbnail + nút chọn ảnh) dùng maximumSize mặc định (nhỏ, gần bằng preferred size do FlatLaf tính) làm trần chiều cao. Set thêm `setMaximumSize(90,120)` mới hết bóp.
   - Đã verify bằng ảnh chụp thật: cả 2 trang Thêm sách và Sửa sách giờ full-width, khung ảnh đúng kích thước, không cần scroll, không exception trong `app_err.log`.

🟢 **Đã verify end-to-end (luồng đầy đủ):** Thêm sách test (ISBN `8888777766`, kèm ảnh JPEG thật qua "Chọn ảnh...") → bấm "Thêm sách" → thành công, file lưu đúng `images/books/8888777766.jpg`. Đăng xuất, đăng nhập `customer01` → ảnh thật hiển thị đúng (cover-fit, bo góc) ở cả lưới "Tìm kiếm sách" và trang "Chi tiết sách". Sách seed cũ (không có file ảnh thật) vẫn hiển thị bìa vẽ tay như cũ, không lỗi. Không exception trong `app_err.log` suốt luồng. Đã dọn dữ liệu test (xóa sách ID 23 + file ảnh qua sqlcmd, vì sách có gán Danh mục/Tác giả nên bị chặn xóa qua UI theo đúng thiết kế).

---

*File này để theo dõi tiến độ test thủ công qua GUI thật, không phải checklist code review tĩnh.*
