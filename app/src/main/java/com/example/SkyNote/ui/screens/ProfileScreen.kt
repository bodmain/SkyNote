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
    val uiState by authViewModel.uiState.collectAsState()
    val isDark = themeMode == 2
    val context = LocalContext.current
    val isGuest = currentUser == null

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showAvatarPicker = false
            authViewModel.clearMessage()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val colorBackground = MaterialTheme.colorScheme.background
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            primaryContainer,
            primaryContainer.copy(alpha = 0.8f),
            colorBackground
        )
    )

    Scaffold(
        containerColor = colorBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cá nhân", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryContainer
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerGradient)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp, top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileHeader(
                            currentUser = currentUser,
                            photoUrl = currentUser?.photoUrl?.toString(),
                            isGuest = isGuest,
                            primaryColor = primaryColor,
                            primaryContainer = surfaceColor,
                            colorBackground = primaryContainer,
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = currentUser?.email ?: "", 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Button(
                                    onClick = { authViewModel.signInWithGoogle(context, AppDatabase.getDatabase(context).noteDao()) },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Login, null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Đăng nhập bằng Google", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Đăng nhập để lưu dữ liệu và thay đổi avatar nhé ✨",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModernCard(title = "Dữ liệu & Bảo mật") {
                    ModernSettingItem(
                        icon = Icons.Default.CloudSync,
                        iconColor = if (syncState == SyncState.SUCCESS) Color(0xFF2E7D32) else Color(0xFF4CAF50),
                        title = "Đồng bộ đám mây",
                        subtitle = if (isGuest) "Yêu cầu đăng nhập" else when (syncState) {
                            SyncState.SUCCESS -> "Đã đồng bộ thành công"
                            SyncState.SYNCING -> "Đang xử lý..."
                            else -> "Nhấn để sao lưu ngay"
                        },
                        containerColor = if (!isGuest && syncState == SyncState.SUCCESS) Color(0xFFE8F5E9) else Color.Transparent,
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
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = onSurfaceColor.copy(alpha = 0.08f))
                    ModernSettingItem(
                        icon = Icons.Default.DeleteSweep,
                        iconColor = Color(0xFFFF7043),
                        title = "Thùng rác",
                        subtitle = "$noteCountInTrash ghi chú đã xóa",
                        onClick = onNavigateToTrash
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ModernCard(title = "Tùy chỉnh giao diện") {
                    ModernSettingItem(
                        icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        iconColor = Color(0xFFFFD600),
                        title = "Chế độ tối",
                        trailing = {
                            Switch(
                                checked = isDark,
                                onCheckedChange = { themeViewModel.toggleTheme(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ModernCard(title = "Thông tin ứng dụng") {
                    ModernSettingItem(
                        icon = Icons.Default.Info,
                        iconColor = Color(0xFF00B0FF),
                        title = "Phiên bản",
                        subtitle = "Phiên bản ${BuildConfig.VERSION_NAME}",
                        trailing = { Text("Mới nhất", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = onSurfaceColor.copy(alpha = 0.08f))
                    ModernSettingItem(
                        icon = Icons.Default.VerifiedUser,
                        iconColor = Color(0xFF66BB6A),
                        title = "Quyền riêng tư",
                        onClick = { 
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy?hl=vi"))
                            context.startActivity(intent)
                        }
                    )
                }

                if (!isGuest) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Đăng xuất khỏi tài khoản", 
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showAvatarPicker && !isGuest) {
        AvatarPickerSheet(
            tempSelectedEmoji = tempSelectedEmoji,
            isLoading = uiState.isLoading,
            primaryColor = primaryColor,
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
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") } }
        )
    }
}
