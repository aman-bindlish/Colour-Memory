package com.bindlish.colourmemory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bindlish.colourmemory.db.AppDatabase

@Entity(tableName = AppDatabase.SCORE_TABLE)
data class Score(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val userName : String,
    val score: Int
)