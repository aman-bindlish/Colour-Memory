package com.bindlish.colourmemory.ui.score

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.bindlish.colourmemory.data.Score
import com.bindlish.colourmemory.db.AppDatabase
import com.bindlish.colourmemory.db.CardDao

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val cardDao : CardDao

    init {
        cardDao = AppDatabase.getDatabase(application).cardDao()
    }

    fun getScores() : LiveData<List<Score>> = cardDao.getAllScores()
}