import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.microseconds

fun main() {
    sevenSegmentDisplay()
}

private fun sevenSegmentDisplay() = runUntilExit {
    pi4j {
        val pins = getPinMap()
        val dataPin = pins.bcm(PinName.GPIO_17)
        val latchPin = pins.bcm(PinName.GPIO_27)
        val clockPin = pins.bcm(PinName.GPIO_22)
        val dataOutput = digitalOutput(dataPin)
        val latchOutput = digitalOutput(latchPin)
        val clockOutput = digitalOutput(clockPin)
        //Encoding for characters 0-F of common anode
        val charactersEncoding =
            listOf(0xc0, 0xf9, 0xa4, 0xb0, 0x99, 0x92, 0x82, 0xf8, 0x80, 0x90, 0x88, 0x83, 0xc6, 0xa1, 0x86, 0x8e)
        while (isRunning) {
            charactersEncoding.forEach {
                latchOutput.low()
                shiftOut(it, dataOutput, clockOutput, false)
                latchOutput.high()
                delay(500)
            }
            charactersEncoding.forEach {
                latchOutput.low()
                //0x7f is for displaying a decimal point
                shiftOut(it.and(0x7f), dataOutput, clockOutput, false)
                latchOutput.high()
                delay(500)
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