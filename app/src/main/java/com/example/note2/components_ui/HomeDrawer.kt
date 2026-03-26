package com.example.note2.components_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeDrawerContent(
    currentUser: FirebaseUser?,
    onLoginClick: () -> Unit,
    onAllNotesClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        // Header của Drawer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            if (currentUser != null) {
                // Hiển thị ảnh đại diện Google
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
                // Guest Mode
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

        // Menu items
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
            label = { Text("Đồng bộ hóa") },
            selected = false,
            onClick = {
                if (currentUser != null) {
                    onSyncClick()
                    onCloseDrawer()
                }
            },
            icon = { Icon(Icons.Default.Sync, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = if (currentUser != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                unselectedIconColor = if (currentUser != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
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
