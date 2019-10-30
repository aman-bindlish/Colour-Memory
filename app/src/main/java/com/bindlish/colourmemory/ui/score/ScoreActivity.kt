package com.bindlish.colourmemory.ui.score

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bindlish.colourmemory.R
import com.bindlish.colourmemory.data.Score
import com.bindlish.colourmemory.databinding.MainFragmentBinding
import com.bindlish.colourmemory.databinding.ScoreActivityBinding
import com.bindlish.colourmemory.ui.adapter.ScoreAdapter
import com.bindlish.colourmemory.ui.main.MainViewModel
import com.bindlish.colourmemory.ui.main.ViewModelFactory

class ScoreActivity : AppCompatActivity() {

    private lateinit var viewModel: ScoreViewModel
    private lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewBinding: ScoreActivityBinding

    private var scoreAdapter : ScoreAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModelFactory = ViewModelFactory(this.application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ScoreViewModel::class.java)

        viewBinding = DataBindingUtil.setContentView(this, R.layout.score_activity)

        viewBinding.scoreRecycler.apply {
            scoreAdapter = ScoreAdapter()
            layoutManager = LinearLayoutManager(this@ScoreActivity)
            adapter = scoreAdapter
        }
        observeData()
    }

    private fun observeData() {
        viewModel.getScores().observe(this, Observer { scores ->
            scoreAdapter?.updateList(scores)
        })
    }
}