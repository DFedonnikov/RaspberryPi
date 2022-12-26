import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

inline fun runUntilExit(crossinline block: suspend (() -> Boolean) -> Unit) = runBlocking {
    var isRunning = true
    launch(Dispatchers.Default) {
        block { isRunning }
    }
    var input = ""
    while (input != "exit") {
        input = readln()
    }
    isRunning = false
    delay(2000)
}