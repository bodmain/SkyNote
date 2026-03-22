package com.example.note2.components_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.note2.model.NoteModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSearchBar(
    searchText: String,
    onSearch: (String) -> Unit,
    searchResults: List<NoteModel>,
    onClose: () -> Unit,
    onNoteClick: (NoteModel) -> Unit,
    onDeleteClick: (NoteModel) -> Unit,
    modifier: Modifier = Modifier
) {
    SearchBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(if (searchText.isEmpty()) 16.dp else 0.dp),
        inputField = {
            SearchBarDefaults.InputField(
                query = searchText,
                onQueryChange = onSearch,
                onSearch = { /* Thường dùng để lưu lịch sử tìm kiếm */ },
                expanded = true,
                onExpandedChange = { if (!it) onClose() },
                placeholder = {
                    Text(
                        "Tìm ghi chú của bạn...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFF57C00)
                        )
                    }
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onSearch("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )
        },
        expanded = true,
        onExpandedChange = { if (!it) onClose() },
        colors = SearchBarDefaults.colors(
            containerColor = Color(0xFFFFF3E0),
        ),
        shape = RoundedCornerShape(if (searchText.isEmpty()) 28.dp else 0.dp)
    ) {
        if (searchResults.isEmpty() && searchText.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔍",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Không tìm thấy ghi chú nào",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(top = 8.dp)
            ) {
                items(searchResults) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onDelete = { onDeleteClick(note) }
                    )
                }
            }
        }
    }
}