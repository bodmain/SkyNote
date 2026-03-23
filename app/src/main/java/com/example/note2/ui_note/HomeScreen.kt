package com.example.note2.ui_note

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.note2.Screen
import com.example.note2.components_ui.DeleteConfirmDialog
import com.example.note2.components_ui.NoteFAB
import com.example.note2.model.NoteModel
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.components_ui.NoteItem
import com.example.note2.components_ui.NoteSearchBar
import com.example.note2.components_ui.NotesTopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onAddNote: () -> Unit,
    onEditNote: (NoteModel) -> Unit,
    onNoteClick: (Int) -> Unit,
    onNavigateToProfile: () -> Unit
) {

    Scaffold(
        topBar = {
            if (!viewModel.isSearchActive) {
                NotesTopBar(
                    onToggleSearch = { viewModel.toggleSearch() },
                    onCalendarClick = { /* Handle calendar click */ },
                    onNotificationClick = { /* Handle notification click */ },
                    onMenuClick = { onNavigateToProfile() }
                )
            }
        },
        floatingActionButton = {
            if (!viewModel.isSearchActive) {
                NoteFAB(
                    onAddNote = onAddNote,
                    onAddChecklist = { /* ... */ },
                    onAddPhoto = { /* ... */ }
                )
            }
        }
    ) { innerPadding ->

        viewModel.noteToDelete?.let {
            DeleteConfirmDialog(
                onConfirm = { viewModel.confirmDelete() },
                onDismiss = { viewModel.dismissDeleteDialog() }
            )
        }

        if (viewModel.isSearchActive) {
            NoteSearchBar(
                searchText = viewModel.searchText,
                onSearch = { viewModel.filterNotes(it) },
                searchResults = viewModel.searchResults,
                onClose = { viewModel.toggleSearch() },
                onNoteClick = {
                    viewModel.toggleSearch()
                    onEditNote(it)
                },
                onDeleteClick = { viewModel.showDeleteDialog(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(viewModel.notes) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onEditNote(note) },
                        onDelete = { viewModel.showDeleteDialog(it) }
                    )
                }
            }
        }
    }
}


