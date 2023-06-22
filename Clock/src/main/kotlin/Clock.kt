import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.DateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val DISPLAY_DELAY = 1L
private val nums = listOf(0xc0, 0xf9, 0xa4, 0xb0, 0x99, 0x92, 0x82, 0xf8, 0x80, 0x90)

fun main() {
    clock()
}

private fun clock() = runUntilExit { coroutineScope ->
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
        var date = DateTime.now()
        val timeJob = coroutineScope.launch {
            while (true) {
                delay(1.minutes)
                date = DateTime.now()
            }
        }
        var isShowSeparator = false
        val separatorJob = coroutineScope.launch {
            while (true) {
                delay(1.seconds)
                isShowSeparator = !isShowSeparator
            }
        }
        while (isRunning) {
            display(date, dataOutput, latchOutput, clockOutput, digitPins, isShowSeparator)
        }
        timeJob.cancel()
        separatorJob.cancel()
        turnClockOff(digitPins, dataOutput, latchOutput, clockOutput)
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
    date: DateTime,
    dataOutput: DigitalOutput,
    latchOutput: DigitalOutput,
    clockOutput: DigitalOutput,
    digitPins: List<DigitalOutput>,
    isShowSeparator: Boolean,
) {
    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x01)
    val fourthDigit = date.minuteOfHour % 10
    outData(nums[fourthDigit], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)

    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x02)
    outData(nums[date.minuteOfHour % 100 / 10], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)

    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x04)
    val thirdDigitCode = nums[date.hourOfDay % 10]
    outData(if (isShowSeparator) thirdDigitCode.and(0x7f) else thirdDigitCode, dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)

    outData(0xFF, dataOutput, latchOutput, clockOutput)
    digitPins.selectDigit(0x08)
    outData(nums[date.hourOfDay % 100 / 10], dataOutput, latchOutput, clockOutput)
    delay(DISPLAY_DELAY)
}

private fun turnClockOff(
    digitPins: List<DigitalOutput>,
    dataOutput: DigitalOutput,
    latchOutput: DigitalOutput,
    clockOutput: DigitalOutput,
) {
    digitPins.forEach {
        it.low()
        outData(0xFF, dataOutput, latchOutput, clockOutput)
        it.high()
    }
}