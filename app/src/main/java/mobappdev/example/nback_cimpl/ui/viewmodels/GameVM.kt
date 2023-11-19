package mobappdev.example.nback_cimpl.ui.viewmodels

import android.text.BoringLayout
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val sideLength: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: StateFlow<Int>
    val isPlaying: StateFlow<Boolean>

    val nrOfScores: StateFlow<Int>//Kanske inte behövs

    val percentMatches: StateFlow<Int>
    val nrOfTurns: StateFlow<Int>
    val eventInterval : StateFlow<Long>
    fun setGameType(gameType: GameType)
    fun startGame()
    fun enableSpeech()
    fun disableSpeech()
    fun checkMatch()
    fun resetGame()

    fun increaseNback()
    fun decreaseNback()

    fun increaseSideLength()
    fun decreaseSideLength()

    fun increaseTurns()
    fun decreaseTurns()

    fun decreasePercent(percent:Int)
    fun increasePercent(percent:Int)

    fun increaseTime()
    fun decreaseTime()

}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score
    private val _sideLength = MutableStateFlow(3)
    override val sideLength: StateFlow<Int>
        get() = _sideLength

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    private val _nBack = MutableStateFlow(2)
    override val nBack: StateFlow<Int>
        get() = _nBack.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean>
        get() = _isPlaying
    override val nrOfScores: StateFlow<Int>
        get() = _nrOfScores
    private val _nrOfScores = MutableStateFlow(0)


    private val _percentMatches = MutableStateFlow(30)
    override val percentMatches: StateFlow<Int>
        get() = _percentMatches

    private val _nrOfTurns = MutableStateFlow(10)
    override val nrOfTurns: StateFlow<Int>
        get() = _nrOfTurns


    private var job: Job? = null  // coroutine job for the game event
    private val _eventInterval = MutableStateFlow(2000L)  // 2000 ms (2s)
    override val eventInterval: StateFlow<Long>
        get() = _eventInterval.asStateFlow()

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(
            _nrOfTurns.value,
            _sideLength.value*_sideLength.value,
            _percentMatches.value,
            _nBack.value).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
            if(_highscore.value < _score.value)
                userPreferencesRepository.saveHighScore(_score.value)
        }
    }

    override fun enableSpeech() {
        _gameState.value = _gameState.value.copy(isSpeech = true)
    }

    override fun disableSpeech() {
        _gameState.value = _gameState.value.copy(isSpeech = false)
    }

    override fun checkMatch() {
        if(_gameState.value.eventValue == -1 || Guess.NONE != _gameState.value.guess)
            return
        val currentValue = _gameState.value.eventValue
        val previousValue = _gameState.value.previousValue
        val guess: Guess
        if(previousValue != -1 && currentValue == previousValue ){
            _nrOfScores.value +=1
            _score.value +=1
            guess = Guess.CORRECT
        }else{
            _score.value -=1
            guess = Guess.FALSE
        }
        _gameState.value = _gameState.value.copy(guess = guess)
    }

    override fun resetGame() {
        job?.cancel()
        _gameState.value = _gameState.value.copy(
            gameType = _gameState.value.gameType,
            guess = Guess.NONE,
            eventValue = -1,
            previousValue = -1,
        )
        _nrOfScores.value = 0
        _isPlaying.value = false
        _score.value = 0
    }

    override fun increaseNback() {
        if(nBack.value<= 15)
            _nBack.value += 1
    }

    override fun decreaseNback() {
        if(nBack.value > 1)
            _nBack.value -= 1
    }

    override fun increaseSideLength() {
        if(_sideLength.value <= 5)
            _sideLength.value += 1
    }

    override fun decreaseSideLength() {
        if(_sideLength.value >= 3)
            _sideLength.value -= 1
    }

    override fun increaseTurns() {
        if(_nrOfTurns.value < 40)
            _nrOfTurns.value += 1
    }

    override fun decreaseTurns() {
        if(_nrOfTurns.value - 1 > 10)
            _nrOfTurns.value -= 1
    }

    override fun decreasePercent(percent: Int) {
        if(_percentMatches.value - percent >= 10)
            _percentMatches.value -= percent
    }

    override fun increasePercent(percent: Int) {
        if(_percentMatches.value + percent <= 50)
            _percentMatches.value += percent
    }

    override fun increaseTime() {
        if(_eventInterval.value + 500L <= 4000L)
            _eventInterval.value += 250L
    }

    override fun decreaseTime() {
        if(_eventInterval.value - 500L >= 500L)
            _eventInterval.value -= 250L
    }


    private suspend fun runAudioGame() {
        resetGame()
        var previousValue: Int = -1
        _isPlaying.value = true
        for (i in events.indices) {
            if(i >= _nBack.value){
                previousValue = events[i-_nBack.value]
            }
            _gameState.value = _gameState.value.copy(
                gameType = GameType.Audio,
                eventValue = events[i],
                previousValue = previousValue,
                guess = Guess.NONE,
                letter = intToLetter(events[i]),
                isSpeech = true
            )
            delay(_eventInterval.value)
            previousValue = -1
        }
        _isPlaying.value = false
    }

    private suspend fun runVisualGame(events: Array<Int>){
        resetGame()
        var previousValue: Int = -1
        _isPlaying.value = true
        for (i in events.indices) {
            if(i >= _nBack.value){
                previousValue = events[i-_nBack.value]
            }
            _gameState.value = _gameState.value.copy(
                gameType = GameType.Visual,
                eventValue = events[i],
                previousValue = previousValue,
                guess = Guess.NONE,
            )
            delay(eventInterval.value)
            previousValue = -1
        }
        _isPlaying.value = false
    }

    private suspend fun runAudioVisualGame() {
        resetGame()
        var previousValue: Int = -1
        _isPlaying.value = true
        for (i in events.indices) {
            if (i >= _nBack.value) {
                previousValue = events[i - _nBack.value]
            }
            _gameState.value = _gameState.value.copy(
                gameType = GameType.AudioVisual,
                eventValue = events[i],
                previousValue = previousValue,
                guess = Guess.NONE,
                letter = intToLetter(events[i]),
                isSpeech = true
            )
            delay(_eventInterval.value)
            previousValue = -1
        }
        _isPlaying.value = false
    }

    private fun intToLetter(value:Int): String{
        return when(value){
            1 -> "A"
            2 -> "B"
            3 -> "C"
            4 -> "D"
            5 -> "E"
            6 -> "F"
            7 -> "G"
            8 -> "H"
            9 -> "I"
            10 -> "J"
            11 -> "K"
            12 -> "L"
            13 -> "M"
            14 -> "N"
            15 -> "O"
            else -> {"?"}
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}
enum class Guess{
    CORRECT,
    FALSE,
    NONE
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val previousValue: Int = -1,
    val guess: Guess = Guess.NONE,
    val letter: String = "?",
    val isSpeech: Boolean = false
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val sideLength: StateFlow<Int>
        get() = MutableStateFlow(3).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: StateFlow<Int>
        get() = MutableStateFlow(4).asStateFlow()
    override val isPlaying: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val nrOfScores: StateFlow<Int>
        get() = MutableStateFlow(0).asStateFlow()
    override val percentMatches: StateFlow<Int>
        get() = MutableStateFlow(30).asStateFlow()
    override val nrOfTurns: StateFlow<Int>
        get() = MutableStateFlow(10).asStateFlow()
    override val eventInterval: StateFlow<Long>
        get() = MutableStateFlow(2000L).asStateFlow()

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun enableSpeech() {

    }

    override fun disableSpeech() {

    }

    override fun checkMatch() {
    }

    override fun resetGame() {

    }

    override fun increaseNback() {

    }

    override fun decreaseNback() {
    }

    override fun increaseSideLength() {

    }

    override fun decreaseSideLength() {
    }

    override fun increaseTurns() {

    }

    override fun decreaseTurns() {

    }

    override fun decreasePercent(percent: Int) {

    }

    override fun increasePercent(percent: Int) {

    }

    override fun increaseTime() {

    }

    override fun decreaseTime() {

    }
}