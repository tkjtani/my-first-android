package com.example.my_first_android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

const val GITHUB_OWNER = "tkjtani"
const val GITHUB_REPO = "my-first-android"

data class LatestRelease(
    val tag: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val apkUrl: String?
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

fun showUpdateNotification(context: Context, release: LatestRelease) {
    val channelId = "update_channel"
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
