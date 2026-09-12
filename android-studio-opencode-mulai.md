# Memulai Android Studio dengan OpenCode di Terminal (Fedora 44)

Panduan untuk host ini: Fedora 44 Workstation x86_64, Wayland, RAM 7.6GB.
Android Studio user-local, tanpa emulator (pakai HP fisik).

## 1. Instalasi (sudah dilakukan)

- Lokasi: `~/Applications/android-studio/`
  - Versi: Quail 3 Patch 1 (2026.1.3.8, Build AI-261.26222.65)
  - Sumber: `https://dl.google.com/dl/android/studio/ide-zips/2026.1.3.8/android-studio-quail3-patch1-linux.tar.gz`
- Launcher: `~/.local/share/applications/android-studio.desktop`
- Command: `~/.local/bin/android-studio` -> `bin/studio.sh`
- SDK: `~/Android/Sdk/` (platforms/android-37.0, build-tools/36.0.0, platform-tools/adb 37.0.1)
- OpenCode: `1.18.30` di `~/.opencode/bin/opencode`
- KVM: `/dev/kvm` tersedia. Emulator tidak diwajibkan.

Verifikasi cepat:

```bash
~/Applications/android-studio/bin/studio.sh --version
adb version
opencode --version
ls ~/Android/Sdk/platforms
```

## 2. Jalankan Android Studio Pertama Kali

```bash
android-studio
# atau via menu aplikasi: Android Studio
```

- Pilih Standard setup saat wizard (jika muncul).
- Pastikan SDK path: `~/Android/Sdk`.
- Jangan set `JAVA_HOME` ke OpenJDK 25 sistem. Biarkan Studio memakai JBR bawaannya.

## 3. Buat Project Pertama (Empty Activity)

1. `New Project > Phone and Tablet`
2. Pilih template:
   - `Empty Activity` = Jetpack Compose (default baru)
   - `Empty Views Activity` = XML klasik (pilih ini jika ikut tutorial XML lama)
3. Isi:
   - Name: `MyFirstApp`
   - Package: `com.example.myfirstapp`
   - Save location: `~/projects/MyFirstApp`
   - Language: Kotlin
   - Build config: Kotlin DSL (`build.gradle.kts`)
   - Min SDK: 26
4. `Finish` -> tunggu Gradle sync (download wrapper ~200MB, jangan tutup).

Struktur penting:

```
app/src/main/java/.../MainActivity.kt
app/src/main/AndroidManifest.xml
app/build.gradle.kts
settings.gradle.kts
```

## 4. Terminal Bawaan Android Studio

Buka: `View > Tool Windows > Terminal`. Terminal otomatis di root project.

```bash
pwd
./gradlew assembleDebug
adb devices
```

Build manual tanpa klik Run:

```bash
./gradlew installDebug
```

## 5. Jalankan di HP Fisik (tanpa Emulator)

1. HP: aktifkan Developer Options -> USB Debugging.
2. Colok USB, setuju dialog RSA fingerprint.
3. Cek:

```bash
adb devices
# harus: <id>  device
```

4. Di Studio klik `Run ▶`, pilih device fisik.
5. Alternatif Wi-Fi debugging: `Settings > System > Developer > Wireless debugging > Pair`, lalu `adb pair <ip:port> <code>`.

> Emulator opsional. Tambah nanti via `Device Manager > Create Virtual Device` jika butuh. Hemat ~10-15GB dan RAM dengan HP fisik.

## 6. Pakai OpenCode dari Terminal yang Sama

Di terminal bawaan Studio (root project):

```bash
opencode
```

Perintah awal yang aman:

```
jelaskan struktur project ini
tambahkan tombol ke MainActivity yang tampilkan Toast "Halo"
cek ./gradlew assembleDebug dan perbaiki error jika ada
```

Tips:

- Beri izin hanya untuk `./gradlew` dan `adb` saat OpenCode meminta approval.
- Jangan minta auto-commit. Review diff di Studio (`Commit > Diff`) dulu.
- Contoh `opencode.jsonc` minimal untuk Android:

```jsonc
{
  "permission": {
    "bash": {
      "./gradlew *": "allow",
      "adb *": "allow"
    }
  }
}
```

## 7. Troubleshooting Ringkas

| Gejala | Solusi |
|---|---|
| `JAVA_HOME` error / Gradle butuh Java 17/21 | unset `JAVA_HOME`, pakai JBR Studio |
| `adb devices` -> `no permissions` / `unauthorized` | cabut-colok USB, setujui ulang di HP; cek `lsusb`; udev rules hanya jika perlu |
| Gradle sync lambat / OOM (RAM 7.6GB) | tutup browser berat, di `gradle.properties`: `org.gradle.jvmargs=-Xmx2g` |
| Studio blank di Wayland | jalankan ulang via `android-studio`, jangan paksa `GDK_BACKEND=x11` kecuali perlu |
| Arsip 1.5GB penuh | hapus `~/Downloads/android-studio-quail3-patch1-linux.tar.gz` setelah stabil |

## 8. Perintah Harian

```bash
android-studio            # buka IDE
cd ~/projects/MyFirstApp  # pindah project
opencode                  # asisten coding
./gradlew assembleDebug   # build APK debug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat | grep -i "myfirstapp\|AndroidRuntime"
```
