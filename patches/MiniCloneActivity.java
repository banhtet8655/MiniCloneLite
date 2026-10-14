package io.virtualapp.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.sys.Installd;
import java.io.File;
import java.util.ArrayList;

/** A simple single-clone shell. The virtual engine belongs to the upstream VirtualApp project. */
public final class MiniCloneActivity extends Activity {
    private static final String TIKTOK = "com.ss.android.ugc.tiktok.lite";
    private TextView status;
    private Button cloneButton;
    private Button openButton;
    private Button deleteButton;

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private void addGap(LinearLayout panel, int height) {
        View gap = new View(this);
        panel.addView(gap, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private Button button(LinearLayout panel, String label, View.OnClickListener callback) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setOnClickListener(callback);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56));
        p.bottomMargin = dp(10);
        panel.addView(b, p);
        return b;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView screen = new ScrollView(this);
        screen.setFillViewport(true);
        screen.setBackgroundColor(Color.rgb(246, 248, 251));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(45), dp(22), dp(24));
        panel.setGravity(Gravity.TOP);
        screen.addView(panel);

        TextView heading = text("Mini Clone Lite", 28, Color.rgb(26, 40, 60));
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panel.addView(heading);
        addGap(panel, 8);
        panel.addView(text("Samsung Note 8 · Android 9 · TikTok Lite 1+1", 14, Color.DKGRAY));
        addGap(panel, 30);
        status = text("Đang kiểm tra ...", 17, Color.rgb(24, 72, 95));
        panel.addView(status);
        addGap(panel, 24);
        cloneButton = button(panel, "Tạo bản TikTok Lite nhân bản", v -> createClone());
        openButton = button(panel, "Mở TikTok Lite nhân bản", v -> launchClone());
        button(panel, "Mở TikTok Lite gốc", v -> launchOriginal());
        deleteButton = button(panel, "Xóa bản nhân bản", v -> confirmDelete());
        addGap(panel, 12);
        panel.addView(text("Hai bản dùng vùng dữ liệu riêng. Bản nhân bản không được bảo đảm tương thích với mọi bản TikTok Lite. Không cần Work Profile hoặc root.", 13, Color.GRAY));
        setContentView(screen);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) refresh();
    }

    private boolean originalExists() {
        try {
            getPackageManager().getApplicationInfo(TIKTOK, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean cloned() {
        try {
            return VirtualCore.get().isAppInstalled(TIKTOK);
        } catch (Throwable e) {
            return false;
        }
    }

    private void refresh() {
        boolean original = originalExists();
        boolean virtual = cloned();
        status.setText("TikTok Lite gốc: " + (original ? "Đã cài" : "Chưa cài")
                + "\nTikTok Lite nhân bản: " + (virtual ? "Đã tạo" : "Chưa tạo"));
        cloneButton.setEnabled(original && !virtual);
        openButton.setEnabled(virtual);
        deleteButton.setEnabled(virtual);
    }

    private void createClone() {
        if (cloned()) {
            Toast.makeText(this, "Đã có một bản nhân bản", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(TIKTOK, 0);
            String apk = info.sourceDir;
            // Split APKs are commonly shipped as a directory; upstream handles the package path.
            if (info.splitSourceDirs != null && info.splitSourceDirs.length > 0) {
                File parent = new File(info.sourceDir).getParentFile();
                if (parent != null) apk = parent.getAbsolutePath();
            }
            ArrayList<AppInfoLite> selected = new ArrayList<>();
            selected.add(new AppInfoLite(TIKTOK, apk, false, false));
            Installd.startInstallerActivity(this, selected);
            Toast.makeText(this, "Xác nhận cài TikTok Lite ở màn hình kế tiếp", Toast.LENGTH_LONG).show();
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Hãy cài TikTok Lite gốc trước", Toast.LENGTH_LONG).show();
        } catch (Throwable e) {
            Toast.makeText(this, "Lỗi tạo bản nhân bản: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void launchClone() {
        if (!cloned()) return;
        try {
            if (!LoadingActivity.launch(this, TIKTOK, 0)) {
                Toast.makeText(this, "Không mở được bản nhân bản", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            Toast.makeText(this, "Lỗi mở bản nhân bản: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void launchOriginal() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(TIKTOK);
        if (intent == null) {
            Toast.makeText(this, "Chưa cài TikTok Lite gốc", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private void confirmDelete() {
        if (!cloned()) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa TikTok Lite nhân bản?")
                .setMessage("Dữ liệu của bản nhân bản sẽ mất. TikTok Lite gốc không bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try {
                        VirtualCore.get().uninstallPackage(TIKTOK);
                        Toast.makeText(this, "Đã yêu cầu xóa bản nhân bản", Toast.LENGTH_SHORT).show();
                        refresh();
                    } catch (Throwable e) {
                        Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
