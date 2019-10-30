package com.bindlish.colourmemory.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.bindlish.colourmemory.data.Card


class CardDiffCallback(private var newCards: List<Card>, private var oldCards: List<Card>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldCards.size

    override fun getNewListSize(): Int = newCards.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldCards[oldItemPosition].id == newCards[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldCards[oldItemPosition].isFlipped == newCards[newItemPosition].isFlipped
    }

}