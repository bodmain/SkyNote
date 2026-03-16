package com.example.note2.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.note2.data.NoteDao
import com.example.note2.model.NoteModel

@Database(entities = [NoteModel::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
