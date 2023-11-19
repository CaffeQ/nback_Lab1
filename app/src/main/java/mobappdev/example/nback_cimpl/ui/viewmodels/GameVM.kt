package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    val audioState: StateFlow<GameState>
    val visualState: StateFlow<GameState>
    val score: StateFlow<Int>
    val sideLength: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: StateFlow<Int>
    val isPlaying: StateFlow<Boolean>
    val gameType: StateFlow<GameType>  // Type of the game

    val nrOfScores: StateFlow<Int>//Kanske inte behövs

    val percentMatches: StateFlow<Int>
    val nrOfTurns: StateFlow<Int>
    val eventInterval : StateFlow<Long>
    fun setGameType(gameType: GameType)
    fun startGame()
    fun enableSpeech()
    fun disableSpeech()
    fun checkVisualMatch()
    fun checkAudioMatch()
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

    private val _audioState = MutableStateFlow(GameState())
    override val audioState: StateFlow<GameState>
        get() = _audioState

    private val _visualState = MutableStateFlow(GameState())
    override val visualState: StateFlow<GameState>
        get() = _visualState

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score
    private val _sideLength = MutableStateFlow(3)
    override val sideLength: StateFlow<Int>
        get() = _sideLength

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _nBack = MutableStateFlow(3)
    override val nBack: StateFlow<Int>
        get() = _nBack
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean>
        get() = _isPlaying

    private val _gameType = MutableStateFlow(GameType.Visual)
    override val gameType: StateFlow<GameType>
        get() = _gameType
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
    private var visualEvents = emptyArray<Int>()  // Array with all events
    private var audioEvents = emptyArray<Int>()
    override fun setGameType(gameType: GameType) {
        _gameType.value = gameType
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        visualEvents = nBackHelper.generateNBackString(
            _nrOfTurns.value,
            _sideLength.value*_sideLength.value,
            _percentMatches.value,
            _nBack.value).toList().toTypedArray()

        audioEvents = nBackHelper.generateNBackString(
            _nrOfTurns.value,
            _sideLength.value*_sideLength.value,
            _percentMatches.value,
            _nBack.value).toList().toTypedArray()
        Log.d("GameVM", "The following sequence was generated: ${visualEvents.contentToString()}")

        job = viewModelScope.launch {
            when (_gameType.value) {
                GameType.Audio -> runAudioGame(audioEvents)
                GameType.AudioVisual -> runAudioVisualGame(audioEvents,visualEvents)
                GameType.Visual -> runVisualGame(visualEvents)
            }
            if(_highscore.value < _score.value){
                userPreferencesRepository.saveHighScore(_score.value)
            }
            Log.d("Saving",_sideLength.value.toString())
            userPreferencesRepository.saveSideLength(_sideLength.value)
            userPreferencesRepository.saveN(_nBack.value)
            userPreferencesRepository.saveTurns(_nrOfTurns.value)
            userPreferencesRepository.savePercent(_percentMatches.value)
            userPreferencesRepository.saveTime(_eventInterval.value)
        }
    }

    override fun enableSpeech() {
        _audioState.value = _audioState.value.copy(isSpeech = true)
    }

    override fun disableSpeech() {
        _audioState.value = _audioState.value.copy(isSpeech = false)
    }

    override fun checkVisualMatch() {
        if(_visualState.value.eventValue == -1 || Guess.NONE != _visualState.value.guess)
            return
        val currentValue = _visualState.value.eventValue
        val previousValue = _visualState.value.previousValue
        val guess: Guess
        if(previousValue != -1 && currentValue == previousValue ){
            _nrOfScores.value +=1
            _score.value +=1
            guess = Guess.CORRECT
        }else{
            _score.value -=1
            guess = Guess.FALSE
        }
        _visualState.value = _visualState.value.copy(guess = guess, isSpeech = false)
    }

    override fun checkAudioMatch() {
        if(_audioState.value.eventValue == -1 || Guess.NONE != _audioState.value.guess)
            return
        val currentValue = _audioState.value.eventValue
        val previousValue = _audioState.value.previousValue
        val guess: Guess
        if(previousValue != -1 && currentValue == previousValue ){
            _nrOfScores.value +=1
            _score.value +=1
            guess = Guess.CORRECT
        }else{
            _score.value -=1
            guess = Guess.FALSE
        }
        _audioState.value = _audioState.value.copy(guess = guess, isSpeech = false)
    }


    override fun resetGame(){
        resetGame(_visualState)
        resetGame(_audioState)
    }
    private fun resetGame(gameState: MutableStateFlow<GameState>) {
        job?.cancel()
        gameState.value = gameState.value.copy(
            guess = Guess.NONE,
            eventValue = -1,
            previousValue = -1,
            isSpeech = false
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
        if(_sideLength.value - 1 >= 3)
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


    private suspend fun runAudioGame(events: Array<Int>) {
        resetGame()
        _gameType.value = GameType.Audio
        var previousValue: Int = -1
        _isPlaying.value = true
        for (i in events.indices) {
            if(i >= _nBack.value){
                previousValue = events[i-_nBack.value] % 26
            }
            _audioState.value = _audioState.value.copy(
                eventValue = events[i] % 26,
                previousValue = previousValue,
                guess = Guess.NONE,
                letter = intToLetter(events[i] % 26),
                isSpeech = true
            )
            delay(_eventInterval.value)
            previousValue = -1
        }
        _isPlaying.value = false
    }

    private suspend fun runVisualGame(events: Array<Int>){
        resetGame()
        _gameType.value = GameType.Visual
        var previousValue: Int = -1
        _isPlaying.value = true
        for (i in events.indices) {
            if(i >= _nBack.value){
                previousValue = events[i-_nBack.value]
            }
            _visualState.value = _visualState.value.copy(
                eventValue = events[i],
                previousValue = previousValue,
                guess = Guess.NONE,
                isSpeech = true
            )
            delay(eventInterval.value)
            previousValue = -1
        }

        _isPlaying.value = false
    }

    private suspend fun runAudioVisualGame(audioEvents:Array<Int>,visualEvents:Array<Int>) {
        resetGame()
        _gameType.value = GameType.AudioVisual
        var previousAudioValue: Int = -1
        var previousVisualValue: Int = -1
        _isPlaying.value = true
        for (i in visualEvents.indices) {
            if(i >= _nBack.value){
                previousVisualValue = visualEvents[i-_nBack.value]
                previousAudioValue = audioEvents[i-_nBack.value] % 26
            }
            _visualState.value = _visualState.value.copy(
                eventValue = visualEvents[i],
                previousValue = previousVisualValue,
                guess = Guess.NONE,
            )
            _audioState.value = _audioState.value.copy(
                eventValue = audioEvents[i] % 26,
                previousValue = previousAudioValue,
                guess = Guess.NONE,
                letter = intToLetter(audioEvents[i] % 26),
                isSpeech = true
            )
            delay(eventInterval.value)
            previousVisualValue = -1
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
            16 -> "P"
            17 -> "Q"
            18 -> "R"
            19 -> "S"
            20 -> "T"
            21 -> "U"
            22 -> "V"
            23 -> "W"
            24 -> "X"
            25 -> "Y"
            26 -> "Z"
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
            try {
                Log.d("Init", "Initializing")
                userPreferencesRepository.highscore.collect {
                    Log.d("Highscore", it.toString())
                    _highscore.value = it
                }

                userPreferencesRepository.n.collect {
                    Log.d("Fetching", it.toString())
                    _nBack.value = it
                }

                userPreferencesRepository.sideLength.collect {
                    Log.d("SideLength", it.toString())
                    _sideLength.value = it
                }

                userPreferencesRepository.turns.collect {
                    Log.d("Turns", it.toString())
                    _nrOfTurns.value = it
                }

                userPreferencesRepository.percent.collect {
                    Log.d("Percent", it.toString())
                    _percentMatches.value = it
                }

                userPreferencesRepository.time.collect {
                    Log.d("Time", it.toString())
                    _eventInterval.value = it
                }
            } catch (e: Exception) {
                Log.e("Init", "Error in init block", e)
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
    val eventValue: Int = -1,  // The value of the array string
    val previousValue: Int = -1,
    val guess: Guess = Guess.NONE,
    val letter: String = "?",
    val isSpeech: Boolean = false
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val audioState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val visualState: StateFlow<GameState>
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
    override val gameType: StateFlow<GameType>
        get() = MutableStateFlow(GameType.Visual).asStateFlow()
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

    override fun checkVisualMatch() {
    }

    override fun checkAudioMatch() {

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