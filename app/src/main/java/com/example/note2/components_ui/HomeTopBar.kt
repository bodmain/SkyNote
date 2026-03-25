package com.example.note2.components_ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.note2.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(
    onToggleSearch: () -> Unit,
    onNotificationClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        title = {
            Text(
                text = "SkyNote",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )
        },
        actions = {
            //  Tìm kiếm
            IconButton(onClick = onToggleSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            //  Lịch
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
            }

            // Thông báo
            IconButton(
                onClick = onNotificationClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}