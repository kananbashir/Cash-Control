package com.example.cashcontrol.util.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList (stringList: MutableList<String>): String {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList (value: String): MutableList<String> {
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringSet (stringSet: MutableSet<String>): String {
        return gson.toJson(stringSet)
    }

    @TypeConverter
    fun toStringSet (value: String): MutableSet<String> {
        val listType = object : TypeToken<MutableSet<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}