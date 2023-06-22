import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DISPLAY_DELAY = 1L
private val nums = listOf(0xc0, 0xf9, 0xa4, 0xb0, 0x99, 0x92, 0x82, 0xf8, 0x80, 0x90)

fun main() {
    stopWatch()
}

private fun stopWatch() = runUntilExit {
    pi4j {
        val pins = getPinMap()
        val dataPin = pins.bcm(PinName.GPIO_24)
        val latchPin = pins.bcm(PinName.GPIO_23)
        val clockPin = pins.bcm(PinName.GPIO_18)
        val dataOutput = digitalOutput(dataPin)
        val latchOutput = digitalOutput(latchPin)
        val clockOutput = digitalOutput(clockPin)
        val digitPins = listOf(
            digitalOutput(pins.bcm(PinName.GPIO_17)) { initial(DigitalState.HIGH) },
            digitalOutput(pins.bcm(PinName.GPIO_27)) { initial(DigitalState.HIGH) },
            digitalOutput(pins.bcm(PinName.GPIO_22)) { initial(DigitalState.HIGH) },
            digitalOutput(pins.bcm(PinName.MOSI)) { initial(DigitalState.HIGH) }
        )
        //Encoding for characters 0-F of common anode
        var counter = 0
        val timeJob = it.launch {
            while (true) {
                if (counter > 9999) {
                    counter = 0
                }
                delay(1000)
                counter++
            }
        }
        while (isRunning) {
            display(counter, dataOutput, latchOutput, clockOutput, digitPins)
        }
        timeJob.cancel()
    }
}

fun List<DigitalOutput>.selectDigit(digit: Int) {
    require(size == 4) { "Function supposed to be called for 4-digit display" }
    if (digit.and(0x08) == 0x08) this[0].low() else this[0].high()
    if (digit.and(0x04) == 0x04) this[1].low() else this[1].high()
    if (digit.and(0x02) == 0x02) this[2].low() else this[2].high()
    if (digit.and(0x01) == 0x01) this[3].low() else this[3].high()
}

fun shiftOut(value: Int, dataOutput: DigitalOutput, clockOutput: DigitalOutput, isLeftShift: Boolean) {
    repeat(8) {
        clockOutput.low()
        if (isLeftShift) {
            if (1.and(value.shr(it)) == 1) dataOutput.high() else dataOutput.low()
        } else {
            if (128.and(value.shl(it)) == 128) dataOutput.high() else dataOutput.low()
        }
        clockOutput.high()
    }
}

fun outData(data: Int, dataOutput: DigitalOutput, latchOutput: DigitalOutput, clockOutput: DigitalOutput) {
    latchOutput.low()
    shiftOut(data, dataOutput, clockOutput, false)
    latchOutput.high()
}

suspend fun display(
    decimal: Int,
    dataOutput: DigitalOutput,
    latchOutput: DigitalOutput,
    clockOutput: DigitalOutput,
    digitPins: List<DigitalOutput>
) {
    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x01)
    outData(nums[decimal % 10], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)

    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x02)
    outData(nums[decimal % 100 / 10], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)

    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x04)
    outData(nums[decimal % 1000 / 100], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)

    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x08)
    outData(nums[decimal % 10000 / 1000], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)
}