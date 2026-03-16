package com.example.note2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.note2.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val existingNote = viewModel.notes.find { it.id == noteId }
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var description by remember { mutableStateOf(existingNote?.description ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "Ghi chú mới" else "Chỉnh sửa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Nút lưu nhanh
                    IconButton(onClick = {
                        if (noteId == -1) {
                            viewModel.addNote(title, description)
                        } else {
                            existingNote?.let {
                                viewModel.updateNote(it.copy(title = title, description = description))
                            }
                        }
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Lưu", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Ô nhập tiêu đề
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Tiêu đề", style = MaterialTheme.typography.headlineSmall) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.headlineSmall
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Bắt đầu viết...") },
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}