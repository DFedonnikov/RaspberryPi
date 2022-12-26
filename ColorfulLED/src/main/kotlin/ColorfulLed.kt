import com.pi4j.io.pwm.Pwm
import com.pi4j.ktx.console
import com.pi4j.ktx.io.piGpioProvider
import com.pi4j.ktx.io.pwm
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.*
import kotlin.random.Random

fun main() {
    colorfulLed()
}

private fun colorfulLed() = runUntilExit { isRunning ->
    val pins = getPinMap()
    val redPin = pins.bcm(PinName.GPIO_17)
    val greenPin = pins.bcm(PinName.GPIO_18)
    val bluePin = pins.bcm(PinName.GPIO_27)
    console {
        pi4j {
            val redPwm = pwm(redPin) {
                piGpioProvider()
            }
            val greenPwm = pwm(greenPin) {
                piGpioProvider()
            }
            val bluePwm = pwm(bluePin) {
                piGpioProvider()
            }
            var currentRed = redPwm.dutyCycle
            var currentGreen = greenPwm.dutyCycle
            var currentBlue = bluePwm.dutyCycle
            while (isRunning()) {
                val nextRed = Random.nextFloat() * 100
                val nextGreen = Random.nextFloat() * 100
                val nextBlue = Random.nextFloat() * 100
                val red = redPwm.animateAsync(currentRed, nextRed)
                red.await()
                val green = greenPwm.animateAsync(currentGreen, nextGreen)
                green.await()
                val blue = bluePwm.animateAsync(currentBlue, nextBlue)
                blue.await()
                currentRed = nextRed
                currentGreen = nextGreen
                currentBlue = nextBlue
            }
        }
    }
}

context(CoroutineScope)
        suspend fun Pwm.animateAsync(from: Float, to: Float): Deferred<Unit> {
    var current = from
    return async {
        while (current < to) {
            on(current)
            current++
            delay(25)
        }
    }
}