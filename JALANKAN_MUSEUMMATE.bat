@echo off
title MuseumMate — Museum Surabaya (Gedung Siola)
cls
echo =======================================================================
echo          MUSEUMMATE — MUSEUM SURABAYA (GEDUNG SIOLA)
echo    Sistem Manajemen Koleksi & Konservasi Cagar Budaya Berbasis JavaFX
echo =======================================================================
echo.
echo Sedang menyiapkan lingkungan JavaFX dan memeriksa koneksi basis data...
echo.

cd /d "%~dp0"

REM Jalankan aplikasi desktop MuseumMate via Maven Wrapper
call .\mvnw.cmd javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [PERINGATAN] Terjadi kendala saat menjalankan MuseumMate.
    echo Pastikan Laragon / MySQL Server Anda sudah aktif di port 3306.
    echo.
    pause
)
