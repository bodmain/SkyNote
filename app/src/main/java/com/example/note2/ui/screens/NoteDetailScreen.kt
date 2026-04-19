package com.example.note2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.note2.data.model.ChecklistItem
import com.example.note2.data.model.NoteModel
import com.example.note2.ui.theme.NoteColors
import com.example.note2.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val existingNote = viewModel.notes.find { it.id == noteId }
    
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var description by remember { mutableStateOf(existingNote?.description ?: "") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var selectedColor by remember { mutableStateOf(existingNote?.color ?: 0xFFFFFFFF) }
    var reminderTime by remember { mutableStateOf(existingNote?.reminderTime) }
    var labels by remember { mutableStateOf(existingNote?.labels ?: emptyList<String>()) }
    var checklist by remember { mutableStateOf(existingNote?.checklist ?: emptyList<ChecklistItem>()) }
    var isChecklistEnabled by remember { mutableStateOf(checklist.isNotEmpty()) }

    val focusRequester = remember { FocusRequester() }
    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }
    
    var showDateTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    fun isNoteEmpty(): Boolean {
        val isTitleEmpty = title.isBlank()
        val isDescriptionEmpty = description.isBlank()
        val isChecklistEmpty = if (isChecklistEnabled) {
            checklist.none { it.text.isNotBlank() }
        } else {
            true
        }
        return isTitleEmpty && isDescriptionEmpty && isChecklistEmpty
    }

    val onSave = {
        if (!isNoteEmpty()) {
            val note = (existingNote ?: NoteModel(id = if(noteId == "-1") UUID.randomUUID().toString() else noteId)).copy(
                title = title,
                description = description,
                isPinned = isPinned,
                color = selectedColor,
                reminderTime = reminderTime,
                labels = labels,
                checklist = if (isChecklistEnabled) checklist else emptyList(),
                timestamp = System.currentTimeMillis()
            )
            viewModel.saveNote(context, note)
        }
    }

    Scaffold(
        containerColor = Color(selectedColor),
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { 
                        onSave()
                        onBack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            onSave()
                            onBack()
                        }
                    ) {
                        Text(
                            "Lưu",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Color")
                    }
                    IconButton(onClick = { 
                        if (reminderTime == null) {
                            showDateTimePicker = true
                        } else {
                            reminderTime = null
                        }
                    }) {
                        Icon(
                            if (reminderTime != null) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone, 
                            contentDescription = "Reminder",
                            tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { showLabelDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Labels")
                    }
                    IconButton(onClick = { 
                        isChecklistEnabled = !isChecklistEnabled
                        if (isChecklistEnabled && checklist.isEmpty()) {
                            checklist = listOf(ChecklistItem(text = ""))
                        }
                    }) {
                        Icon(
                            if (isChecklistEnabled) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Checklist"
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (labels.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(labels) { label ->
                        InputChip(
                            selected = true,
                            onClick = { labels = labels - label },
                            label = { Text(label, fontSize = 12.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            reminderTime?.let { time ->
                SuggestionChip(
                    onClick = { showDateTimePicker = true },
                    label = { 
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(time)))
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f)
                ),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) Text("Tiêu đề", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isChecklistEnabled) {
                checklist.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { checked ->
                                checklist = checklist.toMutableList().apply {
                                    this[index] = item.copy(isChecked = checked)
                                }
                            }
                        )
                        BasicTextField(
                            value = item.text,
                            onValueChange = { newText ->
                                checklist = checklist.toMutableList().apply {
                                    this[index] = item.copy(text = newText)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = Color.Black.copy(alpha = if (item.isChecked) 0.5f else 0.8f)
                            )
                        )
                        IconButton(onClick = { 
                            checklist = checklist.toMutableList().apply { removeAt(index) }
                            if (checklist.isEmpty()) isChecklistEnabled = false
                        }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                TextButton(onClick = { checklist = checklist + ChecklistItem(text = "") }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm mục")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            BasicTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black.copy(alpha = 0.7f)),
                decorationBox = { innerTextField ->
                    if (description.isEmpty()) {
                        Text(
                            text = if (isChecklistEnabled) "Thêm ghi chú khác..." else "Ghi chú",
                            fontSize = 18.sp, 
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            )
        }
    }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Chọn màu") },
            text = {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NoteColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color.toArgb().toLong()) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color.toArgb().toLong(); showColorPicker = false }
                        )
                    }
                }
            },
            confirmButton = { }
        )
    }

    if (showLabelDialog) {
        var newLabel by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLabelDialog = false },
            title = { Text("Thêm nhãn") },
            text = {
                TextField(
                    value = newLabel, 
                    onValueChange = { newLabel = it }, 
                    placeholder = { Text("Tên nhãn") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { 
                    if (newLabel.isNotBlank() && !labels.contains(newLabel)) {
                        labels = labels + newLabel
                    }
                    showLabelDialog = false 
                }) { Text("Thêm") }
            },
            dismissButton = {
                TextButton(onClick = { showLabelDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showDateTimePicker) {
        var showTimePicker by remember { mutableStateOf(false) }

        if (!showTimePicker) {
            DatePickerDialog(
                onDismissRequest = { showDateTimePicker = false },
                confirmButton = {
                    TextButton(onClick = { showTimePicker = true }) { Text("Tiếp tục") }
                },
                dismissButton = {
                    TextButton(onClick = { showDateTimePicker = false }) { Text("Hủy") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        } else {
            AlertDialog(
                onDismissRequest = { showDateTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val calendar = Calendar.getInstance()
                        datePickerState.selectedDateMillis?.let { calendar.timeInMillis = it }
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        
                        reminderTime = calendar.timeInMillis
                        showDateTimePicker = false
                    }) { Text("Xong") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Quay lại") }
                },
                title = { Text("Chọn giờ") },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (noteId == "-1") focusRequester.requestFocus()
    }
}
