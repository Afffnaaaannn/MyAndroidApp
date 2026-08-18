package com.example.mytodoapplication
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val isCompleted: Boolean = false
)