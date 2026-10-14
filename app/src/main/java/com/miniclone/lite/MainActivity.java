package com.miniclone.lite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

/**
 * Android 9 / Samsung Note 8 minimalist work-profile manager.
 * Does not virtualize or alter TikTok Lite and does not require Internet permission.
 */
public final class MainActivity extends Activity {
    private static final int REQ_PROFILE = 2001;
    private static final String TIKTOK = MiniAdminReceiver.TIKTOK_PKG;
    private DevicePolicyManager dpm;
    private LauncherApps launcher;
    private LinearLayout root;
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        launcher = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
        drawScreen();
    }

    @Override public void onResume() {
        super.onResume();
        showState();
    }

    private void drawScreen() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(32), dp(22), dp(22));
        root.setBackgroundColor(Color.rgb(246, 247, 249));

        TextView heading = new TextView(this);
        heading.setText("Mini Clone Lite");
        heading.setTextSize(26);
        heading.setTextColor(Color.rgb(20, 28, 35));
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(heading);

        TextView caption = new TextView(this);
        caption.setText("Note 8 / Android 9  •  TikTok Lite 1+1");
        caption.setTextSize(14);
        caption.setTextColor(Color.DKGRAY);
        LinearLayout.LayoutParams capParams = new LinearLayout.LayoutParams(-1, -2);
        capParams.topMargin = dp(10);
        root.addView(caption, capParams);

        status = new TextView(this);
        status.setTextSize(15);
        status.setTextColor(Color.rgb(35, 51, 48));
        status.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(Color.WHITE);
        panel.setCornerRadius(dp(14));
        status.setBackground(panel);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(24);
        root.addView(status, statusParams);
        setContentView(root);
    }

    private boolean isProfileOwner() {
        return dpm != null && dpm.isProfileOwnerApp(getPackageName());
    }

    private boolean isTikTokInstalledHere() {
        return getPackageManager().getLaunchIntentForPackage(TIKTOK) != null;
    }

    private boolean hasOtherProfile() {
        if (launcher == null) return false;
        try {
            for (UserHandle user : launcher.getProfiles()) {
                if (!user.equals(Process.myUserHandle())) return true;
            }
        } catch (RuntimeException ignored) { }
        return false;
    }

    private void showState() {
        while (root.getChildCount() > 3) root.removeViewAt(3);
        if (isProfileOwner()) {
            if (isTikTokInstalledHere()) {
                status.setText("✓ Bạn đang ở hồ sơ Công việc.\n✓ TikTok Lite nhân bản đã được cài.\n✓ Dữ liệu tách riêng bản gốc.");
                button("MỞ TIKTOK LITE NHÂN BẢN", this::openTikTokHere);
            } else {
                status.setText("✓ Hồ sơ Công việc đã tạo.\n! Chưa có TikTok Lite trong hồ sơ này.\n\nHãy cài TikTok Lite bằng CH Play trong Work, hoặc dùng ADB.");
                button("CÀI TIKTOK LITE (CH PLAY WORK)", this::openStoreHere);
                button("THỬ NHÂN BẢN CÓ SẴN", this::tryInstallExisting);
            }
            button("HƯỚNG DẪN ADB", this::showAdbHelp);
        } else {
            boolean original = isTikTokInstalledHere();
            boolean profile = hasOtherProfile();
            status.setText((original ? "✓ Có TikTok Lite gốc." : "! Chưa tìm thấy TikTok Lite gốc.")
                    + "\n" + (profile ? "✓ Đã có hồ sơ phụ trên thiết bị." : "• Chưa phát hiện hồ sơ Công việc.")
                    + "\n\nBản gốc + bản Work có dữ liệu riêng. Không dùng máy ảo.");
            if (!profile) {
                button("TẠO BẢN NHÂN BẢN", this::createWorkProfile);
            } else {
                button("MỞ TIKTOK LITE NHÂN BẢN", this::openTikTokInWork);
                button("MỞ MINI CLONE TRONG WORK", this::openManagerInWork);
            }
            button("MỞ TIKTOK LITE GỐC", this::openTikTokHere);
            button("HƯỚNG DẪN", this::showAdbHelp);
        }
        button("LÀM MỚI TRẠNG THÁI", this::showState);
    }

    private void button(String text, Runnable task) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(48));
        b.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(27, 127, 108)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(12);
        root.addView(b, lp);
        b.setOnClickListener(v -> task.run());
    }

    private void createWorkProfile() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) {
            message("Chưa hỗ trợ", "ROM không báo hỗ trợ Work Profile. Có thể thử tính năng Secure Folder của Samsung.");
            return;
        }
        if (dpm == null || !dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
            message("Không thể tạo hồ sơ", "Máy đã có Work Profile hoặc bị hạn chế bởi Samsung/Knox. Không xóa hồ sơ cũ khi chưa sao lưu dữ liệu.");
            return;
        }
        Intent i = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        i.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                MiniAdminReceiver.component(this));
        try {
            startActivityForResult(i, REQ_PROFILE);
        } catch (RuntimeException ex) {
            message("Tạo không thành công", "Hệ thống từ chối trình tạo Work Profile: " + ex.getClass().getSimpleName());
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PROFILE && resultCode != RESULT_OK) {
            message("Chưa hoàn tất", "Bạn đã hủy hoặc Android/Knox không cho phép tạo Work Profile.");
        }
        showState();
    }

    private void tryInstallExisting() {
        if (!isProfileOwner()) {
            message("Chưa đúng hồ sơ", "Hãy mở Mini Clone Lite có biểu tượng cặp ở tab Công việc.");
            return;
        }
        if (!dpm.isAffiliatedUser()) {
            message("Cần bước cài riêng", "Android 9 yêu cầu hồ sơ được liên kết quản trị mới cho dùng API nhân bản trực tiếp. Chọn CH Play Work hoặc dùng ADB; đây không phải lỗi của TikTok Lite.");
            return;
        }
        try {
            boolean done = dpm.installExistingPackage(MiniAdminReceiver.component(this), TIKTOK);
            if (done) {
                showState();
                message("Đã bật bản nhân bản", "TikTok Lite có thể mở từ tab Công việc.");
            } else {
                showAdbHelp();
            }
        } catch (RuntimeException ex) {
            showAdbHelp();
        }
    }

    private void openStoreHere() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TIKTOK));
            startActivity(i);
        } catch (RuntimeException ex) {
            message("Không có CH Play Work", "Không tìm thấy cửa hàng ứng dụng trong hồ sơ này. Hãy dùng ADB để bật TikTok Lite bản thứ hai.");
        }
    }

    private void openTikTokHere() {
        Intent i = getPackageManager().getLaunchIntentForPackage(TIKTOK);
        if (i == null) {
            message("Chưa cài", "Hồ sơ hiện tại chưa có TikTok Lite. Hãy cài trước.");
            return;
        }
        try {
            startActivity(i);
        } catch (RuntimeException ex) {
            message("Không mở được", "Hãy kiểm tra TikTok Lite có bị tắt hoặc tạm dừng không.");
        }
    }

    private void openTikTokInWork() {
        openCrossProfile(TIKTOK);
    }

    private void openManagerInWork() {
        openCrossProfile(getPackageName());
    }

    private void openCrossProfile(String packageName) {
        if (launcher != null) {
            try {
                List<UserHandle> profiles = launcher.getProfiles();
                for (UserHandle user : profiles) {
                    if (user.equals(Process.myUserHandle())) continue;
                    List<LauncherActivityInfo> activities = launcher.getActivityList(packageName, user);
                    if (activities != null && !activities.isEmpty()) {
                        ComponentName component = activities.get(0).getComponentName();
                        launcher.startMainActivity(component, user, null, null);
                        return;
                    }
                }
            } catch (RuntimeException ignored) { }
        }
        message("Mở từ tab Công việc", "Mở danh sách ứng dụng, chuyển sang tab Công việc (Work) rồi chọn ứng dụng có biểu tượng cặp. Nếu chưa có TikTok Lite trong Work, cần cài bằng CH Play hoặc ADB.");
    }

    private void showAdbHelp() {
        message("Cách cài TikTok Lite 1+1", "1. Tạo hồ sơ Công việc trong ứng dụng.\n"
                + "2. Cài TikTok Lite trên hồ sơ chính.\n"
                + "3. Kết nối PC, dùng:\n\n"
                + "adb shell pm list users\n\n"
                + "adb shell cmd package install-existing --user ID " + TIKTOK + "\n\n"
                + "4. Thay ID bằng số hồ sơ công việc (thường là 10 nhưng phải kiểm tra).\n"
                + "5. Mở TikTok Lite trong tab Công việc.\n\n"
                + "Không xóa Work Profile nếu muốn giữ dữ liệu bản nhân bản.");
    }

    private void message(String heading, String contents) {
        new AlertDialog.Builder(this).setTitle(heading).setMessage(contents)
                .setPositiveButton("Đã hiểu", null).show();
    }

    private int dp(float n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }
}
