package com.bindlish.colourmemory.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bindlish.colourmemory.R
import com.bindlish.colourmemory.data.GameState.*
import com.bindlish.colourmemory.databinding.MainFragmentBinding
import com.bindlish.colourmemory.ui.adapter.GridAdapter
import com.bindlish.colourmemory.ui.adapter.GridItemAnimator
import com.bindlish.colourmemory.ui.adapter.SpacingGridLayoutManager
import com.bindlish.colourmemory.ui.score.ScoreActivity

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewBinding: MainFragmentBinding

    private lateinit var rootView: View

    private var gridAdapter: GridAdapter? = null
    private var userDialog : AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        rootView = viewBinding.root
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelFactory = ViewModelFactory(activity!!.application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        viewBinding.executePendingBindings()
        setUpRecyclerView()
        observeLiveData()
        viewBinding.highScoreTxt.setOnClickListener {
            showScoreScreen()
        }
    }

    private fun observeLiveData() {
        observeGameState()
        observeGameBoard()
        // observing and updating live score value
        viewModel.getLiveScore().observe(this, Observer { score ->
            score?.let {
                viewBinding.liveScoreVal.text = score.toString()
            }
        })
        viewModel.getHighestScore().observe(this, Observer { score ->
            score?.let {
                viewBinding.highScoreVal.text = score.score.toString()
            }
        })
        viewModel.getSaveScoreStatus().observe(this, Observer {
            if (it){
                userDialog?.dismiss()
                userDialog = null
                Toast.makeText(activity, "Your score is ${viewBinding.liveScoreVal.text}", Toast.LENGTH_SHORT).show()
                showScoreScreen()
            } else {
                Toast.makeText(activity, "Please enter name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun observeGameState() {
        viewModel.getGameState()
            .observe(this, Observer { gameState ->
                when (gameState) {
                    LOADING -> {
                        setUpRecyclerView()
                    }
                    IN_PROGRESS -> gridAdapter?.enableCardClick()
                    RESETTING_CARDS -> gridAdapter?.disableCardClicks()
                    GAME_COMPLETE -> {
                        gridAdapter?.disableCardClicks()
                        showUserNameDialog()
                    }
                }
            })
    }

    private fun showUserNameDialog() {
        val dialog = AlertDialog.Builder(this.context)
        dialog.setTitle("Save Result")
        dialog.setCancelable(false)
        val view = layoutInflater.inflate(R.layout.user_name_layout, null)
        val userField = view.findViewById(R.id.user_name) as EditText
        val button = view.findViewById<Button>(R.id.save_btn)
        dialog.setView(view)
        userDialog = dialog.show();
        button.setOnClickListener {
            viewModel.saveResults(userField.text.toString())
        }
    }

    private fun showScoreScreen() {
        startActivity(Intent(activity, ScoreActivity::class.java))
    }

    private fun observeGameBoard() {
        viewModel.observeGameBoard().observe(this, Observer { cards ->
            cards?.let {
                gridAdapter?.updateList(cards)
            }
        })
    }

    private fun setUpRecyclerView() {
        activity?.let {
            gridAdapter = GridAdapter(viewModel)
            with(viewBinding.recyclerView) {
                itemAnimator = GridItemAnimator()
                layoutManager = SpacingGridLayoutManager(it, 4, android.widget.GridLayout.VERTICAL, false)
                adapter = gridAdapter
            }
        }
    }

}
