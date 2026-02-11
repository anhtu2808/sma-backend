# Plan & Subscription Model

## Tổng quan
Mô hình mới thay thế hoàn toàn phần Package/Credit cũ bằng các bảng `plans`, `plan_prices`, `usage_limits`, `usage_events`, `subscriptions`, `payment_histories`, và `features` (có `usage_type`).

## Entities/Bảng chính
1. **Plans**
   - Xác định đối tượng áp dụng (`plan_target`: `COMPANY` hoặc `CANDIDATE`)
   - Xác định loại gói (`plan_type`: `ADDONS_FEATURE`, `ADDONS_QUOTA`, `MAIN`)
   - `plan_details`: nội dung chi tiết để hiển thị "What's included" (có thể lưu text/markdown theo convention FE)
   - `is_popular`: cờ đánh dấu plan nổi bật để FE render badge "Popular"
   - Không lưu giá trực tiếp; giá nằm ở `plan_prices`

2. **Plan_prices**
   - Một plan có thể có nhiều mức giá theo thời lượng (`duration` + `unit`)
   - `unit` dùng `MONTH` hoặc `YEAR`
   - `original_price` và `sale_price` là snapshot giá tại thời điểm cấu hình

3. **Features**
   - `usage_type`:
     - `BOOLEAN`: feature bật/tắt, không cần log usage
     - `EVENT`: có log mỗi lần sử dụng
     - `STATE`: max limit giới hạn theo tổng state hiện tại (ví dụ limit upload tối đa 10 CV, thì count tất cả record hiện có trong bảng `resume` không log và event usage)

4. **Usage_limits**
   - Quota theo từng feature cho từng plan
   - `limit_unit`:
     - `TOTAL`: tổng quota không reset
     - `PER_MONTH`: reset theo chu kỳ subscription (không theo tháng dương lịch)

5. **Subscriptions**
   - Bắt buộc **chỉ một** trong `company_id` hoặc `candidate_id` được set (XOR)
   - `price` là snapshot giá đã mua
   - `start_date`/`end_date` là chu kỳ billing hiện tại
   - Status flow: `PENDING_PAYMENT` → `ACTIVE` → `EXPIRED`/`CANCELLED`

6. **Usage_events**
   - Log usage theo `subscription_id` + `feature_id`
   - Dùng để tính quota theo `Usage_limits`

7. **Payment_histories**
   - Log thanh toán cho subscription

## Business Rules
1. **Tạo plan**
   - Plan phải có `plan_target` và `plan_type`
   - Giá bán cấu hình ở `plan_prices`

2. **Mua plan**
   - Khi tạo subscription, lưu `price` từ `plan_prices` (snapshot)
   - `start_date`/`end_date` quyết định chu kỳ billing

3. **Tính usage**
   - Dựa trên `usage_events` và `usage_limits`
   - `PER_MONTH` reset theo chu kỳ subscription

4. **Data integrity**
   - FK bắt buộc cho `plan_prices`, `usage_limits`, `usage_events`, `payment_histories`
   - XOR constraint giữa `company_id` và `candidate_id`

## Ghi chú triển khai
- Mọi enum dùng UPPERCASE và map đúng Postgres enum type.
- Không sử dụng lại các bảng/enum `Package`, `Credit*` cũ.
