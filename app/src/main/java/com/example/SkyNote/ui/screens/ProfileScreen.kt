package com.example.SkyNote.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SkyNote.BuildConfig
import com.example.SkyNote.data.local.AppDatabase
import com.example.SkyNote.ui.components.*
import com.example.SkyNote.viewmodel.AuthViewModel
import com.example.SkyNote.viewmodel.SyncState
import com.example.SkyNote.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: FirebaseUser?,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    syncState: SyncState,
    noteCountInTrash: Int,
    onSyncClick: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var tempSelectedEmoji by remember { mutableStateOf("") }
    
    val themeMode by themeViewModel.themeMode.collectAsState()
    val autoSyncEnabled by themeViewModel.autoSyncEnabled.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    
    val isDark = themeMode == 2
    val context = LocalContext.current
    val isGuest = currentUser == null

    // Adaptive colors for Dark/Light mode
    val colorBackground = MaterialTheme.colorScheme.background
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val purplePrimary = MaterialTheme.colorScheme.primary

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            primaryContainer.copy(alpha = if (isDark) 0.3f else 0.5f),
            colorBackground
        )
    )

    Scaffold(
        containerColor = colorBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cá nhân", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerGradient)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileHeader(
                        currentUser = currentUser,
                        photoUrl = currentUser?.photoUrl?.toString(),
                        isGuest = isGuest,
                        primaryColor = purplePrimary,
                        primaryContainer = surfaceColor,
                        colorBackground = colorBackground,
                        onAvatarClick = { 
                            tempSelectedEmoji = currentUser?.photoUrl?.toString() ?: ""
                            showAvatarPicker = true 
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!isGuest) {
                        Text(
                            text = currentUser?.displayName ?: "SkyNote User", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = currentUser?.email ?: "", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Button(
                            onClick = { authViewModel.signInWithGoogle(context, AppDatabase.getDatabase(context).noteDao()) },
                            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = purplePrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Đăng nhập bằng Google", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nhóm Dữ liệu
                ModernCard(title = "Dữ liệu & Đồng bộ") {
                    ModernSettingItem(
                        icon = Icons.Default.CloudSync,
                        iconColor = purplePrimary,
                        title = "Đồng bộ đám mây",
                        subtitle = if (isGuest) "Yêu cầu đăng nhập" else when (syncState) {
                            SyncState.SUCCESS -> "Đã đồng bộ thành công"
                            SyncState.SYNCING -> "Đang đồng bộ..."
                            SyncState.ERROR -> "Đồng bộ thất bại"
                            else -> "Nhấn để sao lưu ngay"
                        },
                        trailing = {
                            if (!isGuest && syncState == SyncState.SYNCING) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(if (isGuest) Icons.Default.Lock else Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                            }
                        },
                        enabled = !isGuest,
                        onClick = onSyncClick
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    
                    ModernSettingItem(
                        icon = Icons.Default.Autorenew,
                        iconColor = if (autoSyncEnabled) purplePrimary else Color.Gray,
                        title = "Đồng bộ tự động",
                        subtitle = "Tự động lưu thay đổi lên Cloud",
                        enabled = !isGuest,
                        trailing = {
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = { themeViewModel.toggleAutoSync(it) },
                                enabled = !isGuest
                            )
                        }
                    )
                }

                // Nhóm Giao diện
                ModernCard(title = "Tùy chỉnh giao diện") {
                    ModernSettingItem(
                        icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        iconColor = Color(0xFFFFD600),
                        title = "Chế độ tối",
                        subtitle = if (isDark) "Đang sử dụng chế độ tối" else "Đang sử dụng chế độ sáng",
                        trailing = {
                            Switch(
                                checked = isDark,
                                onCheckedChange = { themeViewModel.toggleTheme(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = purplePrimary)
                            )
                        }
                    )
                }

                // Thùng rác & Khác
                ModernCard(title = "Khác") {
                    ModernSettingItem(
                        icon = Icons.Default.DeleteSweep,
                        iconColor = Color(0xFFFF7043),
                        title = "Thùng rác",
                        subtitle = "$noteCountInTrash ghi chú đã xóa",
                        onClick = onNavigateToTrash
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    ModernSettingItem(
                        icon = Icons.Default.VerifiedUser,
                        iconColor = Color(0xFF66BB6A),
                        title = "Chính sách quyền riêng tư",
                        onClick = { 
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy"))
                            context.startActivity(intent)
                        }
                    )
                }

                if (!isGuest) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.2f else 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Đăng xuất tài khoản", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                
                Text(
                    text = "SkyNote v${BuildConfig.VERSION_NAME}",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAvatarPicker && !isGuest) {
        AvatarPickerSheet(
            tempSelectedEmoji = tempSelectedEmoji,
            isLoading = uiState.isLoading,
            primaryColor = purplePrimary,
            surfaceColor = surfaceColor,
            onEmojiSelect = { tempSelectedEmoji = it },
            onSave = { authViewModel.updateProfile(currentUser?.displayName ?: "", tempSelectedEmoji) },
            onDismiss = { showAvatarPicker = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất?") },
            text = { Text("Bạn có muốn đăng xuất? Mọi ghi chú mới chưa đồng bộ có thể bị mất.") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) { 
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }
}
