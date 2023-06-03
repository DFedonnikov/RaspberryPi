import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.ktx.console
import com.pi4j.ktx.io.digital.*
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.*

fun main() {
    waterLed()
}

private fun waterLed() = runUntilExit {
    val pinMap = getPinMap()
    val pins = with(pinMap) {
        arrayOf(
            bcm(PinName.GPIO_18), bcm(PinName.GPIO_27), bcm(PinName.GPIO_22),
            bcm(PinName.GPIO_23), bcm(PinName.GPIO_24), bcm(PinName.GPIO_25),
            bcm(PinName.SDA_1), bcm(PinName.SCL_1), bcm(PinName.CE_0)
        )
    }
    console {
        pi4j {
            val pinOutputs = pins.map {
                digitalOutput(it) {
                    id("led: $it")
                    name("LED BAR ITEM: $it")
                    shutdown(DigitalState.LOW)
                    initial(DigitalState.LOW)
                    piGpioProvider()
                }
            }
            while (isRunning) {
                pinOutputs.forEach {
                    it.low()
                    delay(100)
                    it.high()
                }
                (pinOutputs.lastIndex downTo 0).forEach {
                    pinOutputs[it].low()
                    delay(100)
                    pinOutputs[it].high()
                }
            }
        }
    }
}