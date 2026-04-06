package com.example.note2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.note2.model.NoteModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotesByUser(userId: String): Flow<List<NoteModel>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteModel?

    @Query("SELECT * FROM notes WHERE userId = :userId")
    suspend fun getNotesByUserList(userId: String): List<NoteModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteModel: NoteModel)

    @Update
    suspend fun update(noteModel: NoteModel)

    @Delete
    suspend fun delete(noteModel: NoteModel)

    @Query("UPDATE notes SET userId = :newUserId WHERE userId = 'guest'")
    suspend fun migrateGuestNotes(newUserId: String)
}
