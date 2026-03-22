package com.example.note2.ui_note

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            NotesTopBar(
                onToggleSearch = { viewModel.toggleSearch() },
                onProfileClick = onNavigateToProfile
            )
        },
        floatingActionButton = {
            NoteFAB(lazyListState = listState, onAddNote = onAddNote)
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
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(innerPadding)
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


