package mobappdev.example.nback_cimpl.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameState
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.Guess

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GameScreen(
    vm:GameViewModel,
    navigate: ()->Unit
){
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val audioState by vm.audioState.collectAsState()
    val visualState by vm.visualState.collectAsState()
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val sideLength by vm.sideLength.collectAsState()
    val score by vm.score.collectAsState()
    val N by vm.nBack.collectAsState()
    val nrOfscores by vm.nrOfScores.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    if (textToSpeech == null) {
        textToSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                print("Successfull")
            }
        }
    }


    if( isPlaying && audioState.isSpeech){ //fungerar inte med vanliga gameState.isSpeech
        val letter = audioState.letter
        textToSpeech?.speak(letter, TextToSpeech.QUEUE_FLUSH, null, null)
        vm.disableSpeech()
    }
    if(isLandscape){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()

        ) {

            Box(modifier = Modifier
                .weight(1f)){
                Column {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = "Score: $score N = $N E: ${visualState.eventValue} N# Correct $nrOfscores",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    VisualAndAudio(vm = vm,navigate)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                Grid(vm = vm, gameState = visualState, sideLength = sideLength)
            }
        }
    }else{
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Score: $score N = $N E: ${visualState.eventValue} N# Correct $nrOfscores",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Grid(vm,visualState,sideLength = sideLength)

            VisualAndAudio(vm,navigate)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun VisualAndAudio(vm: GameViewModel, navigate: () -> Unit){
    val audioState by vm.audioState.collectAsState()
    val gameType by vm.gameType.collectAsState()
    var visualButtonColor by remember { mutableStateOf(Color(127, 82, 255)) }
    var audioButtonColor by remember { mutableStateOf(Color(127, 82, 255)) }
    val visualState by vm.visualState.collectAsState()

    if(visualState.guess == Guess.NONE ){
        visualButtonColor = Color(127, 82, 255)
    }
    if(audioState.guess == Guess.NONE)
        audioButtonColor = Color(127, 82, 255)

    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val scope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) })
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if(gameType == GameType.Audio || gameType == GameType.AudioVisual)
                        vm.checkAudioMatch()
                    }
                ,
                modifier = Modifier
                    .background(
                        color = audioButtonColor,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                LaunchedEffect(audioState.guess) {
                    audioButtonColor = when (audioState.guess) {
                        Guess.FALSE -> Color.Red
                        Guess.CORRECT -> Color.Green
                        else -> Color(127, 82, 255)
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.sound_on),
                    contentDescription = "Sound",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
            Button(
                onClick = {
                    if(gameType == GameType.Visual || gameType == GameType.AudioVisual)
                        vm.checkVisualMatch()
                },
                modifier = Modifier
                    .background(
                        color = visualButtonColor,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                LaunchedEffect(visualState.guess){
                    visualButtonColor = when (visualState.guess) {
                        Guess.FALSE -> Color.Red
                        Guess.CORRECT -> Color.Green
                        else -> Color(127, 82, 255)
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.visual),
                    contentDescription = "Visual",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            StartGame(vm)
            GoHome(vm,navigate)
            ResetGame(vm)
        }
    }
}

fun audioGame(vm:GameViewModel){

}

@Composable
fun ResetGame(vm:GameViewModel){
    Button(
        onClick = { vm.resetGame()}
    ){
        Text(
            modifier = Modifier.padding(2.dp),
            text = "Reset".uppercase(),
            color = Color.Black,
        )
    }
}

@Composable
fun Grid(vm: GameViewModel,gameState: GameState, sideLength: Int){
    val gameType by vm.gameType.collectAsState()
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        repeat(sideLength) { rowIndex ->
            Row(
            ) {
                repeat(sideLength) { columnIndex ->
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                if (gameType != GameType.Audio && vm.isPlaying.value && rowIndex * sideLength + columnIndex == gameState.eventValue - 1) {
                                    Color(177, 253, 132)
                                } else {
                                    Color.LightGray
                                }
                            )
                    )
                }
            }
        }
    }
}
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DebugginText(vm:GameViewModel){
    val TAG ="TESTING"
    Text(text = "Event value: ${vm.gameState.value.eventValue}")
    Text(text = "Previous value: ${vm.gameState.value.previousValue}")
    Text(text = "Game type: ${vm.gameType}")

    Log.d(TAG,"Event value: "+vm.gameState.value.eventValue.toString())
    Log.d(TAG,"Previous value: ${vm.gameState.value.previousValue}")
    Log.d(TAG,"Game type: ${vm.gameType}")
}

@Composable
fun GetBoxColor(row: Int, column: Int,sideLength: Int ,eventValue:Int): Color {
    if(eventValue < 0 || eventValue > sideLength)
        return Color.LightGray
    return if ( row * sideLength + column == eventValue) {
        Color(177,253,132)
    } else {
        Color.LightGray
    }
}

@Composable
fun GoHome(vm:GameViewModel,navigate:()->Unit){
    Button(
        onClick = {
            navigate.invoke()
            vm.resetGame()
        }
    ){
        Text(
            modifier = Modifier.padding(2.dp),
            text = "Go Home".uppercase(),
            color = Color.Black,
        )
    }
}

@Composable
fun StartGame(vm:GameViewModel){
    Button(
        onClick = vm::startGame
    ){
        Text(
            modifier = Modifier.padding(2.dp),
            text = "Play Game".uppercase(),
            color = Color.Black,
        )
    }

}

@Preview(showBackground = true, device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun GameScreenLandscapePreview(){
    GameScreen(FakeVM(),navigate = {"home"})
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview(){
    GameScreen(FakeVM(),navigate = {"home"})
}
