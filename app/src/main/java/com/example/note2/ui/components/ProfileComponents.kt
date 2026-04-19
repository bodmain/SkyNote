package com.example.note2.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileHeader(
    currentUser: FirebaseUser?,
    photoUrl: String?,
    isGuest: Boolean,
    primaryColor: Color,
    primaryContainer: Color,
    colorBackground: Color,
    onAvatarClick: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        key(currentUser?.uid, photoUrl) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(
                        width = 4.dp,
                        brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                            listOf(primaryColor, Color.Cyan, primaryColor)
                        ),
                        shape = CircleShape
                    )
                    .padding(6.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl != null) {
                        if (photoUrl.startsWith("http") || photoUrl.startsWith("content")) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clickable(enabled = !isGuest) { onAvatarClick() },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = photoUrl, 
                                fontSize = 56.sp,
                                modifier = Modifier.clickable(enabled = !isGuest) { onAvatarClick() }
                            )
                        }
                    } else {
                        Text(
                            text = currentUser?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "G",
                            style = MaterialTheme.typography.displayMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.clickable(enabled = !isGuest) { onAvatarClick() }
                        )
                    }
                }
            }
        }
        
        Surface(
            onClick = { if (!isGuest) onAvatarClick() },
            shape = CircleShape,
            color = if (isGuest) Color.Gray else primaryColor,
            modifier = Modifier
                .size(38.dp)
                .shadow(4.dp, CircleShape)
                .border(3.dp, colorBackground, CircleShape)
        ) {
            Icon(
                imageVector = if (isGuest) Icons.Default.Lock else Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ModernCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            fontWeight = FontWeight.ExtraBold
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 0.5.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ModernSettingItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .background(containerColor)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    tempSelectedEmoji: String,
    isLoading: Boolean,
    primaryColor: Color,
    surfaceColor: Color,
    onEmojiSelect: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val avatarIcons = listOf("🚀", "🌟", "🍀", "🎨", "☁️", "🍎", "🐱", "🐶", "🦊", "🌈", "⚡", "💎")
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor
    ) {
        Column(modifier = Modifier.padding(20.dp).padding(bottom = 32.dp)) {
            Text("Chọn Avatar của bạn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(avatarIcons) { emoji ->
                    val isSelected = tempSelectedEmoji == emoji
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) primaryColor else primaryColor.copy(alpha = 0.1f))
                            .clickable { onEmojiSelect(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 32.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(16.dp),
                enabled = tempSelectedEmoji.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Lưu thay đổi", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
