package com.example.note2

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(noteModel: NoteModel)
    @Update
    suspend fun update(noteModel: NoteModel )
    @Delete
    suspend fun delete(noteModel: NoteModel)

    @Query("SELECT * FROM NoteModel")
    suspend fun getAllNotes():  List<NoteModel>

}

