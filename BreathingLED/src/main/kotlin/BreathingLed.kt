import com.pi4j.ktx.console
import com.pi4j.ktx.io.piGpioProvider
import com.pi4j.ktx.io.pwm
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay

fun main() {
    breathingLed()
}

private fun breathingLed() = runUntilExit {
    val pin = getPinMap().bcm(PinName.GPIO_18)
    console {
        pi4j {
            val pwm = pwm(pin) {
                piGpioProvider()
            }
            while (isRunning) {
                (0 until 100).forEach {
                    pwm.on(it)
                    delay(20)
                }
                (100 downTo 0).forEach {
                    pwm.on(it)
                    delay(20)
                }
            }
        }
    }
}