package com.example.note2.ui_Screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.note2.Screen
import com.example.note2.auth.AuthViewModel
import com.example.note2.components_ui.DeleteConfirmDialog
import com.example.note2.components_ui.NoteFAB
import com.example.note2.model.NoteModel
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.components_ui.NoteItem
import com.example.note2.components_ui.NoteSearchBar
import com.example.note2.components_ui.NotesTopBar
import com.example.note2.components_ui.HomeDrawerContent
import com.example.note2.data.AppDatabase
import com.example.note2.utils.FileHelper
import com.example.note2.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onAddNote: () -> Unit,
    onEditNote: (NoteModel) -> Unit,
    onNoteClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val tabs = listOf("Tất cả", "Có ảnh", "Lời nhắc")
    val pagerState = rememberPagerState(pageCount = { tabs.size })


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = FileHelper.saveImageToInternalStorage(context, it)
            if (path != null) {
                viewModel.addNote(
                    title = "Ghi chú ảnh", 
                    description = "", 
                    imagePath = path
                )
            }
        }
    }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(currentUser) {
        viewModel.startObservingData(currentUser?.uid ?: NoteViewModel.GUEST_USER_ID)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                currentUser = currentUser,
                syncState = viewModel.syncState,
                onLoginClick = { 
                    val db = AppDatabase.getDatabase(context)
                    authViewModel.signInWithGoogle(context, db.noteDao()) 
                },
                onAllNotesClick = { scope.launch { drawerState.close() } },
                onSyncClick = { 
                    viewModel.syncAllNotes() 
                },
                onSettingsClick = { onNavigateToProfile() },
                onCloseDrawer = { scope.launch { drawerState.close() } },
                themeViewModel = themeViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (!viewModel.isSearchActive) {
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        NotesTopBar(
                            onToggleSearch = { viewModel.toggleSearch() },
                            onCalendarClick = { /* Handle calendar click */ },
                            onNotificationClick = { navController.navigate(Screen.Notification.route) },
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                        
                        PrimaryTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = Color.Transparent,
                            indicator = { TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                width = 32.dp,
                                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                color = MaterialTheme.colorScheme.primary
                            )},
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { 
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { 
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    }
                                )
                            }
                        }
                        
                        // Đường kẻ ngang mờ ở dưới tab
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            },
            floatingActionButton = {
                if (!viewModel.isSearchActive) {
                    NoteFAB(
                        onAddNote = onAddNote,
                        onAddChecklist = { /* ... */ },
                        onAddPhoto = { 
                            photoPickerLauncher.launch("image/*")
                        }
                    )
                }
            }
        ) { innerPadding ->
            
            if (authUiState.errorMessage != null) {
                // Hiển thị thông báo lỗi
            }

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
                val notifications by viewModel.notifications.collectAsState()
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) { pageIndex ->
                    val filteredNotes = when (pageIndex) {
                        0 -> viewModel.notes
                        1 -> viewModel.notes.filter { it.imagePath != null }
                        2 -> {
                            val noteIdsWithReminders = notifications.map { it.noteId }.toSet()
                            viewModel.notes.filter { noteIdsWithReminders.contains(it.id) }
                        }
                        else -> viewModel.notes
                    }

                    if (filteredNotes.isEmpty()) {
                        val message = when (pageIndex) {
                            0 -> "Chưa có ghi chú nào. Hãy bắt đầu viết gì đó!"
                            1 -> "Không tìm thấy ghi chú có hình ảnh."
                            2 -> "Bạn không có lời nhắc nào sắp tới."
                            else -> ""
                        }
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp
                        ) {
                            items(filteredNotes) { note ->
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
        }
    }
}
