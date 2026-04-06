package com.example.note2.ui_Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.note2.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val existingNote = viewModel.notes.find { it.id == noteId }
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var description by remember { mutableStateOf(existingNote?.description ?: "") }
    var selectedColor by remember { mutableStateOf(existingNote?.color ?: 0xFFFFFFFF) }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        containerColor = Color(selectedColor),
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(selectedColor)
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (title.isNotBlank() || description.isNotBlank() || existingNote?.imagePath != null) {
                            if (noteId == "-1") {
                                viewModel.addNote(title, description, selectedColor)
                            } else {
                                existingNote?.let {
                                    viewModel.updateNote(it.copy(title = title, description = description, color = selectedColor))
                                }
                            }
                            onBack()
                        }
                    }) {
                        Text(
                            "Xong", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            existingNote?.imagePath?.let { path ->
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                    modifier = Modifier.fillMaxWidth(),
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
    }

    LaunchedEffect(Unit) {
        if (noteId == "-1") focusRequester.requestFocus()
    }
}
