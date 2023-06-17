import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.ktx.Konsole
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import kotlinx.coroutines.delay

fun main() {
    stepperMotor()
}

private fun stepperMotor() = runUntilExit {
    pi4j(builder = {
        add(LinuxFsI2CProviderImpl())
    }) {
        val pins = getPinMap()
        val in1 = pins.bcm(PinName.GPIO_18)
        val in2 = pins.bcm(PinName.GPIO_23)
        val in3 = pins.bcm(PinName.GPIO_24)
        val in4 = pins.bcm(PinName.GPIO_25)
        val outputs = listOf(digitalOutput(in1), digitalOutput(in2), digitalOutput(in3), digitalOutput(in4))
        while (isRunning) {
            outputs.moveSteps(true, 3, 512)
            delay(500)
            outputs.moveSteps(false, 3, 512)
            delay(500)
        }
        outputs.motorStop()
    }
}

context(Konsole)
suspend fun List<DigitalOutput>.moveOnePeriod(isClockwise: Boolean, delay: Long) {
    for (step in indices) {
        if (isClockwise) {
            forEachIndexed { index, output ->
                if (index == step) output.high() else output.low()
                println("Clockwise: motor pin $index, state: ${output.state().name}")
            }
        }  else {
            for (inputIndex in 3 downTo 0) {
                if (3 - inputIndex == step) this[inputIndex].high() else this[inputIndex].low()
                println("Counterclockwise: motor pin $inputIndex, state: ${this[inputIndex].state().name}")
            }
        }
        println("Step cycle!")
        delay(if (delay < 3) 3 else delay)
    }
}

context(Konsole)
suspend fun List<DigitalOutput>.moveSteps(isClockwise: Boolean, delay: Long, steps: Int) {
    for (step in 0 until steps) {
        moveOnePeriod(isClockwise, delay)
    }
}

context(Konsole)
suspend fun List<DigitalOutput>.motorStop() {
    forEach { it.low() }
}