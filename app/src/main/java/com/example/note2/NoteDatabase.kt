package com.example.note2

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NoteModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
