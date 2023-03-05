import com.pi4j.ktx.console
import com.pi4j.ktx.io.digital.digitalInput
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j

fun main() {
    buzzer()
}

fun buzzer() = runUntilExit { isRunning ->
    val pins = getPinMap()
    val buzzer = pins.bcm(PinName.GPIO_17)
    val button = pins.bcm(PinName.GPIO_18)
    console {
        pi4j {
            val buttonInput = digitalInput(button)
            val buzzerOutput = digitalOutput(buzzer)
            buttonInput.pull()
            while(isRunning()) {
                if (buttonInput.isLow) {
                    buzzerOutput.high()
                } else {
                    buzzerOutput.low()
                }
            }
        }
    }
}