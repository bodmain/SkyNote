package com.example.note2

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: NoteViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedNote = remember { mutableStateOf<NoteModel?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Notes") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary ))},
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(viewModel.notes) { note ->
                NoteItem(
                    note = note, onClick = { clickedNote ->
                        selectedNote.value = clickedNote
                    },
                    title = note.title, content = note.description
                )
            }

        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Thêm ghi chú") },
            text = {
                Column {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tiêu đề") })
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Nội dung") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addNote(title, description)
                    title = ""
                    description = ""
                    showDialog = false
                }) { Text("Lưu") }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Hủy") }
            }
        )
    }
    if (selectedNote.value != null) {
        LaunchedEffect(selectedNote.value) {
            editTitle = selectedNote.value!!.title
            editDescription = selectedNote.value!!.description
        }

        AlertDialog(
           onDismissRequest = { selectedNote.value = null },
            title = { Text("Sửa ghi chú") },
            text = {
                Column {
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Tiêu đề") })
                    TextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Nội dung") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updatedNote = selectedNote.value!!.copy(
                        title = editTitle,
                        description = editDescription
                    )
                    viewModel.updateNote(updatedNote)
                    selectedNote.value = null
                }) { Text("Lưu") }
            },
            dismissButton = {
                Button(onClick = { selectedNote.value = null }) { Text("Đóng") }
            }

        )
    }
}

@Composable
fun NoteItem(
    note: NoteModel,
    onClick: (NoteModel) -> Unit,
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(note) }
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = content, style = MaterialTheme.typography.bodyMedium)
    }
}




