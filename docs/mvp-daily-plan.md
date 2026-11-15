# Kế hoạch MVP 2 tuần: task từng ngày, từng người

Nguồn: [`proposal.md`](../proposal.md) v0.3, [`rule.md`](../rule.md). Thay thế [`backlog.md`](backlog.md) cho 2 tuần đầu.

**Quy ước:** Ngày 1 là thứ Hai tuần 1; ngày 6 là thứ Hai tuần 2; ngày 10 là thứ Sáu tuần 2. Task có mã `D1-n`, `D2-n`, `D3-n` (Dev, ngày n) và `ALL-n`. Mã trong ngoặc như `(FR-2.5)` là yêu cầu của `rule.md` mà task đáp ứng.

> **Cảnh báo:** mỗi người có đúng **10 ngày công**, kế hoạch dưới đây dùng hết cả 10, **không có ngày dư**. Mọi ước lượng là thô và cần nhóm chỉnh ngay ngày 1. Nếu một ngày trễ, áp dụng thứ tự cắt ở cuối file; đừng dồn việc sang ngày sau mà không báo.

---

## 0. Quy tắc làm việc hằng ngày

| Việc | Quy tắc |
|---|---|
| Họp đầu ngày | 15 phút: hôm qua xong gì, hôm nay làm gì, ai đang chặn ai |
| Commit | **≥ 2 commit/ngày/người**, mỗi commit một việc, thông điệp `type(scope): mô tả` (ví dụ `feat(auth): add otp verification`). Mục tiêu hết ngày 10: mỗi người ≥ 20 commit, cả nhóm ≥ 50 (`VC-3..5`) |
| Nhánh | `feature/<id>-<mo-ta>` hoặc `bugfix/<id>-<mo-ta>`; không đẩy thẳng `main` (`VC-6..8`) |
| Pull Request | Mỗi PR có ít nhất **một người khác** review; PR nhỏ, xong trong ngày; `main` luôn chạy được |
| Cuối ngày | Hợp nhất việc đã xong, ghi vào `docs/progress.md` (1–2 dòng mỗi người) những gì chưa xong và bị chặn |
| Email git | Mỗi người `git config user.email` trùng email GitHub để commit được tính (`git shortlog -sne` kiểm tra) |
| Hợp đồng API | Đổi API thì sửa `docs/api/openapi.yaml` trong cùng PR |

---

## 1. Việc chung

| Mã | Việc | Khi nào | Ai |
|---|---|---|---|
| ALL-1 | **Hội thảo ER (2 giờ)**: chốt bảng, cột, kiểu, khóa ngoại, `CHECK`, `UNIQUE`, chỉ mục, các bảng trung gian (`product_tags`, `cart_items`, `order_items`), cột kiểm toán/xóa mềm, tên enum; ghi kết quả vào `docs/er-notes.md`. Đây là đầu vào của schema ngày 2 | Chiều ngày 1 | Cả nhóm |
| ALL-2 | Họp kiểm tra giữa kỳ, đối chiếu tiêu chí cuối ngày 5 | Chiều ngày 5 | Cả nhóm |
| ALL-3 | **Cổng MVP**: chạy danh sách kiểm tra mục 4 trên URL công khai | Chiều ngày 10 | Cả nhóm |

---

## 2. Kế hoạch từng ngày

### Tuần 1

#### Ngày 1 (Thứ Hai) – Nền tảng và khởi động

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-1 Dọn repo và cấu hình** (`VC-1, VC-2`): `.gitignore` gốc đủ (IDE, `target/`, `node_modules/`, `.env`, `uploads/`, log); `.env.example`; đưa `spring.datasource.*` ra biến môi trường, **bỏ mật khẩu đã commit và đổi mật khẩu đó**; thống nhất tên DB/user giữa `application.properties` và `compose.yaml`; đặt `spring.docker.compose.enabled=false` mặc định; dọn `pom.xml` (bỏ khai báo trùng `mysql-connector-j`, bỏ version thừa; thêm `validation`, `mail`, `springdoc`); mẫu Pull Request; khung `README.md`; **nhờ chủ repo bật bảo vệ nhánh `main`** | App chạy bằng `mvnw spring-boot:run` với `.env`; không còn secret trong repo; PR template; ≥ 3 commit |
| **Dev 2** | **D2-1 Khung frontend**: Vite + React + TypeScript + Tailwind, router, client API (gửi cookie, gắn header CSRF `X-XSRF-TOKEN`, tự làm mới token khi 401), proxy `/api` tới `localhost:8080`, ESLint/Prettier, thương hiệu WELO | `npm run dev` hiển thị khung, gọi được API mẫu; ≥ 2 commit |
| **Dev 3** | **D3-1 Máy trạng thái đơn**: enum `OrderStatus` (`PENDING_CONFIRMATION → AWAITING_PICKUP → SHIPPING → DELIVERED → COMPLETED`, `CANCELLED`), bảng chuyển hợp lệ theo vai trò, `OrderStateMachine` là nơi duy nhất đổi trạng thái; test đơn vị phủ chuyển hợp lệ, nhảy cóc, đi ngược (`FR-5.1`) | Test xanh; ≥ 2 commit |
| Cả nhóm | **ALL-1** hội thảo ER | `docs/er-notes.md` |

#### Ngày 2 (Thứ Ba) – Schema và thành phần giao diện nền

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-2 Schema V1 (Flyway)** (`FR-4, TR-C1..C3`): toàn bộ bảng theo ER (users, refresh_tokens, otp_codes, body_profile, categories, tags, products, product_variants, product_images, product_tags, carts, cart_items, orders, order_items, order_status_history, vouchers, voucher_usages); khóa ngoại ở DB, `NOT NULL`, `UNIQUE`, `CHECK`, chỉ mục, cột `created_*/updated_*/deleted_*`; `ddl-auto=validate`; sơ đồ ER (Mermaid) bản đầu trong `docs/` | DB tạo từ migration; app khởi động với `validate`; **xong trước 12:00** để hai người kia dùng được |
| **Dev 2** | **D2-2 Bố cục và thành phần cơ bản** (`TR-A1, TR-A2`): `AppShell` responsive (thanh trên, **menu hamburger** dưới 768 px, drawer), bố cục quản trị có sidebar; Button, Input, Select, Textarea, Checkbox, Badge, Modal, **ConfirmDialog**, Toast, Spinner/Skeleton, **EmptyState**, **ErrorBoundary** có nút "Thử lại" và "Về trang chủ"; mục tiêu chạm ≥ 44 px | Trang mẫu hiển thị đúng ở 1920, 768, 375 |
| **Dev 3** | **D3-2 Tồn kho**: `InventoryService` (trừ/trả tồn bằng `UPDATE … WHERE stock_quantity >= ?`, `@Version`, không âm); ánh xạ entity `Cart`, `Order`, `OrderItem`, `ProductVariant` khớp schema; **kiểm thử đồng thời** (50 luồng mua 5 sản phẩm cuối, đúng 5 thành công) (`FR-5.1`) | Test xanh |

#### Ngày 3 (Thứ Tư) – Bộ khung dùng chung

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-3 Bộ khung backend dùng chung** (`FR-2.5, 2.6, FR-3, TR-B1..B3`): lớp `Auditable` (JPA Auditing + `AuditorAware` từ người dùng hiện tại); **xóa mềm** (`deleted_at/by`, lọc mặc định, `restore`, `purge`); `GlobalExceptionHandler` trả `application/problem+json` với mã lỗi ổn định, ghi log có `requestId`; `PageResponse` có `page,size,totalElements,from,to`; bộ dựng `Specification` cho lọc và **sắp xếp nhiều cột** (`sort=a,asc&sort=b,desc`); cỡ trang chỉ nhận 10/20/50/100; tiện ích **làm sạch HTML** cho trường văn bản tự do (`TR-B2`) | Test xanh; tài liệu ngắn "cách thêm một thực thể CRUD"; **bàn giao cho Dev 2, Dev 3 cuối ngày** |
| **Dev 2** | **D2-3 DataTable và bộ form** (`FR-3, TR-A2, TR-A3`): `DataTable` (cột, sắp xếp nhiều cột, chọn cỡ trang 10/20/50/100, dòng **"Đang hiển thị x–y trong tổng số z kết quả"**, loading skeleton, empty state), `FilterPanel` (thu gọn được trên di động), tab **Thùng rác** với Khôi phục/Xóa vĩnh viễn; bộ form dùng react-hook-form + zod: **kiểm tra dòng**, vô hiệu nút khi đang gửi (**chống gửi trùng**), ánh xạ lỗi `errors[]` từ server vào từng ô; máy chủ giả (MSW) từ `openapi.yaml` | Trang demo với dữ liệu giả |
| **Dev 3** | **D3-3 Giỏ hàng và tính voucher** (`FR-5.1`): API giỏ hàng (xem, thêm, sửa, xóa; kiểm tra tồn; lọc theo người dùng từ token); `VoucherService` tính `PERCENT` (có giảm tối đa), `FIXED_AMOUNT`, `FREESHIP`, đơn tối thiểu, hiệu lực, giới hạn lượt, một voucher mỗi đơn; test đơn vị cho ma trận các trường hợp | Test xanh |

#### Ngày 4 (Thứ Năm) – Bảo mật; danh mục; voucher

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-4 Bảo mật** (`TR-B4..B8, FR-1.2..1.4`): JWT filter; access token + **refresh token xoay vòng** (lưu băm); vai trò `ADMIN/STAFF/CUSTOMER`, `SecurityFilterChain` mặc định từ chối, `@PreAuthorize`; **CSRF** bằng cookie `XSRF-TOKEN` + header cho `POST/PUT/PATCH/DELETE`; **CSP** và header bảo mật; BCrypt cost 12; **giới hạn đăng nhập sai** (trong bộ nhớ); `CurrentUser`; test 401/403/CSRF/giới hạn tần suất | Test xanh; hai người kia bật bảo mật trong nhánh của mình |
| **Dev 2** | **D2-4 Danh mục** (`FR-2.1..2.6, FR-3`): API CRUD `Category` (tự tham chiếu danh mục cha, slug, mô tả, thứ tự, hoạt động) + xóa mềm/khôi phục + validation + kiểm toán; **5 bộ lọc** (từ khóa, danh mục cha, hoạt động, có sản phẩm, khoảng ngày tạo); không cho xóa danh mục còn sản phẩm đang hoạt động (`FR-5.2`); trang quản trị Danh mục bằng `DataTable` (modal tạo/sửa, xác nhận xóa, thùng rác); API `Tag` tối thiểu | Trang chạy được với DB thật |
| **Dev 3** | **D3-4 Voucher CRUD** (`FR-2.1..2.6, FR-3, FR-5.2`): API CRUD `Voucher` (mã, loại, giá trị, giảm tối đa, đơn tối thiểu, bắt đầu, kết thúc, giới hạn lượt, đã dùng, hoạt động); **kiểm tra chéo trường** (kết thúc sau bắt đầu, % trong 0–100, giá trị không âm); xóa mềm/khôi phục; **5 bộ lọc** (mã, loại, hoạt động, khoảng hiệu lực, khoảng đơn tối thiểu); endpoint xem trước đơn (`checkout/preview`) tính tổng có voucher và phí ship cố định cấu hình | Test xanh |

#### Ngày 5 (Thứ Sáu) – Đăng nhập, sản phẩm, đặt hàng; kiểm tra giữa kỳ

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-5 Đăng ký, OTP, đăng nhập** (`FR-1.1, FR-1.2`): đăng ký (tài khoản chờ xác thực) → sinh **OTP** (6 số, hạn 5 phút, một lần, giới hạn gửi lại) gửi qua `EmailService` (SMTP qua biến môi trường; chưa cấu hình thì ghi ra console); xác thực OTP; đăng nhập (chặn chưa xác thực/khóa/đã xóa); làm mới và đăng xuất; **chọn nhà cung cấp SMTP**; dữ liệu mẫu **3 tài khoản theo vai trò** với mật khẩu băm | Test xanh; luồng chạy bằng curl/Postman |
| **Dev 2** | **D2-5 Sản phẩm và thẻ** (`FR-2.1..2.6, FR-3, FR-4.3`): API CRUD `Product` (tên, slug, mô tả, thương hiệu, chất liệu, giá gốc, khối lượng, danh mục, hoạt động) với **biến thể lồng** (size, màu, tồn kho gốc gọi `InventoryService.initStock`) và **gán thẻ (nhiều-nhiều)**; xóa mềm/khôi phục; **xóa vĩnh viễn kéo theo biến thể, ảnh, liên kết thẻ** (`FR-2.4`); **≥ 5 bộ lọc** (từ khóa đa trường, danh mục, khoảng giá, thẻ, còn hàng, hoạt động, size, màu), sắp xếp nhiều cột, phân trang; `CatalogQueryService` | Test xanh |
| **Dev 3** | **D3-5 Đặt hàng COD** (`FR-5.1`): `POST /orders` với `Idempotency-Key`; kiểm tra lại tồn và giá; **chốt giá và địa chỉ vào đơn**; trừ kho nguyên tử; áp voucher + ghi `voucher_usages`; phí ship cố định; đơn ở `PENDING_CONFIRMATION`; ghi `order_status_history`; phát sự kiện; `GET /orders` của tôi (lọc), chi tiết, hủy khi `PENDING_CONFIRMATION` kèm trả kho; test tích hợp, gồm **hai người cùng mua sản phẩm cuối** | Test xanh |
| Cả nhóm | **ALL-2 Kiểm tra giữa kỳ** (chiều) | Xem tiêu chí dưới |

**Tiêu chí kiểm tra giữa kỳ (cuối ngày 5):** schema và bộ khung dùng chung đã vào `main`; đăng ký + OTP + đăng nhập hoạt động; danh mục và sản phẩm CRUD hoạt động trên DB thật; đặt hàng COD hoạt động qua API; CI/test cục bộ xanh. **Trễ ≥ 1 ngày ở bất kỳ hạng mục nào thì áp dụng thứ tự cắt (mục 5) ngay.**

### Tuần 2

#### Ngày 6 (Thứ Hai)

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-6 Hồ sơ và giao diện đăng nhập** (`FR-1.5, TR-A3`): API xem/sửa hồ sơ (họ tên, điện thoại, ảnh đại diện nếu còn thời gian), **đổi mật khẩu** (kiểm tra mật khẩu cũ, thu hồi phiên khác); giao diện đăng nhập, đăng ký, **nhập OTP** (đếm ngược gửi lại), hồ sơ, đổi mật khẩu; bảo vệ route theo vai trò; xử lý phiên hết hạn; kiểm tra dòng | Đăng ký đến vào hồ sơ bằng giao diện |
| **Dev 2** | **D2-6 Upload và trang quản trị sản phẩm** (`FR-6.1, TR-B2`): API tải ảnh `product_images` (kiểm tra **loại bằng nội dung thật của tệp**, ≤ 2 MB, jpeg/png/webp, đặt tên an toàn), phục vụ ảnh có header cache, ảnh chính; trang quản trị Sản phẩm: `DataTable`, biểu mẫu tạo/sửa có **trình soạn biến thể**, chọn thẻ nhiều giá trị, tải ảnh có xem trước, xác nhận xóa, thùng rác, xóa vĩnh viễn có cascade | Tạo sản phẩm có ảnh từ giao diện |
| **Dev 3** | **D3-6 API xử lý đơn cho Staff/Admin** (`FR-1.4, FR-3, FR-5.1`): danh sách đơn với **5 bộ lọc** (trạng thái, khoảng ngày, khoảng tổng tiền, phương thức thanh toán, từ khóa), sắp xếp nhiều cột, phân trang; chi tiết; `POST /admin/orders/{id}/transitions` qua máy trạng thái (kiểm tra vai trò, mã vận đơn khi giao); Admin/Staff hủy trước khi xuất kho kèm trả tồn; điều chỉnh tồn kho (Staff/Admin); khách bấm "Đã nhận"; phát sự kiện đổi trạng thái | Test xanh, gồm phân quyền |

#### Ngày 7 (Thứ Ba)

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-7 Quản trị người dùng** (`FR-1.6, FR-2.*, FR-3`): API + giao diện danh sách (**5 bộ lọc**: từ khóa, vai trò, trạng thái, đã xác thực, khoảng ngày tạo), **tạo người dùng**, **vô hiệu hóa/kích hoạt**, **đổi vai trò**, xóa mềm/khôi phục, **xóa vĩnh viễn kéo theo hồ sơ, giỏ, token và đơn hàng** có xác nhận (`FR-2.4`); không cho hạ/vô hiệu hóa Admin cuối cùng (`FR-5.2`); hiển thị người tạo/sửa (`FR-2.6`) | Admin quản lý được người dùng bằng giao diện |
| **Dev 2** | **D2-7 Cửa hàng: API và danh sách** (`FR-3, TR-A1`): API công khai danh sách/tìm kiếm sản phẩm kèm thống kê (`from/to/total`); trang chủ; trang danh sách với bảng lọc (≥ 5 lọc), sắp xếp, cỡ trang, dòng thống kê, trạng thái trống/đang tải; bộ lọc dạng ngăn kéo trên di động | Duyệt sản phẩm chạy trên 3 kích thước |
| **Dev 3** | **D3-7 Giao diện giỏ hàng và thanh toán** (`TR-A2, TR-A3`): trang giỏ (tăng giảm số lượng, xóa, cảnh báo tồn), trang đặt hàng (địa chỉ, ghi chú, **nhập và áp voucher**, hiển thị tổng chi tiết), kiểm tra dòng, **chống gửi trùng** (khóa nút + `Idempotency-Key`), màn hình xác nhận | Đặt hàng COD bằng giao diện |

#### Ngày 8 (Thứ Tư)

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-8 Lập lịch, thông báo, kiểm thử bảo mật; chuẩn bị hạ tầng** (`FR-6.1`): `@Scheduled` dọn OTP và refresh token hết hạn, dọn giỏ hàng bỏ quên quá 30 ngày; **email khi đơn đổi trạng thái** (nghe sự kiện, thử lại khi lỗi, lỗi gửi không làm hỏng giao dịch); kiểm thử **IDOR** (đơn, hồ sơ, giỏ) và ma trận RBAC; **mở sớm** dự án GCP, kiểm tra thanh toán, tạo instance Cloud SQL (mất thời gian chờ) | Test xanh; Cloud SQL sẵn sàng |
| **Dev 2** | **D2-8 Chi tiết sản phẩm và dữ liệu mẫu danh mục** (`TR-C4`): trang chi tiết (thư viện ảnh, chọn màu/size, tồn kho, thẻ, nút thêm vào giỏ, trạng thái hết hàng); biểu tượng giỏ có số lượng; **script sinh dữ liệu mẫu** `scripts/gen_seed.py` → migration seed hồ sơ `demo`: ~8 danh mục, ~15 thẻ, ~40 sản phẩm tên và mô tả tiếng Việt thực tế, ~150 biến thể, ảnh placeholder sinh tự động | App khởi động hồ sơ `demo` có sẵn cửa hàng đầy đủ |
| **Dev 3** | **D3-8 Giao diện đơn hàng** (`FR-3, TR-A2`): "Đơn của tôi" (lọc, danh sách, chi tiết có dòng thời gian trạng thái, hủy, đã nhận hàng); màn hình **Staff/Admin** quản lý đơn bằng `DataTable` (≥ 5 lọc), chi tiết, nút chuyển trạng thái theo vai trò, nhập mã vận đơn, điều chỉnh tồn kho | Luồng xử lý đơn đầy đủ bằng giao diện |

#### Ngày 9 (Thứ Năm)

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-9 Triển khai lần đầu** (`DL-5, TR-B8`): `Dockerfile` đa giai đoạn (build frontend vào tài nguyên tĩnh của backend); Cloud Run (`max-instances=1`, `min-instances=1` nếu giữ cron); kết nối Cloud SQL; biến môi trường/Secret Manager; Flyway chạy khi khởi động; bật hồ sơ `demo`; **HTTPS**; thử đăng ký với OTP **email thật** | Địa chỉ `*.run.app` truy cập được từ ngoài |
| **Dev 2** | **D2-9 Kiểm thử responsive và rà giao diện** (`TR-A1..A3`): duyệt **toàn bộ màn hình ở 1920×1080, 768×1024, 375×667**, lưu ảnh chụp vào `docs/qa/`; sửa hamburger, kích thước chạm, tràn ngang; rà danh sách kiểm tra loading/empty/confirm/inline validation cho mọi màn hình; đồng bộ `openapi.yaml` với API thật | Báo cáo QA + các sửa lỗi |
| **Dev 3** | **D3-9 Voucher giao diện và dữ liệu mẫu thương mại** (`FR-2.*, TR-C4`): trang quản trị Voucher (`DataTable`, form có kiểm tra chéo trường, thùng rác); script sinh dữ liệu mẫu: ~25 người dùng, ~40 đơn kèm ~90 dòng hàng và lịch sử trạng thái, ~8 voucher; test cascade xóa người dùng/sản phẩm | Dữ liệu mẫu tổng **> 300 bản ghi**; test xanh |

#### Ngày 10 (Thứ Sáu) – Ổn định và cổng MVP

| Ai | Task | Sản phẩm bàn giao |
|---|---|---|
| **Dev 1** | **D1-10 Hoàn tất triển khai và tài liệu** (`DL-1, DL-2, DL-5, VC-2`): sửa lỗi môi trường thật, triển khai lại từ `main`, kiểm tra **3 tài khoản mẫu theo vai trò** trên URL công khai; `README.md` đầy đủ (cài đặt từng bước, chạy local, biến môi trường, tài khoản mẫu, triển khai); **xuất sơ đồ ER** (hình) vào `docs/`; rà header bảo mật; gắn tag `v0.1-mvp` | URL công khai hoạt động; README dùng được từ máy sạch |
| **Dev 2** | **D2-10 Tài liệu API và sửa lỗi** (`DL-3`): **Postman Collection** (xuất từ `openapi.yaml`) kèm biến môi trường và ví dụ cho từng vai trò; dò lỗi giao diện trên URL công khai và sửa | `docs/postman_collection.json`, `openapi.yaml` khớp thực tế |
| **Dev 3** | **D3-10 Kiểm thử luồng chính và sửa lỗi** (`FR-5`): chạy kịch bản đặt hàng → xử lý → hoàn tất trên URL công khai; **hai người cùng mua sản phẩm cuối** trên bản triển khai; sửa lỗi; rà số commit mỗi người | Kịch bản nghiệm thu đạt |
| Cả nhóm | **ALL-3 Cổng MVP** (chiều): chạy danh sách kiểm tra mục 4 | Biên bản đạt/không đạt từng dòng |

---

## 3. Tổng quan tải (ngày công)

| | Ngày 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | **Tổng** |
|---|---|---|---|---|---|---|---|---|---|---|---:|
| Dev 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **10** |
| Dev 2 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **10** |
| Dev 3 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **10** |

Đây là **100% sức chứa**, không có ngày dư và **chưa tính** hội thảo ER 2 giờ (ngày 1), họp đầu ngày, review PR của nhau, ốm đau, sửa lỗi phát sinh. Các hạng mục có rủi ro cao về độ phức tạp so với một ngày: **D1-3** (bộ khung dùng chung), **D1-4** (bảo mật), **D1-7** (quản trị người dùng gồm cả cascade), **D2-5** (sản phẩm lồng biến thể và thẻ), **D3-5** (đặt hàng), **D1-9** (triển khai lần đầu).

---

## 4. Danh sách kiểm tra cổng MVP (đối chiếu `rule.md`)

Đánh dấu đạt/không đạt trên **URL công khai**. Dòng không đạt phải có người chịu trách nhiệm và hạn sửa.

| Mã | Điều kiện | Task | Đạt? |
|---|---|---|:--:|
| FR-1.1 | Đăng ký phải qua OTP email mới kích hoạt | D1-5, D1-6 | ☐ |
| FR-1.2 | Đăng nhập/đăng xuất bằng JWT + refresh token | D1-4, D1-5 | ☐ |
| FR-1.3 | Có ≥ 3 vai trò với quyền khác nhau (Admin, Staff, Customer) | D1-4 | ☐ |
| FR-1.4 | RBAC chặn đúng API và giao diện (có test) | D1-4, D1-8 | ☐ |
| FR-1.5 | Xem/sửa hồ sơ, đổi mật khẩu | D1-6 | ☐ |
| FR-1.6 | Admin tạo, vô hiệu hóa, đổi vai trò người dùng | D1-7 | ☐ |
| FR-2.1 | CRUD đầy đủ cho `Category`, `Product`, `User`, `Voucher` | D2-4, D2-5, D1-7, D3-4 | ☐ |
| FR-2.2 | Mỗi thực thể ≥ 6–8 thuộc tính có ý nghĩa | D1-2 | ☐ |
| FR-2.3 | Validation cả client (inline) và server | D2-3, D1-3 | ☐ |
| FR-2.4 | Xóa vĩnh viễn kéo theo bản ghi phụ thuộc (có test) | D1-7, D2-5, D3-9 | ☐ |
| FR-2.5 | Xóa mềm cho mọi thực thể CRUD | D1-3 | ☐ |
| FR-2.6 | Ai tạo/sửa, lúc nào, hiển thị được | D1-3, D1-7 | ☐ |
| FR-2.7 | Thông báo lỗi thân thiện và có khôi phục (thùng rác, "Thử lại") | D2-2, D2-3 | ☐ |
| FR-3.1 | Tìm kiếm đa tiêu chí trên nhiều trường | D2-5, D2-7 | ☐ |
| FR-3.2 | ≥ 5 bộ lọc mỗi thực thể (Category, Product, User, Voucher, Order) | D2-4, D2-5, D1-7, D3-4, D3-6 | ☐ |
| FR-3.3 | Sắp xếp nhiều cột | D1-3, D2-3 | ☐ |
| FR-3.4 | Cỡ trang 10/20/50/100 | D1-3, D2-3 | ☐ |
| FR-3.5 | Dòng "Đang hiển thị x–y trong tổng số z kết quả" | D1-3, D2-3 | ☐ |
| FR-4.1 | ≥ 6–8 thực thể liên quan | D1-2 | ☐ |
| FR-4.2 | ≥ 2 quan hệ một-nhiều | D1-2 | ☐ |
| FR-4.3 | Có quan hệ nhiều-nhiều kèm bảng trung gian (`product_tags`) | D1-2, D2-5 | ☐ |
| FR-4.4 | Khóa ngoại ở cấp DB | D1-2 | ☐ |
| FR-4.5 | 3NF (ghi chú ngoại lệ có chủ ý) | D1-2, D1-10 | ☐ |
| FR-5.1 | Máy trạng thái đơn, tồn kho nguyên tử, voucher, tính tiền | D3-1, D3-2, D3-3, D3-5 | ☐ |
| FR-5.2 | Kiểm tra chéo trường và theo trạng thái DB | D3-4, D2-4, D1-7 | ☐ |
| FR-6.1 | 3 tính năng nâng cao: Upload, Thông báo email, Lập lịch | D2-6, D1-5, D1-8 | ☐ |
| TR-A1 | Responsive 3 kích thước, hamburger, chạm tốt (có ảnh chụp) | D2-2, D2-9 | ☐ |
| TR-A2 | Loading, empty, confirm cho mọi màn hình | D2-2, D2-9 | ☐ |
| TR-A3 | Inline validation, chống gửi trùng | D2-3, D3-7 | ☐ |
| TR-B1 | REST chuẩn, mã trạng thái đúng, `/api/v1/...` | D1-3 | ☐ |
| TR-B2 | Validate + làm sạch XSS + kiểm tra file tải lên | D1-3, D2-6 | ☐ |
| TR-B3 | Try-catch, lỗi có cấu trúc, log lỗi | D1-3 | ☐ |
| TR-B4 | BCrypt | D1-4 | ☐ |
| TR-B5 | Truy vấn có tham số/ORM (không nối chuỗi SQL) | D1-3 | ☐ |
| TR-B6 | CSRF token, CSP, thoát đầu ra | D1-4, D1-3 | ☐ |
| TR-B7 | JWT + refresh, kiểm tra quyền trước thao tác nhạy cảm | D1-4, D1-8 | ☐ |
| TR-B8 | HTTPS khi triển khai; giới hạn tần suất đăng nhập | D1-9, D1-4 | ☐ |
| TR-C1 | ≥ 6–8 bảng chính, ≥ 2 bảng trung gian, ≥ 50 cột | D1-2 | ☐ |
| TR-C2 | PK, FK ở DB, NOT NULL, UNIQUE, CHECK | D1-2 | ☐ |
| TR-C3 | Chỉ mục cho mọi FK và cột hay truy vấn | D1-2 | ☐ |
| TR-C4 | ≥ 100 bản ghi thực tế, script tự chạy | D2-8, D3-9 | ☐ |
| VC-1, VC-2 | `.gitignore` đúng; README từng bước | D1-1, D1-10 | ☐ |
| VC-3..5 | ≥ 50 commit, mỗi người ≥ 20, thông điệp chuẩn, nguyên tử | cả nhóm | ☐ |
| VC-6..8 | `main` được bảo vệ, nhánh riêng, PR có review | D1-1 | ☐ |
| DL-1 | Mã nguồn + script DB + seed | D1-2, D2-8, D3-9 | ☐ |
| DL-2 | Sơ đồ ER (hình) | D1-10 | ☐ |
| DL-3 | OpenAPI + Postman | D2-9, D2-10 | ☐ |
| DL-5 | URL công khai + tài khoản mẫu mỗi vai trò | D1-9, D1-10 | ☐ |
| DL-4 | Báo cáo PDF 25–30 trang | **Tuần 3–4 (NC-2), chờ cấu trúc** | – |

---

## 5. Thứ tự cắt khi trễ

Kiểm tra cuối ngày 5 và cuối ngày 8. Khi trễ, cắt từ trên xuống, chỉ cắt thứ **không làm mất dòng bắt buộc** ở mục 4:

1. Email khi đơn đổi trạng thái (giữ email OTP). Thay `FR-6.1` bằng Upload + OTP email + Lập lịch.
2. Ảnh đại diện người dùng; nhiều ảnh cho một sản phẩm (giữ một ảnh).
3. Màn hình Staff riêng: dùng màn hình đơn của Admin, ẩn nút theo vai trò.
4. Khách tự hủy đơn (Admin/Staff hủy thay).
5. Xóa vĩnh viễn kéo theo bản ghi ở `Category` và `Voucher` (giữ cho `User` và `Product`).
6. Khôi phục dữ liệu bằng nút "Thử lại" ở mọi màn hình (giữ ở `ErrorBoundary` chung).

**Không được cắt:** schema và ràng buộc DB, xóa mềm, kiểm toán, bảo mật bắt buộc (BCrypt, CSRF, CSP, JWT + refresh, giới hạn tần suất), 4 thực thể CRUD kèm bộ lọc, triển khai HTTPS và tài khoản mẫu.

Nếu cuối ngày 10 vẫn có dòng không đạt ở mục 4, các dòng đó chuyển lên đầu tuần 3 (trước NC-1) và đẩy lùi hàng đợi NC.

---

## 6. Ghi chú về các giả định

- Ngày 1 thứ Hai; 5 ngày làm việc mỗi tuần.
- Nhóm có tài khoản GCP có thanh toán trước ngày 8 (Cloud SQL, Cloud Run). Nếu không có, **dừng và báo** vì ngày 8–10 của Dev 1 phụ thuộc.
- Có SMTP dùng được cho OTP thật (chọn nhà cung cấp ngày 5).
- Cấu trúc báo cáo PDF chưa có; việc này nằm ở tuần 3–4.
- Dev 2 và Dev 3 chưa có tên, dùng nhãn.
- Mỗi người dùng email git khớp GitHub; hiện repo có 13 commit và chưa thấy commit của Dev 1.
