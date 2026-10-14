package com.miniclone.lite;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/** Work-profile owner. No device-owner request, no background monitoring. */
public final class MiniAdminReceiver extends DeviceAdminReceiver {
    public static final String TIKTOK_PKG = "com.ss.android.ugc.tiktok.lite";

    static ComponentName component(Context ctx) {
        return new ComponentName(ctx, MiniAdminReceiver.class);
    }

    @Override public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager dpm = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm == null) return;
        ComponentName admin = component(context);
        try {
            dpm.setProfileName(admin, "Mini Clone");
            dpm.setProfileEnabled(admin);
        } catch (RuntimeException e) {
            Log.e("MiniClone", "Failed to activate work profile", e);
            return;
        }
        // Restricted on stock Android 9 when profile is not affiliated.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dpm.isAffiliatedUser()) {
            try {
                Log.i("MiniClone", "Install existing TikTok Lite: "
                        + dpm.installExistingPackage(admin, TIKTOK_PKG));
            } catch (RuntimeException e) {
                Log.w("MiniClone", "User must install TikTok Lite in the work profile", e);
            }
        }
    }
}
