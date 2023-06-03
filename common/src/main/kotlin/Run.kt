import com.pi4j.ktx.Konsole
import com.pi4j.ktx.console
import kotlinx.coroutines.*

inline fun runUntilExit(crossinline block: suspend Konsole.(CoroutineScope) -> Unit) = runBlocking {
    withContext(Dispatchers.Default) {
        console {
            clearScreen()
            promptForExit()
            block(this@withContext)
            waitForExit()
        }
    }
    delay(2000)
}