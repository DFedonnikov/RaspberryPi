import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.microseconds

fun main() {
    lightWater()
}

private fun lightWater() = runUntilExit {
    pi4j {
        val pins = getPinMap()
        val dataPin = pins.bcm(PinName.GPIO_17)
        val latchPin = pins.bcm(PinName.GPIO_27)
        val clockPin = pins.bcm(PinName.GPIO_22)
        val dataOutput = digitalOutput(dataPin)
        val latchOutput = digitalOutput(latchPin)
        val clockOutput = digitalOutput(clockPin)
        while (isRunning) {
            var value = 1
            repeat(8){
                latchOutput.low()
                shiftOut(value, dataOutput, clockOutput, true)
                latchOutput.high()
                value = value.shl(1)
                delay(100)
            }
            value = 128
            repeat(8){
                latchOutput.low()
                shiftOut(value, dataOutput, clockOutput, true)
                latchOutput.high()
                value = value.shr(1)
                delay(100)
            }
        }
    }
}

suspend fun shiftOut(value: Int, dataOutput: DigitalOutput, clockOutput: DigitalOutput, isLeftShift: Boolean) {
    repeat(8) {
        clockOutput.low()
        if (isLeftShift) {
            if (1.and(value.shr(it)) == 1) dataOutput.high() else dataOutput.low()
            delay(10.microseconds)
        } else {
            if (128.and(value.shl(it)) == 128) dataOutput.high() else dataOutput.low()
            delay(10.microseconds)
        }
        delay(10.microseconds)
        clockOutput.high()
    }

}