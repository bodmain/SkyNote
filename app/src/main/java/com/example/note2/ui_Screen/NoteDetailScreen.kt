package com.example.note2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val focusRequester = remember { FocusRequester() }

    var selectedColor by remember { mutableStateOf(existingNote?.color ?: 0xFFFFFFFF) }

    Scaffold(
        containerColor = Color(selectedColor),
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (title.isNotBlank() || description.isNotBlank()) {
                            if (noteId == -1) {
                                viewModel.addNote(title, description, selectedColor)
                            } else {
                                existingNote?.let {
                                    viewModel.updateNote(it.copy(title = title, description = description, color = selectedColor))
                                }
                            }
                            onBack()
                        }
                    }) {
                        Text("Xong", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
//        bottomBar = {
//            BottomAppBar(
//                containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f),
//                contentPadding = PaddingValues(horizontal = 8.dp),
//                actions = {
//                    IconButton(onClick = {  }) {
//
//                        Icon(Icons.Default.Palette, contentDescription = "Chọn màu")
//                    }
//                    IconButton(onClick = { /* */ }) {
//                        Icon(Icons.Default.Image, contentDescription = "Thêm ảnh")
//                    }
//                    IconButton(onClick = { /*  */ }) {
//                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Danh sách")
//                    }
//
//                    Spacer(modifier = Modifier.weight(1f))
//                    Text(
//                        text = "18 tháng 3 | ${description.length} ký tự",
//                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier.padding(end = 16.dp)
//                    )
//                }
//            )
//        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text("Tiêu đề",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Bắt đầu viết nội dung của bạn...") },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (noteId == -1) focusRequester.requestFocus()
    }
}