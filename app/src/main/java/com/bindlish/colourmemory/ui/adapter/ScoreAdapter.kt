package com.bindlish.colourmemory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bindlish.colourmemory.data.Card
import com.bindlish.colourmemory.data.Score
import com.bindlish.colourmemory.databinding.ScoreItemLayoutBinding

class ScoreAdapter : RecyclerView.Adapter<ScoreAdapter.ScoreHolder>() {

    private val scores : MutableList<Score> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHolder {
        val binding = ScoreItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScoreHolder(binding)
    }

    override fun getItemCount(): Int {
        return scores.size
    }

    fun updateList(newScores: List<Score>) {
        scores.clear()
        scores.addAll(newScores)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ScoreHolder, position: Int) {
        holder.onBind(position)
    }

    inner class ScoreHolder(private val binding : ScoreItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        lateinit var score: Score

        private val rankVal: TextView = binding.rankVal

        fun onBind(position: Int) {
            score = scores[position]
            binding.viewModel = score
            rankVal.text = (position + 1).toString()
            binding.executePendingBindings()
        }
    }
}