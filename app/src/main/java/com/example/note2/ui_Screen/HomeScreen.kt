package com.example.note2.ui_Screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onNoteClick: (Int) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


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
        viewModel.startObservingNotes()
        viewModel.startObservingNotifications()
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
                    NotesTopBar(
                        onToggleSearch = { viewModel.toggleSearch() },
                        onCalendarClick = { /* Handle calendar click */ },
                        onNotificationClick = { navController.navigate(Screen.Notification.route) },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
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
                if (viewModel.notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Chưa có ghi chú nào")
                    }
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
    }
}
