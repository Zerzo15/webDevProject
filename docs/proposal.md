# PROPOSAL – HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ THỜI TRANG WELO

| Mục | Nội dung |
|---|---|
| Phiên bản | 0.3 – đối chiếu với [`rule.md`](rule.md), MVP 2 tuần, triển khai trực tuyến cuối tuần 2 |
| Ngày | 15/11/2026 |
| Vai trò người viết | Trưởng nhóm kỹ sư phần mềm |
| Thời gian | **4 tuần**: **MVP trong 2 tuần đầu** (đáp ứng toàn bộ yêu cầu chức năng và kỹ thuật của `rule.md`, đã triển khai trực tuyến) + **2 tuần sau** (hoàn thiện, báo cáo, tính năng nâng cao theo thứ tự ưu tiên; không kịp thì bỏ) |
| Nhóm | 3 thành viên: Dev 1 (Kiệt), Dev 2, Dev 3 |
| Nền tảng | Web responsive (PWA). Không có Mobile App |
| Mô hình kinh doanh | Một shop duy nhất (WELO), không phải sàn đa người bán |
| Kế hoạch chi tiết 2 tuần đầu | [`docs/mvp-daily-plan.md`](docs/mvp-daily-plan.md) |

**Nhãn:** **[MVP]** xong trong 2 tuần đầu · **[NC-n]** hoàn thiện/nâng cao, n là thứ tự ưu tiên trong tuần 3–4, hết thời gian thì bỏ · **[BỎ]** không làm trong 4 tuần này.

**Giả định thời gian:** 1 tuần = 5 ngày làm việc; 2 tuần = 10 ngày công mỗi người, 30 ngày công cả nhóm cho MVP. Ngày 1 là thứ Hai (xem `docs/mvp-daily-plan.md`).

> **Cảnh báo về khả năng hoàn thành:** `rule.md` được chọn đưa **toàn bộ vào MVP 2 tuần**. Ước lượng khối lượng khớp đúng 30 ngày công, **không có dư** (mục 9). Rủi ro trễ rất cao; kế hoạch có điểm kiểm tra ngày 5 và thứ tự cắt (mục 8.4).

---

## 1. Tóm tắt tổng quan

WELO là website thương mại điện tử của một shop thời trang. Ngoài chức năng bán hàng chuẩn (xem sản phẩm, giỏ hàng, đặt hàng COD, theo dõi đơn), hệ thống có điểm khác biệt là **gợi ý trang phục bằng AI** theo ngữ cảnh (đi biển, leo núi, đi họp, phỏng vấn) và số đo cơ thể, chỉ chọn sản phẩm còn hàng. Phần AI **không thuộc `rule.md`** nên xếp vào tuần 3–4 (NC-3).

Mục tiêu 2 tuần đầu: một hệ thống bán hàng hoàn chỉnh, **đáp ứng mọi mục bắt buộc của `rule.md`** và **chạy trên URL công khai HTTPS**.

Hiện trạng mã nguồn: backend Spring Boot 4.1.1 / Java 21 / MySQL có 9 entity, 9 DTO, 4 repository; **chưa có** controller, bảo mật, mapper. Frontend hiện là HTML/CSS tĩnh "UNIKURO" sẽ được thay bằng React + Tailwind mang tên WELO. Repo git hiện có **13 commit** (3 tác giả; chưa thấy commit của Dev 1), xem rủi ro ở mục 9.

---

## 2. Đối chiếu `rule.md` với bản MVP cũ (phần thiếu đã bổ sung)

Bản v0.2 của proposal thiếu nhiều mục bắt buộc. Bảng dưới liệt kê và cách bổ sung ở bản này.

| Mã `rule.md` | Thiếu ở bản cũ | Bổ sung trong bản này |
|---|---|---|
| FR-1.1 | Xác thực nhiều bước (OTP) để ngoài MVP | Đăng ký → OTP email → kích hoạt tài khoản: **[MVP]** |
| FR-1.3, 1.4 | Chỉ có Guest/User/Admin; RBAC bị bỏ | **3 vai trò trong DB: `ADMIN`, `STAFF`, `CUSTOMER`** (+ Guest) với quyền khác nhau; RBAC ở API và giao diện: **[MVP]** |
| FR-1.5 | Đổi mật khẩu để sau | Xem/sửa hồ sơ, đổi mật khẩu: **[MVP]** |
| FR-1.6 | Quản lý người dùng của Admin để sau | Admin Dashboard quản lý người dùng (tạo, vô hiệu hóa, đổi vai trò): **[MVP]** |
| FR-2.1, 2.2 | Chỉ có CRUD đơn hàng; sản phẩm/danh mục nạp bằng seed | **4 thực thể CRUD đầy đủ**: `Category`, `Product` (+biến thể), `User`, `Voucher`; mỗi thực thể ≥ 6–8 thuộc tính: **[MVP]** |
| FR-2.3 | Chỉ có validation phía server | Validation **cả client** (inline) **và server**: **[MVP]** |
| FR-2.4 | Không có xóa phụ thuộc | Xóa vĩnh viễn có **cascade** (người dùng → hồ sơ, giỏ, đơn; sản phẩm → biến thể): **[MVP]** |
| FR-2.5, 2.7 | Không có xóa mềm, khôi phục | **Xóa mềm** + màn hình **Khôi phục** cho mọi thực thể quản lý: **[MVP]** |
| FR-2.6 | Không có kiểm toán | `created_by/at`, `updated_by/at`, `deleted_by/at` trên mọi bảng chính (JPA Auditing); lịch sử trạng thái đơn: **[MVP]** |
| FR-3 | Chỉ lọc theo danh mục và giá | Tìm đa tiêu chí, **≥ 5 bộ lọc cho mỗi thực thể**, sắp xếp nhiều cột, cỡ trang 10/20/50/100, dòng thống kê "Đang hiển thị x–y trong z": **[MVP]**, dựng bằng bộ khung dùng chung |
| FR-4.3, TR-C1 | Không có quan hệ nhiều-nhiều | Thêm `Tag` + bảng trung gian `product_tags`; cùng `cart_items`, `order_items`: **[MVP]** |
| FR-5 | Logic nghiệp vụ có nhưng voucher bị hoãn | Máy trạng thái đơn, trừ kho nguyên tử, **voucher (giảm %, giảm cố định, freeship)**, kiểm tra chéo trường: **[MVP]** |
| FR-6 | Chưa chốt 3 tính năng nâng cao | **Upload file** (ảnh sản phẩm), **Thông báo** (email OTP + email trạng thái đơn), **Lập lịch** (cron dọn dữ liệu hết hạn): **[MVP]**. AI và biểu đồ làm thêm ở tuần 3–4 |
| TR-A1, A2, A3 | Chưa có yêu cầu giao diện | Responsive 3 kích thước + hamburger + loading/empty/confirm + inline validation + chống gửi trùng: **[MVP]** |
| TR-B6 | Thiếu CSRF/CSP | Token CSRF cho mọi thao tác ghi, header CSP, làm sạch đầu vào chống XSS: **[MVP]** |
| TR-B8 | HTTPS chưa có do chạy local | Triển khai **trực tuyến có HTTPS**: **[MVP]** (mục 3.4) |
| TR-C1–C4 | Thiếu ràng buộc CHECK, chỉ mục, số cột, dữ liệu mẫu | Schema ≥ 8 bảng chính, ≥ 50 cột, FK + NOT NULL + UNIQUE + CHECK, chỉ mục; **dữ liệu mẫu ≥ 100 bản ghi thực tế** kèm script tự chạy: **[MVP]** |
| VC-1…VC-8 | Chưa có quy ước git | `.gitignore` đủ, README, commit chuẩn, ≥ 50 commit (mỗi người ≥ 20), nhánh `feature/`, `main` được bảo vệ, Pull Request có review: **[MVP]**, xem mục 8.1 |
| DL-1…DL-3 | ER, tài liệu API chưa có kế hoạch | Script DB + seed, sơ đồ ER, `openapi.yaml` + Postman Collection: **[MVP]** |
| DL-4 | Chưa có báo cáo | Báo cáo kỹ thuật PDF 25–30 trang: **[NC-2]**, **chờ cấu trúc từ đề bài** |
| DL-5 | Chạy local, mâu thuẫn | **Triển khai cuối tuần 2**, kèm tài khoản mẫu cho từng vai trò: **[MVP]** |

**Thay đổi so với v0.2:** bỏ quyết định "chỉ chạy local" (thay bằng *phát triển local, triển khai cloud ở cuối tuần 2*); đưa quản lý sản phẩm, voucher, quản trị người dùng, OTP lên MVP; AI chuyển xuống tuần 3–4 (nó không có trong `rule.md`); bỏ VietQR, trả hàng/hoàn tiền, đánh giá, phí ship theo tỉnh (không bắt buộc, tuần 3–4 nếu còn thời gian).

---

## 3. Kiến trúc và công nghệ

### 3.1. Đã chốt

| Lớp | Lựa chọn |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, Bean Validation |
| Frontend | **React + TypeScript + Vite + Tailwind CSS** (SPA/PWA) |
| Cơ sở dữ liệu | MySQL 8 (≥ 8.0.16 để ràng buộc `CHECK` có hiệu lực) |
| Xác thực | JWT (access ngắn) + refresh token xoay vòng; mật khẩu BCrypt |
| Di trú DB | Flyway; `ddl-auto=validate` |
| AI (tuần 3–4) | Claude (`claude-sonnet-5-5`) qua interface `LlmClient` |

### 3.2. Kiến trúc: modular monolith, **một khối triển khai**

Backend chia module theo package (identity, catalog, commerce, inventory, notification). **Frontend được build thành tệp tĩnh và do chính backend phục vụ**, nên SPA và API cùng một địa chỉ (cùng origin). Lợi ích: không phải cấu hình CORS, cookie refresh/CSRF hoạt động đơn giản, chỉ một thứ để triển khai.

```
 Trình duyệt ──HTTPS──▶ Cloud Run (1 container: Spring Boot + SPA tĩnh)
                                │
                                ├── Cloud SQL (MySQL 8)
                                ├── ảnh sản phẩm: lưu trong MySQL (bảng product_images)
                                └── SMTP (email OTP/thông báo), Claude API (tuần 3–4)
 Phát triển: Vite dev server (http://localhost:5173, proxy /api) + Spring Boot (http://localhost:8080) + MySQL local
```

### 3.3. Quyết định kỹ thuật kèm lý do

| Hạng mục | Chọn | Lý do |
|---|---|---|
| Bộ khung CRUD dùng chung | Backend: lớp nền `Auditable` (audit + xóa mềm), `PageResponse` có `from/to/totalElements`, bộ dựng `Specification` cho lọc và sắp xếp nhiều cột. Frontend: `DataTable` có bộ lọc, cỡ trang, thống kê, thùng rác; `ConfirmDialog`; bộ form có kiểm tra dòng | `rule.md` đòi bộ lọc, phân trang, xóa mềm, audit cho **mọi** thực thể: dựng một lần rồi dùng lại là cách duy nhất cho vừa thời gian |
| Lưu ảnh | Trong MySQL (`product_images`, giới hạn 2 MB, jpeg/png/webp) | Ổ đĩa Cloud Run là tạm thời, ảnh sẽ mất; tránh thêm dịch vụ Cloud Storage. Chấp nhận nhược điểm hiệu năng ở quy mô demo |
| Chống bán lố | `UPDATE … SET stock = stock - ? WHERE stock >= ?` + `@Version` | Không cần Redis ở một bản chạy |
| Giới hạn tần suất | Trong bộ nhớ (Bucket4j hoặc tương đương) | Cloud Run đặt `max-instances=1` để giới hạn và bộ lập lịch nhất quán |
| Lập lịch | `@Scheduled` (dọn OTP và refresh token hết hạn, dọn giỏ hàng bỏ quên quá 30 ngày) | Cần `min-instances=1` khi triển khai để tác vụ chạy được; có chi phí. Chọn các việc dọn dẹp để không đụng quy tắc nghiệp vụ của đơn hàng |
| CSRF | Bật CSRF của Spring Security với cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN` cho mọi `POST/PUT/PATCH/DELETE` | Đáp ứng `TR-B6` đúng nghĩa đen, kể cả khi dùng JWT |
| Làm sạch XSS | Làm sạch các trường văn bản tự do ở server (mô tả, ghi chú), React thoát đầu ra mặc định, header CSP | `TR-B2`, `TR-B6` |
| Email | SMTP qua biến môi trường (chọn nhà cung cấp ở ngày 5); ở local ghi OTP ra console nếu chưa cấu hình | Triển khai thật cần email thật cho OTP |
| Kiểm thử | JUnit 5; DB test riêng; Testcontainers chỉ khi máy có Docker (máy hiện tại chưa có) | |

### 3.4. Triển khai (cuối tuần 2)

Theo cấu hình hạ tầng ban đầu của nhóm: **Google Cloud Run + Cloud SQL (MySQL)**, HTTPS do Cloud Run cấp (địa chỉ `*.run.app`).

- Build từ mã nguồn bằng Cloud Build (không cần Docker ở máy cá nhân); `Dockerfile` đa giai đoạn vẫn có để chạy ở nơi khác.
- Cấu hình bí mật (mật khẩu DB, khóa JWT, SMTP) bằng biến môi trường/Secret Manager, không vào repo.
- Flyway chạy khi khởi động; seed dữ liệu mẫu bật bằng hồ sơ `demo`.
- **Chi phí:** Cloud SQL tính phí (cần tài khoản GCP có thanh toán); `min-instances=1` cũng tính phí. Nhóm cần có tài khoản thanh toán trước ngày 8. Nếu không có, cần phương án nền tảng khác (xem rủi ro).

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
| Đăng ký nhiều bước | Điền form → nhận **OTP qua email** → nhập OTP → tài khoản kích hoạt (`email_verified`). OTP 6 số, hết hạn 5 phút, dùng một lần, có giới hạn gửi lại |
| Đăng nhập | Email + mật khẩu → access token JWT (~15 phút, giữ trong bộ nhớ, không lưu `localStorage`) + refresh token (cookie `HttpOnly`, `Secure`, `SameSite=Strict`, **xoay vòng**, lưu băm trong DB) |
| Mật khẩu | BCrypt cost 12; quy tắc độ mạnh tối thiểu; đổi mật khẩu thu hồi các phiên khác |
| Chống brute-force | Giới hạn đăng nhập sai theo email và IP; khóa tạm; thông báo lỗi chung |
| Phân quyền | `@PreAuthorize` + cấu hình `SecurityFilterChain`; mặc định từ chối; lọc dữ liệu theo người dùng từ token; truy cập dữ liệu người khác trả `404` |
| Tài khoản bị vô hiệu | `LOCKED` hoặc đã xóa mềm không đăng nhập được, refresh token bị thu hồi |
| Header | CSP, `X-Content-Type-Options`, `Referrer-Policy`; HTTPS ở môi trường triển khai |

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

### 5.1. Phân hệ Khách hàng [MVP]

- **Tài khoản:** đăng ký + OTP, đăng nhập/đăng xuất, xem/sửa hồ sơ (họ tên, điện thoại, ảnh đại diện tùy chọn), đổi mật khẩu.
- **Khám phá sản phẩm:** trang chủ; danh sách có **tìm kiếm đa trường** (tên, mô tả, thương hiệu, thẻ) và **≥ 5 bộ lọc** (danh mục, khoảng giá, size, màu, còn hàng, thẻ); sắp xếp nhiều cột; cỡ trang 10/20/50/100; dòng thống kê kết quả.
- **Chi tiết sản phẩm:** ảnh, mô tả, chọn màu/size, tồn kho, thẻ.
- **Giỏ hàng:** theo tài khoản, tự cập nhật giá và tồn kho, cảnh báo hết hàng.
- **Đặt hàng COD:** nhập địa chỉ giao hàng, nhập mã voucher, **phí ship cố định** (cấu hình một mức), chốt giá và địa chỉ vào đơn, trừ kho nguyên tử, chống gửi trùng bằng `Idempotency-Key`.
- **Đơn của tôi:** danh sách có lọc, chi tiết kèm lịch sử trạng thái, hủy khi `Chờ xác nhận`, bấm "Đã nhận hàng".

### 5.2. Phân hệ Quản trị (Staff/Admin) [MVP]

Mọi màn hình danh sách đều dùng `DataTable` chung (lọc ≥ 5, sắp xếp nhiều cột, phân trang, thống kê, thùng rác với khôi phục).

| Màn hình | Thực thể | Vai trò | Bộ lọc (≥ 5) |
|---|---|---|---|
| Người dùng | `User` | Admin | từ khóa (email/tên/điện thoại), vai trò, trạng thái, đã xác thực email, khoảng ngày tạo |
| Danh mục | `Category` | Admin | từ khóa, danh mục cha, đang hoạt động, có sản phẩm hay không, khoảng ngày tạo |
| Sản phẩm | `Product` (+ biến thể, thẻ, ảnh) | Admin | từ khóa, danh mục, khoảng giá, thẻ, còn hàng, đang hoạt động |
| Voucher | `Voucher` | Admin | mã, loại, đang hoạt động, khoảng hiệu lực, khoảng đơn tối thiểu |
| Đơn hàng | `Order` | Staff, Admin | trạng thái, khoảng ngày, khoảng tổng tiền, phương thức thanh toán, từ khóa (mã đơn/email) |

### 5.3. Tính năng nâng cao bắt buộc (`FR-6.1`) [MVP]

1. **Quản lý file / Upload:** ảnh sản phẩm (kiểm tra định dạng, kích thước, nội dung thật của tệp).
2. **Giao tiếp và Thông báo:** email OTP và email khi đơn đổi trạng thái.
3. **Lập lịch tự động (cron):** dọn OTP và refresh token hết hạn; dọn giỏ hàng bỏ quên quá 30 ngày.

Làm thêm ở tuần 3–4: **Tìm kiếm nâng cao/AI** (NC-3) và **Báo cáo kèm biểu đồ** (NC-4).

### 5.4. Gợi ý trang phục bằng AI [NC-3]

Hồ sơ cơ thể → nhập ngữ cảnh → 1–3 bộ trang phục chỉ gồm sản phẩm còn hàng, backend kiểm chứng đầu ra. Chi tiết: [`docs/ai-spec.md`](docs/ai-spec.md). Dùng quan hệ nhiều-nhiều chuẩn hóa (`outfit_recommendation_items`) thay cho cột chuỗi danh sách id.

### 5.5. Ngoài phạm vi 4 tuần [BỎ]

Chat, Flash Sale/Mega Sale, tách đơn theo kho, đăng nhập số điện thoại/2FA, Push/SMS/Zalo, tích hợp hãng vận chuyển, RBAC chi tiết hơn, Mobile App.

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

- COD trừ tồn khả dụng **ngay khi đặt hàng thành công**, bằng câu lệnh nguyên tử; không bao giờ âm.
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

Kiểm tra theo trạng thái cơ sở dữ liệu (không đặt hàng khi hết hàng; không xóa danh mục còn sản phẩm đang hoạt động; không vô hiệu hóa tài khoản Admin cuối cùng), kiểm tra chéo trường, và ràng buộc `CHECK` ở tầng DB.

---

## 7. Mô hình dữ liệu (`FR-4`, `TR-C`)

### 7.1. Bảng và quan hệ

| Nhóm | Bảng | Ghi chú |
|---|---|---|
| Danh tính | `users`, `refresh_tokens`, `otp_codes`, `body_profile` | `body_profile` chỉ dùng từ NC-3 nhưng tạo bảng sẵn |
| Danh mục hàng | `categories` (tự tham chiếu cha-con), `tags`, `products`, `product_variants`, `product_images` | |
| **Nhiều-nhiều** | **`product_tags`** (Product ⇄ Tag), **`cart_items`** (Cart ⇄ Variant), **`order_items`** (Order ⇄ Variant, có số lượng và giá chốt) | ≥ 2 bảng trung gian ✔ |
| Thương mại | `carts`, `orders`, `order_status_history`, `vouchers`, `voucher_usages` | |
| AI (NC-3) | `outfit_recommendations`, `outfit_recommendation_items` | quan hệ nhiều-nhiều thứ tư |

- **Một-nhiều** (≥ 2 ✔): `users → orders`, `categories → products`, `products → product_variants`, `orders → order_items`, `categories → categories` (cha-con), `users → refresh_tokens`.
- **Bảng thực thể chính** (không tính bảng trung gian): ≥ 12; **tổng cột** dự kiến > 100 (yêu cầu ≥ 50).
- **Cột chung có kiểm toán và xóa mềm** (kế thừa từ `Auditable`): `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`.
- **Thực thể CRUD chính** và thuộc tính (≥ 6–8): `User` (email, họ tên, điện thoại, mật khẩu băm, vai trò, trạng thái, đã xác thực email, lần đăng nhập cuối), `Category` (tên, slug, mô tả, danh mục cha, thứ tự, hoạt động), `Product` (tên, slug, mô tả, thương hiệu, chất liệu, giá gốc, khối lượng, danh mục, hoạt động), `Voucher` (mã, loại, giá trị, giảm tối đa, đơn tối thiểu, bắt đầu, kết thúc, giới hạn lượt, đã dùng, hoạt động).
- **Toàn vẹn:** khóa chính tự tăng; khóa ngoại cấu hình ở DB; `NOT NULL`, `UNIQUE` (email, slug, mã voucher), `CHECK` (giá ≥ 0, tồn kho ≥ 0, số lượng > 0, % trong 0–100, ngày kết thúc ≥ ngày bắt đầu); chỉ mục cho mọi khóa ngoại và cột hay truy vấn (email, slug, `orders(user_id, status, created_at)`, `products(category_id, is_active)`).
- **3NF:** mỗi bảng chỉ chứa thuộc tính phụ thuộc khóa chính của nó; dữ liệu lặp có chủ ý duy nhất là **ảnh chụp lúc đặt hàng** (`order_items.unit_price`, `orders.shipping_address_text`), ghi rõ trong ER và báo cáo như ngoại lệ có lý do nghiệp vụ.
- **Cascade:** xóa vĩnh viễn người dùng → xóa hồ sơ cơ thể, giỏ, refresh token, OTP và **đơn hàng của họ**; xóa vĩnh viễn sản phẩm → xóa biến thể, ảnh, liên kết thẻ. Xóa vĩnh viễn chỉ dành Admin, sau khi bản ghi đã nằm trong thùng rác, có hộp thoại xác nhận.

### 7.2. Dữ liệu mẫu (`TR-C4`)

Script Flyway/SQL chạy tự động ở hồ sơ `demo`, dữ liệu thực tế bằng tiếng Việt (không "Test 1"): ~8 danh mục, ~15 thẻ, ~40 sản phẩm với mô tả đầy đủ, ~150 biến thể, ~25 người dùng, ~40 đơn kèm ~90 dòng hàng, ~8 voucher: **> 300 bản ghi**. Có sẵn **tài khoản mẫu cho từng vai trò** (Admin, Staff, Customer) ghi trong `README.md`.

---

## 8. Lộ trình và phân công

### 8.1. Nguyên tắc làm việc

- **Chia theo chiều dọc (full-stack)**: mỗi người sở hữu module từ DB đến giao diện. Bộ khung dùng chung (mục 3.3) được dựng sớm và dùng lại.
- Hợp đồng API trước: cập nhật `docs/api/openapi.yaml` khi đổi API; máy chủ giả cho phần chưa xong.
- **Git (`VC-*`):**
  - `.gitignore` bao gồm IDE, `target/`, `node_modules/`, `.env`, `uploads/`, log.
  - Commit theo chuẩn Conventional Commits (`feat(auth): …`, `fix(cart): …`, `docs: …`), mỗi commit làm **một việc**.
  - **Mục tiêu ≥ 2 commit mỗi ngày mỗi người** (đạt ≥ 20 commit mỗi người trong 2 tuần đầu, tổng ≥ 50).
  - Không đẩy thẳng lên `main`; nhánh `feature/<ten>`/`bugfix/<ten>`; Pull Request có ít nhất một người review; bảo vệ nhánh `main` trên GitHub (cần chủ repo bật).
  - Mỗi thành viên cấu hình `git config user.email` trùng email tài khoản GitHub để commit được tính.
- Họp ngắn đầu ngày; điểm kiểm tra ngày 5; cổng MVP ngày 10.

### 8.2. Phân công (tóm tắt)

| Thành viên | Sở hữu |
|---|---|
| **Dev 1 – Kiệt** | Hạ tầng cấu hình và schema, bộ khung backend dùng chung, bảo mật (JWT, CSRF, rate limit), đăng ký/đăng nhập/OTP/hồ sơ, quản lý người dùng, email, lập lịch, **triển khai Cloud Run**, README |
| **Dev 2** | Khung frontend và bộ thành phần dùng chung, danh mục, sản phẩm/biến thể/thẻ/ảnh, trang bán hàng (danh sách, chi tiết), dữ liệu mẫu danh mục, kiểm thử responsive |
| **Dev 3** | Máy trạng thái đơn, tồn kho, giỏ hàng, voucher, đặt hàng COD, đơn của tôi, quản trị đơn, dữ liệu mẫu thương mại, giao diện giỏ/đặt hàng/đơn/voucher |

Kế hoạch **từng ngày, từng người** cho 2 tuần đầu: [`docs/mvp-daily-plan.md`](docs/mvp-daily-plan.md).

### 8.3. Mốc

| Mốc | Thời điểm | Tiêu chí |
|---|---|---|
| Kiểm tra giữa kỳ | Cuối ngày 5 | Schema và bộ khung đã có; đăng nhập (có OTP) chạy; danh mục và sản phẩm CRUD chạy trên DB; giỏ hàng gọi được. Trễ thì áp dụng 8.4 |
| **Cổng MVP** | **Cuối ngày 10** | Mọi dòng trong danh sách kiểm tra tuân thủ `rule.md` (trừ DL-4) đạt, **trên URL công khai HTTPS** |
| Tuần 3–4 | Ngày 11–20 | Hàng đợi NC bên dưới; 2 ngày cuối đóng băng tính năng |

### 8.4. Thứ tự cắt khi trễ (trong MVP)

Không cắt được: bảo mật bắt buộc, schema, xóa mềm/audit, CRUD 4 thực thể, bộ lọc/phân trang, triển khai. Cắt từ dưới lên:

1. Email trạng thái đơn (giữ email OTP).
2. Cron dọn dữ liệu (thay bằng một tính năng nâng cao khác đã có, ví dụ biểu đồ cơ bản).
3. Ảnh đại diện người dùng, ảnh nhiều tấm cho sản phẩm (giữ một ảnh).
4. Màn hình Staff riêng (dùng chung màn hình đơn của Admin, ẩn nút theo vai trò).
5. Khách tự hủy đơn (Admin/Staff hủy thay).
6. Xóa vĩnh viễn có cascade ở thực thể ngoài `User` và `Product`.

### 8.5. Tuần 3–4: hàng đợi ưu tiên

Sức chứa ≈ 30 ngày công, trừ 2 ngày đóng băng (6 ngày công) còn ≈ 24.

| # | Việc | Ngày công | Cộng dồn |
|---|---|---:|---:|
| NC-1 | Kiểm tra tuân thủ `rule.md` từng dòng, sửa thiếu sót, viết thêm test | 3 | 3 |
| NC-2 | **Báo cáo kỹ thuật PDF 25–30 trang** (`DL-4`), chờ cấu trúc từ đề bài | 6 | 9 |
| NC-3 | **AI gợi ý trang phục** + hồ sơ cơ thể + trang giao diện | 6,5 | 15,5 |
| NC-4 | Báo cáo kèm **biểu đồ** cho Admin (doanh thu, đơn theo trạng thái) | 2 | 17,5 |
| NC-5 | Kiểm thử đầu cuối (Playwright) các luồng chính | 3 | 20,5 |
| NC-6 | Sổ địa chỉ | 2 | 22,5 |
| NC-7 | Phí ship theo tỉnh × khối lượng | 2,5 | 25 |
| NC-8 | Đánh giá sản phẩm | 2 | 27 |
| NC-9 | VietQR, trả hàng/hoàn tiền | – | [BỎ] |

Hết thời gian là dừng; việc nào ở cuối hàng đợi chưa xong thì bỏ.

---

## 9. Rủi ro

| Rủi ro | Mức | Biện pháp |
|---|---|---|
| **MVP đúng 30/30 ngày công, không dư** | Rất cao | Kế hoạch ngày 1–10 đã xếp đầy; bộ khung dùng chung dựng sớm; điểm kiểm tra ngày 5; thứ tự cắt 8.4; nếu trễ ngày 10 thì nhóm phải dùng một phần tuần 3 cho MVP, đẩy NC xuống |
| Nhóm chưa có tài khoản GCP có thanh toán (Cloud SQL, Cloud Run) | Cao | Xác nhận trước ngày 8. Nếu không, chọn nền tảng khác chạy được container và MySQL (cần kiểm tra gói miễn phí hiện hành) và báo lại, vì kế hoạch ngày 8–10 phụ thuộc |
| Chưa có cấu trúc báo cáo PDF | Trung bình | Nhận cấu trúc từ đề bài trước ngày 11; viết dần các phần từ ngày 8 |
| Số commit: hiện 13, chưa thấy commit của Dev 1; cần ≥ 50 và mỗi người ≥ 20 | Cao | ≥ 2 commit/ngày/người ngay từ ngày 1; kiểm tra `git shortlog -sne` mỗi cuối tuần; sửa email git |
| Bảo vệ nhánh `main` cần quyền chủ repo (Zerzo15) và có thể phụ thuộc gói GitHub | Trung bình | Nhờ chủ repo bật ngày 1; nếu không bật được thì quy ước review bằng PR và ghi nhận trong README |
| Email OTP thật cần SMTP và có thể bị chặn | Trung bình | Chọn nhà cung cấp SMTP ở ngày 5; ở local ghi OTP ra console |
| Cron cần instance luôn chạy trên Cloud Run | Thấp | `min-instances=1` (có phí) hoặc cắt theo 8.4 |
| Ảnh lưu trong MySQL làm chậm khi nhiều ảnh | Thấp | Giới hạn 2 MB, cache header; chấp nhận ở quy mô demo |
| Dữ liệu mật khẩu DB đã commit trong `application.properties` | Cao | Đổi mật khẩu, chuyển sang `.env` trong ngày 1 |
| Máy một số thành viên không có Docker | Thấp | Docker không bắt buộc |

---

## 10. Các quyết định đã chốt và do trưởng nhóm tự đặt

**Đã chốt (theo bạn):** tên WELO; một shop; COD ở MVP; web responsive/PWA; MVP 2 tuần gồm toàn bộ `rule.md`; **triển khai sớm vào cuối tuần 2**; báo cáo PDF: cấu trúc sẽ nhận sau; LLM Claude `claude-sonnet-5-5`; frontend React + Vite + Tailwind; voucher 3 loại, một voucher mỗi đơn, có đơn tối thiểu; `gender` MALE/FEMALE/OTHER, `bodyShape` RECTANGLE/TRIANGLE/INVERTED_TRIANGLE/HOURGLASS/OVAL, `skinTone` FAIR/LIGHT/MEDIUM/TAN/DEEP; đăng nhập chỉ email.

**Do trưởng nhóm tự đặt trong bản này (có thể đổi):**

1. Thêm vai trò **STAFF** để có ≥ 3 vai trò thật trong DB.
2. **4 thực thể CRUD:** `Category`, `Product`, `User`, `Voucher` (chọn vì mỗi cái có ≥ 5 bộ lọc tự nhiên).
3. **3 tính năng nâng cao:** Upload, Thông báo (email), Lập lịch. AI và biểu đồ làm ở tuần 3–4.
4. AI chuyển khỏi MVP vì không nằm trong `rule.md`.
5. Triển khai **Cloud Run + Cloud SQL**, một container phục vụ cả giao diện và API.
6. Lưu ảnh sản phẩm trong MySQL.
7. Phí ship **một mức cố định** trong MVP.
8. Bỏ VietQR, trả hàng/hoàn tiền, đánh giá, sổ địa chỉ khỏi MVP (sổ địa chỉ, đánh giá, phí ship theo tỉnh vào hàng đợi NC).
9. Đơn COD có trạng thái ban đầu là `Chờ xác nhận` (không có bước `Chờ thanh toán`).

**Tài liệu liên quan còn theo bản cũ** (cần cập nhật cho khớp, `docs/backlog.md` đã được thay thế cho 2 tuần đầu bằng `docs/mvp-daily-plan.md`): `codebase.md`, `docs/api/openapi.yaml` (thiếu vai trò STAFF, quản trị người dùng, danh mục/sản phẩm có xóa mềm và thùng rác, voucher CRUD, thẻ, `from/to` trong phân trang), `docs/data-ownership.md`, `docs/ai-spec.md`.
