# Keep DeviceAdminReceiver and activities reachable through Android manifests.
-keep class com.miniclone.lite.MiniAdminReceiver { *; }
-keep class com.miniclone.lite.ProvisioningActivity { *; }
