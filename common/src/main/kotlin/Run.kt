import kotlinx.coroutines.*

inline fun runUntilExit(crossinline block: suspend CoroutineScope.(() -> Boolean) -> Unit) = runBlocking {
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