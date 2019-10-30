package com.bindlish.colourmemory.ui.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.bindlish.colourmemory.R
import com.bindlish.colourmemory.utils.ViewUtils

class GridItemAnimator : DefaultItemAnimator() {

    private var cardFlipAnimationMap: MutableMap<RecyclerView.ViewHolder, AnimatorSet> = HashMap()

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun recordPreLayoutInformation(
            state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder,
            changeFlags: Int, payloads: List<Any>): RecyclerView.ItemAnimator.ItemHolderInfo {

        if (changeFlags == RecyclerView.ItemAnimator.FLAG_CHANGED) {
            return CardItemHolderInfo()
        }

        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
    }


    private fun animateFlipRevealCardFront(holder: GridAdapter.CardViewHolder) {
        val animatorSet = AnimatorSet()

        val startSpin = ObjectAnimator.ofFloat(holder.cardLayout, ROTATION_Y, 0.0f, 90f)
        startSpin.duration = 300
        startSpin.interpolator = ACCELERATE_INTERPOLATOR

        val rotateImageLeft = ObjectAnimator.ofFloat(holder.frontCard, ROTATION_Y, 90f, 180f)
        rotateImageLeft.duration = IMMEDIATE

        val finishSpin = ObjectAnimator.ofFloat(holder.cardLayout, ROTATION_Y, 90f, 180f)
        finishSpin.duration = 300
        finishSpin.interpolator = DECCELERATE_INTERPOLATOR

        startSpin.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                when (holder.card.isFlipped) {
                    true -> {
                        holder.frontCard.visibility = View.VISIBLE
                        holder.backCard.visibility = View.GONE
                        holder.frontCard.background = holder.frontCard.context.getDrawable(holder.card.resId)
                    }
                    else -> {
                        holder.backCard.background = holder.backCard.context.getDrawable(R.drawable.card_bg)
                        holder.frontCard.visibility = View.GONE
                        holder.backCard.visibility = View.VISIBLE

                    }
                }
            }
        })

        finishSpin.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                cardFlipAnimationMap.remove(holder)
                dispatchChangeFinishedIfAllAnimationsEnded(holder)
            }
        })

        animatorSet.play(finishSpin).with(rotateImageLeft).after(startSpin)
        animatorSet.start()
        cardFlipAnimationMap[holder] = animatorSet
    }

    private fun animateFlipRevealCardBack(holder: GridAdapter.CardViewHolder) {
        val animatorSet = AnimatorSet()

        val startSpin = ObjectAnimator.ofFloat(holder.cardLayout, ROTATION_Y, 180f, 90f)
        startSpin.duration = SPIN_DURATION
        startSpin.interpolator = ACCELERATE_INTERPOLATOR

        val finishSpin = ObjectAnimator.ofFloat(holder.cardLayout, ROTATION_Y, 90f, 0.0f)
        finishSpin.duration = SPIN_DURATION
        finishSpin.interpolator = DECCELERATE_INTERPOLATOR

        startSpin.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                when (holder.card.isFlipped) {
                    true -> {
                        holder.frontCard.visibility = View.VISIBLE
                        holder.backCard.visibility = View.GONE
                        holder.frontCard.background = holder.frontCard.context.getDrawable(holder.card.resId)
                    }
                    else -> {
                        holder.backCard.background = holder.backCard.context.getDrawable(R.drawable.card_bg)
                        holder.frontCard.visibility = View.GONE
                        holder.backCard.visibility = View.VISIBLE
                    }
                }
            }
        })

        finishSpin.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                cardFlipAnimationMap.remove(holder)
                dispatchChangeFinishedIfAllAnimationsEnded(holder)
            }
        })

        animatorSet.play(finishSpin).after(startSpin)

        animatorSet.start()
        cardFlipAnimationMap[holder] = animatorSet
    }

    override fun animateAdd(viewHolder: RecyclerView.ViewHolder): Boolean {
        runEnterAnimation(viewHolder as GridAdapter.CardViewHolder, viewHolder.layoutPosition)
        return true
    }


    private fun runEnterAnimation(holder: GridAdapter.CardViewHolder, layoutPosition: Int) {
        val screenHeight = ViewUtils.getScreenHeight(holder.itemView.context)
        holder.itemView.translationY = screenHeight.toFloat()

        val START_DELAY = (layoutPosition * 20).toLong()
        holder.itemView.animate()
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator(3f))
                .setDuration(ENTER_DURATION)
                .setStartDelay(START_DELAY)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        dispatchAddFinished(holder)
                    }
                })
                .start()
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder,
                               newHolder: RecyclerView.ViewHolder,
                               preInfo: RecyclerView.ItemAnimator.ItemHolderInfo,
                               postInfo: RecyclerView.ItemAnimator.ItemHolderInfo): Boolean {
        cancelCurrentAnimationIfExists(newHolder)

        if (preInfo is CardItemHolderInfo) {
            val holder = newHolder as GridAdapter.CardViewHolder

            when (holder.card.isFlipped) {
                true -> animateFlipRevealCardFront(holder)
                false -> animateFlipRevealCardBack(holder)
            }
        }

        return false
    }

    private fun cancelCurrentAnimationIfExists(item: RecyclerView.ViewHolder) {
        if (cardFlipAnimationMap.containsKey(item)) {
            cardFlipAnimationMap[item]?.cancel()
        }
    }

    private fun dispatchChangeFinishedIfAllAnimationsEnded(holder: GridAdapter.CardViewHolder) {
        if (cardFlipAnimationMap.containsKey(holder)) {
            return
        }

        dispatchAnimationFinished(holder)
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        super.endAnimation(item)
        cancelCurrentAnimationIfExists(item)
    }

    companion object {
        private val DECCELERATE_INTERPOLATOR = DecelerateInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(4f)

        private val ROTATION_Y = "rotationY"

        private val IMMEDIATE: Long = 0
        private val SPIN_DURATION: Long = 300
        private val ROTATION_DURATION: Long = 300
        private val BOUNCE_DURATION: Long = 400
        val ENTER_DURATION: Long = 700
    }

    class CardItemHolderInfo : RecyclerView.ItemAnimator.ItemHolderInfo()

}