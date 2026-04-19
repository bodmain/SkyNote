package com.example.note2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.example.note2.ui.components.EmptyStateComponent
import com.example.note2.ui.components.NoteGrid
import com.example.note2.ui.components.SearchBar
import com.example.note2.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    onNoteClick: (com.example.note2.data.model.NoteModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val notes = viewModel.notes
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyStaggeredGridState()

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            notes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (searchQuery.isEmpty()) {
                EmptyStateComponent(
                    icon = Icons.Default.Search,
                    title = "Tìm kiếm ghi chú",
                    description = "Nhập từ khóa bất kỳ để tìm nhanh các ghi chú bạn đã lưu "
                )
            } else if (filteredNotes.isEmpty()) {
                EmptyStateComponent(
                    icon = Icons.Default.SearchOff,
                    title = "Không tìm thấy kết quả",
                    description = "Thử tìm với từ khóa khác hoặc kiểm tra lại chính tả xem sao?"
                )
            } else {
                NoteGrid(
                    notes = filteredNotes,
                    gridState = gridState,
                    onClick = onNoteClick,
                    onDelete = { viewModel.showDeleteDialog(it) }
                )
            }
        }
    }
}
