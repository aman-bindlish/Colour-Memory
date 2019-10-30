package com.bindlish.colourmemory.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.bindlish.colourmemory.R
import com.bindlish.colourmemory.data.Card
import com.bindlish.colourmemory.data.GameState
import com.bindlish.colourmemory.data.Score
import com.bindlish.colourmemory.db.AppDatabase
import com.bindlish.colourmemory.db.CardDao
import com.bindlish.colourmemory.utils.executeInThread
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()
    private val flippedCards = HashMap<String, Card>()
    private val matchedCards = HashMap<String, Card>()
    private var firstCardSelected: Card? = null

    private val liveScore = MutableLiveData<Int>()
    private var score = 0
    private var gameState = MutableLiveData<GameState>()
    private val saveScore = MutableLiveData<Boolean>()
    private val saveStatus = Transformations.map(saveScore, {saveScore.value?:false})

    private val cardDao : CardDao

    companion object {
        private const val DELAY: Long = 1000
        private const val DECK_SIZE = 16
    }

    init {
        cardDao = AppDatabase.getDatabase(application).cardDao()
        startGame()
    }

    private fun startGame() {
        flippedCards.clear()
        matchedCards.clear()
        score = 0
        executeInThread {
            cardDao.insertAll(populateCards())
        }
    }

    fun onCardClicked(card: Card) {
        if (isNotSelected(card)) {
            selectCard(card)
        }
    }

    private fun isNotSelected(card: Card): Boolean {
        return !card.isSelected && !card.isMatched
    }

    private fun selectCard(card: Card) {
        when (flippedCards.isEmpty()) {
            true -> {
                disposables.add(flipFirstCard(card))
            }
            false -> {
                updateState(GameState.RESETTING_CARDS)
                disposables.add(
                    Observable.just(card.selectCard())
                        .subscribeOn(Schedulers.io())
                        .compose(addToFlippedCardMap())
                        .compose(isValidMatch())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(updateScore())
                        .subscribeOn(Schedulers.io())
                        .compose(markAsMatched())
                        .switchMap {
                            Observable.timer(DELAY, TimeUnit.MILLISECONDS)
                        }.compose(getUnmatchedCards())
                        .compose(resetUnmatchedCards())
                        .compose(clearFlippedCardMap())
                        .compose(clearSelectedCard())
                        .compose(isGameOver())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(enableCardClicks())
                        .subscribe())
            }
        }
    }

    private fun markAsMatched(): ObservableTransformer<Boolean, HashMap<String, Card>> {
        return ObservableTransformer {
            it.concatMap { isMatch ->
                if (isMatch) {
                    return@concatMap Observable.fromCallable {
                        flippedCards.values.forEach {
                            val matchedCard = it.matchCard()
                            flippedCards[matchedCard.id.toString()] = matchedCard
                            matchedCards[matchedCard.id.toString()] = matchedCard
                            executeInThread {
                                cardDao.insert(matchedCard)
                            }
                        }
                        return@fromCallable flippedCards
                    }
                } else {
                    return@concatMap Observable.just(flippedCards)
                }
            }
        }
    }

    private fun getUnmatchedCards(): ObservableTransformer<Long, List<Card>> {
        return ObservableTransformer {
            it.flatMap {
                Observable.fromCallable {
                    flippedCards.values.filter { !it.isMatched }
                }
            }
        }
    }

    private fun resetUnmatchedCards(): ObservableTransformer<List<Card>, Unit> {
        return ObservableTransformer {
            it.concatMap { cards ->
                Observable.fromCallable {
                    cards.forEach {
                        executeInThread {
                            cardDao.insert(it.resetCard())
                        }
                    }
                }
            }
        }
    }

    private fun incrementScore(): Int {
        score = score.plus(2)
        liveScore.value = score
        return score
    }

    private fun decrementScore(): Int {
        score = score.minus(1)
        liveScore.value = score
        return score
    }

    private fun updateScore(): ObservableTransformer<Boolean, Boolean> {
        return ObservableTransformer {
            it.concatMap { isMatch ->
                if (isMatch) {
                    incrementScore()
                } else {
                    decrementScore()
                }
                return@concatMap Observable.just(isMatch)
            }
        }
    }

    private fun isGameOver(): ObservableTransformer<Unit, Boolean> {
        return ObservableTransformer {
            it.concatMap {
                Observable.fromCallable {
                    allCardsMatched()
                }
            }
        }
    }

    private fun allCardsMatched() = matchedCards.size == DECK_SIZE

    private fun enableCardClicks(): ObservableTransformer<Boolean, Unit> {
        return ObservableTransformer {
            it.concatMap { isGameOver ->
                Observable.fromCallable {
                    if (isGameOver) {
                        executeInThread {
                            cardDao.deleteTable()
                        }
                        updateState(GameState.GAME_COMPLETE)
                    } else {
                        updateState(GameState.IN_PROGRESS)
                    }
                }
            }
        }
    }

    private fun clearSelectedCard(): ObservableTransformer<Unit, Unit> {
        return ObservableTransformer {
            it.concatMap {
                Observable.fromCallable { firstCardSelected = null }
            }
        }
    }

    private fun clearFlippedCardMap(): ObservableTransformer<Unit, Unit> {
        return ObservableTransformer {
            it.concatMap { Observable.fromCallable { flippedCards.clear() } }
        }
    }

    private fun flipFirstCard(card: Card): DisposableObserver<Card> {
        return Observable.just(card)
            .subscribeOn(Schedulers.io())
            .compose(selectFirstCard())
            .compose(addToFlippedCardMap())
            .subscribeWith(object : DisposableObserver<Card>() {
                override fun onComplete() {}

                override fun onNext(card: Card) {}

                override fun onError(e: Throwable) {}
            })
    }

    private fun selectFirstCard(): ObservableTransformer<Card?, Card?> {
        return ObservableTransformer {
            it.concatMap { card: Card? ->
                firstCardSelected = card?.selectCard()
                return@concatMap Observable.just(firstCardSelected)
            }
        }
    }

    private fun addToFlippedCardMap(): ObservableTransformer<Card?, Card> {
        return ObservableTransformer {
            it.concatMap { selectedCard: Card ->
                Observable.fromCallable {
                    flippedCards[selectedCard.id.toString()] = selectedCard
                    executeInThread {
                        cardDao.insert(selectedCard)
                    }
                    return@fromCallable selectedCard
                }
            }
        }
    }

    private fun isValidMatch(): ObservableTransformer<Card, Boolean> {
        return ObservableTransformer {
            it.concatMap {
                Observable.fromCallable { isValidMatch(it) }
            }
        }
    }

    private fun isValidMatch(card: Card): Boolean {
        return firstCardSelected?.id != card.id && firstCardSelected?.resId == card.resId
    }

    private fun updateState(state: GameState) {
        gameState.value = state
    }

    fun observeGameBoard(): LiveData<List<Card>> {
        return cardDao.getAllCards()
    }

    fun getHighestScore() : LiveData<Score> {
        return cardDao.getHighestScore()
    }

    fun getLiveScore(): LiveData<Int> {
        return liveScore
    }

    fun getGameState(): LiveData<GameState> {
        return gameState
    }

    fun getSaveScoreStatus() : LiveData<Boolean> {
        return saveStatus
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun saveResults(userName: String) {
        if(userName.isNotEmpty()) {
            executeInThread {
                cardDao.insertScore(Score(userName = userName, score = score))
            }
            saveScore.value = true
        } else {
            saveScore.value = false
        }
    }

    private fun populateCards(): List<Card> {
        cardDao.deleteTable()
        val listOfCards = arrayListOf<Card>()
        listOfCards.add(Card(resId = R.drawable.colour1))
        listOfCards.add(Card(resId = R.drawable.colour2))
        listOfCards.add(Card(resId = R.drawable.colour3))
        listOfCards.add(Card(resId = R.drawable.colour4))
        listOfCards.add(Card(resId = R.drawable.colour5))
        listOfCards.add(Card(resId = R.drawable.colour6))
        listOfCards.add(Card(resId = R.drawable.colour7))
        listOfCards.add(Card(resId = R.drawable.colour8))
        listOfCards.add(Card(resId = R.drawable.colour1))
        listOfCards.add(Card(resId = R.drawable.colour2))
        listOfCards.add(Card(resId = R.drawable.colour3))
        listOfCards.add(Card(resId = R.drawable.colour4))
        listOfCards.add(Card(resId = R.drawable.colour5))
        listOfCards.add(Card(resId = R.drawable.colour6))
        listOfCards.add(Card(resId = R.drawable.colour7))
        listOfCards.add(Card(resId = R.drawable.colour8))
        return listOfCards
    }
}
