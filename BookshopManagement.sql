/* 
   BOOKSHOP MANAGEMENT SYSTEM
   Database: SQL Server
   */

CREATE DATABASE BookshopManagementDB;
GO

USE BookshopManagementDB;
GO


/* 
   1. PUBLISHER
*/

CREATE TABLE Publisher (
    publisherID INT IDENTITY(1,1) PRIMARY KEY,
    publisherName NVARCHAR(150) NOT NULL,
    address NVARCHAR(255) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(150) NULL
);
GO


/*
   2. CATEGORY
*/

CREATE TABLE Category (
    categoryID INT IDENTITY(1,1) PRIMARY KEY,
    categoryName NVARCHAR(150) NOT NULL,
    description NVARCHAR(500) NULL,

    CONSTRAINT UQ_Category_CategoryName
        UNIQUE (categoryName)
);
GO


/*
   3. AUTHOR
*/

CREATE TABLE Author (
    authorID INT IDENTITY(1,1) PRIMARY KEY,
    authorName NVARCHAR(150) NOT NULL,
    nationality NVARCHAR(100) NULL,
    description NVARCHAR(500) NULL
);
GO


/* 
   4. BOOK
*/

CREATE TABLE Book (
    bookID INT IDENTITY(1,1) PRIMARY KEY,
    ISBN VARCHAR(20) NOT NULL,
    title NVARCHAR(255) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    description NVARCHAR(MAX) NULL,
    image NVARCHAR(500) NULL,
    publisherID INT NOT NULL,

    CONSTRAINT UQ_Book_ISBN
        UNIQUE (ISBN),

    CONSTRAINT CK_Book_Price
        CHECK (price > 0),

    CONSTRAINT FK_Book_Publisher
        FOREIGN KEY (publisherID)
        REFERENCES Publisher(publisherID)
);
GO


/* 
   5. BOOK CATEGORY
*/

CREATE TABLE BookCategory (
    bookID INT NOT NULL,
    categoryID INT NOT NULL,

    CONSTRAINT PK_BookCategory
        PRIMARY KEY (bookID, categoryID),

    CONSTRAINT FK_BookCategory_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID)
        ON DELETE CASCADE,

    CONSTRAINT FK_BookCategory_Category
        FOREIGN KEY (categoryID)
        REFERENCES Category(categoryID)
        ON DELETE CASCADE
);
GO


/* 
   6. BOOK AUTHOR
*/

CREATE TABLE BookAuthor (
    bookID INT NOT NULL,
    authorID INT NOT NULL,

    CONSTRAINT PK_BookAuthor
        PRIMARY KEY (bookID, authorID),

    CONSTRAINT FK_BookAuthor_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID)
        ON DELETE CASCADE,

    CONSTRAINT FK_BookAuthor_Author
        FOREIGN KEY (authorID)
        REFERENCES Author(authorID)
        ON DELETE CASCADE
);
GO


/* 
   7. INVENTORY
   Book và Inventory có quan hệ 1 - 1.
 */

CREATE TABLE Inventory (
    bookID INT PRIMARY KEY,
    importQuantity INT NOT NULL
        CONSTRAINT DF_Inventory_ImportQuantity DEFAULT 0,
    exportQuantity INT NOT NULL
        CONSTRAINT DF_Inventory_ExportQuantity DEFAULT 0,
    stockQuantity INT NOT NULL
        CONSTRAINT DF_Inventory_StockQuantity DEFAULT 0,

    CONSTRAINT CK_Inventory_ImportQuantity
        CHECK (importQuantity >= 0),

    CONSTRAINT CK_Inventory_ExportQuantity
        CHECK (exportQuantity >= 0),

    CONSTRAINT CK_Inventory_StockQuantity
        CHECK (stockQuantity >= 0),

    CONSTRAINT FK_Inventory_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID)
        ON DELETE CASCADE
);
GO


/* 
   8. CUSTOMER
 */

CREATE TABLE Customer (
    customerID INT IDENTITY(1,1) PRIMARY KEY,
    fullName NVARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL,
    address NVARCHAR(255) NULL,
    avatar NVARCHAR(500) NULL,

    CONSTRAINT UQ_Customer_Email
        UNIQUE (email)
);
GO


/*
   9. EMPLOYEE
 */

CREATE TABLE Employee (
    employeeID INT IDENTITY(1,1) PRIMARY KEY,
    fullName NVARCHAR(150) NOT NULL,
    position NVARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL,

    CONSTRAINT UQ_Employee_Email
        UNIQUE (email)
);
GO


/*
   10. ACCOUNT
   Một Account thuộc Customer hoặc Employee.
   Không được thuộc đồng thời cả hai.
   Lưu ý: KHÔNG dùng UNIQUE constraint thường cho customerID/employeeID vì
   SQL Server chỉ cho phép đúng 1 dòng NULL với constraint loại đó (ở đây có
   nhiều dòng NULL: 1 Account của Employee thì customerID NULL, và ngược lại).
   Thay vào đó dùng UNIQUE INDEX có lọc (filtered) khai báo ở phần INDEXES
   bên dưới (UQ_Account_Customer, UQ_Account_Employee), cho phép nhiều NULL.
*/

CREATE TABLE Account (
    username VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    customerID INT NULL,
    employeeID INT NULL,

    CONSTRAINT CK_Account_Owner
        CHECK (
            (customerID IS NOT NULL AND employeeID IS NULL)
            OR
            (customerID IS NULL AND employeeID IS NOT NULL)
        ),

    CONSTRAINT FK_Account_Customer
        FOREIGN KEY (customerID)
        REFERENCES Customer(customerID),

    CONSTRAINT FK_Account_Employee
        FOREIGN KEY (employeeID)
        REFERENCES Employee(employeeID)
);
GO


/* 
   11. SHOPPING CART
   Mỗi Customer có tối đa một ShoppingCart.
 */

CREATE TABLE ShoppingCart (
    cartID INT IDENTITY(1,1) PRIMARY KEY,
    createdDate DATETIME2 NOT NULL
        CONSTRAINT DF_ShoppingCart_CreatedDate DEFAULT SYSDATETIME(),
    customerID INT NOT NULL,

    CONSTRAINT UQ_ShoppingCart_Customer
        UNIQUE (customerID),

    CONSTRAINT FK_ShoppingCart_Customer
        FOREIGN KEY (customerID)
        REFERENCES Customer(customerID)
        ON DELETE CASCADE
);
GO


/* 
   12. CART ITEM
 */

CREATE TABLE CartItem (
    cartID INT NOT NULL,
    bookID INT NOT NULL,
    quantity INT NOT NULL,
    unitPrice DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,

    CONSTRAINT PK_CartItem
        PRIMARY KEY (cartID, bookID),

    CONSTRAINT CK_CartItem_Quantity
        CHECK (quantity > 0),

    CONSTRAINT CK_CartItem_UnitPrice
        CHECK (unitPrice >= 0),

    CONSTRAINT CK_CartItem_Subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT FK_CartItem_ShoppingCart
        FOREIGN KEY (cartID)
        REFERENCES ShoppingCart(cartID)
        ON DELETE CASCADE,

    CONSTRAINT FK_CartItem_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID)
);
GO


/*
   13. ORDERS
   Dùng Orders thay cho Order vì ORDER là từ khóa SQL.
   status: khớp đúng 3 giá trị code Java hiện đang dùng (ShoppingCartFrm,
   PaymentFrm, OrderDAO.updateOrderStatus) — 'Pending' (mới tạo) ->
   'Paid' (thanh toán thành công) hoặc 'Cancelled' (hủy ở bước thanh toán).
   Không dùng 'Processing'/'Completed' vì code hiện tại không có luồng nào
   sinh ra 2 giá trị đó.
 */

CREATE TABLE Orders (
    orderID INT IDENTITY(1,1) PRIMARY KEY,
    orderDate DATETIME2 NOT NULL
        CONSTRAINT DF_Orders_OrderDate DEFAULT SYSDATETIME(),
    totalAmount DECIMAL(14,2) NOT NULL,
    shippingAddress NVARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    customerID INT NOT NULL,

    CONSTRAINT CK_Orders_TotalAmount
        CHECK (totalAmount >= 0),

    CONSTRAINT FK_Orders_Customer
        FOREIGN KEY (customerID)
        REFERENCES Customer(customerID)
);
GO


/* 
   14. ORDER DETAIL
 */

CREATE TABLE OrderDetail (
    orderID INT NOT NULL,
    bookID INT NOT NULL,
    quantity INT NOT NULL,
    unitPrice DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,

    CONSTRAINT PK_OrderDetail
        PRIMARY KEY (orderID, bookID),

    CONSTRAINT CK_OrderDetail_Quantity
        CHECK (quantity > 0),

    CONSTRAINT CK_OrderDetail_UnitPrice
        CHECK (unitPrice >= 0),

    CONSTRAINT CK_OrderDetail_Subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT FK_OrderDetail_Orders
        FOREIGN KEY (orderID)
        REFERENCES Orders(orderID)
        ON DELETE CASCADE,

    CONSTRAINT FK_OrderDetail_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID)
);
GO


/* 
   15. PAYMENT
   Mỗi Order có tối đa một Payment.
 */

CREATE TABLE Payment (
    paymentID INT IDENTITY(1,1) PRIMARY KEY,
    paymentDate DATETIME2 NOT NULL
        CONSTRAINT DF_Payment_PaymentDate DEFAULT SYSDATETIME(),
    amount DECIMAL(14,2) NOT NULL,
    paymentMethod VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    orderID INT NOT NULL,

    CONSTRAINT UQ_Payment_Order
        UNIQUE (orderID),

    CONSTRAINT CK_Payment_Amount
        CHECK (amount >= 0),

    CONSTRAINT FK_Payment_Orders
        FOREIGN KEY (orderID)
        REFERENCES Orders(orderID)
        ON DELETE CASCADE
);
GO


/* 
   16. INVOICE
   Mỗi Order có tối đa một Invoice.
   Một Employee có thể lập nhiều Invoice.
 */

CREATE TABLE Invoice (
    invoiceID INT IDENTITY(1,1) PRIMARY KEY,
    invoiceDate DATETIME2 NOT NULL
        CONSTRAINT DF_Invoice_InvoiceDate DEFAULT SYSDATETIME(),
    totalAmount DECIMAL(14,2) NOT NULL,
    paymentMethod VARCHAR(50) NOT NULL,
    orderID INT NOT NULL,
    employeeID INT NOT NULL,

    CONSTRAINT UQ_Invoice_Order
        UNIQUE (orderID),

    CONSTRAINT CK_Invoice_TotalAmount
        CHECK (totalAmount >= 0),

    CONSTRAINT FK_Invoice_Orders
        FOREIGN KEY (orderID)
        REFERENCES Orders(orderID)
        ON DELETE CASCADE,

    CONSTRAINT FK_Invoice_Employee
        FOREIGN KEY (employeeID)
        REFERENCES Employee(employeeID)
);
GO


/*
   14. REVIEW
   Không có trong Entity Class.drawio gốc — bổ sung theo đúng ghi chú của
   FR-CUS-09 (Rate Book) trong SRS: "nhóm cần bổ sung entity Review/Rating
   nếu triển khai chức năng này". UNIQUE(bookID, customerID): mỗi khách chỉ
   đánh giá 1 sách đúng 1 lần (giả định hợp lý, thông lệ TMĐT phổ biến,
   không có trong tài liệu gốc).
*/

CREATE TABLE Review (
    reviewID INT IDENTITY(1,1) PRIMARY KEY,
    bookID INT NOT NULL,
    customerID INT NOT NULL,
    rating INT NOT NULL,
    comment NVARCHAR(500) NULL,
    reviewDate DATETIME2 NOT NULL
        CONSTRAINT DF_Review_ReviewDate DEFAULT SYSDATETIME(),

    CONSTRAINT CK_Review_Rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT UQ_Review_Book_Customer
        UNIQUE (bookID, customerID),

    CONSTRAINT FK_Review_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID)
        ON DELETE CASCADE,

    CONSTRAINT FK_Review_Customer
        FOREIGN KEY (customerID)
        REFERENCES Customer(customerID)
        ON DELETE CASCADE
);
GO


/*
   15. SUPPLIER
   Không có trong Entity Class.drawio gốc — bổ sung để nhân viên kho có thể
   ghi nhận nhập/trả hàng gắn với 1 nhà cung cấp cụ thể, khác Publisher (đơn
   vị xuất bản, không phải nơi kho nhập hàng thực tế).
*/

CREATE TABLE Supplier (
    supplierID INT IDENTITY(1,1) PRIMARY KEY,
    supplierName NVARCHAR(150) NOT NULL,
    address NVARCHAR(255) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(150) NULL
);
GO


/*
   16. STOCKTRANSACTION
   Không có trong Entity Class.drawio gốc — bổ sung để nhân viên kho có nhật
   ký nhập/trả hàng (trước đây Inventory.importQuantity chỉ là 1 số cộng dồn,
   không lưu lại từng lần nhập là ai/khi nào/bao nhiêu/từ nhà cung cấp nào).
   Dùng chung 1 bảng cho cả 2 chiều (type='Import'/'Return') thay vì 2 bảng
   riêng vì cùng hình dạng dữ liệu, chỉ khác chiều tác động lên tồn kho.
*/

CREATE TABLE StockTransaction (
    stockTransactionID INT IDENTITY(1,1) PRIMARY KEY,
    bookID INT NOT NULL,
    supplierID INT NOT NULL,
    employeeID INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    transactionDate DATETIME2 NOT NULL
        CONSTRAINT DF_StockTransaction_Date DEFAULT SYSDATETIME(),
    note NVARCHAR(255) NULL,

    CONSTRAINT CK_StockTransaction_Type
        CHECK (type IN ('Import', 'Return')),

    CONSTRAINT CK_StockTransaction_Quantity
        CHECK (quantity > 0),

    CONSTRAINT FK_StockTransaction_Book
        FOREIGN KEY (bookID)
        REFERENCES Book(bookID),

    CONSTRAINT FK_StockTransaction_Supplier
        FOREIGN KEY (supplierID)
        REFERENCES Supplier(supplierID),

    CONSTRAINT FK_StockTransaction_Employee
        FOREIGN KEY (employeeID)
        REFERENCES Employee(employeeID)
);
GO


/*
   INDEXES
 */

CREATE INDEX IX_Book_Title
ON Book(title);
GO

CREATE INDEX IX_Orders_CustomerID
ON Orders(customerID);
GO

CREATE INDEX IX_Orders_OrderDate
ON Orders(orderDate);
GO

CREATE INDEX IX_OrderDetail_BookID
ON OrderDetail(bookID);
GO

CREATE INDEX IX_CartItem_BookID
ON CartItem(bookID);
GO

CREATE INDEX IX_Invoice_EmployeeID
ON Invoice(employeeID);
GO

CREATE INDEX IX_Review_BookID
ON Review(bookID);
GO

-- Filtered index bắt buộc phải bật QUOTED_IDENTIFIER, nếu không CREATE INDEX sẽ báo lỗi 1934.
SET QUOTED_IDENTIFIER ON;
GO

CREATE UNIQUE INDEX UQ_Account_Customer
ON Account(customerID)
WHERE customerID IS NOT NULL;
GO

CREATE UNIQUE INDEX UQ_Account_Employee
ON Account(employeeID)
WHERE employeeID IS NOT NULL;
GO


/* =========================
   INSERT PUBLISHER
   ========================= */

INSERT INTO Publisher (publisherName, address, phone, email)
VALUES
(N'Nhà xuất bản Trẻ', N'161B Lý Chính Thắng, TP.HCM', '02839316289', 'contact@nxbtre.com.vn'),
(N'Nhà xuất bản Kim Đồng', N'55 Quang Trung, Hà Nội', '02439434730', 'info@nxbkimdong.com.vn'),
(N'Penguin Random House', N'New York, USA', '+12127829000', 'support@penguinrandomhouse.com'),
(N'HarperCollins', N'London, United Kingdom', '+442087417070', 'contact@harpercollins.co.uk'),
(N'O''Reilly Media', N'California, USA', '+17078277000', 'support@oreilly.com');
GO

/* =========================
   INSERT SUPPLIER
   ========================= */

INSERT INTO Supplier (supplierName, address, phone, email)
VALUES
(N'Công ty Phát hành sách Fahasa', N'60-62 Lê Lợi, Quận 1, TP.HCM', '02838222567', 'lienhe@fahasa.com'),
(N'Công ty CP Văn hóa Phương Nam', N'940 Ba Tháng Hai, Quận 11, TP.HCM', '02839575195', 'contact@pnc.com.vn'),
(N'Công ty Sách Alpha Books', N'176 Thái Hà, Đống Đa, Hà Nội', '02435730407', 'info@alphabooks.vn'),
(N'Công ty TNHH Thái Hà Books', N'119 C5 Từ Liêm, Hà Nội', '02462701734', 'lienhe@thaihabooks.com');
GO

/* =========================
   INSERT CATEGORY
   ========================= */

INSERT INTO Category (categoryName, description)
VALUES
(N'Công nghệ thông tin', N'Sách về lập trình và công nghệ'),
(N'Kinh doanh', N'Sách kinh doanh và tài chính'),
(N'Phát triển bản thân', N'Sách kỹ năng sống'),
(N'Văn học', N'Tiểu thuyết và văn học'),
(N'Thiếu nhi', N'Sách dành cho trẻ em'),
(N'Khoa học viễn tưởng', N'Sách khoa học viễn tưởng'),
(N'Trinh thám', N'Sách điều tra và phá án'),
(N'Tâm lý học', N'Sách về tâm lý và hành vi');
GO

/* =========================
   INSERT AUTHOR
   ========================= */

INSERT INTO Author (authorName, nationality, description)
VALUES
(N'Robert C. Martin', N'Mỹ', N'Chuyên gia kỹ nghệ phần mềm'),
(N'James Clear', N'Mỹ', N'Tác giả Atomic Habits'),
(N'Morgan Housel', N'Mỹ', N'Tác giả The Psychology of Money'),
(N'J. K. Rowling', N'Anh', N'Tác giả Harry Potter'),
(N'Arthur Conan Doyle', N'Anh', N'Tác giả Sherlock Holmes'),
(N'Frank Herbert', N'Mỹ', N'Tác giả Dune'),
(N'J. R. R. Tolkien', N'Anh', N'Tác giả The Hobbit'),
(N'Nguyễn Nhật Ánh', N'Việt Nam', N'Nhà văn Việt Nam'),
(N'Dale Carnegie', N'Mỹ', N'Tác giả How to Win Friends'),
(N'Daniel Kahneman', N'Mỹ', N'Tác giả Thinking, Fast and Slow');
GO

/* =========================
   INSERT BOOK
   ========================= */

INSERT INTO Book (ISBN, title, price, description, image, publisherID)
VALUES
('9780132350884', N'Clean Code', 420000, N'Cẩm nang viết mã nguồn sạch.', N'clean-code.jpg', 5),
('9780134494166', N'Clean Architecture', 460000, N'Kiến trúc phần mềm.', N'clean-architecture.jpg', 5),
('9780735211292', N'Atomic Habits', 280000, N'Xây dựng thói quen tốt.', N'atomic-habits.jpg', 3),
('9780857197689', N'The Psychology of Money', 310000, N'Bài học về tiền bạc.', N'psychology-money.jpg', 4),
('9780747532699', N'Harry Potter and the Philosopher''s Stone', 250000, N'Harry Potter tập 1.', N'hp1.jpg', 4),
('9780747538493', N'Harry Potter and the Chamber of Secrets', 260000, N'Harry Potter tập 2.', N'hp2.jpg', 4),
('9780140439083', N'The Adventures of Sherlock Holmes', 220000, N'Truyện Sherlock Holmes.', N'sherlock.jpg', 3),
('9780441172719', N'Dune', 390000, N'Tiểu thuyết khoa học viễn tưởng.', N'dune.jpg', 3),
('9780547928227', N'The Hobbit', 340000, N'Cuộc phiêu lưu của Bilbo.', N'hobbit.jpg', 4),
('9786041129832', N'Mắt Biếc', 120000, N'Tác phẩm của Nguyễn Nhật Ánh.', N'mat-biec.jpg', 1),
('9786041135604', N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', 135000, N'Tuổi thơ Việt Nam.', N'hoa-vang.jpg', 1),
('9780671027032', N'How to Win Friends and Influence People', 240000, N'Nghệ thuật giao tiếp.', N'friends.jpg', 3),
('9780374533557', N'Thinking, Fast and Slow', 360000, N'Tâm lý học hành vi.', N'thinking.jpg', 3),
('9781491950296', N'Designing Data-Intensive Applications', 520000, N'Thiết kế hệ thống dữ liệu.', N'ddia.jpg', 5),
('9780596007126', N'Head First Design Patterns', 480000, N'Design Pattern.', N'hfdp.jpg', 5),
('9780061120084', N'To Kill a Mockingbird', 230000, N'Tiểu thuyết nổi tiếng.', N'mockingbird.jpg', 4),
('9780141439518', N'Pride and Prejudice', 210000, N'Tác phẩm kinh điển.', N'pride.jpg', 3),
('9780062316097', N'Sapiens', 330000, N'Lược sử loài người.', N'sapiens.jpg', 4),
('9786042112383', N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', 110000, N'Tác phẩm Nguyễn Nhật Ánh.', N've-tuoi-tho.jpg', 2),
('9780590353427', N'Harry Potter and the Sorcerer''s Stone', 270000, N'Ấn bản Mỹ.', N'hp-us.jpg', 3);
GO


/* =========================
   INSERT BOOKCATEGORY
   ========================= */

INSERT INTO BookCategory VALUES
(1,1),
(2,1),
(3,3),
(4,2),
(4,8),
(5,5),
(5,6),
(6,5),
(6,6),
(7,7),
(8,6),
(9,5),
(9,6),
(10,4),
(11,4),
(12,3),
(13,8),
(14,1),
(15,1),
(16,4),
(17,4),
(18,4),
(19,5),
(19,4),
(20,5),
(20,6);
GO


/* =========================
   INSERT BOOKAUTHOR
   ========================= */

INSERT INTO BookAuthor VALUES
(1,1),
(2,1),
(3,2),
(4,3),
(5,4),
(6,4),
(7,5),
(8,6),
(9,7),
(10,8),
(11,8),
(12,9),
(13,10),
(14,1),
(15,1),
(16,9),
(17,10),
(18,3),
(19,8),
(20,4);
GO


/* =========================
   INSERT INVENTORY
   ========================= */

INSERT INTO Inventory
(bookID, importQuantity, exportQuantity, stockQuantity)
VALUES
(1,50,18,32),
(2,40,12,28),
(3,60,25,35),
(4,45,17,28),
(5,50,20,30),
(6,35,10,25),
(7,30,9,21),
(8,40,14,26),
(9,32,7,25),
(10,55,23,32),
(11,45,20,25),
(12,40,16,24),
(13,38,15,23),
(14,30,8,22),
(15,28,7,21),
(16,35,13,22),
(17,30,11,19),
(18,42,18,24),
(19,50,24,26),
(20,36,12,24);
GO

/* =========================
   INSERT CUSTOMER
   ========================= */

INSERT INTO Customer (fullName, phone, email, address)
VALUES
(N'Nguyễn Văn An', '0901000001', 'an.nguyen@email.com', N'Hà Nội'),
(N'Trần Thị Bình', '0901000002', 'binh.tran@email.com', N'TP. Hồ Chí Minh'),
(N'Lê Minh Châu', '0901000003', 'chau.le@email.com', N'Đà Nẵng'),
(N'Phạm Gia Duy', '0901000004', 'duy.pham@email.com', N'Cần Thơ'),
(N'Hoàng Thu Hà', '0901000005', 'ha.hoang@email.com', N'Hải Phòng'),
(N'Vũ Quốc Huy', '0901000006', 'huy.vu@email.com', N'Huế'),
(N'Đặng Ngọc Lan', '0901000007', 'lan.dang@email.com', N'Nha Trang'),
(N'Bùi Anh Minh', '0901000008', 'minh.bui@email.com', N'Đồng Nai'),
(N'Đỗ Khánh Ngân', '0901000009', 'ngan.do@email.com', N'Bình Dương'),
(N'Ngô Đức Phúc', '0901000010', 'phuc.ngo@email.com', N'Vũng Tàu');
GO


/* =========================
   INSERT EMPLOYEE
   ========================= */

INSERT INTO Employee (fullName, position, phone, email)
VALUES
(N'Nguyễn Hoàng', 'Admin', '0912000001', 'admin@bookstore.com'),
(N'Trần Minh', 'Sales Staff', '0912000002', 'sales1@bookstore.com'),
(N'Lê Thanh', 'Sales Staff', '0912000003', 'sales2@bookstore.com'),
(N'Phạm Quốc', 'Inventory Staff', '0912000004', 'inventory@bookstore.com');
GO


/* =========================
   INSERT ACCOUNT
   ========================= */

INSERT INTO Account
(username, password, role, status, customerID, employeeID)
VALUES
('admin', '123456', 'Admin', 'Active', NULL, 1),

('sales01', '123456', 'Sales Staff', 'Active', NULL, 2),
('sales02', '123456', 'Sales Staff', 'Active', NULL, 3),

('inventory01', '123456', 'Inventory Staff', 'Active', NULL, 4),

('customer01', '123456', 'Customer', 'Active', 1, NULL),
('customer02', '123456', 'Customer', 'Active', 2, NULL),
('customer03', '123456', 'Customer', 'Active', 3, NULL),
('customer04', '123456', 'Customer', 'Active', 4, NULL),
('customer05', '123456', 'Customer', 'Active', 5, NULL),
('customer06', '123456', 'Customer', 'Active', 6, NULL),
('customer07', '123456', 'Customer', 'Active', 7, NULL),
('customer08', '123456', 'Customer', 'Active', 8, NULL),
('customer09', '123456', 'Customer', 'Active', 9, NULL),
('customer10', '123456', 'Customer', 'Active', 10, NULL);
GO

SELECT * FROM Publisher;
GO

SELECT * FROM Category;
GO

SELECT * FROM Author;
GO

SELECT * FROM Book;
GO

SELECT * FROM BookCategory;
GO

SELECT * FROM BookAuthor;
GO

SELECT * FROM Inventory;
GO

SELECT * FROM Customer;
GO

SELECT * FROM Employee;
GO

SELECT * FROM Account;
GO

/* =========================
   INSERT SHOPPING CART
   ========================= */

INSERT INTO ShoppingCart (createdDate, customerID)
VALUES
('2026-07-01T09:00:00', 1),
('2026-07-02T10:30:00', 2),
('2026-07-03T14:15:00', 3),
('2026-07-04T16:20:00', 4),
('2026-07-05T08:45:00', 5);
GO


/* =========================
   INSERT CART ITEM
   Giả sử cartID được tạo lần lượt từ 1 đến 5
   ========================= */

INSERT INTO CartItem
(cartID, bookID, quantity, unitPrice, subtotal)
VALUES
(1, 1, 1, 420000, 420000),
(1, 3, 2, 280000, 560000),

(2, 4, 1, 310000, 310000),
(2, 8, 1, 390000, 390000),
(2, 13, 1, 360000, 360000),

(3, 10, 2, 120000, 240000),
(3, 11, 1, 135000, 135000),

(4, 14, 1, 520000, 520000),
(4, 15, 1, 480000, 480000),

(5, 5, 1, 250000, 250000),
(5, 9, 1, 340000, 340000),
(5, 12, 1, 240000, 240000);
GO


/* =========================
   INSERT ORDERS
   ========================= */

INSERT INTO Orders
(orderDate, totalAmount, shippingAddress, status, customerID)
VALUES
('2026-07-01T10:00:00', 700000, N'12 Nguyễn Trãi, Hà Nội', 'Paid', 1),
('2026-07-03T11:30:00', 640000, N'45 Lê Lợi, TP.HCM', 'Paid', 2),
('2026-07-05T14:00:00', 375000, N'18 Trần Phú, Đà Nẵng', 'Paid', 3),
('2026-07-08T09:20:00', 1000000, N'90 Hai Bà Trưng, Hà Nội', 'Paid', 4),
('2026-07-10T15:10:00', 590000, N'31 Võ Văn Tần, TP.HCM', 'Pending', 5),
('2026-07-12T16:40:00', 550000, N'72 Điện Biên Phủ, Huế', 'Paid', 6),
('2026-07-15T08:35:00', 720000, N'26 Nguyễn Huệ, Cần Thơ', 'Paid', 7),
('2026-07-18T13:25:00', 480000, N'15 Lý Thường Kiệt, Hải Phòng', 'Cancelled', 8),
('2026-07-21T17:15:00', 600000, N'39 Pasteur, TP.HCM', 'Paid', 9),
('2026-07-25T19:00:00', 730000, N'22 Cầu Giấy, Hà Nội', 'Pending', 10);
GO


/* =========================
   INSERT ORDER DETAIL
   Giả sử orderID được tạo lần lượt từ 1 đến 10
   ========================= */

INSERT INTO OrderDetail
(orderID, bookID, quantity, unitPrice, subtotal)
VALUES
(1, 1, 1, 420000, 420000),
(1, 3, 1, 280000, 280000),

(2, 4, 1, 310000, 310000),
(2, 18, 1, 330000, 330000),

(3, 10, 2, 120000, 240000),
(3, 11, 1, 135000, 135000),

(4, 14, 1, 520000, 520000),
(4, 15, 1, 480000, 480000),

(5, 5, 1, 250000, 250000),
(5, 9, 1, 340000, 340000),

(6, 7, 1, 220000, 220000),
(6, 18, 1, 330000, 330000),

(7, 2, 1, 460000, 460000),
(7, 6, 1, 260000, 260000),

(8, 15, 1, 480000, 480000),

(9, 12, 1, 240000, 240000),
(9, 13, 1, 360000, 360000),

(10, 8, 1, 390000, 390000),
(10, 16, 1, 230000, 230000),
(10, 19, 1, 110000, 110000);
GO


/* =========================
   INSERT PAYMENT
   ========================= */

INSERT INTO Payment
(paymentDate, amount, paymentMethod, status, orderID)
VALUES
('2026-07-01T10:05:00', 700000, 'Credit Card', 'Paid', 1),
('2026-07-03T11:35:00', 640000, 'Bank Transfer', 'Paid', 2),
('2026-07-05T14:05:00', 375000, 'E-Wallet', 'Paid', 3),
('2026-07-08T09:25:00', 1000000, 'Credit Card', 'Paid', 4),
('2026-07-10T15:15:00', 590000, 'Cash on Delivery', 'Pending', 5),
('2026-07-12T16:45:00', 550000, 'E-Wallet', 'Paid', 6),
('2026-07-15T08:40:00', 720000, 'Bank Transfer', 'Paid', 7),
('2026-07-18T13:30:00', 480000, 'Credit Card', 'Refunded', 8),
('2026-07-21T17:20:00', 600000, 'E-Wallet', 'Paid', 9),
('2026-07-25T19:05:00', 730000, 'Cash on Delivery', 'Pending', 10);
GO


/* =========================
   INSERT INVOICE
   ========================= */

INSERT INTO Invoice
(invoiceDate, totalAmount, paymentMethod, orderID, employeeID)
VALUES
('2026-07-01T10:10:00', 700000, 'Credit Card', 1, 2),
('2026-07-03T11:40:00', 640000, 'Bank Transfer', 2, 2),
('2026-07-05T14:10:00', 375000, 'E-Wallet', 3, 3),
('2026-07-08T09:30:00', 1000000, 'Credit Card', 4, 2),
('2026-07-10T15:20:00', 590000, 'Cash on Delivery', 5, 3),
('2026-07-12T16:50:00', 550000, 'E-Wallet', 6, 2),
('2026-07-15T08:45:00', 720000, 'Bank Transfer', 7, 3),
('2026-07-18T13:35:00', 480000, 'Credit Card', 8, 2),
('2026-07-21T17:25:00', 600000, 'E-Wallet', 9, 3),
('2026-07-25T19:10:00', 730000, 'Cash on Delivery', 10, 2);
GO

SELECT * FROM ShoppingCart;
SELECT * FROM CartItem;
SELECT * FROM Orders;
SELECT * FROM OrderDetail;
SELECT * FROM Payment;
SELECT * FROM Invoice;
GO

SELECT b.title, p.publisherName
FROM Book b
JOIN Publisher p ON b.publisherID = p.publisherID;

SELECT o.orderID, c.fullName
FROM Orders o
JOIN Customer c
ON o.customerID = c.customerID;

SELECT i.invoiceID, e.fullName
FROM Invoice i
JOIN Employee e
ON i.employeeID = e.employeeID;

