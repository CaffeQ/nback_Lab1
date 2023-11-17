package mobappdev.example.nback_cimpl.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@Composable
fun GameScreen(
    vm:GameViewModel,
    navigate: ()->Unit
){
    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        ChooseGameModes()
        Grid(rowAndCol = 3)
        VisualAndAudio(navigate)
    }


}

@Composable
fun ChooseGameModes(){
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
                onClick = {/*TODO*/}
            ){
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Visual".uppercase(),
                    color = Color.Black,
                )
            }
            Button(
                onClick = {/*TODO*/}
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VisualAndAudio(navigate: () -> Unit){
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
            Button(onClick = {
                // Todo: change this button behaviour
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = "Hey! you clicked the audio button"
                    )
                }
            }) {
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
                    // Todo: change this button behaviour
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "Hey! you clicked the visual button",
                            duration = SnackbarDuration.Short
                        )
                    }
                }) {
                Icon(
                    painter = painterResource(id = R.drawable.visual),
                    contentDescription = "Visual",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }
        GoHome(navigate)
    }
}

@Composable
fun Grid(rowAndCol:Int){
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        repeat(rowAndCol) { rowIndex ->
            Row(
            ) {
                repeat(rowAndCol) { columnIndex ->
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(Color.LightGray),
                    ) {

                    }
                }
            }
        }
    }
}

@Composable
fun GoHome(navigate:()->Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(
            onClick = {navigate.invoke()}
        ){
            Text(
                modifier = Modifier.padding(4.dp),
                text = "Go Home".uppercase(),
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview(){
    GameScreen(FakeVM(),navigate = {"home"})
}
