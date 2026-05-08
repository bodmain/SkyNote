package com.example.SkyNote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.SkyNote.navigation.Screen
import com.example.SkyNote.ui.components.*
import com.example.SkyNote.ui.dialogs.DeleteConfirmDialog
import com.example.SkyNote.viewmodel.AuthViewModel
import com.example.SkyNote.viewmodel.NoteViewModel
import com.example.SkyNote.viewmodel.ThemeViewModel

private const val FILTER_ALL = "Tất cả"
private const val FILTER_REMINDER = "Lời nhắc"

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onAddNote: () -> Unit,
    onEditNote: (com.example.SkyNote.data.model.NoteModel) -> Unit,
    onNoteClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyStaggeredGridState()
    
    val notes = viewModel.notes
    val currentUser by authViewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FILTER_ALL) }

    val dynamicLabels = remember(notes) {
        notes.flatMap { it.labels }.distinct().sorted()
    }

    val allFilters = remember(dynamicLabels) {
        listOf(FILTER_ALL, FILTER_REMINDER) + dynamicLabels
    }

    LaunchedEffect(currentUser) {
        // FIX: startObservingData không nhận tham số
        viewModel.startObservingData()
    }

    val filteredNotes = remember(notes, searchQuery, selectedFilter) {
        notes
            .filter {
                when (selectedFilter) {
                    FILTER_ALL -> true
                    FILTER_REMINDER -> it.reminderTime != null
                    else -> it.labels.contains(selectedFilter)
                }
            }
            .filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(compareByDescending<com.example.SkyNote.data.model.NoteModel> { it.isPinned }.thenByDescending { it.timestamp })
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onNotificationClick = { navController.navigate(Screen.Notification.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        },
        floatingActionButton = {
            NoteFAB(onAddNote = onAddNote)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterSection(
                filters = allFilters,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            
            if (notes.isEmpty()) {
                EmptyStateComponent(
                    icon = Icons.Default.NoteAdd,
                    title = "Chưa có ghi chú nào",
                    description = "Hãy bắt đầu ghi lại những ý tưởng tuyệt vời của bạn ngay bây giờ ✨"
                )
            } else {
                NoteGrid(
                    notes = filteredNotes,
                    gridState = gridState,
                    onClick = onEditNote,
                    onDelete = { viewModel.showDeleteDialog(it) }
                )
            }
        }

        viewModel.noteToDelete?.let {
            DeleteConfirmDialog(
                onConfirm = { viewModel.confirmDelete(context) },
                onDismiss = { viewModel.dismissDeleteDialog() }
            )
        }
    }
}
