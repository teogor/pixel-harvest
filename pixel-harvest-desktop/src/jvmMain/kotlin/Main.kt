import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    println("Application Starting...")
    println("Retrieving Bot Token...")
    val dotenv = dotenv()
    val botToken = dotenv["BOT_TOKEN"]
    if (botToken != null) {
        println("Bot Token Retrieved.")
        val botBeta = PixelHarvestBot(botToken)
        botBeta.start()
    } else {
        println("Please provide a token using the BOT_TOKEN environment variable.")
    }

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
