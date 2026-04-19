package com.example.note2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.note2.ui.components.EmptyStateComponent
import com.example.note2.ui.components.NoteItem
import com.example.note2.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var noteToProcess by remember { mutableStateOf<com.example.note2.data.model.NoteModel?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thùng rác") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.deletedNotes.isNotEmpty()) {
                        IconButton(onClick = { showEmptyTrashDialog = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.deletedNotes.isEmpty()) {
            EmptyStateComponent(
                icon = Icons.Default.DeleteSweep,
                title = "Thùng rác trống không",
                description = "Ghi chú bị xóa sẽ tạm nghỉ chân tại đây trước khi biến mất mãi mãi 🗑️",
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        "Ghi chú trong Thùng rác sẽ bị xóa vĩnh viễn sau 30 ngày.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    items(viewModel.deletedNotes) { note ->
                        NoteItem(
                            note = note,
                            onClick = { noteToProcess = note },
                            onDelete = { viewModel.permanentlyDeleteNote(context, note) }
                        )
                    }
                }
            }
        }

        if (showEmptyTrashDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashDialog = false },
                title = { Text("Dọn sạch thùng rác?") },
                text = { Text("Tất cả ghi chú trong thùng rác sẽ bị xóa vĩnh viễn và không thể khôi phục.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.emptyTrash(context)
                        showEmptyTrashDialog = false
                    }) {
                        Text("Dọn sạch", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmptyTrashDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        noteToProcess?.let { note ->
            AlertDialog(
                onDismissRequest = { noteToProcess = null },
                title = { Text("Tùy chọn ghi chú") },
                text = { Text("Bạn muốn làm gì với ghi chú này?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.restoreFromTrash(note)
                        noteToProcess = null
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Khôi phục")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.permanentlyDeleteNote(context, note)
                        noteToProcess = null
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xóa vĩnh viễn", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    }
}
