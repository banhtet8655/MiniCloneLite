#!/usr/bin/env python3
"""Patch a freshly checked out ISEKHON/VirtualApp to add the Mini Clone Lite single-app UI.
No third-party network requests are made by this script. It fails on unexpected upstream changes.
"""
import os
import re
from pathlib import Path

upstream = Path(os.environ.get('VIRTUALAPP_ROOT', 'upstream'))
kit = Path(__file__).resolve().parent.parent
manifest = upstream / 'app/src/main/AndroidManifest.xml'
splash = upstream / 'app/src/main/java/io/virtualapp/splash/SplashActivity.java'
appgradle = upstream / 'app/build.gradle'
new_class = upstream / 'app/src/main/java/io/virtualapp/home/MiniCloneActivity.java'

for required in (manifest, splash, appgradle):
    if not required.is_file():
        raise SystemExit(f'ERROR: missing upstream source: {required}')

src = manifest.read_text(encoding='utf-8')
assert '.home.NewHomeActivity' in src, 'Unexpected AndroidManifest.xml: NewHomeActivity not found'
assert '<application' in src and '</application>' in src, 'Unexpected AndroidManifest.xml: no application'
assert 'android:label="@string/vxp"' in src, 'Unexpected AndroidManifest.xml: label changed'
addition = '''<activity
            android:name=".home.MiniCloneActivity"
            android:exported="false"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Material.Light.NoActionBar" />
        '''
src = src.replace('<activity android:name=".home.NewHomeActivity" />', addition + '<activity android:name=".home.NewHomeActivity" />', 1)
src = src.replace('android:label="@string/vxp"', 'android:label="Mini Clone Lite"', 1)
manifest.write_text(src, encoding='utf-8')

s = splash.read_text(encoding='utf-8')
assert 'import io.virtualapp.home.NewHomeActivity;' in s, 'Unexpected SplashActivity.java import'
assert 'NewHomeActivity.goHome(this);' in s, 'Unexpected SplashActivity.java home redirect'
s = s.replace('import io.virtualapp.home.NewHomeActivity;', 'import io.virtualapp.home.MiniCloneActivity;', 1)
s = s.replace('NewHomeActivity.goHome(this);', 'startActivity(new Intent(this, MiniCloneActivity.class));', 1)
splash.write_text(s, encoding='utf-8')

s = appgradle.read_text(encoding='utf-8')
needle = 'output.outputFileName = "VirtualApp-${variant.versionName}-${abi}.apk"'
assert needle in s, 'Unexpected app/build.gradle APK output naming'
s = s.replace(needle, 'output.outputFileName = "MiniCloneLite-${variant.versionName}-${abi}.apk"', 1)
appgradle.write_text(s, encoding='utf-8')

new_class.parent.mkdir(parents=True, exist_ok=True)
new_class.write_bytes((kit / 'patches/MiniCloneActivity.java').read_bytes())
print('Patched VirtualApp: minimal Vietnamese TikTok Lite UI, original + single virtual clone.')
