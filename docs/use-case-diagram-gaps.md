# So sánh sơ đồ Use Case "Tổng quát" với code hiện tại

> Đối chiếu trang **"Tổng quát"** (diagram đầu tiên, id `0`) trong
> `C:\Users\MSI\Downloads\Usecase BookShop.drawio` với danh sách use case rút
> trực tiếp từ code trong [`use-cases.md`](./use-cases.md) (đã đọc lại toàn
> bộ 4 Shell + nút hành động từng trang trước khi lập bảng này). 3 trang chi
> tiết còn lại trong cùng file (Manage Books / Purchase Books Online / View
> Reports) **không** nằm trong phạm vi so sánh này — chỉ so sánh đúng trang
> Tổng quát theo yêu cầu.

## Tóm tắt số liệu

| Actor | Sơ đồ cũ có | Code thực tế có | Còn thiếu |
|---|---|---|---|
| Admin | 3 module | 10 module | **7** |
| Sales Staff | 1 module khớp đúng (2 module gắn nhầm actor) | 6 module | **5** |
| Inventory Staff | 2 module | 6 module | **4** |
| Customer | 2 module (gộp mờ) | 17 use case | **~14** |
| Chung (Login/Logout/Đổi mật khẩu/Đăng ký) | Đủ 4 | 4 | 0 |

Sơ đồ cũ chỉ có **11 use case cụ thể**; code hiện tại có khoảng **43 use case**
across 4 actor. Ngoài phần thiếu, sơ đồ cũ còn có **2 chỗ vẽ sai quan hệ**
(không phải thiếu, mà cần sửa lại) — nêu ở mục cuối.

---

## 1. Admin — thiếu 7/10 module

Sơ đồ cũ chỉ có 3 bubble gộp chung trong "Admin Login": **Manage Books,
Manage Accounts, View reports**.

| Module (theo code) | Có trong sơ đồ? | Nguồn code |
|---|---|---|
| Quản lý sách (Thêm/Sửa/Xóa/Xem) | ✅ = "Manage Books" | `ManageBooksPage`, `AddBookPage`, `EditBookPage`, `DeleteBookPage` |
| Quản lý tài khoản | ✅ = "Manage Accounts" | `ManageAccountPage` |
| Báo cáo thống kê (+ Xuất Excel) | ✅ = "View reports" | `ReportPage`, `ExcelExportUtil` |
| Xem Dashboard tổng quan | ❌ thiếu | `DashboardPage` |
| Quản lý danh mục | ❌ thiếu | `ManageCategoryPage` |
| Quản lý tác giả | ❌ thiếu | `ManageAuthorPage` |
| Quản lý nhà xuất bản | ❌ thiếu | `ManagePublisherPage` |
| Quản lý nhà cung cấp | ❌ thiếu *(module chưa commit)* | `ManageSupplierPage` |
| Quản lý khách hàng (tách riêng, có trang Xem chi tiết khách hàng) | ❌ thiếu | `ManageCustomerPage`, `CustomerDetailPage` |
| Quản lý nhân viên | ❌ thiếu | `ManageEmployeePage` |

## 2. Sales Staff — thiếu 5/6 module

Sơ đồ cũ gắn 3 use case vào "Sale Staff Login": **Purchase Online, Purchase
in Store, Cancel Online Order**. Trong đó **chỉ "Purchase in Store" là đúng
actor** — "Purchase Online" và "Cancel Online Order" thực chất thuộc về
Customer (xem mục "Lỗi quan hệ" bên dưới), nên Sales Staff coi như **chỉ có
đúng 1 module khớp thật**.

| Module (theo code) | Có trong sơ đồ? | Nguồn code |
|---|---|---|
| Lập hóa đơn tại quầy (+ tạo khách hàng mới nhanh) | ✅ = "Purchase in Store" | `InStoreSaleFrm` |
| Xem/hủy hóa đơn đã lập | ❌ thiếu | `InvoiceHistoryFrm` |
| Xem/cập nhật trạng thái đơn hàng online | ❌ thiếu | `OrderManagementFrm` |
| Tra cứu khách hàng | ❌ thiếu | `SearchCustomerFrm` |
| Tra cứu sách | ❌ thiếu | `SalesSearchBookFrm` |
| Xem cảnh báo tồn kho thấp | ❌ thiếu | `SalesLowStockFrm` |

## 3. Inventory Staff — thiếu 4/6 module

Sơ đồ cũ có 2 use case trong "Inventory Staff Login": **Import Books,
Inventory Checking**.

| Module (theo code) | Có trong sơ đồ? | Nguồn code |
|---|---|---|
| Nhập sách | ✅ = "Import Books" | `ImportBooksFrm` |
| Kiểm kê kho | ✅ = "Inventory Checking" | `InventoryCheckingFrm` |
| Xem sách sắp hết hàng | ❌ thiếu | `LowStockFrm` |
| Xuất trả nhà cung cấp | ❌ thiếu *(module chưa commit)* | `ReturnToSupplierFrm` |
| Xem lịch sử nhập/trả | ❌ thiếu *(module chưa commit)* | `StockHistoryFrm` |
| Tra cứu sách | ❌ thiếu *(module chưa commit)* | `InventorySearchBookFrm` |

## 4. Customer — thiếu khoảng 14/17 use case

Sơ đồ cũ chỉ gộp mờ 2 use case trong "Customer Login": **Purchase Online,
Cancel Online Order**.

| Use case (theo code) | Có trong sơ đồ? | Nguồn code |
|---|---|---|
| Đặt hàng (checkout) → Thanh toán | ✅ ~ gộp trong "Purchase Online" | `CheckoutFrm`, `PaymentFrm` |
| Hủy đơn hàng | ✅ = "Cancel Online Order" | `OrderHistoryFrm` |
| Xem trang chủ | ❌ thiếu | `CustomerHomeFrm` |
| Tìm kiếm sách | ❌ thiếu | `SearchBookForPurchaseFrm` |
| Xem chi tiết sách | ❌ thiếu | `BookDetailFrm` |
| Thêm sách vào giỏ hàng | ❌ thiếu | `BookDetailFrm` |
| Xem giỏ hàng | ❌ thiếu | `ShoppingCartFrm` |
| Xóa sách khỏi giỏ hàng | ❌ thiếu | `ShoppingCartFrm` |
| Thanh toán mô phỏng VNPay/Momo | ❌ thiếu (chi tiết bước thanh toán) | `MockPaymentGatewayFrm` |
| Xem lịch sử đơn hàng | ❌ thiếu | `OrderHistoryFrm` |
| Xem chi tiết đơn hàng | ❌ thiếu | `OrderHistoryFrm` |
| Xem đánh giá sách | ❌ thiếu | `BookDetailFrm` |
| Gửi đánh giá sách | ❌ thiếu | `BookDetailFrm` |
| Xem/sửa thông tin cá nhân | ❌ thiếu | `AccountFrm` |
| Đổi ảnh đại diện | ❌ thiếu | `AccountFrm`, `AvatarStore` |
| Xem thống kê cá nhân (tổng đơn/tổng chi tiêu) | ❌ thiếu | `AccountFrm` |

---

## 5. Lỗi quan hệ cần sửa lại (không phải thiếu — vẽ chưa đúng)

1. **"Purchase Online" và "Cancel Online Order" đang bị gắn vào cả "Sale
   Staff Login" lẫn "Customer Login"** trong sơ đồ cũ. Theo code, đặt hàng
   online và hủy đơn online chỉ là hành động của **Customer**
   (`CheckoutFrm`, `PaymentFrm`, `OrderHistoryFrm`) — Sales Staff không
   tự mua hàng, chỉ **xem/cập nhật trạng thái** đơn hàng online
   (`OrderManagementFrm`, đã liệt kê ở mục 2, hiện chưa có trong sơ đồ nào).
2. **"Register Account" đang generalize (mũi tên rỗng, is-a) vào "Manage
   Accounts"** — tức vẽ như Đăng ký tài khoản là 1 dạng con của nghiệp vụ
   Quản lý tài khoản bên Admin. Thực tế đăng ký là Customer tự làm
   (`RegisterDialog`), không liên quan gì đến Admin quản lý tài khoản — nên bỏ
   quan hệ generalize này, chỉ giữ association Customer — Register Account.

---

## 6. Không thuộc phạm vi so sánh này

Các module/use case đánh dấu *(module chưa commit)* ở trên (Quản lý nhà
cung cấp, Xuất trả NCC, Lịch sử nhập/trả, Tra cứu sách bên Kho) hiện có
trong code (`git status` cho thấy đang ở working tree, chưa `git commit`) —
cần xác nhận có tính vào phạm vi báo cáo đồ án hay không trước khi vẽ thêm
vào sơ đồ mới (đã hỏi ở cuối `use-cases.md`, chưa có câu trả lời).
