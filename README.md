# 📄 README – Dự án JobHub

## 🔍 Giới thiệu

**JobHub** là một ứng dụng di động được phát triển nhằm kết nối hiệu quả giữa **người tìm việc** và **nhà tuyển dụng**.  
Ứng dụng giúp người lao động tìm kiếm, lưu trữ và ứng tuyển vào các vị trí phù hợp với năng lực, đồng thời hỗ trợ doanh nghiệp trong việc đăng tin và quản lý ứng viên một cách nhanh chóng, tiện lợi.

---

## 🧩 Các chức năng chính

### 👤 Dành cho người tìm việc:
- Đăng ký tài khoản bằng Email (OTP) hoặc qua Google/Facebook.
- Đăng nhập, cập nhật và quản lý thông tin cá nhân.
- Tìm kiếm công việc theo từ khóa, vị trí, ngành nghề, lương,...
- Xem chi tiết tin tuyển dụng và thông tin nhà tuyển dụng.
- Ứng tuyển công việc với CV và thư ứng tuyển.
- Lưu công việc yêu thích để xem lại sau.
- Nhận thông báo từ hệ thống về các tin tức liên quan.

### 🏢 Dành cho nhà tuyển dụng:
- Tạo và quản lý tin tuyển dụng.
- Duyệt danh sách ứng viên ứng tuyển theo từng vị trí.
- Gửi lời mời phỏng vấn và phản hồi đến ứng viên.
- Thêm công ty mới và công việc mới.

---

## 🚀 Công nghệ sử dụng

- **Android SDK (Kotlin)** – Xây dựng ứng dụng di động.
- **Spring Boot (Java)** – Xây dựng hệ thống backend API RESTful.
- **MySQL** – Hệ quản trị cơ sở dữ liệu quan hệ.
- **Figma** – Thiết kế giao diện người dùng.
- **GitHub + TortoiseGit** – Quản lý mã nguồn và làm việc nhóm.
- **Google Sign-In SDK** – Tích hợp đăng nhập bằng Google.
- **Facebook SDK** – Tích hợp đăng nhập bằng Facebook.

---

## ⚙️ Cài đặt và chạy ứng dụng

### ✅ Yêu cầu hệ thống
- Android Studio (2022 trở lên)
- JDK 17
- MySQL Server
- Thiết bị Android thật hoặc Android Virtual Device (AVD)

### 🖥️ Cài đặt backend (Spring Boot)
1. Mở thư mục `BE-FindingJob` bằng IntelliJ hoặc Eclipse.
2. Chỉnh sửa file `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/jobhub
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```
3. Chạy lớp `JobhubApplication.java` để khởi động API backend.

### 📱 Cài đặt frontend (Android)
1. Mở thư mục `JobHub` bằng Android Studio.
2. Kết nối thiết bị Android hoặc khởi động AVD.
3. Đồng bộ Gradle và nhấn “Run” để khởi chạy ứng dụng.

> **Lưu ý**: Thiết bị và backend cần cùng mạng LAN hoặc cấu hình IP nội bộ chính xác.

---

## 🤝 Đóng góp

Dự án được thực hiện trong khuôn khổ môn học **Lập trình di động** –  
Trường Đại học Sư phạm Kỹ thuật TP.HCM – Học kỳ II, năm học 2024–2025.

**Nhóm 29**
- Tô Hữu Đức  
- Đỗ Văn Thường

---

## 📫 Giấy phép

Dự án này được phân phối theo [Giấy phép MIT](https://opensource.org/licenses/MIT).
