package com.bindlish.colourmemory.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bindlish.colourmemory.ui.score.ScoreViewModel

class ViewModelFactory constructor(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application) as T
        } else if (modelClass.isAssignableFrom(ScoreViewModel::class.java)){
            return ScoreViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}