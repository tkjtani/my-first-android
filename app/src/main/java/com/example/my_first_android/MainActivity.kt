package com.example.my_first_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.my_first_android.ui.theme.MyfirstandroidTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyfirstandroidTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    var profile by rememberSaveable(stateSaver = ProfileUiStateSaver) {
        mutableStateOf(SampleData.profile)
    }
    var followers by rememberSaveable(stateSaver = FollowersListSaver) {
        mutableStateOf(SampleData.followers)
    }
    var privacy by rememberSaveable(stateSaver = PrivacyUiStateSaver) {
        mutableStateOf(SampleData.privacy)
    }

    NavHost(
        navController = navController,
        startDestination = "profile"
    ) {
        composable("profile") {
            ProfileRoute(
                profile = profile,
                onFollowClick = { profile = profile.copy(isFollowing = !profile.isFollowing) },
                onFollowersClick = { navController.navigate("profile/followers") },
                onPrivacyClick = { navController.navigate("profile/privacy") }
            )
        }
        composable("profile/followers") {
            FollowersScreen(
                followers = followers,
                onBack = { navController.popBackStack() },
                onFollowClick = { id ->
                    followers = followers.map {
                        if (it.id == id) it.copy(isFollowing = !it.isFollowing) else it
                    }
                }
            )
        }
        composable("profile/privacy") {
            PrivacyScreen(
                state = privacy,
                onBack = { navController.popBackStack() },
                onPrivateChange = { privacy = privacy.copy(isPrivateAccount = it) },
                onActivityStatusChange = { privacy = privacy.copy(showActivityStatus = it) }
            )
        }
    }
}

@Composable
fun ProfileRoute(
    profile: ProfileUiState,
    onFollowClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersion = remember { getCurrentVersion(context) }
    var updateState by remember { mutableStateOf(UpdateCheckUiState()) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) updateState.release?.let { showUpdateNotification(context, it) }
    }

    fun doCheck() {
        if (updateState.checking) return
        updateState = updateState.copy(checking = true, status = "Memeriksa...", release = null)
        scope.launch {
            try {
                val rel = fetchLatestRelease()
                updateState = if (isUpdateAvailable(currentVersion, rel.tag)) {
                    val granted = if (Build.VERSION.SDK_INT >= 33) {
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true
                    if (granted) {
                        showUpdateNotification(context, rel)
                    } else if (Build.VERSION.SDK_INT >= 33) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    UpdateCheckUiState(
                        status = "Update tersedia: ${rel.tag}",
                        checking = false,
                        release = rel
                    )
                } else {
                    UpdateCheckUiState(
                        status = "Sudah versi terbaru ($currentVersion).",
                        checking = false,
                        release = null
                    )
                }
            } catch (e: Exception) {
                updateState = UpdateCheckUiState(
                    status = "Gagal: ${e.message}",
                    checking = false,
                    release = null
                )
            }
        }
    }

    ProfileScreen(
        state = profile,
        updateState = updateState,
        currentVersion = currentVersion,
        onFollowersClick = onFollowersClick,
        onFollowClick = onFollowClick,
        onPrivacyClick = onPrivacyClick,
        onCheckUpdate = ::doCheck,
        onOpenUrl = { url -> openUrl(context, url) }
    )
}
