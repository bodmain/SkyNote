package com.example.SkyNote.data.local

import androidx.room.TypeConverter
import com.example.SkyNote.data.model.ChecklistItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromChecklist(value: List<ChecklistItem>?): String {
        return gson.toJson(value ?: emptyList<ChecklistItem>())
    }

    @TypeConverter
    fun toChecklist(value: String?): List<ChecklistItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<ChecklistItem>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
