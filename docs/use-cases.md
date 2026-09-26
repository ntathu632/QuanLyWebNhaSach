# Danh sách Actor & Use Case — QuanLyWebNhaSach

> Rút trực tiếp từ code hiện tại (không suy đoán theo yêu cầu đề bài), tính đến
> commit `49ea8e2` + các thay đổi/chức năng đang làm dở chưa commit (Nhà cung
> cấp, Xuất trả NCC, Lịch sử nhập/trả, Tra cứu sách bên Kho). Cơ sở đối chiếu:
> menu sidebar/navbar của từng Shell (`AdminShellFrm`, `InStoreSaleFrm`,
> `LowStockFrm`, `CustomerHomeFrm`) + nút hành động trong từng trang.
>
> Đây là **bước 1 — liệt kê tổng quan** để xác nhận trước khi viết đặc tả chi
> tiết (precondition, main flow, alternative flow...) cho từng use case ở bước
> sau.

## 1. Actor

| Actor | Mô tả | Vào hệ thống qua |
|---|---|---|
| **Khách hàng** (Customer) | Người mua sách trực tuyến | `CustomerHomeFrm` |
| **Nhân viên bán hàng** (Sales Staff) | Lập hóa đơn tại quầy, xử lý đơn online | `InStoreSaleFrm` |
| **Nhân viên kho** (Inventory Staff) | Nhập/xuất/kiểm kê tồn kho | `LowStockFrm` |
| **Quản trị viên** (Admin) | Quản lý toàn bộ danh mục dữ liệu + báo cáo | `AdminShellFrm` |

3 vai trò nhân viên (Sales/Inventory/Admin) đều là `Employee` gắn với 1
`Account`, đăng nhập chung 1 màn hình `LoginDialog` rồi hệ thống tự điều hướng
theo `Account.Role`. Khách hàng là `Customer` gắn với 1 `Account` riêng.

### Use case dùng chung cho mọi actor đã đăng nhập

| Use case | Actor | Nguồn |
|---|---|---|
| Đăng nhập | Cả 4 actor | `LoginDialog` |
| Đăng xuất | Cả 4 actor | nút "Đăng xuất" mỗi Shell |
| Đổi mật khẩu | Cả 4 actor | `ChangePasswordDialog` |
| Đăng ký tài khoản | Khách hàng | `RegisterDialog` (chỉ Khách hàng tự đăng ký được; tài khoản nhân viên do Admin tạo qua "Quản lý nhân viên") |

---

## 2. Use case theo Actor

### 2.1 Quản trị viên (Admin) — `AdminShellFrm`

| Nhóm | Use case | Ghi chú | Nguồn |
|---|---|---|---|
| Dashboard | Xem dashboard tổng quan | Số liệu sách/đơn/khách/doanh thu + biểu đồ | `DashboardPage` |
| Quản lý sách | Xem danh sách sách | | `ManageBooksPage` |
| | Thêm sách | Kèm NXB (bắt buộc), Danh mục/Tác giả (tùy chọn), ảnh bìa | `AddBookPage` |
| | Sửa sách | Tìm sách trước rồi mới sửa | `SearchBookForEditPage` → `EditBookPage` |
| | Xóa sách | Bị chặn nếu sách còn liên kết Danh mục/Tác giả/Tồn kho/Giỏ hàng/Đơn hàng | `DeleteBookPage` |
| Quản lý danh mục | Xem / Thêm / Sửa / Xóa danh mục | Xóa bị chặn nếu còn sách thuộc danh mục | `ManageCategoryPage` |
| Quản lý tác giả | Xem / Thêm / Sửa / Xóa tác giả | Xóa bị chặn nếu còn sách của tác giả | `ManageAuthorPage` |
| Quản lý nhà xuất bản | Xem / Thêm / Sửa / Xóa NXB | Xóa bị chặn nếu còn sách của NXB | `ManagePublisherPage` |
| Quản lý nhà cung cấp *(chưa commit)* | Xem / Thêm / Sửa / Xóa nhà cung cấp | | `ManageSupplierPage` |
| Quản lý khách hàng | Xem danh sách khách hàng | | `ManageCustomerPage` |
| | Xem chi tiết khách hàng | Thông tin cá nhân + thống kê đơn hàng/chi tiêu + lịch sử đơn + đánh giá đã viết | `CustomerDetailPage` |
| | Sửa thông tin khách hàng | | `ManageCustomerPage` |
| Quản lý nhân viên | Xem danh sách nhân viên | | `ManageEmployeePage` |
| | Thêm nhân viên | Tạo đồng thời Employee + Account | `ManageEmployeePage` |
| | Sửa thông tin nhân viên | | `ManageEmployeePage` |
| | Xóa nhân viên | Bị chặn nếu nhân viên đang có Account (phải khóa Account thay vì xóa) | `ManageEmployeePage` |
| Quản lý tài khoản | Xem danh sách tài khoản | Toàn bộ Account (Admin/Sales/Inventory/Customer) | `ManageAccountPage` |
| | Sửa tài khoản | Đổi vai trò + khóa/mở khóa (status); có xác nhận riêng nếu tự khóa tài khoản đang đăng nhập | `ManageAccountPage` |
| | Xem chi tiết khách hàng (từ danh sách tài khoản) | Chỉ áp dụng tài khoản vai trò Customer | `ManageAccountPage` → `CustomerDetailPage` |
| Báo cáo thống kê | Xem báo cáo Doanh thu (theo khoảng ngày) | | `ReportPage` |
| | Xem báo cáo Tồn kho | | `ReportPage` |
| | Xem báo cáo Bán hàng (sách bán chạy + doanh số theo nhân viên) | | `ReportPage` |
| | Xuất Excel báo cáo | 1 nút riêng cho mỗi tab (Doanh thu/Tồn kho/Bán hàng) | `ReportPage`, `ExcelExportUtil` |

### 2.2 Nhân viên bán hàng (Sales Staff) — `InStoreSaleFrm`

| Nhóm | Use case | Ghi chú | Nguồn |
|---|---|---|---|
| Bán hàng | Lập hóa đơn tại quầy | Tìm sách, thêm dòng, xóa dòng, chọn khách quen hoặc tạo khách mới nhanh | `InStoreSaleFrm` |
| | Tạo khách hàng mới (nhanh, trong lúc bán) | | `InStoreSaleFrm` |
| | Xem hóa đơn đã lập | | `InvoiceHistoryFrm` |
| | Xem chi tiết hóa đơn | | `InvoiceHistoryFrm` |
| | Hủy hóa đơn | | `InvoiceHistoryFrm` |
| | Xem danh sách đơn hàng online | | `OrderManagementFrm` |
| | Xem chi tiết đơn hàng online | | `OrderManagementFrm` |
| | Cập nhật trạng thái đơn hàng online | | `OrderManagementFrm` |
| Tra cứu | Tra cứu khách hàng | | `SearchCustomerFrm` |
| | Tra cứu sách | | `SalesSearchBookFrm` |
| | Xem cảnh báo tồn kho thấp | | `SalesLowStockFrm` |

### 2.3 Nhân viên kho (Inventory Staff) — `LowStockFrm`

| Nhóm | Use case | Ghi chú | Nguồn |
|---|---|---|---|
| Kho sách | Xem sách sắp hết hàng | Trang mặc định sau đăng nhập | `LowStockFrm` |
| | Nhập sách | Tìm sách, nhập số lượng vào kho | `ImportBooksFrm` |
| | Xuất trả nhà cung cấp *(chưa commit)* | Chỉ sách còn tồn kho > 0; chọn NCC + số lượng + lý do | `ReturnToSupplierFrm` |
| | Kiểm kê kho *(chưa commit)* | Nhập số lượng đếm thực tế; hệ thống chỉ điều chỉnh khi có chênh lệch | `InventoryCheckingFrm` |
| | Xem lịch sử nhập/trả *(chưa commit)* | Nhật ký chung cho Nhập sách + Xuất trả NCC | `StockHistoryFrm` |
| Tra cứu | Tra cứu sách *(chưa commit)* | | `InventorySearchBookFrm` |

### 2.4 Khách hàng (Customer) — `CustomerHomeFrm`

| Nhóm | Use case | Ghi chú | Nguồn |
|---|---|---|---|
| Mua sắm | Xem trang chủ | | `CustomerHomeFrm` |
| | Tìm kiếm sách | Ô tìm nhanh trên navbar + trang tìm kiếm riêng | `SearchBookForPurchaseFrm` |
| | Xem chi tiết sách | Ảnh bìa, giá, mô tả, đánh giá sao trung bình | `BookDetailFrm` |
| | Thêm sách vào giỏ hàng | | `BookDetailFrm` |
| | Xem giỏ hàng | | `ShoppingCartFrm` |
| | Xóa sách khỏi giỏ hàng | | `ShoppingCartFrm` |
| | Đặt hàng (checkout) | Xác nhận thông tin đơn trước khi thanh toán | `CheckoutFrm` |
| | Thanh toán đơn hàng | Có thể hủy đơn ở bước này | `PaymentFrm` |
| | Thanh toán mô phỏng VNPay/Momo | Xác nhận đã thanh toán hoặc hủy giao dịch quay lại | `MockPaymentGatewayFrm` |
| | Xem lịch sử đơn hàng | | `OrderHistoryFrm` |
| | Xem chi tiết đơn hàng | | `OrderHistoryFrm` |
| | Hủy đơn hàng | | `OrderHistoryFrm` |
| Đánh giá | Xem đánh giá của sách | Xem được kể cả khi chưa mua sách đó | `BookDetailFrm` |
| | Gửi đánh giá sách | Chỉ khi đã mua **và** đơn đã thanh toán | `BookDetailFrm` (extend của "Xem chi tiết sách") |
| Tài khoản | Xem/sửa thông tin cá nhân | Họ tên, SĐT, Email, Địa chỉ (từng trường sửa riêng) | `AccountFrm` |
| | Đổi ảnh đại diện | | `AccountFrm`, `AvatarStore` |
| | Xem thống kê cá nhân | Tổng số đơn hàng, tổng chi tiêu | `AccountFrm` |

---

## 3. Ghi chú quan hệ include/extend đáng chú ý (sơ bộ)

- **Đặt hàng (checkout)** *include* **Thanh toán đơn hàng** *include*
  **Thanh toán mô phỏng VNPay/Momo**.
- **Gửi đánh giá sách** *extend* **Xem chi tiết sách** (chỉ xuất hiện điều
  kiện khi đã mua + đã thanh toán).
- **Lập hóa đơn tại quầy** *include* **Tạo khách hàng mới (nhanh)** khi khách
  chưa có trong hệ thống (tùy chọn, không bắt buộc mỗi lần bán).
- **Xóa danh mục / tác giả / NXB / sách / nhân viên** đều có *precondition*
  chặn xóa nếu còn dữ liệu liên quan — cần thể hiện rõ trong đặc tả chi tiết
  ở bước sau vì đây là rule nghiệp vụ lặp lại nhiều nơi.
- **Xem chi tiết khách hàng** được vào từ 2 nơi khác nhau (Quản lý khách hàng
  **và** Quản lý tài khoản) nhưng cùng 1 trang đích `CustomerDetailPage`.

---

## 4. Việc cần xác nhận trước khi làm đặc tả chi tiết

1. Các mục đánh dấu *(chưa commit)* (Nhà cung cấp, Xuất trả NCC, Kiểm kê kho,
   Lịch sử nhập/trả, Tra cứu sách bên Kho) — có tính vào phạm vi báo cáo đồ án
   không, hay chỉ tính những gì đã commit?
2. Có cần vẽ riêng use case cho từng thao tác CRUD (Thêm/Sửa/Xóa) hay gộp
   chung thành 1 use case "Quản lý X" trên sơ đồ (phổ biến hơn khi vẽ Use Case
   Diagram ở mức tổng quan, chi tiết CRUD để trong đặc tả)?
