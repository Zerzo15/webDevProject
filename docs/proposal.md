# PROPOSAL – HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ THỜI TRANG WELO

| Mục | Nội dung |
|---|---|
| Phiên bản | 0.4 – nhất quán với [`rule.md`](rule.md); ảnh sản phẩm lưu trên bucket; có ma trận tuân thủ từng mã |
| Ngày | 19/11/2026 |
| Vai trò người viết | Trưởng nhóm kỹ sư phần mềm |
| Thời gian | **4 tuần**: **MVP trong 2 tuần đầu** (đáp ứng toàn bộ yêu cầu chức năng và kỹ thuật của `rule.md`, đã triển khai trực tuyến) + **2 tuần sau** (việc bắt buộc còn lại, rồi tính năng nâng cao theo thứ tự ưu tiên; không kịp thì bỏ) |
| Nhóm | 3 thành viên: Dev 1 (Quang), Dev 2 (Nguyên), Dev 3 (Kiệt) |
| Nền tảng | Web responsive (PWA). Không có Mobile App |
| Mô hình kinh doanh | Một shop duy nhất (WELO), không phải sàn đa người bán |
| Kế hoạch chi tiết 2 tuần đầu | [`docs/mvp-daily-plan.md`](docs/mvp-daily-plan.md) |

**Nhãn:** **[MVP]** xong trong 2 tuần đầu · **[BB]** bắt buộc, làm ở tuần 3–4, **không được cắt** · **[NC-n]** hoàn thiện/nâng cao, n là thứ tự ưu tiên, hết thời gian thì bỏ · **[BỎ]** không làm trong 4 tuần này.

**Giả định thời gian:** 1 tuần = 5 ngày làm việc; 2 tuần = 10 ngày công mỗi người, 30 ngày công cả nhóm cho MVP. Ngày 1 là thứ Hai (xem `docs/mvp-daily-plan.md`).

> **Cảnh báo về khả năng hoàn thành:** `rule.md` được chọn đưa **toàn bộ vào MVP 2 tuần**. Ước lượng khối lượng khớp đúng 30 ngày công, **gần như không có dư** (mục 8.5, 9). Rủi ro trễ rất cao; kế hoạch có điểm kiểm tra ngày 5 và thứ tự cắt (mục 8.4). Các con số ngày công là **ước lượng**, nhóm xác nhận lại ở ngày 1.

---

## 1. Tóm tắt tổng quan

WELO là website thương mại điện tử của một shop thời trang. Ngoài chức năng bán hàng chuẩn (xem sản phẩm, giỏ hàng, đặt hàng COD, theo dõi đơn), hệ thống có điểm khác biệt là **gợi ý trang phục bằng AI** theo ngữ cảnh (đi biển, leo núi, đi họp, phỏng vấn) và số đo cơ thể, chỉ chọn sản phẩm còn hàng. Phần AI **không thuộc `rule.md`** nên xếp vào tuần 3–4 (NC-3).

Mục tiêu 2 tuần đầu: một hệ thống bán hàng hoàn chỉnh, **đáp ứng mọi mục bắt buộc của `rule.md`** (đối chiếu từng mã ở mục 2) và **chạy trên URL công khai HTTPS**.

Hiện trạng mã nguồn: backend Spring Boot / Java 21 / MySQL đã có khung xác thực JWT và một phần entity; nhiều phần bắt buộc (OTP, refresh token, CSRF, controller catalog/commerce, migration Flyway, triển khai) chưa có. Frontend hiện là HTML/CSS tĩnh "UNIKURO", sẽ được thay bằng React + Tailwind mang tên WELO. Hiện trạng chi tiết xem [`codebase.md`](codebase.md).

---

## 2. Ma trận tuân thủ `rule.md`

Mỗi mã trong `rule.md` có đúng một dòng: cách đáp ứng, nơi mô tả trong proposal, và người sở hữu. Cuối ngày 10 và ở BB-1 (mục 8.5), nhóm tích từng dòng bằng bằng chứng chạy được. Mọi dòng **[MVP]** trừ khi ghi khác.

### 2.1. Chức năng (FR)

| Mã | Cách đáp ứng | Mục | Sở hữu |
|---|---|---|---|
| FR-1.1 | Đăng ký → OTP 6 số gửi email → nhập OTP → kích hoạt tài khoản | 4.2 | Dev 1 |
| FR-1.2 | JWT access ngắn + refresh token xoay vòng; đăng xuất thu hồi refresh token | 4.2 | Dev 1 |
| FR-1.3 | 3 vai trò lưu trong DB: `ADMIN`, `STAFF`, `CUSTOMER` (+ Guest không lưu) | 4.1 | Dev 1 |
| FR-1.4 | `@PreAuthorize` + `SecurityFilterChain` mặc định từ chối; giao diện ẩn nút theo vai trò | 4.2, 4.3 | Dev 1 (API), Dev 2/3 (UI) |
| FR-1.5 | Xem/sửa hồ sơ, đổi mật khẩu (thu hồi phiên khác) | 5.1 | Dev 1 |
| FR-1.6 | Admin Dashboard quản lý người dùng: tạo, vô hiệu hóa, đổi vai trò | 5.2 | Dev 1 |
| FR-2.1 | CRUD đầy đủ 4 thực thể: `Category`, `Product` (+ biến thể, thẻ, ảnh), `User`, `Voucher` | 5.2 | Dev 2 (Category, Product), Dev 1 (User), Dev 3 (Voucher) |
| FR-2.2 | Mỗi thực thể CRUD có 6–11 thuộc tính có ý nghĩa | 7.1 | Dev 1/2/3 |
| FR-2.3 | Validation ở client (inline) và server (Bean Validation); cùng bộ quy tắc | 3.3 | Dev 2 (client), Dev 1 (khung server) |
| FR-2.4 | `ON DELETE CASCADE` ở DB cho khóa ngoại bảng con; xóa người dùng xóa đơn hàng của họ | 7.1 | Dev 1 |
| FR-2.5 | Xóa mềm (`deleted_at`) mặc định; xóa vĩnh viễn chỉ Admin, sau khi vào thùng rác | 6.5, 7.1 | Dev 1 (khung) |
| FR-2.6 | JPA Auditing: `created_by/at`, `updated_by/at`, `deleted_by/at`; `order_status_history` cho đơn hàng | 7.1 | Dev 1 |
| FR-2.7 | Lỗi có cấu trúc, thông báo thân thiện; **thùng rác + nút "Hoàn tác" sau khi xóa** để khôi phục | 3.3, 5.2 | Dev 1/2 |
| FR-3.1 | Tìm đa trường (tên, mô tả, thương hiệu, thẻ…) qua `Specification` | 3.3, 5.1 | Dev 2 |
| FR-3.2 | ≥ 5 bộ lọc cho mỗi thực thể (bảng ở 5.2) | 5.2 | Dev 1/2/3 |
| FR-3.3 | Sắp xếp nhiều cột | 3.3 | Dev 1 (khung) |
| FR-3.4 | Cỡ trang 10/20/50/100 | 3.3 | Dev 1 (khung), Dev 2 (UI) |
| FR-3.5 | Dòng thống kê "Đang hiển thị x–y trong z" (`PageResponse.from/to/totalElements`) | 3.3 | Dev 1/2 |
| FR-4.1 | ≥ 12 thực thể liên quan | 7.1 | Dev 1 |
| FR-4.2 | ≥ 2 quan hệ một-nhiều (`users → orders`, `categories → products`, `products → product_variants`…) | 7.1 | Dev 1 |
| FR-4.3 | Nhiều-nhiều có bảng trung gian: `product_tags`, `cart_items`, `order_items` | 7.1 | Dev 1/2 |
| FR-4.4 | Khóa ngoại cấu hình ở DB (Flyway) | 7.1 | Dev 1 |
| FR-4.5 | 3NF; ngoại lệ có chủ ý ghi rõ (giá/địa chỉ chốt lúc đặt) | 7.1 | Dev 1 |
| FR-5.1 | Máy trạng thái đơn, trừ kho nguyên tử, voucher 3 loại, tính tiền `BigDecimal` | 6 | Dev 3 |
| FR-5.2 | Kiểm tra theo trạng thái DB, kiểm tra chéo trường, `CHECK` ở DB | 6.4 | Dev 3 |
| FR-6.1 | 3 tính năng nâng cao: Upload ảnh (bucket), Thông báo email, Lập lịch cron | 5.3 | Dev 2, Dev 1 |

### 2.2. Kỹ thuật (TR)

| Mã | Cách đáp ứng | Mục | Sở hữu |
|---|---|---|---|
| TR-A1 | Responsive; kiểm thử 1920×1080, 768×1024, 375×667; menu hamburger trên di động; nút/ form đủ lớn cho cảm ứng | 5 | Dev 2 |
| TR-A2 | Hệ thống thành phần dùng chung: loading, empty state kèm hướng dẫn, `ConfirmDialog` cho hành động hủy diệt | 3.3 | Dev 2 |
| TR-A3 | Inline validation, thông báo lỗi rõ, khóa nút khi đang gửi (chống gửi trùng); đặt hàng có `Idempotency-Key` | 3.3, 5.1 | Dev 2/3 |
| TR-B1 | REST chuẩn, JSON, `/api/v1/...`, mã trạng thái đúng; tài liệu sinh bằng springdoc | 3.3 | Dev 1 (khung) |
| TR-B2 | Validate mọi đầu vào ở server; làm sạch XSS; upload kiểm tra kích thước + nội dung thật của tệp | 3.3 | Dev 1/2 |
| TR-B3 | `GlobalExceptionHandler`, lỗi có cấu trúc (ProblemDetails), log kèm `requestId` | 3.3 | Dev 1 |
| TR-B4 | BCrypt cost 12; không plain text/MD5 | 4.2 | Dev 1 |
| TR-B5 | Chỉ dùng JPA/`Specification`/truy vấn tham số, không nối chuỗi SQL | 3.3 | Cả nhóm |
| TR-B6 | CSRF token cho mọi `POST/PUT/PATCH/DELETE`; CSP; thoát đầu ra (React mặc định) | 3.3 | Dev 1 |
| TR-B7 | JWT + refresh token xoay vòng; kiểm tra quyền trước mọi thao tác nhạy cảm | 4.2 | Dev 1 |
| TR-B8 | HTTPS do Cloud Run cấp; rate limit đăng nhập (khóa tạm theo email trong DB + giới hạn theo IP) | 3.3, 4.2 | Dev 1 |
| TR-C1 | ≥ 12 bảng chính, ≥ 3 bảng trung gian, > 100 cột | 7.1 | Dev 1 |
| TR-C2 | PK tự tăng, FK ở DB, `NOT NULL`, `UNIQUE`, `CHECK` | 7.1 | Dev 1 |
| TR-C3 | 3NF; chỉ mục cho mọi FK và cột lọc, không trùng với `UNIQUE` | 7.1 | Dev 1 |
| TR-C4 | > 300 bản ghi tiếng Việt thực tế, script tự chạy ở hồ sơ `demo`, kèm ảnh mẫu trên bucket | 7.2 | Dev 2, Dev 3 |

### 2.3. Phiên bản và bàn giao (VC, DL)

| Mã | Cách đáp ứng | Mục | Sở hữu |
|---|---|---|---|
| VC-1 | `.gitignore` đủ (IDE, `target/`, `node_modules/`, `.env`, `uploads/`, log); không commit secret | 8.1 | Dev 1 |
| VC-2 | `README.md` cài đặt và chạy từng bước | 8.1 | Dev 1 |
| VC-3 | ≥ 50 commit toàn dự án | 8.1 | Cả nhóm |
| VC-4 | Conventional Commits `type(scope): mô tả` | 8.1 | Cả nhóm |
| VC-5 | Commit nguyên tử; mỗi người ≥ 20 commit | 8.1 | Cả nhóm |
| VC-6 | Bảo vệ nhánh `main` (cần chủ repo bật) | 8.1, 9 | Dev 1 + chủ repo |
| VC-7 | Nhánh `feature/<id>-<mo-ta>`, `bugfix/<id>-<mo-ta>` | 8.1 | Cả nhóm |
| VC-8 | Gộp bằng Pull Request, ít nhất một người khác review | 8.1 | Cả nhóm |
| DL-1 | Mã nguồn + migration Flyway + seed `demo` | 7.2 | Dev 1/2/3 |
| DL-2 | Sơ đồ ER (Mermaid, xuất hình) | 7.1 | Dev 1 |
| DL-3 | `openapi.json` sinh từ springdoc + Postman Collection xuất ra | 3.3 | Dev 1 |
| DL-4 | Báo cáo kỹ thuật PDF 25–30 trang, **[BB-2]**, chờ cấu trúc từ đề bài | 8.5 | Cả nhóm |
| DL-5 | Triển khai Cloud Run, URL công khai HTTPS, tài khoản mẫu cho Admin/Staff/Customer | 3.4 | Dev 1 |

---

## 3. Kiến trúc và công nghệ

### 3.1. Đã chốt

| Lớp | Lựa chọn |
|---|---|
| Backend | Java 21, Spring Boot, Spring Security, Spring Data JPA, Bean Validation, springdoc-openapi |
| Frontend | **React + TypeScript + Vite + Tailwind CSS** (SPA/PWA) |
| Cơ sở dữ liệu | MySQL 8 (≥ 8.0.16 để ràng buộc `CHECK` có hiệu lực) |
| Lưu ảnh | **Google Cloud Storage (bucket)**; DB chỉ lưu khóa đối tượng và URL |
| Xác thực | JWT (access ngắn) + refresh token xoay vòng; mật khẩu BCrypt |
| Di trú DB | Flyway; `ddl-auto=validate` |
| Lập lịch | Cloud Scheduler gọi endpoint nội bộ (triển khai); `@Scheduled` khi chạy local |
| AI (tuần 3–4) | Claude (`claude-sonnet-5-5`) qua interface `LlmClient` |

### 3.2. Kiến trúc: modular monolith, **một khối triển khai**

Backend chia module theo package (identity, catalog, commerce, inventory, notification, storage). **Frontend được build thành tệp tĩnh và do chính backend phục vụ**, nên SPA và API cùng một địa chỉ (cùng origin). Lợi ích: không phải cấu hình CORS, cookie refresh/CSRF hoạt động đơn giản, chỉ một thứ để triển khai. Ảnh sản phẩm được trình duyệt tải thẳng từ bucket (URL công khai chỉ đọc); mọi thao tác **ghi/xóa** ảnh đi qua backend.

```
 Trình duyệt ──HTTPS──▶ Cloud Run (1 container: Spring Boot + SPA tĩnh)
      │                         │
      │                         ├── Cloud SQL (MySQL 8)
      │                         ├── Bucket (ghi/xóa ảnh qua service account)
      │                         ├── SMTP (email OTP/thông báo), Claude API (tuần 3–4)
      │                         ▲
      │                Cloud Scheduler ──▶ POST /internal/jobs/cleanup (có token)
      └──(đọc ảnh)──▶ Bucket (công khai chỉ đọc)
 Phát triển: Vite dev server (http://localhost:5173, proxy /api) + Spring Boot (http://localhost:8080) + MySQL local + LocalStorageService (./uploads)
```

### 3.3. Quyết định kỹ thuật kèm lý do

| Hạng mục | Chọn | Lý do |
|---|---|---|
| Bộ khung CRUD dùng chung | Backend: lớp nền `Auditable` (audit + xóa mềm), `PageResponse` có `from/to/totalElements`, bộ dựng `Specification` cho lọc và sắp xếp nhiều cột. Frontend: `DataTable` có bộ lọc, cỡ trang, thống kê, thùng rác; `ConfirmDialog`; bộ form có kiểm tra dòng | `rule.md` đòi bộ lọc, phân trang, xóa mềm, audit cho **mọi** thực thể: dựng một lần rồi dùng lại là cách duy nhất cho vừa thời gian |
| Lưu ảnh | **Bucket**, qua interface `StorageService` với hai bản cài: `GcsStorageService` (triển khai) và `LocalStorageService` (ghi `./uploads`, gitignore; dùng khi dev và test), chọn bằng `storage.provider` | Ổ đĩa Cloud Run là tạm thời nên không lưu tệp trên đĩa; không phình DB; ảnh được cache. Bản local giữ cho máy không có GCP/Docker không bị chặn |
| Luồng upload ảnh | Client gửi `multipart` tới backend → kiểm tra **≤ 2 MB, định dạng và nội dung thật (magic bytes) là jpeg/png/webp** → đặt tên object bằng UUID → ghi lên bucket → lưu `object_key`, `url`, `content_type`, `size_bytes`, `sort_order`, `is_primary` | `TR-B2`: kiểm tra kỹ file tải lên; tên ngẫu nhiên tránh đè tệp và đoán đường dẫn |
| Quyền truy cập bucket | Uniform access; `allUsers` chỉ đọc object; chỉ service account của Cloud Run được ghi/xóa (dùng ADC, **không có tệp khóa**); `Cache-Control` dài cho ảnh tên UUID | Ảnh sản phẩm không nhạy cảm; URL cố định, cache được, ít code nhất |
| Chống bán lố | `UPDATE … SET stock = stock - ? WHERE stock >= ?` + `@Version` | Không cần Redis ở một bản chạy |
| Giới hạn tần suất | Khóa tạm theo email lưu trong DB (`failed_login_count`, `locked_until`); giới hạn theo IP trong bộ nhớ (Bucket4j) | Khóa theo email bền qua các lần khởi động; Cloud Run đặt `max-instances=1` để bộ đếm IP nhất quán |
| Lập lịch | Endpoint nội bộ `POST /internal/jobs/cleanup` do **Cloud Scheduler** gọi, bảo vệ bằng token trong Secret Manager (so sánh hằng thời gian), loại khỏi CSRF; việc: dọn OTP và refresh token hết hạn, giỏ hàng bỏ quên quá 30 ngày, ảnh mồ côi trên bucket. Local dùng `@Scheduled` | Cho phép Cloud Run scale về 0 (không cần `min-instances=1`, rẻ hơn). Chọn việc dọn dẹp để không đụng quy tắc nghiệp vụ của đơn hàng |
| CSRF | Bật CSRF của Spring Security với cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN` cho mọi `POST/PUT/PATCH/DELETE` | Đáp ứng `TR-B6` đúng nghĩa đen, kể cả khi dùng JWT |
| Làm sạch XSS | Làm sạch các trường văn bản tự do ở server (mô tả, ghi chú), React thoát đầu ra mặc định, header CSP | `TR-B2`, `TR-B6` |
| Xử lý lỗi | `GlobalExceptionHandler`; phản hồi lỗi có cấu trúc (ProblemDetails: mã, thông báo thân thiện, lỗi theo trường); log kèm `requestId`, không lộ stack trace ra client | `TR-B3`, `FR-2.7` |
| Tài liệu API | springdoc-openapi sinh `openapi.json` từ code, commit bản xuất vào `docs/api/`; Postman Collection xuất từ đó | Rẻ hơn viết tay `openapi.yaml` và không lệch khỏi code (`DL-3`) |
| Email | SMTP qua biến môi trường (chọn nhà cung cấp ở ngày 5); ở local ghi OTP ra console nếu chưa cấu hình | Triển khai thật cần email thật cho OTP |
| Kiểm thử | JUnit 5; DB test riêng; Testcontainers chỉ khi máy có Docker (một số máy chưa có) | |

### 3.4. Triển khai (cuối tuần 2)

Theo cấu hình hạ tầng ban đầu của nhóm: **Google Cloud Run + Cloud SQL (MySQL) + Cloud Storage**, HTTPS do Cloud Run cấp (địa chỉ `*.run.app`).

- Build từ mã nguồn bằng Cloud Build (không cần Docker ở máy cá nhân); `Dockerfile` đa giai đoạn vẫn có để chạy ở nơi khác.
- Cấu hình bí mật (mật khẩu DB, khóa JWT, SMTP, token của job) bằng biến môi trường/Secret Manager, không vào repo. Truy cập bucket bằng service account gắn vào Cloud Run, không dùng tệp khóa.
- Flyway chạy khi khởi động; seed dữ liệu mẫu bật bằng hồ sơ `demo`.
- Cloud Scheduler tạo một job định kỳ (ví dụ mỗi ngày) gọi `POST /internal/jobs/cleanup`.
- **Chi phí:** Cloud SQL tính phí (cần tài khoản GCP có thanh toán); Cloud Run scale về 0 khi rảnh; bucket và Cloud Scheduler chỉ phát sinh chi phí nhỏ ở quy mô demo (cần kiểm tra bảng giá hiện hành). Nhóm cần có tài khoản thanh toán trước ngày 8. Nếu không có, cần phương án nền tảng khác (xem rủi ro).

---

## 4. Đăng nhập bảo mật và phân quyền

### 4.1. Vai trò (RBAC)

| Vai trò | Lưu trong DB | Quyền chính |
|---|:--:|---|
| **Guest** | – | Xem sản phẩm, tìm kiếm, đăng ký, đăng nhập |
| **CUSTOMER** | ✔ | Giỏ hàng, đặt hàng, xem/hủy đơn của mình, hồ sơ, đổi mật khẩu |
| **STAFF** | ✔ | Xem và xử lý đơn hàng (xác nhận, giao, hủy), điều chỉnh tồn kho, xem sản phẩm. **Không** quản lý người dùng, voucher, danh mục, xóa vĩnh viễn |
| **ADMIN** | ✔ | Mọi quyền: người dùng, danh mục, sản phẩm, voucher, đơn hàng, thùng rác, xóa vĩnh viễn |

### 4.2. Cơ chế

| Thành phần | Cách làm |
|---|---|
| Đăng ký nhiều bước | Điền form → nhận **OTP qua email** → nhập OTP → tài khoản kích hoạt (`email_verified`). OTP 6 số, hết hạn 5 phút, dùng một lần, **lưu dạng băm**, có giới hạn gửi lại và số lần nhập sai |
| Đăng nhập | Email + mật khẩu → access token JWT (~15 phút, giữ trong bộ nhớ, không lưu `localStorage`) + refresh token (cookie `HttpOnly`, `Secure`, `SameSite=Strict`, **xoay vòng**, lưu băm trong DB) |
| Đăng xuất | Thu hồi refresh token hiện tại, xóa cookie; access token hết hạn sớm nên không cần danh sách đen |
| Mật khẩu | BCrypt cost 12; quy tắc độ mạnh tối thiểu; đổi mật khẩu thu hồi các phiên khác |
| Chống brute-force | Khóa tạm theo email sau nhiều lần sai (lưu DB) và giới hạn theo IP; thông báo lỗi chung |
| Phân quyền | `@PreAuthorize` + cấu hình `SecurityFilterChain`; mặc định từ chối; lọc dữ liệu theo người dùng từ token; truy cập dữ liệu người khác trả `404` |
| Tài khoản bị vô hiệu | `LOCKED` hoặc đã xóa mềm không đăng nhập được, refresh token bị thu hồi |
| Header | CSP, `X-Content-Type-Options`, `Referrer-Policy`; HTTPS ở môi trường triển khai |
| Endpoint nội bộ | `/internal/jobs/*` chỉ nhận token của job, không dùng JWT người dùng |

### 4.3. Ma trận quyền

| Chức năng | Guest | Customer | Staff | Admin |
|---|:--:|:--:|:--:|:--:|
| Xem trang chủ, danh sách, chi tiết, tìm kiếm, lọc | ✔ | ✔ | ✔ | ✔ |
| Đăng ký, đăng nhập | ✔ | – | – | – |
| Hồ sơ cá nhân, đổi mật khẩu | – | ✔ | ✔ | ✔ |
| Giỏ hàng, đặt hàng, đơn của mình | – | ✔ | – | – |
| Xem/lọc/xử lý đơn của mọi khách | – | – | ✔ | ✔ |
| Điều chỉnh tồn kho | – | – | ✔ | ✔ |
| CRUD danh mục, sản phẩm, biến thể, thẻ, ảnh | – | – | – | ✔ |
| CRUD voucher | – | – | – | ✔ |
| Quản lý người dùng (tạo, vô hiệu hóa, đổi vai trò) | – | – | – | ✔ |
| Thùng rác: khôi phục, xóa vĩnh viễn | – | – | – | ✔ |

---

## 5. Module chức năng

Mọi giao diện đáp ứng `TR-A1` (3 kích thước, menu hamburger trên di động) và `TR-A2`/`TR-A3` (loading, empty state, xác nhận, inline validation, chống gửi trùng). Kiểm thử responsive ghi kết quả vào checklist ở `docs/mvp-daily-plan.md`.

### 5.1. Phân hệ Khách hàng [MVP]

- **Tài khoản:** đăng ký + OTP, đăng nhập/đăng xuất, xem/sửa hồ sơ (họ tên, điện thoại, ảnh đại diện tùy chọn), đổi mật khẩu.
- **Khám phá sản phẩm:** trang chủ; danh sách có **tìm kiếm đa trường** (tên, mô tả, thương hiệu, thẻ) và **≥ 5 bộ lọc** (danh mục, khoảng giá, size, màu, còn hàng, thẻ); sắp xếp nhiều cột; cỡ trang 10/20/50/100; dòng thống kê kết quả.
- **Chi tiết sản phẩm:** ảnh (tải từ bucket), mô tả, chọn màu/size, tồn kho, thẻ.
- **Giỏ hàng:** theo tài khoản, tự cập nhật giá và tồn kho, cảnh báo hết hàng.
- **Đặt hàng COD:** nhập địa chỉ giao hàng, nhập mã voucher, **phí ship cố định** (cấu hình một mức), chốt giá và địa chỉ vào đơn, trừ kho nguyên tử, chống gửi trùng bằng `Idempotency-Key`.
- **Đơn của tôi:** danh sách có lọc, chi tiết kèm lịch sử trạng thái, hủy khi `Chờ xác nhận`, bấm "Đã nhận hàng".

### 5.2. Phân hệ Quản trị (Staff/Admin) [MVP]

Mọi màn hình danh sách đều dùng `DataTable` chung (lọc ≥ 5, sắp xếp nhiều cột, phân trang, thống kê, thùng rác với khôi phục). Sau mỗi thao tác xóa mềm có thông báo kèm nút **Hoàn tác** trong vài giây (gọi lại thao tác khôi phục), đáp ứng `FR-2.7`.

| Màn hình | Thực thể | Vai trò | Bộ lọc (≥ 5) |
|---|---|---|---|
| Người dùng | `User` | Admin | từ khóa (email/tên/điện thoại), vai trò, trạng thái, đã xác thực email, khoảng ngày tạo |
| Danh mục | `Category` | Admin | từ khóa, danh mục cha, đang hoạt động, có sản phẩm hay không, khoảng ngày tạo |
| Sản phẩm | `Product` (+ biến thể, thẻ, ảnh) | Admin | từ khóa, danh mục, khoảng giá, thẻ, còn hàng, đang hoạt động |
| Voucher | `Voucher` | Admin | mã, loại, đang hoạt động, khoảng hiệu lực, khoảng đơn tối thiểu |
| Đơn hàng | `Order` | Staff, Admin | trạng thái, khoảng ngày, khoảng tổng tiền, phương thức thanh toán, từ khóa (mã đơn/email) |

### 5.3. Tính năng nâng cao bắt buộc (`FR-6.1`) [MVP]

1. **Quản lý file / Upload:** ảnh sản phẩm lên bucket (kiểm tra định dạng, kích thước, nội dung thật của tệp). MVP bắt buộc **một ảnh chính** cho mỗi sản phẩm; nhiều ảnh là [NC-9].
2. **Giao tiếp và Thông báo:** email OTP và email khi đơn đổi trạng thái.
3. **Lập lịch tự động (cron):** Cloud Scheduler gọi job dọn OTP và refresh token hết hạn, giỏ hàng bỏ quên quá 30 ngày, ảnh mồ côi trên bucket.

Làm thêm ở tuần 3–4: **Tìm kiếm nâng cao/AI** (NC-3) và **Báo cáo kèm biểu đồ** (NC-4).

### 5.4. Gợi ý trang phục bằng AI [NC-3]

Hồ sơ cơ thể → nhập ngữ cảnh → 1–3 bộ trang phục chỉ gồm sản phẩm còn hàng, backend kiểm chứng đầu ra. Dùng quan hệ nhiều-nhiều chuẩn hóa (`outfit_recommendation_items`) thay cho cột chuỗi danh sách id. Đặc tả chi tiết sẽ viết thành `docs/ai-spec.md` khi bắt đầu NC-3.

### 5.5. Ngoài phạm vi 4 tuần [BỎ]

Chat, Flash Sale/Mega Sale, tách đơn theo kho, đăng nhập số điện thoại/2FA, Push/SMS/Zalo, tích hợp hãng vận chuyển, VietQR, trả hàng/hoàn tiền, RBAC chi tiết hơn, Mobile App.

---

## 6. Quy tắc nghiệp vụ cốt lõi (`FR-5`)

Chi tiết ở [`codebase.md`](codebase.md) mục 4. Phần áp dụng trong MVP:

### 6.1. Vòng đời đơn hàng

Máy trạng thái chặt, một nơi duy nhất đổi trạng thái (`OrderStateMachine`), mọi lần đổi ghi `order_status_history` (ai, lúc nào, lý do).

`Chờ xác nhận → Chờ lấy hàng → Đang giao hàng → Đã giao hàng → Hoàn tất`, và `Đã hủy`.

| Quy tắc | MVP |
|---|---|
| Đơn COD tạo thẳng ở `Chờ xác nhận` | ✔ |
| Khách hủy tự do khi `Chờ xác nhận` | ✔ |
| Từ `Chờ lấy hàng` trở đi khách không tự hủy; Admin/Staff hủy được trước khi xuất kho | ✔ |
| Từ `Đang giao hàng` không hủy được | ✔ |
| Khách bấm "Đã nhận" để sang `Hoàn tất` | ✔ |
| Tự hoàn tất sau N = 7 ngày | [NC] |
| Bước `Chờ thanh toán` (VietQR) | [BỎ] |
| Trả hàng/hoàn tiền | [BỎ] |

### 6.2. Tồn kho

- COD trừ tồn khả dụng **ngay khi đặt hàng thành công**, bằng câu lệnh nguyên tử; không bao giờ âm (có `CHECK` ở DB).
- Hủy trước khi xuất kho thì cộng lại tồn kho.
- Xóa mềm sản phẩm không làm mất đơn cũ (`order_items.unit_price` đã chốt giá).

### 6.3. Voucher và tính tiền (`FR-5.1`)

| Loại | Cách tính |
|---|---|
| `PERCENT` | Giảm theo % trên tiền hàng, có **mức giảm tối đa** |
| `FIXED_AMOUNT` | Giảm số tiền cố định |
| `FREESHIP` | Miễn hoặc giảm phần phí vận chuyển |

- **Một voucher mỗi đơn**, không cộng dồn; có **đơn tối thiểu**; có thời hạn hiệu lực và giới hạn số lượt dùng; mỗi người dùng một lần nếu voucher quy định.
- Tổng tiền: `tiền hàng − giảm giá + phí ship − giảm phí ship`, làm tròn rõ ràng, dùng `BigDecimal`.
- Kiểm tra chéo trường khi tạo/sửa voucher: ngày kết thúc sau ngày bắt đầu; % trong 0–100; giá trị cố định không âm; đơn tối thiểu không âm.

### 6.4. Kiểm tra hợp lệ nâng cao (`FR-5.2`)

Kiểm tra theo trạng thái cơ sở dữ liệu (không đặt hàng khi hết hàng; không xóa danh mục còn sản phẩm đang hoạt động; không vô hiệu hóa tài khoản Admin cuối cùng; **không xóa vĩnh viễn sản phẩm hoặc biến thể đã nằm trong `order_items`**, chỉ xóa mềm), kiểm tra chéo trường, và ràng buộc `CHECK` ở tầng DB.

### 6.5. Quy tắc xóa (`FR-2.4`, `FR-2.5`)

- **Xóa mềm là mặc định:** đặt `deleted_at`/`deleted_by`; mọi truy vấn thường tự bỏ qua bản ghi đã xóa (`@SQLRestriction("deleted_at IS NULL")` trên lớp nền `Auditable`); thùng rác dùng truy vấn riêng.
- **Giải phóng giá trị duy nhất:** khi xóa mềm, `slug`/`code`/`email` được đổi hậu tố `__deleted_<id>` để người khác dùng lại giá trị cũ; khôi phục thì trả lại nếu không trùng.
- **Xóa vĩnh viễn:** chỉ Admin, chỉ khi bản ghi đã nằm trong thùng rác, có hộp thoại xác nhận; cascade ở DB (xem 7.1). Xóa vĩnh viễn sản phẩm xóa các ảnh của nó trên bucket (làm hết sức, job dọn ảnh mồ côi bù sau). Xóa mềm **giữ nguyên ảnh**.

---

## 7. Mô hình dữ liệu (`FR-4`, `TR-C`)

### 7.1. Bảng và quan hệ

| Nhóm | Bảng | Ghi chú |
|---|---|---|
| Danh tính | `users`, `refresh_tokens`, `otp_codes`, `body_profile` | `body_profile` chỉ dùng từ NC-3 nhưng tạo bảng sẵn. `users` có `failed_login_count`, `locked_until` |
| Danh mục hàng | `categories` (tự tham chiếu cha-con), `tags`, `products`, `product_variants`, `product_images` | `product_images` lưu `object_key`, `url`, `content_type`, `size_bytes`, `sort_order`, `is_primary` (ảnh nằm trên bucket) |
| **Nhiều-nhiều** | **`product_tags`** (Product ⇄ Tag), **`cart_items`** (Cart ⇄ Variant), **`order_items`** (Order ⇄ Variant, có số lượng và giá chốt) | ≥ 2 bảng trung gian ✔ |
| Thương mại | `carts`, `orders`, `order_status_history`, `vouchers`, `voucher_usages` | |
| AI (NC-3) | `outfit_recommendations`, `outfit_recommendation_items` | quan hệ nhiều-nhiều thứ tư |

- **Một-nhiều** (≥ 2 ✔): `users → orders`, `categories → products`, `products → product_variants`, `products → product_images`, `orders → order_items`, `categories → categories` (cha-con), `users → refresh_tokens`.
- **Bảng thực thể chính** (không tính bảng trung gian): ≥ 12; **tổng cột** dự kiến > 100 (yêu cầu ≥ 50).
- **Cột chung có kiểm toán và xóa mềm** (kế thừa từ `Auditable`): `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`. Cần `@EnableJpaAuditing` và `AuditorAware` lấy người dùng từ token thì `*_by` mới được điền.
- **Thực thể CRUD chính** và thuộc tính (≥ 6–8): `User` (email, họ tên, điện thoại, mật khẩu băm, vai trò, trạng thái, đã xác thực email, lần đăng nhập cuối), `Category` (tên, slug, mô tả, danh mục cha, thứ tự, hoạt động), `Product` (tên, slug, mô tả, thương hiệu, chất liệu, giá gốc, khối lượng, danh mục, hoạt động), `Voucher` (mã, loại, giá trị, giảm tối đa, đơn tối thiểu, bắt đầu, kết thúc, giới hạn lượt, đã dùng, hoạt động).
- **Toàn vẹn:** khóa chính tự tăng; khóa ngoại cấu hình ở DB; `NOT NULL`, `UNIQUE` (email, phone, slug, mã voucher), `CHECK` (giá ≥ 0, `stock_quantity` ≥ 0, số lượng > 0, % trong 0–100, ngày kết thúc > ngày bắt đầu); `product_variants` có cột `version` cho `@Version`.
- **Chỉ mục:** đánh cho mọi khóa ngoại và cột hay lọc (`orders(user_id, status, created_at)`, `products(category_id, is_active)`); **không** tạo thêm chỉ mục thường trên cột đã có `UNIQUE` (slug, code, token_hash) để tránh chỉ mục trùng.
- **3NF:** mỗi bảng chỉ chứa thuộc tính phụ thuộc khóa chính của nó; dữ liệu lặp có chủ ý duy nhất là **ảnh chụp lúc đặt hàng** (`order_items.unit_price`, `orders.shipping_address_text`), ghi rõ trong ER và báo cáo như ngoại lệ có lý do nghiệp vụ.
- **Cascade (`FR-2.4`):** mọi khóa ngoại của bảng con trỏ tới bảng bị xóa cứng đều khai báo `ON DELETE CASCADE` ở DB: `users → orders, carts, refresh_tokens, otp_codes, body_profile`; `orders → order_items, order_status_history, voucher_usages`; `products → product_variants, product_images, product_tags`; `carts → cart_items`. Ngoại lệ có chủ ý ở 6.4: không xóa cứng sản phẩm/biến thể còn nằm trong `order_items` (chỉ xóa mềm).
- **Sơ đồ ER (`DL-2`):** viết bằng Mermaid trong `docs/`, xuất hình ở ngày 10.

### 7.2. Dữ liệu mẫu (`TR-C4`)

Script Flyway/SQL chạy tự động ở hồ sơ `demo` (đường dẫn seed tách khỏi migration schema), dữ liệu thực tế bằng tiếng Việt (không "Test 1"): ~8 danh mục, ~15 thẻ, ~40 sản phẩm với mô tả đầy đủ, ~150 biến thể, ~25 người dùng, ~40 đơn kèm ~90 dòng hàng, ~8 voucher: **> 300 bản ghi**. Mỗi sản phẩm mẫu có một ảnh: một script nạp khoảng 40 ảnh mẫu lên bucket và `product_images.url` trỏ tới chúng. Có sẵn **tài khoản mẫu cho từng vai trò** (Admin, Staff, Customer) ghi trong `README.md`.

---

## 8. Lộ trình và phân công

### 8.1. Nguyên tắc làm việc

- **Chia theo chiều dọc (full-stack)**: mỗi người sở hữu module từ DB đến giao diện. Bộ khung dùng chung (mục 3.3) được dựng sớm và dùng lại.
- Hợp đồng API: đổi API thì cập nhật bản `openapi.json` xuất từ springdoc trong cùng PR; dùng dữ liệu giả ở frontend cho phần backend chưa xong.
- **Git (`VC-*`):**
  - `.gitignore` bao gồm IDE, `target/`, `node_modules/`, `.env`, `uploads/`, log; không commit mật khẩu, khóa hay token.
  - Commit theo chuẩn Conventional Commits (`feat(auth): …`, `fix(cart): …`, `docs: …`), mỗi commit làm **một việc**.
  - **Mục tiêu ≥ 2 commit mỗi ngày mỗi người** (đạt ≥ 20 commit mỗi người trong 2 tuần đầu, tổng ≥ 50).
  - Không đẩy thẳng lên `main`; nhánh `feature/<id>-<mo-ta>`/`bugfix/<id>-<mo-ta>`; Pull Request có ít nhất một người khác review; bảo vệ nhánh `main` trên GitHub (cần chủ repo bật).
  - Mỗi thành viên cấu hình `git config user.email` trùng email tài khoản GitHub để commit được tính.
- Họp ngắn đầu ngày; điểm kiểm tra ngày 5; cổng MVP ngày 10.

### 8.2. Phân công (tóm tắt)

| Thành viên | Sở hữu |
|---|---|
| **Dev 1 – Quang** | Hạ tầng cấu hình và schema Flyway, bộ khung backend dùng chung (`Auditable`, `PageResponse`, xử lý lỗi), bảo mật (JWT, CSRF, rate limit), đăng ký/đăng nhập/OTP/hồ sơ, quản lý người dùng, email, endpoint job + Cloud Scheduler, `GcsStorageService` và cấu hình bucket/IAM, **triển khai Cloud Run**, README |
| **Dev 2 – Nguyên** | Khung frontend và bộ thành phần dùng chung, danh mục, sản phẩm/biến thể/thẻ/ảnh (giao diện `StorageService` + `LocalStorageService` + kiểm tra upload), trang bán hàng (danh sách, chi tiết), dữ liệu mẫu danh mục và nạp ảnh mẫu, kiểm thử responsive |
| **Dev 3 – Kiệt** | Máy trạng thái đơn, tồn kho, giỏ hàng, voucher, đặt hàng COD, đơn của tôi, quản trị đơn, dữ liệu mẫu thương mại, giao diện giỏ/đặt hàng/đơn/voucher |

Kế hoạch **từng ngày, từng người** cho 2 tuần đầu: [`docs/mvp-daily-plan.md`](docs/mvp-daily-plan.md) (cần cập nhật theo bản này, xem mục 10).

### 8.3. Mốc

| Mốc | Thời điểm | Tiêu chí |
|---|---|---|
| Kiểm tra giữa kỳ | Cuối ngày 5 | Schema và bộ khung đã có; đăng nhập (có OTP) chạy; danh mục và sản phẩm CRUD chạy trên DB (upload ảnh qua `LocalStorageService`); giỏ hàng gọi được. Trễ thì áp dụng 8.4 |
| **Cổng MVP** | **Cuối ngày 10** | Mọi dòng ở ma trận mục 2 (trừ DL-4) đạt, **trên URL công khai HTTPS**, ảnh tải từ bucket |
| Tuần 3–4 | Ngày 11–20 | Làm BB-1, BB-2 trước, rồi hàng đợi NC; 2 ngày cuối đóng băng tính năng |

### 8.4. Thứ tự cắt khi trễ (trong MVP)

Không cắt được: bảo mật bắt buộc, schema, xóa mềm/audit, CRUD 4 thực thể, bộ lọc/phân trang, triển khai. Cắt từ dưới lên:

1. Email trạng thái đơn (giữ email OTP).
2. Cron dọn dữ liệu (thay bằng một tính năng nâng cao khác đã có, ví dụ biểu đồ cơ bản; `FR-6.1` vẫn cần đủ 3).
3. Ảnh đại diện người dùng; nhiều ảnh cho sản phẩm (giữ một ảnh).
4. Màn hình Staff riêng (dùng chung màn hình đơn của Admin, ẩn nút theo vai trò).
5. Khách tự hủy đơn (Admin/Staff hủy thay).
6. Xóa vĩnh viễn có cascade ở thực thể ngoài `User` và `Product`.

### 8.5. Tuần 3–4: việc bắt buộc rồi hàng đợi ưu tiên

Sức chứa ≈ 30 ngày công, trừ 2 ngày đóng băng (6 ngày công) còn ≈ 24. **BB không bị cắt**; NC làm theo thứ tự, hết thời gian là dừng.

| # | Việc | Ngày công | Cộng dồn |
|---|---|---:|---:|
| **BB-1** | Rà soát tuân thủ `rule.md` từng dòng theo ma trận mục 2, sửa thiếu sót, viết thêm test | 3 | 3 |
| **BB-2** | **Báo cáo kỹ thuật PDF 25–30 trang** (`DL-4`), chờ cấu trúc từ đề bài | 6 | 9 |
| NC-3 | **AI gợi ý trang phục** + hồ sơ cơ thể + trang giao diện | 6,5 | 15,5 |
| NC-4 | Báo cáo kèm **biểu đồ** cho Admin (doanh thu, đơn theo trạng thái) | 2 | 17,5 |
| NC-5 | Kiểm thử đầu cuối (Playwright) các luồng chính | 3 | 20,5 |
| NC-6 | Sổ địa chỉ | 2 | 22,5 |
| NC-7 | Phí ship theo tỉnh × khối lượng | 2,5 | 25 |
| NC-8 | Đánh giá sản phẩm | 2 | 27 |
| NC-9 | Nhiều ảnh cho mỗi sản phẩm (kéo thả, đổi ảnh chính) | 1 | 28 |

Với sức chứa ≈ 24 ngày công, dự kiến làm tới **NC-6**; NC-7 trở đi chỉ khi dư thời gian. VietQR và trả hàng/hoàn tiền là **[BỎ]**.

---

## 9. Rủi ro

| Rủi ro | Mức | Biện pháp |
|---|---|---|
| **MVP đúng 30/30 ngày công, gần như không dư** (đã gồm thêm ~0,5–1 ngày cho bucket) | Rất cao | Kế hoạch ngày 1–10 đã xếp đầy; bộ khung dùng chung dựng sớm; chỉ bắt buộc một ảnh chính; điểm kiểm tra ngày 5; thứ tự cắt 8.4; nếu trễ ngày 10 thì dùng một phần tuần 3 cho MVP, đẩy NC xuống (BB không đẩy) |
| Nhóm chưa có tài khoản GCP có thanh toán (Cloud SQL, Cloud Run, bucket) | Cao | Xác nhận trước ngày 8. Nếu không, chọn nền tảng khác chạy được container, MySQL và lưu đối tượng (cần kiểm tra gói miễn phí hiện hành) và báo lại, vì kế hoạch ngày 8–10 phụ thuộc |
| Cấu hình bucket/IAM sai (ảnh không hiện hoặc ai cũng ghi được) | Trung bình | Chỉ service account của Cloud Run được ghi; kiểm tra thủ công hai trường hợp trước cổng MVP; `LocalStorageService` giúp dev không bị chặn |
| Chưa có ảnh mẫu thực tế cho 40 sản phẩm | Trung bình | Chọn ảnh có thể dùng hợp pháp hoặc tự chụp/tạo; giao cho Dev 2 trong ngày 6–7; nếu chậm, dùng ảnh minh họa chung theo danh mục |
| Chưa có cấu trúc báo cáo PDF | Trung bình | Nhận cấu trúc từ đề bài trước ngày 11; viết dần các phần từ ngày 8 |
| Số commit: cần ≥ 50 và mỗi người ≥ 20 | Cao | ≥ 2 commit/ngày/người ngay từ ngày 1; kiểm tra `git shortlog -sne` mỗi cuối tuần; sửa email git |
| Bảo vệ nhánh `main` cần quyền chủ repo (Zerzo15) và có thể phụ thuộc gói GitHub | Trung bình | Nhờ chủ repo bật ngày 1; nếu không bật được thì quy ước review bằng PR và ghi nhận trong README |
| Email OTP thật cần SMTP và có thể bị chặn | Trung bình | Chọn nhà cung cấp SMTP ở ngày 5; ở local ghi OTP ra console |
| Endpoint job bị gọi trái phép | Thấp | Token trong Secret Manager, so sánh hằng thời gian, không nằm dưới `/api` |
| Bộ đếm giới hạn IP trong bộ nhớ không đúng nếu có nhiều instance | Thấp | `max-instances=1`; khóa theo email nằm trong DB nên vẫn đúng |
| Mật khẩu DB đã từng commit trong cấu hình | Cao | Đổi mật khẩu, chuyển sang `.env`/Secret Manager trong ngày 1 |
| Máy một số thành viên không có Docker | Thấp | Docker không bắt buộc |

---

## 10. Các quyết định đã chốt và do trưởng nhóm tự đặt

**Đã chốt:** tên WELO; một shop; COD ở MVP; web responsive/PWA; MVP 2 tuần gồm toàn bộ `rule.md`; **triển khai sớm vào cuối tuần 2**; báo cáo PDF: cấu trúc sẽ nhận sau; LLM Claude `claude-sonnet-5-5`; frontend React + Vite + Tailwind; voucher 3 loại, một voucher mỗi đơn, có đơn tối thiểu; `gender` MALE/FEMALE/OTHER, `bodyShape` RECTANGLE/TRIANGLE/INVERTED_TRIANGLE/HOURGLASS/OVAL, `skinTone` FAIR/LIGHT/MEDIUM/TAN/DEEP; đăng nhập chỉ email; **ảnh sản phẩm lưu trên bucket**; bucket công khai chỉ đọc; cron chạy bằng Cloud Scheduler; Dev 1 Quang, Dev 2 Nguyên, Dev 3 Kiệt.

**Do trưởng nhóm tự đặt (có thể đổi):**

1. Thêm vai trò **STAFF** để có ≥ 3 vai trò thật trong DB.
2. **4 thực thể CRUD:** `Category`, `Product`, `User`, `Voucher` (chọn vì mỗi cái có ≥ 5 bộ lọc tự nhiên).
3. **3 tính năng nâng cao:** Upload (bucket), Thông báo (email), Lập lịch (Cloud Scheduler). AI và biểu đồ làm ở tuần 3–4.
4. AI chuyển khỏi MVP vì không nằm trong `rule.md`.
5. Triển khai **Cloud Run + Cloud SQL + Cloud Storage**, một container phục vụ cả giao diện và API.
6. `StorageService` có bản local để dev và test không cần GCP.
7. Tài liệu API sinh từ springdoc thay vì viết tay `openapi.yaml`.
8. Phí ship **một mức cố định** trong MVP.
9. Bỏ VietQR, trả hàng/hoàn tiền khỏi 4 tuần; đánh giá, sổ địa chỉ, phí ship theo tỉnh vào hàng đợi NC.
10. Đơn COD có trạng thái ban đầu là `Chờ xác nhận` (không có bước `Chờ thanh toán`).
11. Báo cáo PDF và rà soát tuân thủ chuyển thành **[BB]**, không thuộc hàng đợi có thể bỏ.
12. Không cho xóa cứng sản phẩm/biến thể còn nằm trong đơn hàng; xóa mềm đổi hậu tố để giải phóng giá trị duy nhất.

**Tài liệu liên quan cần cập nhật cho khớp bản này:** `docs/mvp-daily-plan.md` (task bucket, Cloud Scheduler, ảnh mẫu, kiểm tra cuối ngày 10 theo ma trận mục 2, BB-1/BB-2), `codebase.md` (mục công nghệ, mô hình dữ liệu, `product_images`, cascade), và các tài liệu mà bản này nhắc nhưng chưa có: `docs/api/openapi.json`, `docs/er-notes.md`, `docs/ai-spec.md`.
