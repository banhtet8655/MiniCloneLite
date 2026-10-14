# Mini Clone Lite – v0.3 thử nghiệm Samsung Note 8 Android 9 GỐC

**Dự án nguồn để biên dịch. Chưa có APK và chưa chạy thử trên điện thoại thực tế.**

## Mục đích

Chỉ có TikTok Lite bản cá nhân và TikTok Lite thứ hai trong Android Work Profile. App không chứa cơ chế Android ảo (khác Dual Space), không cần root, không mạng, không quảng cáo, không theo dõi, không ép cập nhật. Dữ liệu ứng dụng tách biệt giữa 2 hồ sơ. Bản thứ hai **không** vượt qua bất cứ chính sách cập nhật hay đăng nhập nào của TikTok Lite.

## Tạo APK bằng GitHub Actions (không cần cài Android Studio)

1. Đăng nhập https://github.com/ và tạo repo **private** mới.
2. Giải nén toàn bộ dự án này vào máy tính, giữ nguyên thư mục `.github/workflows/` (lưu ý thư mục `.github` bị ẩn trong vài trình quản lý file).
3. Tải **toàn bộ nội dung dự án** lên repo, không tải cả file ZIP như một file duy nhất. Có thể dùng GitHub Desktop hoặc git.
4. Vào repo > **Actions** > **Build Android APK** > **Run workflow**. Nếu workflow đã được kích hoạt bởi push thì có thể chọn run tương ứng.
5. Khi workflow báo thành công, vào mục **Artifacts**, tải `MiniCloneLite-Note8-Android9-debug.zip`, giải nén lấy `app-debug.apk`.
6. APK debug đã được ký bằng khóa debug mặc định của môi trường GitHub Actions. Dùng cài thử; các lần build khác nhau có thể có chữ ký khác nhau nên không bảo đảm cài đè. Đừng dùng bản debug cho dữ liệu quan trọng.

Nếu có Android Studio: mở thư mục dự án > Sync > Build > Build APK(s) > `app/build/outputs/apk/debug/app-debug.apk`.

## Cài thử trên Note 8

1. Cài TikTok Lite bản gốc (gói `com.ss.android.ugc.tiktok.lite`).
2. Cài `app-debug.apk` mới biên dịch. Bật cho phép cài ứng dụng từ nguồn đang dùng nếu Android yêu cầu.
3. Mở Mini Clone Lite > **TẠO BẢN NHÂN BẢN** > chấp nhận các bước tạo Work Profile Android.
4. Sau khi có hồ sơ Work, mở **Mini Clone Lite** có biểu tượng cặp / Công việc.
5. Thử nút **CÀI TIKTOK LITE (CH PLAY WORK)** hoặc **THỬ NHÂN BẢN CÓ SẴN**. Android 9 thường từ chối API nhân bản trực tiếp ở Work Profile không affiliated (hạn chế Android, không phải lỗi app).
6. Nếu không cài được, nối ADB từ Windows, chạy `adb devices`, `adb shell pm list users` và xác định Work Profile ID; sau đó dùng:

```bat
adb shell cmd package install-existing --user ID com.ss.android.ugc.tiktok.lite
```

**Thay ID** bằng mã hồ sơ công việc; **không** mặc định ID=10. Nếu hệ thống không nhận `cmd package`, có thể thử `adb shell pm install-existing --user ID com.ss.android.ugc.tiktok.lite`.

7. Mở TikTok Lite có biểu tượng cặp từ tab Công việc. Đăng nhập riêng nếu cần.

## An toàn và giới hạn

- Không xóa Work Profile nếu bên trong có dữ liệu tài khoản cần giữ; xóa hồ sơ sẽ xóa dữ liệu TikTok Lite ở hồ sơ đó.
- Work Profile cần được ROM/Knox cho phép; một thiết bị thường chỉ có một managed profile, có thể xung đột với hồ sơ công việc đã tồn tại.
- Nếu bản gốc chưa được cài, `install-existing` sẽ không thể kích hoạt nó trong Work Profile.
- App không sao chép phiên đăng nhập, không sửa TikTok Lite, không bỏ yêu cầu cập nhật TikTok Lite.
- Máy Note 8 gốc vẫn phải tương thích phiên bản TikTok Lite bạn đang dùng.
- APK có thể cần quyền quản trị **trong hồ sơ Work** theo thiết kế Android. Không cấp quyền device owner hoặc root.
- Chưa thể bảo đảm app chạy cho đến khi build và thử trên thiết bị thật.

## Khi có lỗi

Gửi ảnh màn hình và kết quả 4 lệnh sau, không gửi thông tin tài khoản cá nhân:

```bat
adb shell getprop ro.build.version.release
adb shell pm list users
adb shell pm list packages --user 0 com.ss.android.ugc.tiktok.lite
adb shell dumpsys device_policy
```

## Cấu trúc

- `app/src/main/java/com/miniclone/lite/MainActivity.java`: màn hình và kiểm tra trạng thái, mở TikTok Lite ở 2 hồ sơ.
- `app/src/main/java/com/miniclone/lite/MiniAdminReceiver.java`: kích hoạt Work Profile qua Android DevicePolicyManager.
- `.github/workflows/build-apk.yml`: tự biên dịch APK bằng GitHub Actions.
- `scripts/ADB_TEST_Note8.bat`: kiểm tra hồ sơ và gói TikTok Lite qua ADB trên Windows.
