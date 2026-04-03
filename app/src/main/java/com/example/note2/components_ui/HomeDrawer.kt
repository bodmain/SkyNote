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

@Composable
fun HomeDrawerContent(
    currentUser: FirebaseUser?,
    syncState: SyncState = SyncState.IDLE,
    onLoginClick: () -> Unit,
    onAllNotesClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )


    val iconColor by animateColorAsState(
        targetValue = when (syncState) {
            SyncState.SYNCING -> MaterialTheme.colorScheme.primary
            SyncState.SUCCESS -> Color(0xFF4CAF50)
            SyncState.ERROR -> MaterialTheme.colorScheme.error
            else -> if (currentUser != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        },
        label = "icon_color"
    )

    ModalDrawerSheet {
        // Header của Drawer
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
                Text(
                    text = "Đăng nhập để sao lưu dữ liệu",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
                        SyncState.ERROR -> "Lỗi đồng bộ"
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
                Icon(
                    imageVector = if (syncState == SyncState.SUCCESS) Icons.Default.CheckCircle else Icons.Default.Sync,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = if (syncState == SyncState.SYNCING) Modifier.rotate(rotation) else Modifier
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
    }
}
