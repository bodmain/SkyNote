package com.example.note2.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.note2.BuildConfig
import com.example.note2.data.local.AppDatabase
import com.example.note2.viewmodel.AuthViewModel
import com.example.note2.viewmodel.SyncState
import com.example.note2.viewmodel.ThemeViewModel
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
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    
    val themeMode by themeViewModel.themeMode.collectAsState()
    val isDark = themeMode == 2
    val context = LocalContext.current

    // Màu sắc thích ứng với Light/Dark Mode
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF7F5FF)
    val purplePrimary = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF5533CC)
    val purpleLight = if (isDark) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color(0xFFEDE9FF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val sectionHeaderColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF5533CC)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Cá nhân & Cài đặt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Profile
            Spacer(modifier = Modifier.height(16.dp))
            if (currentUser != null) {
                val firstChar = currentUser.displayName?.firstOrNull()?.toString() ?: "U"
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(purpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstChar.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = purplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentUser.displayName ?: "Người dùng SkyNote",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = currentUser.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showEditNameDialog = true }) {
                    Text("Chỉnh sửa hồ sơ", color = purplePrimary, style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Khách", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        val db = AppDatabase.getDatabase(context)
                        authViewModel.signInWithGoogle(context, db.noteDao()) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purplePrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Đăng nhập bằng Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = purpleLight.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = purplePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dữ liệu chỉ lưu trên máy này — gỡ app là mất hết ghi chú.",
                            style = MaterialTheme.typography.bodySmall,
                            color = purplePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sections
            if (currentUser != null) {
                SectionHeader("Tài khoản", sectionHeaderColor)
                SettingItem(
                    icon = Icons.Default.Badge,
                    title = "Đổi tên hiển thị",
                    purpleLight = purpleLight,
                    purplePrimary = purplePrimary,
                    textColor = textColor,
                    onClick = { showEditNameDialog = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            SectionHeader("Giao diện", sectionHeaderColor)
            SettingToggle(
                icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "Chế độ tối",
                checked = isDark,
                purpleLight = purpleLight,
                purplePrimary = purplePrimary,
                textColor = textColor,
                onCheckedChange = { themeViewModel.toggleTheme(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Dữ liệu", sectionHeaderColor)
            if (currentUser != null) {
                SettingToggle(
                    icon = Icons.Default.CloudSync,
                    title = "Đồng bộ tự động",
                    checked = true, 
                    purpleLight = purpleLight,
                    purplePrimary = purplePrimary,
                    textColor = textColor,
                    onCheckedChange = { onSyncClick() },
                    subtitle = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (syncState == SyncState.SUCCESS) Color.Green else if (syncState == SyncState.ERROR) Color.Red else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when (syncState) {
                                    SyncState.SYNCING -> "Đang đồng bộ..."
                                    SyncState.SUCCESS -> "Đã đồng bộ · 09:14"
                                    SyncState.ERROR -> "Thất bại · Thử lại"
                                    else -> "Sẵn sàng"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            } else {
                SettingItem(
                    icon = Icons.Default.CloudSync,
                    title = "Đồng bộ Firebase",
                    purpleLight = purpleLight,
                    purplePrimary = purplePrimary,
                    textColor = textColor,
                    subtitle = { Text("Yêu cầu đăng nhập", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailing = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                    enabled = false
                )
            }

            
            SettingItem(
                icon = Icons.Default.FileUpload,
                title = "Xuất dữ liệu",
                purpleLight = purpleLight,
                purplePrimary = purplePrimary,
                textColor = textColor,
                onClick = { /* Export logic */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Khác", sectionHeaderColor)
            SettingItem(
                icon = Icons.Default.PrivacyTip,
                title = "Chính sách quyền riêng tư",
                purpleLight = purpleLight,
                purplePrimary = purplePrimary,
                textColor = textColor,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                    context.startActivity(intent)
                }
            )
            SettingItem(
                icon = Icons.Default.Info,
                title = "Phiên bản app",
                purpleLight = purpleLight,
                purplePrimary = purplePrimary,
                textColor = textColor,
                subtitle = { Text(BuildConfig.VERSION_NAME, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )

            if (currentUser != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showLogoutDialog = true },
                    color = if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất?") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Đăng xuất", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Đổi tên hiển thị") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.updateProfile(newName)
                    showEditNameDialog = false
                }) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        ),
        color = color,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    purpleLight: Color,
    purplePrimary: Color,
    textColor: Color,
    subtitle: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(purpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = purplePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
            if (subtitle != null) {
                subtitle()
            }
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun SettingToggle(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    purpleLight: Color,
    purplePrimary: Color,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: @Composable (() -> Unit)? = null
) {
    SettingItem(
        icon = icon,
        title = title,
        purpleLight = purpleLight,
        purplePrimary = purplePrimary,
        textColor = textColor,
        subtitle = subtitle,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = purplePrimary
                )
            )
        }
    )
}
