package com.bindlish.colourmemory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bindlish.colourmemory.data.Card
import com.bindlish.colourmemory.databinding.CardItemLayoutBinding
import com.bindlish.colourmemory.ui.main.MainViewModel
import com.bindlish.colourmemory.ui.main.OnCardClickedListener
import java.util.ArrayList

class GridAdapter(private val gameViewModel: MainViewModel) : RecyclerView.Adapter<GridAdapter.CardViewHolder>(),
    OnCardClickedListener {

    var isClickable: Boolean = true
    val cards: MutableList<Card>

    init {
        this.cards = ArrayList()
    }

    fun enableCardClick() {
        isClickable = true
    }

    fun disableCardClicks() {
        isClickable = false
    }

    override fun onCardClicked(card: Card) {
        if (isClickable) {
            gameViewModel.onCardClicked(card)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = CardItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    fun updateList(newCards: List<Card>) {
        val result: DiffUtil.DiffResult =
            DiffUtil.calculateDiff(CardDiffCallback(newCards, cards), true)

        cards.clear()
        cards.addAll(newCards)
        result.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.adapterPosition
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            when {adapterPosition != RecyclerView.NO_POSITION -> onCardClicked(cards[adapterPosition])
            }
        }
        holder.onBind(position)
    }

    override fun getItemCount(): Int = cards.size


    inner class CardViewHolder(private val binding: CardItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        lateinit var card: Card

        val frontCard: ImageView = binding.cardFrontImage
        val backCard: ImageView = binding.cardBackImage
        val cardLayout: ConstraintLayout = binding.cardLayout

        fun onBind(position: Int) {
            card = cards[position]
            binding.viewModel = card
            binding.executePendingBindings()
        }
    }
}