# RULE – Yêu cầu bắt buộc của đề tài

> Đây là các nội dung **bắt buộc** của đồ án. Mọi tài liệu khác (`proposal.md`, `docs/*`) phải đáp ứng file này. Mỗi yêu cầu có mã (ví dụ `FR-1.3`) để đối chiếu.

---

## 1. Yêu cầu chức năng (Functional Requirements)

Hệ thống bắt buộc phải hoàn thành các nhóm tính năng cốt lõi sau.

### FR-1. Quản lý người dùng toàn diện (Comprehensive User Management)

| Mã | Yêu cầu |
|---|---|
| FR-1.1 | Đăng ký tài khoản với **xác thực nhiều bước**. |
| FR-1.2 | Đăng nhập/Đăng xuất bảo mật (dùng session hoặc JWT token). |
| FR-1.3 | Phân chia **ít nhất 3 vai trò** (roles) khác nhau với quyền hạn riêng biệt (ví dụ: Admin, Manager, Staff, Customer, Guest). |
| FR-1.4 | **Phân quyền dựa trên vai trò (RBAC)** để giới hạn tính năng và dữ liệu tương ứng. |
| FR-1.5 | Quản lý hồ sơ cá nhân: xem, chỉnh sửa thông tin, đổi mật khẩu. |
| FR-1.6 | Trang quản trị (**Admin Dashboard**) để quản lý người dùng: tạo mới, vô hiệu hóa, thay đổi vai trò. |

### FR-2. Thao tác CRUD phức tạp (Complex CRUD Operations)

| Mã | Yêu cầu |
|---|---|
| FR-2.1 | Đầy đủ Tạo, Đọc, Cập nhật, Xóa (CRUD) cho **ít nhất 4 thực thể chính**. |
| FR-2.2 | Mỗi thực thể có tối thiểu **6–8 thuộc tính** có ý nghĩa. |
| FR-2.3 | Kiểm tra dữ liệu hợp lệ (Validation) ở **cả phía client và server**. |
| FR-2.4 | Các thao tác phụ thuộc/ràng buộc (Cascading operations), ví dụ xóa một khách hàng sẽ xóa các đơn hàng liên quan. |
| FR-2.5 | Tùy chọn **xóa mềm (Soft delete)** cho các thực thể quan trọng (đánh dấu không hoạt động thay vì xóa vĩnh viễn khỏi DB). |
| FR-2.6 | **Nhật ký kiểm toán (Audit trail)**: ghi nhận ai tạo/sửa bản ghi và vào thời gian nào. |
| FR-2.7 | Xử lý lỗi với thông báo thân thiện cho người dùng và cung cấp **tùy chọn khôi phục**. |

### FR-3. Tìm kiếm và Lọc nâng cao (Advanced Search and Filtering)

| Mã | Yêu cầu |
|---|---|
| FR-3.1 | Tìm kiếm **đa tiêu chí** trên nhiều trường dữ liệu. |
| FR-3.2 | Bộ lọc nâng cao với **ít nhất 5 tùy chọn lọc cho mỗi thực thể**. |
| FR-3.3 | Sắp xếp kết quả theo **nhiều cột**. |
| FR-3.4 | Phân trang với **kích thước trang cấu hình được** (ví dụ 10, 20, 50, 100 mục). |
| FR-3.5 | Hiển thị thống kê kết quả tìm kiếm (ví dụ: "Đang hiển thị 21–40 trong tổng số 156 kết quả"). |

### FR-4. Mối quan hệ dữ liệu phức tạp (Complex Data Relationships)

| Mã | Yêu cầu |
|---|---|
| FR-4.1 | Mô hình miền (domain model) có **ít nhất 6–8 thực thể** liên quan với nhau. |
| FR-4.2 | Thể hiện **ít nhất 2 quan hệ một-nhiều** (one-to-many). |
| FR-4.3 | Thể hiện **ít nhất 1 quan hệ nhiều-nhiều** (many-to-many) kèm **bảng trung gian** (junction table). |
| FR-4.4 | Đảm bảo toàn vẹn tham chiếu bằng **ràng buộc khóa ngoại** (foreign key). |
| FR-4.5 | Chuẩn hóa cơ sở dữ liệu tối thiểu ở mức **3NF**. |

### FR-5. Logic nghiệp vụ đáng kể (Substantial Business Logic)

| Mã | Yêu cầu |
|---|---|
| FR-5.1 | Các quy tắc nghiệp vụ phức tạp đặc thù theo đề tài (ví dụ: kho tự động đặt hàng khi dưới ngưỡng, hệ thống đặt chỗ kiểm tra xung đột thời gian, tính toán tài chính, quản lý trạng thái luồng công việc…). |
| FR-5.2 | Kiểm tra tính hợp lệ nâng cao: quy tắc nghiệp vụ, kiểm tra chéo giữa các trường, trạng thái cơ sở dữ liệu. |

### FR-6. Tính năng nâng cao (Advanced Features)

| Mã | Yêu cầu |
|---|---|
| FR-6.1 | Chọn **ít nhất 3 tính năng nâng cao** từ các nhóm sau. |

Các nhóm cho phép:
1. Quản lý file / Upload.
2. Giao tiếp và Thông báo.
3. Phân tích và Báo cáo kèm biểu đồ.
4. Tính năng thời gian thực / WebSocket.
5. Tích hợp API bên thứ ba / Thanh toán / Bản đồ.
6. Tìm kiếm nâng cao / AI.
7. Lên lịch tự động / Cron jobs.

---

## 2. Yêu cầu kỹ thuật (Technical Requirements)

### A. Frontend

| Mã | Yêu cầu |
|---|---|
| TR-A1 | **Thiết kế đáp ứng (Responsive):** hoạt động mượt trên desktop (1920x1080), tablet (768x1024), mobile (375x667); **kiểm thử trên ít nhất 3 kích thước**; có **menu hamburger** trên di động; nút bấm và form thân thiện với cảm ứng. |
| TR-A2 | **Giao diện chuyên nghiệp:** ngôn ngữ thiết kế nhất quán, phân cấp trực quan, **trạng thái chờ (loading states)** cho tác vụ bất đồng bộ, **màn hình trống (empty states)** kèm hướng dẫn, **hộp thoại xác nhận** cho hành động mang tính hủy diệt. |
| TR-A3 | **Xác thực và phản hồi form:** lỗi hiển thị tức thì ở client, thông báo lỗi rõ ràng, **kiểm tra dòng (inline validation)** khi nhập, **ngăn gửi form nhiều lần**. |

### B. Backend và API

| Mã | Yêu cầu |
|---|---|
| TR-B1 | **RESTful API chuẩn:** đúng phương thức HTTP (GET, POST, PUT/PATCH, DELETE); mã trạng thái chính xác (200, 201, 400, 401, 403, 404, 500); URL nhất quán (`/api/resources/{id}`); dữ liệu JSON; chiến lược phiên bản (`/api/v1/...`). |
| TR-B2 | **Xác thực và làm sạch đầu vào:** kiểm tra dữ liệu ở server cho **mọi đầu vào**; làm sạch dữ liệu chống **XSS**; kiểm tra kỹ file tải lên (định dạng, kích thước). |
| TR-B3 | **Xử lý lỗi toàn diện:** khối try-catch, phản hồi lỗi có cấu trúc, ghi log lỗi đầy đủ phục vụ gỡ lỗi. |

**Biện pháp bảo mật bắt buộc (Non-negotiable)**

| Mã | Yêu cầu |
|---|---|
| TR-B4 | **Mật khẩu:** băm bằng **BCrypt hoặc Argon2**; tuyệt đối không lưu plain text hay MD5. |
| TR-B5 | **SQL Injection:** phòng chống bằng truy vấn có tham số hoặc ORM. |
| TR-B6 | **XSS và CSRF:** thoát dữ liệu đầu ra, cấu hình **Content Security Policy**, dùng **token CSRF** cho các thao tác thay đổi trạng thái. |
| TR-B7 | **Xác thực và phân quyền:** session an toàn hoặc **JWT có refresh token**; luôn kiểm tra quyền trước mọi thao tác nhạy cảm. |
| TR-B8 | **Khác:** dùng **HTTPS** trong môi trường triển khai thực tế; **giới hạn tốc độ (rate limiting)** chống brute-force trang đăng nhập. |

### C. Cơ sở dữ liệu (Database)

| Mã | Yêu cầu |
|---|---|
| TR-C1 | **Cấu trúc lược đồ:** tối thiểu **6–8 bảng** (không tính bảng trung gian thuần túy); **ít nhất 2 bảng trung gian** cho quan hệ nhiều-nhiều; tổng số cột toàn schema **từ 50 trở lên**. |
| TR-C2 | **Toàn vẹn dữ liệu:** khóa chính chuẩn (surrogate key: UUID hoặc auto-increment); **khóa ngoại cấu hình ở cấp cơ sở dữ liệu**; ràng buộc **NOT NULL, UNIQUE, CHECK**. |
| TR-C3 | **Chuẩn hóa và chỉ mục:** tối thiểu 3NF; **đánh chỉ mục cho tất cả khóa ngoại** và các cột thường truy vấn. |
| TR-C4 | **Dữ liệu mẫu:** **100+ bản ghi** trong các bảng chính, dữ liệu **thực tế, hợp lý** (không dùng "Test 1", "Test 2"); kèm **script khởi tạo tự động**. |

---

## 3. Quản lý phiên bản và tài liệu bàn giao (Version Control & Deliverables)

### 3.1. Git repository

| Mã | Yêu cầu |
|---|---|
| VC-1 | Tệp `.gitignore` chuẩn xác: không đẩy file rác IDE, build artifacts, **thông tin mật**. |
| VC-2 | Tệp `README.md` mô tả toàn diện, **hướng dẫn cài đặt và chạy từng bước**. |

### 3.2. Thực hành commit

| Mã | Yêu cầu |
|---|---|
| VC-3 | **Tối thiểu 50+ commit** cho toàn dự án, không dồn vào vài commit lớn. |
| VC-4 | Thông điệp commit rõ ràng theo quy chuẩn, ví dụ `feat(auth): add password reset`. |
| VC-5 | Commit **nguyên tử** (atomic); **mỗi thành viên tối thiểu 20+ commit**. |

### 3.3. Chiến lược nhánh

| Mã | Yêu cầu |
|---|---|
| VC-6 | Nhánh `main` **được bảo vệ** và luôn sẵn sàng triển khai. |
| VC-7 | Dùng nhánh riêng cho tính năng (`feature/`) hoặc sửa lỗi (`bugfix/`). |
| VC-8 | Gộp mã bằng **Pull Request có review code**. |

### 3.4. Tài liệu bàn giao bắt buộc

| Mã | Yêu cầu |
|---|---|
| DL-1 | Mã nguồn hoàn chỉnh **kèm script khởi tạo database và dữ liệu mẫu**. |
| DL-2 | **Sơ đồ thực thể kết hợp (ER Diagram)**. |
| DL-3 | **Tài liệu API**: Postman Collection hoặc Swagger/OpenAPI spec. |
| DL-4 | **Báo cáo kỹ thuật chi tiết, 25–30 trang, định dạng PDF**, tuân thủ đúng cấu trúc yêu cầu. |
| DL-5 | Ứng dụng **triển khai trực tuyến (Deployed)** có **URL công khai** truy cập được từ bất kỳ đâu (không chỉ localhost), kèm **tài khoản đăng nhập mẫu cho từng phân quyền**. |

---

## 4. Điểm cần bổ sung thông tin

- **Cấu trúc báo cáo kỹ thuật (DL-4):** đề bài nói "tuân thủ đúng cấu trúc yêu cầu" nhưng chưa có mục lục/cấu trúc cụ thể. Cần nhận cấu trúc này từ giảng viên/đề bài trước khi viết báo cáo.
