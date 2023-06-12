import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.i2c.I2C
import com.pi4j.io.pwm.Pwm
import com.pi4j.ktx.Konsole
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.io.digital.piGpioProvider
import com.pi4j.ktx.io.i2c
import com.pi4j.ktx.io.linuxFsI2CProvider
import com.pi4j.ktx.io.piGpioProvider
import com.pi4j.ktx.io.pwm
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.random.Random

private const val ADC_7830_ADDRESS = 0x4b

fun main() {
    motor()
}

private fun motor() = runUntilExit {
    pi4j(builder = {
        add(LinuxFsI2CProviderImpl())
    }) {
        val pins = getPinMap()
        val motorIn1Pin = pins.bcm(PinName.GPIO_27)
        val motorIn2Pin = pins.bcm(PinName.GPIO_17)
        val motorEnable = pins.bcm(PinName.GPIO_22)
        val motorIn1 = digitalOutput(motorIn1Pin) {
            piGpioProvider()
            initial(DigitalState.LOW)
        }
        val motorIn2 = digitalOutput(motorIn2Pin) {
            piGpioProvider()
            initial(DigitalState.LOW)
        }
        val motorEnablePwm = pwm(motorEnable) {
            piGpioProvider()
            frequency(1000)
            initial(0)
        }
        i2c(1, ADC_7830_ADDRESS) {
            id("adc7830")
            linuxFsI2CProvider()
        }.use { i2c ->
            val config = i2c.readRegister(ADC_7830_ADDRESS)
            if (config >= 0) {
                println("Successfully read config from address: $ADC_7830_ADDRESS")
            } else {
                println("Failed to read config from address: $ADC_7830_ADDRESS")
                return@use
            }
            motorEnablePwm.on(0)

            fun powerMotor(
                adc: Int,
                motorIn1: DigitalOutput,
                motorIn2: DigitalOutput,
                motorEnablePwm: Pwm
            ) {
                if (adc > 0) {
                    motorIn1.high()
                    motorIn2.low()
                    println("Turn forward...")
                } else if (adc < 0) {
                    motorIn1.low()
                    motorIn2.high()
                    println("Turn backward...")
                } else {
                    motorIn1.low()
                    motorIn2.low()
                    println("Motor stop...")
                }
                val dutyCycle = mapRange(adc.absoluteValue, 0, 128, 0, 100)
                motorEnablePwm.on(dutyCycle)
                println("The PWM duty cycle is is $dutyCycle")
            }

            while (isRunning) {

                val rawValue = i2c.readRegister(15)
                println("Raw register value: $rawValue")
                val adc = rawValue - 127
                println("ADC value: ${adc * 100 / 127}")
                powerMotor(adc, motorIn1, motorIn2, motorEnablePwm)
                delay(500)
            }
            motorIn1.off()
            motorIn2.off()
            motorEnablePwm.off()
            i2c.close()
        }
    }
}


private fun mapRange(value: Int, fromLow: Int, fromHigh: Int, toLow: Int, toHigh: Int): Int {
    return (toHigh - toLow) * (value - fromLow) / (fromHigh - fromLow) + toLow
}