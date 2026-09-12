# Dokumentasi Sesi — MyFirstAndroid (Compose + GitHub Releases)

> Dokumen ini merangkum seluruh sesi chat agar bisa dibaca **manusia** sebagai
> dokumentasi dan **agent AI** sebagai konteks lanjutan. Bahasa: Indonesia.

## 0. Konteks Mesin (untuk AI agent)

```yaml
project_dir: /home/ownhp/AndroidStudioProjects/myfirstandroid
repo: https://github.com/tkjtani/my-first-android
repo_visibility: public  # awalnya private, diubah public agar Release bisa diunduh anonim
branch: main
package: com.example.my_first_android
app_id: com.example.my_first_android
template: "Empty Activity" (Jetpack Compose, bukan XML)
language: Kotlin
build: Kotlin DSL (build.gradle.kts)
minSdk: 26
targetSdk: 37
compileSdk: 37
versionCode: 4
versionName: "1.3"
agp: "9.3.2"
kotlin: "2.2.10"
composeBom: "2026.02.01"
key_deps:
  - androidx.navigation:navigation-compose:2.9.3
  - org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0
  - androidx.compose.material:material-icons-core (ikon TopAppBar)
host: Fedora 44 Workstation x86_64, Wayland, RAM 7.6GB
sdk: /home/ownhp/Android/Sdk (platforms/android-37.0, build-tools/36.0.0, platform-tools/adb 37.0.1)
studio: ~/Applications/android-studio/ (Quail 3 Patch 1)
jbr_java: ~/Applications/android-studio/jbr/bin/java (OpenJDK 25, bawaan Studio)
device: HP fisik via USB debugging (tanpa emulator)
gradle_memory: org.gradle.jvmargs=-Xmx2048m
keystore: /home/ownhp/my-release-key.jks (PKCS12, alias=mykey, 10000 hari, chmod 600, DI LUAR repo)
secrets: local.properties (gitignored, chmod 600) berisi RELEASE_STORE_FILE/PASSWORD/ALIAS/KEY_PASSWORD
release_tag: v1.3 (latest; riwayat: v1.0 → v1.1 redesain → v1.2 download-dalam-app → v1.3 uji nyata)
commits:
  - 8b9677b "Initial commit: Compose + nav 2 screen"
  - 9431e6b "Tambah cek update via GitHub Releases + notifikasi in-app"
  - b41233b "Fix signing: baca keystore dari local.properties, samakan password PKCS12"
  - c8b5b0d "Redesain profile/followers/privacy (DESIGN.md) + v1.1 untuk uji update"
  - 6d131ec "Download APK dalam app + kartu Tentang dummy (v1.2)"
  - 71173c7 "Penanda dummy v1.3 (kartu Yang baru + Tentang v1.3)"
```

## 1. Ringkasan Sesi (kronologis)

1. **Project pertama dibuat di Android Studio** — diverifikasi: template Compose,
   `MainActivity.kt`, `minSdk 26`, SDK 37, `gradle.properties -Xmx2g`. `adb`
   hanya tersedia via full path `~/Android/Sdk/platform-tools/adb`.
2. **"Hello Android" sukses** — setup SDK + Gradle + HP fisik beres.
3. **Tombol Toast** — `Greeting()` diubah jadi `Column` tengah + `Button`
   `Toast.makeText(context, "Halo", ...)`. Build `assembleDebug` sukses (~22 dtk).
4. **Navigasi antar screen** — tambah `navigation-compose:2.9.3`, `AppNav()` +
   `NavHost`: `home` → `HomeScreen`, `detail/{name}` → `DetailScreen`
   (`navArgument`, `popBackStack`). Build sukses (~1 mnt 24 dtk).
5. **Sync ke GitHub** — `git init`, stage, commit gagal dulu karena
   `Author identity unknown` → set `user.name/user.email`. Push HTTPS gagal
   (`Password authentication is not supported`) → pakai PAT sebagai password.
   Sempat 404 karena repo belum ada/salah nama. Akhirnya push sukses ke
   `tkjtani/my-first-android`.
6. **Cek `git status`** — stage sudah benar (`local.properties`, `build/`,
   `.gradle/` ter-ignore). Disarankan `git branch -M main`.
7. **Commit + push pakai token tempel di chat** — BERHASIL tapi token terekspos;
   user diminta **revoke segera**. (Detail di §8 Keamanan.)
8. **Push dengan kredensial tersimpan** — diset `credential.helper store`
   + PAT baru; verifikasi `~/.git-credentials` (600) dan
   `git ls-remote origin HEAD` cocok `9431e6b`.
9. **APK instalasi** — `./gradlew assembleDebug` → `app-debug.apk` (12MB);
   install via `adb install -r` atau salin file ke HP (unknown sources).
   Debug cukup untuk pribadi; rilis butuh signed.
10. **GitHub Releases untuk APK signed** — dipasang template `signingConfigs.release`
    baca `local.properties`/env dengan fallback unsigned saat dev.
11. **Distribusi & update** — sideload manual (tiap update = download ulang);
    opsi: Play Store (otomatis), Firebase App Distribution (limit!), atau
    **hybrid GitHub** (dipilih).
12. **Limit Firebase untuk 1500 siswa** — default 500 tester/project, 200/grup,
    200/distribusi, 1000 rilis/app, rilis kedaluwarsa 150 hari. Butuh request
    limit increase + onboarding Google — tidak praktis → tetap hybrid GitHub.
13. **Fitur hybrid: notifikasi + tombol update in-app** — diimplementasikan
    (§4). Perlu `INTERNET`, `POST_NOTIFICATIONS`, `kotlinx-coroutines-android`.
14. **Langkah 1 rilis: keystore + signed APK** — keystore dibuat ulang 2x:
    (a) password store≠key gagal di PKCS12 (`Given final block not properly
    padded`); (b) fix loader `local.properties` (Gradle tidak baca otomatis)
    + samakan password store=key. Hasil: `app-release.apk` 7.8MB, `apksigner`
    OK (`CN=MyFirstAndroid`). Commit+push `b41233b` via kredensial tersimpan.
15. **Upload Release** — user upload `app-release.apk` ke Release `v1.0`;
    repo masih private → download butuh login.
16. **Repo dijadikan public** — diverifikasi: repo Public, API
    `releases/latest` balas `v1.0` + asset `app-release.apk` (8.084.755 bytes,
    sha256 `564f61fd…`), link download anonim 302 OK.
17. **Setiap rilis baru = upload ulang APK** — ya; alur bump versi → build →
    upload dijelaskan; ditawari GitHub Actions otomatis (belum dikerjakan).
18. **Dokumen ini** — dibuat atas permintaan user.
19. **Redesain UI v1.1 (DESIGN.md)** — baca `DESIGN.md` dulu (wajib AGENTS.md);
    rute `profile` → `profile/followers` + `profile/privacy`; komponen reusable
    (`ProfileHeader`, `FollowerRow`, `PrivacySettingRow`, `UpdateCard`);
    state `rememberSaveable` + Saver; tambah `material-icons-core`;
    `versionCode 2`/`"1.1"`; debug+release ✓; push `c8b5b0d`.
20. **Rilis via CLI** — tag bisa `git tag + push` (kredensial tersimpan cukup);
    Release + asset butuh `gh` (`sudo dnf install gh`, `gh auth login` sekali).
    Tag `v1.1` di-push via git, Release dibuat via web; setelah `gh` login,
    Release `v1.2`/`v1.3` penuh via `gh release create`.
21. **Download dalam app (v1.2)** — `REQUEST_INSTALL_PACKAGES` + FileProvider +
    DownloadManager + progress + Install Sekarang; kartu Tentang dummy;
    `versionCode 3`/`"1.2"`; commit `6d131ec`; Release v1.2 via `gh`.
    Temuan uji: HP yang masih v1.0/v1.1 tetap buka browser (wajar — kode baru
    baru ada di v1.2; butuh 1x update browser sebagai bootstrap).
22. **Penanda dummy v1.3** — kartu "Yang baru di v1.3" + judul Tentang v1.3;
    `versionCode 4`; commit `71173c7`; Release v1.3 via `gh`; dipakai menguji
    alur download-dalam-app dari v1.2. Hasil: jalan, tapi verifikasi Play
    Protect + izin unknown-apps tetap wajib (proteksi OS, tak bisa bypass).
23. **Dokumentasi fitur download (§11)** — dibuat + digabung ke dokumen ini.

## 2. Struktur Project Akhir

```
myfirstandroid/
├── android-studio-opencode-mulai.md   # panduan awal (lokal)
├── DOKUMENTASI-SESI.md                # file ini
├── app/
│   ├── build.gradle.kts               # signing release + deps nav/coroutines/icons
│   └── src/main/
│       ├── AndroidManifest.xml        # INTERNET + POST_NOTIFICATIONS + REQUEST_INSTALL_PACKAGES + FileProvider
│       ├── res/xml/file_paths.xml     # jalur FileProvider (Download privat app)
│       ├── java/.../MainActivity.kt   # AppNav (profile/…) + ProfileRoute (cek+download+install)
│       ├── java/.../ProfileScreens.kt # model, komponen reusable, 3 layar, preview
│       └── java/.../UpdateChecker.kt  # fetch releases/latest, notifikasi, downloader, installer
├── gradle/libs.versions.toml
├── gradle.properties                  # -Xmx2048m, configuration-cache
├── local.properties                   # GITIGNORED: sdk.dir + RELEASE_* secrets
└── .gitignore                         # local.properties, /build, .gradle, dsb.
```

## 3. Perubahan Kode Penting

### 3.1 Tombol Toast (`MainActivity.kt`)

`Greeting()` → `Column(fillMaxSize, Center)` berisi `Text("Hello …")`,
`Spacer(16.dp)`, `Button(onClick = Toast "Halo")`.

### 3.2 Navigasi (`MainActivity.kt`, `app/build.gradle.kts`)

- Dep: `androidx.navigation:navigation-compose:2.9.3`.
- `AppNav()` + `rememberNavController()` + `NavHost(startDestination="home")`.
- `composable("home")`, `composable("detail/{name}", navArgument String)`.
- `navigate("detail/Android")`, `popBackStack()`.

### 3.3 Signing release (`app/build.gradle.kts`)

```kotlin
import java.io.FileInputStream
import java.util.Properties
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) FileInputStream(f).use { fis -> load(fis) }
}
fun releaseProp(name: String): String? =
    (project.findProperty(name) as String?)
        ?: localProps.getProperty(name)
        ?: System.getenv(name)
```

`signingConfigs.create("release")` membaca 4 properti; `buildTypes.release`
memakai signing itu hanya jika `RELEASE_STORE_FILE` ada (fallback unsigned).

> Pelajaran: (1) `local.properties` TIDAK otomatis jadi project property —
> harus di-load manual. (2) Keystore PKCS12 (default JDK 9+) wajib password
> store == key; beda password → `KeytoolException: Given final block not
> properly padded`. (3) Kotlin DSL butuh `import` eksplisit di atas `plugins`.

### 3.4 Hybrid update (`UpdateChecker.kt` + `MainActivity.kt` + Manifest)

> Alur lama (v1.0–v1.1, layar Home/detail — sudah diganti redesain §3.5).

- `GITHUB_OWNER = "tkjtani"`, `GITHUB_REPO = "my-first-android"`.
- `fetchLatestRelease()` (coroutine IO, `HttpURLConnection`, `org.json`):
  GET `api.github.com/.../releases/latest` → `tag/name/body/html_url` +
  cari asset `.apk` → `apkUrl`.
- `normalizeTag()` hapus prefix `v`; `isUpdateAvailable()` = beda string.
- `showUpdateNotification()`: channel `update_channel`, `PendingIntent`
  ke `apkUrl ?: htmlUrl`; minta `POST_NOTIFICATIONS` (Android 13+) via
  `rememberLauncherForActivityResult`.
- Perilaku: versi == tag → "Sudah versi terbaru"; tag baru → status + kartu +
  notifikasi. Tanpa Release → error 404 yang ditangani ("buat Release dulu").

### 3.5 Redesain v1.1 (DESIGN.md: profil Bing iOS → Material 3 Android)

- Rute Navigation Compose: `profile` (start) → `profile/followers`,
  `profile/privacy`; kembali via `popBackStack()`. Demo Toast/layar detail lama dihapus.
- `ProfileScreens.kt`: model sesuai DESIGN.md + komponen reusable dengan state
  hoisting (`ProfileHeader`, `ProfileStat`, `FollowerRow`, `PrivacySettingRow`,
  `UpdateCard`); layar `ProfileScreen`/`FollowersScreen`/`PrivacyScreen`.
- State di `AppNav` via `rememberSaveable` + Saver kustom (tahan rotasi tanpa
  ViewModel/dep baru); kartu Cek Update dipindah ke Profile (`ProfileRoute`).
- Aturan DESIGN.md: TopAppBar + divider (tanpa nested card), spacing grid 4dp,
  tipografi semantik, `navigationBarsPadding`, touch target min 48dp,
  content description ikon aksi, LazyColumn followers, Switch privacy,
  dark theme (ikut tema app), 4 preview (termasuk dark + state update).
- Avatar = inisial huruf (placeholder milik project; tanpa URL Figma sementara).
- Dep baru: `androidx.compose.material:material-icons-core` (ikon resmi Material
  untuk back + settings; sesuai AGENTS.md).

### 3.6 Download dalam app v1.2 + dummy v1.3 (detail penuh di §11)

- v1.2: `UpdateCard` dapat state download (Idle → Mengunduh % → Install Sekarang
  → Gagal); `AboutCard` dummy; `versionCode 3`.
- v1.3: `DummyV13Card` ("Yang baru di v1.3") + judul Tentang v1.3;
  `versionCode 4`. Dipakai membuktikan alur v1.2→v1.3 tanpa browser.

## 4. Build & Verifikasi

```bash
./gradlew assembleDebug     # ±10-50 dtk → app/build/outputs/apk/debug/app-debug.apk (12MB)
./gradlew assembleRelease   # ±1-4 mnt
ls -lh app/build/outputs/apk/release/   # target: app-release.apk (signed 7.8MB)
~/Android/Sdk/build-tools/36.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk | head -6
# → Signer #1 certificate DN: CN=MyFirstAndroid, OU=School, O=tkjtani, C=ID
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 5. Git & GitHub

```bash
git init
git config --global user.name "Nama Kamu"
git config --global user.email "email@contoh.com"
git add -p
git commit -m "pesan jelas"
git branch -M main
git remote add origin https://github.com/tkjtani/my-first-android.git
git push -u origin main
git status -sb; git log --oneline -5; git pull --rebase
```

- Auth HTTPS wajib **PAT** (password biasa ditolak). Opsi: `credential.helper
  store` (dipakai, `~/.git-credentials` 600), `libsecret` (butuh
  `sudo dnf install git-credential-libsecret`), SSH, atau `gh auth login`.
- Hapus kredensial salah: `printf "host=github.com\nprotocol=https" | git credential reject`.
- Jika `git status` tunjuk `[ahead 1]` padahal push via URL eksplisit sukses,
  sinkronkan ref tracking: `git update-ref refs/remotes/origin/main refs/heads/main`
  (hanya jika yakin push masuk; idealnya `git fetch` dengan kredensial valid).

## 6. Rilis & Distribusi (dipakai)

### 6.1 Membuat rilis

```bash
# 1. Naikkan versi di app/build.gradle.kts: versionCode + versionName
# 2. Build + verifikasi signature:
./gradlew assembleRelease
# 3. Commit + push + tag (kredensial tersimpan cukup):
git add -p && git commit -m "..." && git push
git tag vX.Y && git push origin vX.Y
# 4. Release + upload (butuh gh login sekali: sudo dnf install gh && gh auth login):
gh release create vX.Y app/build/outputs/apk/release/app-release.apk \
  --title "vX.Y" --notes "..."
# Alternatif langkah 4 via web: Releases → Create → pilih tag → upload APK → Publish
```

### 6.2 Download & install (user akhir)

**Alur browser (semua versi bisa):**
- Link: `github.com/tkjtani/my-first-android/releases/latest` → Assets →
  `app-release.apk` → ketuk → izinkan `Install unknown apps` → Install.
  Play Protect "Unknown app" → `More details` → `Install anyway` (wajar).
- Repo **private** = wajib login + akses; **public** = anonim bisa (dipilih public).
- Update = timpa install; data aman selama applicationId + keystore sama.
- Gagal umum: `Parse error` (minSdk 26 = Android 8+, file korup, konflik
  signature debug vs release), `Blocked` (izin per-app browser), storage.

**Alur dalam app (butuh v1.2+ terinstall):**
Profile → Cek Update → **Download Update** (progress % di app, tanpa browser) →
**Install Sekarang** → (sekali saja: aktifkan unknown-apps untuk app ini) →
setujui verifikasi Play Protect → selesai.
Catatan bootstrap: HP yang masih v1.0/v1.1 belum punya kode ini → wajib 1x
update via browser ke v1.2 dulu.

### 6.3 Pilihan distribusi (keputusan sesi)

| Jalur | Update | Cocok untuk |
|---|---|---|
| GitHub Releases sideload | manual | Coat awal / 1500 siswa tanpa akun |
| In-app "Cek Update" → Releases | semi-otomatis (tetap klik Install) | **dipilih (hybrid)** |
| Firebase App Distribution | semi-otomatis, butuh Google login | ≤500 tester default (1500 perlu limit increase) |
| Play Store (Internal/Closed/Production) | otomatis penuh | jangka panjang 1500 siswa (butuh akun dev $25) |

Limit Firebase (docs resmi, dicek 2026): 500 tester/project, 200/grup,
200/distribusi, 1000 rilis/app, rilis 150 hari, undangan 30 hari, upload 2GB.

## 7. Status Akhir & TODO

**Selesai:** Toast ✓ (lalu diganti redesain), nav awal ✓, repo public +
Release `v1.0` ✓, updater + notifikasi ✓, kredensial tersimpan ✓, dokumen ini ✓,
redesain profile v1.1 ✓, Release CLI (tag git + `gh`) v1.1–v1.3 ✓,
download-dalam-app v1.2 ✓, uji nyata v1.2→v1.3 ✓ (tetap wajib verifikasi
Play Protect + izin unknown-apps — proteksi OS).

**TODO yang ditawarkan (belum dikerjakan):**
- [x] Redesain profile/followers/privacy (DESIGN.md) v1.1 — commit `c8b5b0d`,
  `versionCode 2`/`versionName "1.1"`, release APK signed 7.9MB. Release v1.1 jadi.
- [x] Test Cek Update berBrowser (v1.0→v1.1) — terganti uji lebih baik v1.2→v1.3.
- [x] Tombol update download+install langsung — jadi di v1.2 (`6d131ec`).
- [ ] Workflow GitHub Actions: push tag `v*` → build signed → upload ke Release otomatis.
- [ ] Background check berkala (`WorkManager`) + notifikasi proaktif.
- [ ] Jalur Play Store (Internal/Closed Testing) untuk 1500 siswa.
- [ ] Backup `~/my-release-key.jks` + password ke brankas (wajib sebelum laptop ganti).

## 8. Keamanan (penting)

- PAT sempat ditempel di chat → **sudah diminta revoke**; buat baru (scope
  `repo`, expiry pendek). Jangan tempel secret di chat/history.
- `local.properties` + `*.jks` tidak boleh masuk git (sudah di-ignore; keystore
  di luar repo). Izin file 600 terverifikasi.
- Jadikan repo public = kode ikut publik; jika ada secret di histori, putar
  ulang secret-nya.

## 9. Perintah Harian (cheat sheet)

```bash
cd /home/ownhp/AndroidStudioProjects/myfirstandroid
./gradlew assembleDebug
./gradlew assembleRelease
~/Android/Sdk/platform-tools/adb devices
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
git status -sb && git log --oneline -5
git add -p && git commit -m "..." && git push
curl -sI https://github.com/tkjtani/my-first-android/releases/download/v1.0/app-release.apk | head -3
```

## 10. Setup PC Baru (development di komputer lain)

> Prinsip: semua ikut clone **kecuali** `local.properties`, `*.jks`, `build/`,
> `.gradle/` (sengaja di-ignore). Kunci update = **keystore yang sama**;
> keystore beda → user tidak bisa install timpa.

1. Install: Android Studio + SDK (platform 37, build-tools 36); biarkan Studio
   pakai JBR bawaannya (jangan set `JAVA_HOME` ke JDK sistem).
   Cek: `~/Android/Sdk/platform-tools/adb devices`.
2. Clone + buka di Studio (tunggu Gradle sync, wrapper terunduh otomatis):
```bash
git clone https://github.com/tkjtani/my-first-android.git
cd my-first-android
```
3. Identitas + auth git (sekali):
```bash
git config --global user.name "Nama Kamu"
git config --global user.email "email@contoh.com"
git config --global credential.helper store   # atau libsecret/SSH (lihat §5)
```
4. Pindahkan keystore via media aman (USB/password manager, **bukan chat/email**):
   - Copy `~/my-release-key.jks` dari PC lama ke path yang sama di PC baru,
     `chmod 600`.
   - Salin 4 baris `RELEASE_STORE_FILE / RELEASE_STORE_PASSWORD /
     RELEASE_KEY_ALIAS / RELEASE_KEY_PASSWORD` dari `local.properties` lama
     ke `local.properties` baru (biarkan baris `sdk.dir` versi PC baru yang
     dibuat otomatis Studio), `chmod 600`.
5. Verifikasi di PC baru:
```bash
./gradlew assembleDebug
./gradlew assembleRelease
ls app/build/outputs/apk/release/   # wajib app-release.apk (signed), bukan unsigned
~/Android/Sdk/build-tools/36.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk | head -3
# → CN=MyFirstAndroid ... (sama dengan PC lama)
```
6. Lanjut kerja seperti biasa (§9); rilis baru tetap wajib tag + upload APK (§6.1).

## 11. Fitur download dalam app — komponen wajib & cara kerja

> Sejak v1.2. Alur: Cek Update → Download Update (di app) → Install Sekarang.
> Terbukti jalan pada uji v1.2→v1.3. Verifikasi Play Protect + izin unknown-apps
> tetap wajib (proteksi OS, tak bisa bypass — lihat §11.4).

### 11.1 Komponen wajib (checklist)

| # | Komponen | Lokasi | Tanpa ini |
|---|---|---|---|
| 1 | `INTERNET` | Manifest | API + unduhan gagal total |
| 2 | `REQUEST_INSTALL_PACKAGES` | Manifest | installer sistem menolak di Android 8+ |
| 3 | `FileProvider` + `res/xml/file_paths.xml` (`external-files-path …/Download/`) | Manifest + res | `FileUriExposedException` / installer tak bisa baca file |
| 4 | `DownloadManager` + polling progres | `UpdateChecker.kt: downloadReleaseApk()` | tak ada unduhan latar + progres % |
| 5 | Cek `canRequestPackageInstalls()` + fallback Settings unknown-sources | `UpdateChecker.kt`, `MainActivity.kt: doInstall()` | user mentok tanpa arahan saat izin belum aktif |
| 6 | Intent `ACTION_VIEW` type `vnd.android.package-archive` + `FLAG_GRANT_READ_URI_PERMISSION` + `FLAG_ACTIVITY_NEW_TASK` | `UpdateChecker.kt: installApkFile()` | install tidak terbuka / akses file ditolak |
| 7 | Asset `.apk` di GitHub Release + `apkUrl` ter-parse | Release + `fetchLatestRelease()` | fallback buka browser (atau tombol mati) |
| 8 | Keystore SAMA + `versionCode` naik tiap rilis | keystore + `build.gradle.kts` | install diblokir (signature mismatch / downgrade) |
| 9 | `POST_NOTIFICATIONS` (Android 13+, runtime) | Manifest + launcher izin | notifikasi "Update tersedia" tidak muncul |

### 11.2 Alur state (UI di `UpdateCard`, logika di `ProfileRoute`)

```
Cek Update → status/kartu → [Download Update]
  → Downloading (progress %, tombol dikunci)
  → ReadyToInstall → [Install Sekarang] → installer OS → verifikasi → selesai
  → Failed → pesan error + [Download Update] (coba lagi)
[Lihat Release] selalu ada sebagai jalan keluar via browser.
Cek ulang me-reset state download ke Idle.
```

### 11.3 Aturan OS yang tak bisa dinego (hasil uji)

1. Izin "Install unknown apps" per-app (Android 8+) — sekali saja per app;
   jika belum aktif, app mengarahkan ke Settings lalu user ketuk Install lagi.
2. Verifikasi Play Protect tiap sideload ("Verifying…" / "Unknown app →
   Install anyway"). Berasal dari OS, bukan dari kode kita.
3. File APK di folder privat app (`getExternalFilesDir(Download)`) — tanpa
   izin storage tambahan; dibagikan ke installer hanya via content URI FileProvider.
4. Bootstrap: fitur ini hanya ada di app ≥ v1.2 — HP v1.0/v1.1 wajib 1x update
   via browser dulu.

### 11.4 Riwayat uji

- v1.2 (`6d131ec`, `versionCode 3`): fitur + kartu Tentang dummy.
- v1.3 (`71173c7`, `versionCode 4`): kartu "Yang baru di v1.3" sebagai penanda.
- Uji v1.2→v1.3 dari HP: TERKONFIRMASI JALAN (progress dalam app, install,
  penanda v1.3 muncul), dengan verifikasi + izin OS seperti §11.3.

---
*Dibuat otomatis dari sesi chat tanggal 2026-09-12. Sesuaikan versi/tag pada rilis berikutnya.*
