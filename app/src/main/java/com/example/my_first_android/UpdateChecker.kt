package com.example.my_first_android

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

const val GITHUB_OWNER = "tkjtani"
const val GITHUB_REPO = "my-first-android"
const val UPDATE_APK_FILE_NAME = "myfirstandroid-update.apk"

data class LatestRelease(
    val tag: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val apkUrl: String?
)

/** State UI untuk kartu cek update. Domain (network) tetap di fungsi fetch. */
data class UpdateCheckUiState(
    val status: String = "Belum dicek.",
    val checking: Boolean = false,
    val release: LatestRelease? = null
)

enum class DownloadPhase { Idle, Downloading, ReadyToInstall, Failed }

data class DownloadUiState(
    val phase: DownloadPhase = DownloadPhase.Idle,
    val progress: Int = 0,
    val file: File? = null,
    val error: String? = null
)

suspend fun fetchLatestRelease(): LatestRelease = withContext(Dispatchers.IO) {
    val url = URL("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 15000
        readTimeout = 15000
        setRequestProperty("Accept", "application/vnd.github+json")
    }
    try {
        val code = conn.responseCode
        if (code != 200) throw Exception("GitHub API $code (belum ada Release? buat Release dulu)")
        val text = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(text)
        val tag = json.optString("tag_name", "")
        val name = json.optString("name", tag)
        val body = json.optString("body", "")
        val htmlUrl = json.optString("html_url", "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
        var apkUrl: String? = null
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val dl = a.optString("browser_download_url", "")
                if (dl.endsWith(".apk")) { apkUrl = dl; break }
            }
            if (apkUrl == null && assets.length() > 0) {
                apkUrl = assets.getJSONObject(0).optString("browser_download_url").ifBlank { null }
            }
        }
        LatestRelease(tag, name, body, htmlUrl, apkUrl)
    } finally {
        conn.disconnect()
    }
}

fun getCurrentVersion(context: Context): String {
    return try {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }
        info.versionName ?: "?"
    } catch (_: Exception) { "?" }
}

/** Normalisasi "v1.0" -> "1.0" untuk banding dengan versionName "1.0". */
fun normalizeTag(tag: String): String = tag.trim().removePrefix("v").removePrefix("V")

fun isUpdateAvailable(current: String, latestTag: String): Boolean {
    val c = normalizeTag(current)
    val l = normalizeTag(latestTag)
    return l.isNotBlank() && c.isNotBlank() && l != c
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}

fun showUpdateNotification(context: Context, release: LatestRelease) {    val channelId = "update_channel"
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= 26) {
        val ch = NotificationChannel(channelId, "Update Aplikasi", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(ch)
    }
    val target = (release.apkUrl ?: release.htmlUrl)
    val pi = PendingIntent.getActivity(
        context, 0,
        Intent(Intent.ACTION_VIEW, target.toUri()),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notif = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle("Update tersedia: ${release.tag}")
        .setContentText("Ketuk untuk download dan install.")
        .setContentIntent(pi)
        .setAutoCancel(true)
        .build()
    nm.notify(1001, notif)
}

/** Unduh APK via DownloadManager ke folder privat app. Lempar Exception saat gagal. */
suspend fun downloadReleaseApk(
    context: Context,
    url: String,
    onProgress: (Int) -> Unit
): File = withContext(Dispatchers.IO) {
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val req = DownloadManager.Request(url.toUri()).apply {
        setTitle("Update aplikasi")
        setDescription("Mengunduh APK...")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        setMimeType("application/vnd.android.package-archive")
        setDestinationInExternalFilesDir(
            context, Environment.DIRECTORY_DOWNLOADS, UPDATE_APK_FILE_NAME
        )
        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)
    }
    val id = dm.enqueue(req)
    try {
        var result: File? = null
        while (result == null) {
            dm.query(DownloadManager.Query().setFilterById(id))?.use { c ->
                if (c.moveToFirst()) {
                    val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val done = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    if (total > 0) onProgress(((done * 100) / total).toInt().coerceIn(0, 100))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val file = File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                UPDATE_APK_FILE_NAME
                            )
                            if (!file.exists()) throw Exception("File unduhan tidak ditemukan")
                            result = file
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            throw Exception("Unduhan gagal (kode $reason)")
                        }
                    }
                }
            }
            if (result == null) delay(500)
        }
        result ?: throw Exception("Unduhan tidak selesai")
    } catch (e: Exception) {
        try { dm.remove(id) } catch (_: Exception) { }
        throw e
    }
}

/** Android 8+: bolehkah app ini meminta install APK? */
fun canInstallUnknownApps(context: Context): Boolean =
    Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()

/** Arahkan user ke Settings "Install unknown apps" untuk app ini. */
fun openUnknownSourcesSettings(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    } catch (_: Exception) { }
}

/** Panggil installer sistem untuk file APK via FileProvider. */
fun installApkFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}
