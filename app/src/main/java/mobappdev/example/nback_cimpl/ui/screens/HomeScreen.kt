package mobappdev.example.nback_cimpl.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(
    vm: GameViewModel,
    navigate: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val N by vm.nBack.collectAsState()
    val sideLength by vm.sideLength.collectAsState()
    val scope = rememberCoroutineScope()
    val nrOfTurns by vm.nrOfTurns.collectAsState()
    val percent by vm.percentMatches.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(32.dp),
                text = "High-Score = $highscore",
                style = MaterialTheme.typography.headlineLarge
            )
            // Todo: You'll probably want to change this "BOX" part of the composable
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { vm.decreaseNback() }) {
                        Text(text = "-1")
                    }
                    Button(onClick = {}) {
                        Text(text = "N = $N")
                    }
                    Button(onClick = { vm.increaseNback() }) {
                        Text(text = "+1")
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(onClick = { vm.decreaseSideLength() }) {
                        Text(text = "-1")
                    }
                    Button(onClick = {}) {
                        Text(text = "$sideLength X $sideLength")
                    }
                    Button(onClick = { vm.increaseSideLength() }) {
                        Text(text = "+1")
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(onClick = { vm.decreaseTurns() }) {
                        Text(text = "-1")
                    }
                    Button(onClick = {}) {
                        Text(text = "Turns = $nrOfTurns")
                    }
                    Button(onClick = { vm.increaseTurns() }) {
                        Text(text = "+1")
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(onClick = { vm.decreasePercent(5) }) {
                        Text(text = "-5")
                    }
                    Button(onClick = {}) {
                        Text(text = "Percent = $percent")
                    }
                    Button(onClick = { vm.increasePercent(5) }) {
                        Text(text = "+5")
                    }
                }
            }


            ChooseGameModes(vm = vm)
            Button(
                onClick = {navigate.invoke()}
            ){
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Go to game".uppercase(),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ChooseGameModes(vm:GameViewModel){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            Button(
                onClick = {vm.setGameType(GameType.Visual)}
            ){
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Visual".uppercase(),
                    color = Color.Black,
                )
            }
            Button(
                onClick = {vm.setGameType(GameType.Audio)}
            ){
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Audio".uppercase(),
                    color = Color.Black,
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface(){
        HomeScreen(FakeVM(), navigate = {"game"})
    }
}