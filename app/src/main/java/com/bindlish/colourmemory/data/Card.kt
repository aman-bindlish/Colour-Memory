package com.bindlish.colourmemory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bindlish.colourmemory.db.AppDatabase
import java.util.*

@Entity(tableName = AppDatabase.CARD_TABLE)
data class Card(
    @PrimaryKey
    val id: Long = UUID.randomUUID().leastSignificantBits,
    val resId : Int,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,
    val isSelected: Boolean = false) {

    fun matchCard() = Card(id, resId, isFlipped = true, isSelected = false, isMatched = true)

    fun selectCard() = Card(id, resId, isFlipped = true, isSelected = true)

    fun resetCard() = Card(id, resId, isFlipped = false, isSelected = false)
}