package com.example.my_first_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToDetail = { navController.navigate("detail/Android") }
            )
        }
        composable(
            route = "detail/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: "Android"
            DetailScreen(
                name = name,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun HomeScreen(onNavigateToDetail: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersion = remember { getCurrentVersion(context) }

    var status by remember { mutableStateOf("Belum dicek.") }
    var checking by remember { mutableStateOf(false) }
    var latest by remember { mutableStateOf<LatestRelease?>(null) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) latest?.let { showUpdateNotification(context, it) }
    }

    fun doCheck() {
        if (checking) return
        checking = true
        status = "Memeriksa..."
        latest = null
        scope.launch {
            try {
                val rel = fetchLatestRelease()
                if (isUpdateAvailable(currentVersion, rel.tag)) {
                    status = "Update tersedia: ${rel.tag}"
                    latest = rel
                    val ok = if (Build.VERSION.SDK_INT >= 33) {
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true
                    if (ok) {
                        showUpdateNotification(context, rel)
                    } else if (Build.VERSION.SDK_INT >= 33) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    status = "Sudah versi terbaru ($currentVersion)."
                }
            } catch (e: Exception) {
                status = "Gagal: ${e.message}"
            } finally {
                checking = false
            }
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Hello Android!")
            Text(text = "Ini Home Screen")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "Halo", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Tampilkan Toast")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToDetail) {
                Text("Ke Detail ▶")
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Versi saat ini: $currentVersion",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = status, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = ::doCheck, enabled = !checking) {
                    Text(if (checking) "Memeriksa..." else "Cek Update")
                }
            }
            latest?.let { rel ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = rel.name.ifBlank { rel.tag }, style = MaterialTheme.typography.titleSmall)
                        if (rel.body.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rel.body.take(300),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                openUrl(context, rel.apkUrl ?: rel.htmlUrl)
                            }) {
                                Text("Download Update")
                            }
                            OutlinedButton(onClick = { openUrl(context, rel.htmlUrl) }) {
                                Text("Lihat Release")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(name: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Halo, $name!")
            Text(text = "Ini screen kedua.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) {
                Text("◀ Kembali")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    MyfirstandroidTheme {
        HomeScreen(onNavigateToDetail = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DetailPreview() {
    MyfirstandroidTheme {
        DetailScreen(name = "Android", onBack = {})
    }
}
