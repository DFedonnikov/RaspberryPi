import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists


fun main() {
    dht()
}

private fun dht() = runUntilExit {
    loadLibrary("dht")
    pi4j {
        val pins = getPinMap()
        val pin = requireNotNull(pins[PinName.GPIO_17]?.wPi)
        val lib = DHTLib()
        var counts = 0
        var check: Int
        while (isRunning) {
            counts++
            println("Measurement counts: $counts")
            for (i in 0 until 15) {
                check = lib.readDHT11(pin)
                if (check == 0) {
                    break
                }
                delay(100)
            }
            println("Humidity is %.2f; Temperature is %.2f".format(lib.humidity, lib.temperature))
            delay(2000)
        }
    }
}

private fun loadLibrary(libName: String) {
    val libFullName = "lib$libName.so"
    val tmpDir = System.getProperty("java.io.tmpdir")

    try {
        val libPath = Paths.get(tmpDir, libFullName)
        println("pathExists: ${libPath.exists()}")
        Files.copy(
            DHTLib::class.java.getResourceAsStream("/$libFullName"),
            libPath,
            StandardCopyOption.REPLACE_EXISTING
        )
        println("pathExists: ${libPath.exists()}")
        System.load(libPath.toAbsolutePath().toString())
    } catch (e: Exception) {
        println("failed to load library $libName because of $e")
    }
}