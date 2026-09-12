package com.example.my_first_android

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.my_first_android.ui.theme.MyfirstandroidTheme

// ---- Model (sesuai DESIGN.md §Model Data Minimum) ----

data class ProfileUiState(
    val name: String,
    val username: String,
    val avatarUrl: String?,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)

data class FollowerUiModel(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String?,
    val isFollowing: Boolean
)

data class PrivacyUiState(
    val isPrivateAccount: Boolean,
    val showActivityStatus: Boolean
)

// ---- Saver agar state bertahan saat rotasi (tanpa ViewModel/dependensi baru) ----

val ProfileUiStateSaver: Saver<ProfileUiState, Any> = mapSaver(
    save = {
        mapOf(
            "name" to it.name,
            "username" to it.username,
            "avatarUrl" to it.avatarUrl,
            "followersCount" to it.followersCount,
            "followingCount" to it.followingCount,
            "isFollowing" to it.isFollowing
        )
    },
    restore = {
        ProfileUiState(
            name = it["name"] as String,
            username = it["username"] as String,
            avatarUrl = it["avatarUrl"] as String?,
            followersCount = it["followersCount"] as Int,
            followingCount = it["followingCount"] as Int,
            isFollowing = it["isFollowing"] as Boolean
        )
    }
)

val PrivacyUiStateSaver: Saver<PrivacyUiState, Any> = mapSaver(
    save = {
        mapOf(
            "isPrivateAccount" to it.isPrivateAccount,
            "showActivityStatus" to it.showActivityStatus
        )
    },
    restore = {
        PrivacyUiState(
            isPrivateAccount = it["isPrivateAccount"] as Boolean,
            showActivityStatus = it["showActivityStatus"] as Boolean
        )
    }
)

val FollowersListSaver: Saver<List<FollowerUiModel>, Any> = listSaver(
    save = { list ->
        list.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "username" to it.username,
                "avatarUrl" to it.avatarUrl,
                "isFollowing" to it.isFollowing
            )
        }
    },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        (saved as List<Map<String, Any?>>).map {
            FollowerUiModel(
                id = it["id"] as String,
                name = it["name"] as String,
                username = it["username"] as String,
                avatarUrl = it["avatarUrl"] as String?,
                isFollowing = it["isFollowing"] as Boolean
            )
        }
    }
)

// ---- Data contoh (prototipe; hanya dipakai holder + preview) ----

object SampleData {
    val profile = ProfileUiState(
        name = "Bing",
        username = "@bing",
        avatarUrl = null,
        followersCount = 1280,
        followingCount = 86,
        isFollowing = false
    )
    val followers = listOf(
        FollowerUiModel("1", "Ayu Lestari", "@ayu", null, true),
        FollowerUiModel("2", "Budi Santoso", "@budi", null, false),
        FollowerUiModel("3", "Citra Dewi", "@citra", null, true),
        FollowerUiModel("4", "Dedi Kurniawan", "@dedi", null, false),
        FollowerUiModel("5", "Eka Prasetyo", "@eka", null, false),
        FollowerUiModel("6", "Fitri Handayani", "@fitri", null, true),
        FollowerUiModel("7", "Gilang Ramadhan", "@gilang", null, false),
        FollowerUiModel("8", "Hana Wijaya", "@hana", null, false)
    )
    val privacy = PrivacyUiState(
        isPrivateAccount = false,
        showActivityStatus = true
    )
}

// ---- Komponen reusable (state hoisting: state + callback dari parent) ----

@Composable
fun ProfileAvatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ProfileStat(
    label: String,
    count: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .let { m ->
                if (onClick != null) {
                    m.clickable(role = Role.Button, onClickLabel = "Lihat $label", onClick = onClick)
                } else m
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileHeader(
    state: ProfileUiState,
    onFollowersClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(name = state.name)
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = state.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = state.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(label = "Followers", count = state.followersCount, onClick = onFollowersClick)
            ProfileStat(label = "Following", count = state.followingCount, onClick = null)
        }
        Spacer(modifier = Modifier.size(16.dp))
        if (state.isFollowing) {
            FilledTonalButton(
                onClick = onFollowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Mengikuti")
            }
        } else {
            Button(
                onClick = onFollowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Ikuti")
            }
        }
    }
}

@Composable
fun FollowerRow(
    follower: FollowerUiModel,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = follower.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = follower.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = follower.username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        if (follower.isFollowing) {
            FilledTonalButton(
                onClick = onFollowClick,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Mengikuti")
            }
        } else {
            OutlinedButton(
                onClick = onFollowClick,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Ikuti")
            }
        }
    }
}

@Composable
fun PrivacySettingRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (description != null) {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AboutCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Tentang (dummy v1.2)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Akun contoh untuk uji coba update via GitHub Releases.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Bergabung • 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UpdateCard(
    state: UpdateCheckUiState,
    download: DownloadUiState,
    currentVersion: String,
    onCheckUpdate: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onOpenRelease: (LatestRelease) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Update Aplikasi", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Versi saat ini: $currentVersion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = state.status, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = onCheckUpdate,
                enabled = !state.checking,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(if (state.checking) "Memeriksa..." else "Cek Update")
            }
            state.release?.let { rel ->
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = rel.name.ifBlank { rel.tag },
                    style = MaterialTheme.typography.titleSmall
                )
                if (rel.body.isNotBlank()) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = rel.body.take(300), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.size(8.dp))
                when (download.phase) {
                    DownloadPhase.Downloading -> {
                        Text(
                            text = "Mengunduh... ${download.progress}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        LinearProgressIndicator(
                            progress = { download.progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    DownloadPhase.ReadyToInstall -> {
                        Button(
                            onClick = onInstall,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Install Sekarang")
                        }
                    }
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onDownload,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("Download Update")
                            }
                            OutlinedButton(
                                onClick = { onOpenRelease(rel) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("Lihat Release")
                            }
                        }
                    }
                }
                if (download.phase == DownloadPhase.Failed) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Gagal: ${download.error ?: "tidak diketahui"} — coba lagi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (download.phase == DownloadPhase.ReadyToInstall) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "APK sudah diunduh. Ketuk Install, lalu setujui verifikasi Play Protect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---- Layar ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    updateState: UpdateCheckUiState,
    download: DownloadUiState,
    currentVersion: String,
    onFollowersClick: () -> Unit,
    onFollowClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onCheckUpdate: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onPrivacyClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Pengaturan privasi"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            ProfileHeader(
                state = state,
                onFollowersClick = onFollowersClick,
                onFollowClick = onFollowClick
            )
            Spacer(modifier = Modifier.size(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.size(24.dp))
            AboutCard()
            Spacer(modifier = Modifier.size(16.dp))
            UpdateCard(
                state = updateState,
                download = download,
                currentVersion = currentVersion,
                onCheckUpdate = onCheckUpdate,
                onDownload = onDownload,
                onInstall = onInstall,
                onOpenRelease = { rel -> onOpenUrl(rel.htmlUrl) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    followers: List<FollowerUiModel>,
    onBack: () -> Unit,
    onFollowClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Followers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            items(followers, key = { it.id }) { follower ->
                FollowerRow(
                    follower = follower,
                    onFollowClick = { onFollowClick(follower.id) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    state: PrivacyUiState,
    onBack: () -> Unit,
    onPrivateChange: (Boolean) -> Unit,
    onActivityStatusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            item {
                Text(
                    text = "Akun",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                PrivacySettingRow(
                    title = "Akun privat",
                    description = "Hanya follower yang disetujui dapat melihat konten.",
                    checked = state.isPrivateAccount,
                    onCheckedChange = onPrivateChange
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                PrivacySettingRow(
                    title = "Tampilkan status aktivitas",
                    description = "Pengguna lain dapat melihat saat kamu aktif.",
                    checked = state.showActivityStatus,
                    onCheckedChange = onActivityStatusChange
                )
            }
        }
    }
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MyfirstandroidTheme {
        ProfileScreen(
            state = SampleData.profile,
            updateState = UpdateCheckUiState(),
            download = DownloadUiState(),
            currentVersion = "1.2",
            onFollowersClick = {},
            onFollowClick = {},
            onPrivacyClick = {},
            onCheckUpdate = {},
            onDownload = {},
            onInstall = {},
            onOpenUrl = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfileFollowingDarkPreview() {
    MyfirstandroidTheme {
        ProfileScreen(
            state = SampleData.profile.copy(isFollowing = true),
            updateState = UpdateCheckUiState(
                status = "Update tersedia: v1.2",
                release = LatestRelease(
                    tag = "v1.2",
                    name = "Versi 1.2",
                    body = "Download dalam app + kartu Tentang.",
                    htmlUrl = "https://github.com/tkjtani/my-first-android/releases/tag/v1.2",
                    apkUrl = null
                )
            ),
            download = DownloadUiState(phase = DownloadPhase.ReadyToInstall, progress = 100),
            currentVersion = "1.1",
            onFollowersClick = {},
            onFollowClick = {},
            onPrivacyClick = {},
            onCheckUpdate = {},
            onDownload = {},
            onInstall = {},
            onOpenUrl = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FollowersPreview() {
    MyfirstandroidTheme {
        FollowersScreen(
            followers = SampleData.followers,
            onBack = {},
            onFollowClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyPreview() {
    MyfirstandroidTheme {
        PrivacyScreen(
            state = SampleData.privacy,
            onBack = {},
            onPrivateChange = {},
            onActivityStatusChange = {}
        )
    }
}
