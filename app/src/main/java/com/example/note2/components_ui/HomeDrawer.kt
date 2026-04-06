package com.example.note2.components_ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.note2.viewmodel.SyncState
import com.google.firebase.auth.FirebaseUser
import com.example.note2.viewmodel.ThemeViewModel


@Composable
fun HomeDrawerContent(
    currentUser: FirebaseUser?,
    syncState: SyncState = SyncState.IDLE,
    onLoginClick: () -> Unit,
    onAllNotesClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    themeViewModel: ThemeViewModel
) {

    val themeMode by themeViewModel.themeMode.collectAsState()
    val isDark = themeMode == 2

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    
    val rotation by if (syncState == SyncState.SYNCING) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }


    val iconColor by animateColorAsState(
        targetValue = when (syncState) {
            SyncState.SYNCING -> MaterialTheme.colorScheme.primary
            SyncState.SUCCESS -> Color(0xFF4CAF50) // Green
            SyncState.ERROR -> MaterialTheme.colorScheme.error // Red
            else -> if (currentUser != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        },
        label = "icon_color"
    )


    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            if (currentUser != null) {
                AsyncImage(
                    model = currentUser.photoUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentUser.displayName ?: "Người dùng SkyNote",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser.email ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Chế độ Khách",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Đăng nhập bằng Google")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        NavigationDrawerItem(
            label = { Text("Tất cả ghi chú") },
            selected = true,
            onClick = {
                onAllNotesClick()
                onCloseDrawer()
            },
            icon = { Icon(Icons.Default.Notes, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = {
                Text(
                    when (syncState) {
                        SyncState.SYNCING -> "Đang đồng bộ..."
                        SyncState.SUCCESS -> "Đã đồng bộ thành công"
                        SyncState.ERROR -> "Lỗi đồng bộ (Hết thời gian)"
                        else -> "Đồng bộ hóa"
                    }
                )
            },
            selected = false,
            onClick = {
                if (currentUser != null && syncState == SyncState.IDLE) {
                    onSyncClick()
                }
            },
            icon = {
                // Tách biệt hoàn toàn modifier xoay
                val rotateModifier = if (syncState == SyncState.SYNCING) {
                    Modifier.rotate(rotation)
                } else {
                    Modifier
                }

                Icon(
                    imageVector = when (syncState) {
                        SyncState.SUCCESS -> Icons.Default.CheckCircle
                        SyncState.ERROR -> Icons.Default.Error
                        else -> Icons.Default.Sync
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = rotateModifier
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = if (currentUser != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
        )

        if (currentUser == null) {
            Text(
                text = "Tính năng yêu cầu đăng nhập",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.error
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Hồ sơ & Cài đặt") },
            selected = false,
            onClick = {
                onSettingsClick()
                onCloseDrawer()
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Chế độ tối", style = MaterialTheme.typography.labelLarge)
                    Switch(
                        checked = isDark,
                        onCheckedChange = { checked ->
                            themeViewModel.toggleTheme(checked)
                        }
                    )
                }
            },
            selected = false,
            onClick = { themeViewModel.toggleTheme(!isDark) },
            icon = {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
