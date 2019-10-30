package com.bindlish.colourmemory.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bindlish.colourmemory.data.Card
import com.bindlish.colourmemory.data.Score
import io.reactivex.Single

@Dao
abstract class CardDao {

    @Query("SELECT * FROM ${AppDatabase.CARD_TABLE} ORDER BY id")
    abstract fun getAllCards(): LiveData<List<Card>>

    @Query("SELECT * FROM ${AppDatabase.CARD_TABLE} WHERE id = :cardId")
    abstract fun getCardById(cardId: String): Single<Card>

    @Query("UPDATE ${AppDatabase.CARD_TABLE} SET isFlipped = :isFlipped WHERE id = :cardId")
    abstract fun updateCardFlip(cardId: String, isFlipped: Boolean)

    @Query("UPDATE ${AppDatabase.CARD_TABLE} SET isMatched = :isMatched WHERE id = :cardId")
    abstract fun updateCardMatch(cardId: String, isMatched: Boolean)

    @Query("DELETE FROM ${AppDatabase.CARD_TABLE}")
    abstract fun deleteTable()

    @Delete
    abstract fun delete(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(cards: List<Card>)

    @Transaction
    open fun repopulateTable(cards: List<Card>) {
        deleteTable()
        insertAll(cards)
    }

    @Transaction
    open fun insertAndReturn(cards: List<Card>) : LiveData<List<Card>> {
        insertAll(cards)
        return getAllCards()
    }

    @Insert
    abstract fun insertScore(score : Score)

    @Query("SELECT * FROM ${AppDatabase.SCORE_TABLE} ORDER BY score DESC limit 1")
    abstract fun getHighestScore() : LiveData<Score>

    @Query("SELECT * FROM ${AppDatabase.SCORE_TABLE} ORDER BY score DESC")
    abstract fun getAllScores() : LiveData<List<Score>>

}