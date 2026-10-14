@echo off
setlocal EnableExtensions
chcp 65001 >nul
where adb >nul 2>nul
if errorlevel 1 (
 echo ERROR: Khong tim thay adb.exe. Hay them platform-tools vao PATH.
 pause
 exit /b 1
)
echo === Danh sach dien thoai ket noi ===
adb devices
set /p "SERIAL=Nhap SERIAL Note8 muon test (bat buoc): "
if not defined SERIAL (
 echo Chua chon may. Khong chay lenh nao.
 pause
 exit /b 1
)
echo.
echo === Kiem tra Note 8 ===
adb -s "%SERIAL%" shell getprop ro.product.model
if errorlevel 1 goto error
echo === Android ===
adb -s "%SERIAL%" shell getprop ro.build.version.release
if errorlevel 1 goto error
echo === User profiles ===
adb -s "%SERIAL%" shell pm list users
if errorlevel 1 goto error
echo === TikTok Lite tren user 0 ===
adb -s "%SERIAL%" shell pm list packages --user 0 com.ss.android.ugc.tiktok.lite
if errorlevel 1 goto error
echo.
echo Chi tiep tuc sau khi da tao Work Profile tren dien thoai.
set /p "PROFILE=Nhap Work Profile ID (Enter bo qua): "
if not defined PROFILE goto end
if "%PROFILE%"=="0" (
 echo Work Profile ID phai khac 0. Da huy.
 goto end
)
echo Dang bat TikTok Lite tren Work Profile %PROFILE%...
adb -s "%SERIAL%" shell cmd package install-existing --user %PROFILE% com.ss.android.ugc.tiktok.lite
if errorlevel 1 goto error
echo === Kiem tra goi trong Work Profile ===
adb -s "%SERIAL%" shell pm list packages --user %PROFILE% com.ss.android.ugc.tiktok.lite
goto end
:error
echo ERROR: Lenh ADB that bai. Chup lai man hinh va gui ket qua de kiem tra.
:end
echo.
pause
endlocal
