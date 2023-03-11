import com.pi4j.io.pwm.Pwm
import com.pi4j.ktx.console
import com.pi4j.ktx.io.digital.digitalInput
import com.pi4j.ktx.io.pwm
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

fun main() {
    alerter()
}

fun alerter() {
    println("Enter duty cycle (0 to 100): ")
    val dutyCycle = readlnOrNull()?.toIntOrNull() ?: 10
    println("Enter sine multiplier: ")
    val sineMultiplier = readlnOrNull()?.toIntOrNull() ?: 500
    println("Duty cycle: $dutyCycle; sine multiplier: $sineMultiplier")
    runUntilExit { isRunning ->
        val pins = getPinMap()
        val buzzer = pins.bcm(PinName.GPIO_17)
        val button = pins.bcm(PinName.GPIO_18)
        console {
            pi4j {
                val buttonInput = digitalInput(button)
                val buzzerOutput: Pwm = pwm(buzzer)
                buttonInput.pull()
                while (isRunning()) {
                    if (buttonInput.isLow) {
                        buzzerOutput.runAlert(dutyCycle, sineMultiplier)
                    } else {
                        buzzerOutput.stopAlert()
                    }
                }
            }
        }
    }
}

private suspend fun Pwm.runAlert(dutyCycle: Int, sineMultiplier: Int) {
    var sin: Double
    var tone: Double
    repeat(360) {
        sin = sin(it * (PI / 180))
        tone = 2000 + sin * sineMultiplier
        on(dutyCycle, tone.toInt())
        delay(1)
    }
}


private fun Pwm.stopAlert() = frequency(0).also { off() }