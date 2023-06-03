import com.pi4j.ktx.io.i2c
import com.pi4j.ktx.io.linuxFsI2CProvider
import com.pi4j.ktx.io.piGpioProvider
import com.pi4j.ktx.io.pwm
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import kotlinx.coroutines.delay

private const val ADC_7830_ADDRESS = 0x4b

fun main() {
    colorfulSoftlight()
}

private fun colorfulSoftlight() = runUntilExit {
    pi4j(builder = {
        add(LinuxFsI2CProviderImpl())
    }) {
        val pins = getPinMap()
        val redLed = pins.bcm(PinName.GPIO_17)
        val redLedPwm = pwm(redLed) {
            piGpioProvider()
            frequency(1000)
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
            redLedPwm.on(0)
            while (isRunning) {
                val redValue = 255 - i2c.readRegister(0)
                val dutyCycle = redValue * 100 / 255
                redLedPwm.on(dutyCycle)
                val voltage = redValue / 255.0 * 3.3
                println("Dutycycle: $dutyCycle; frequency: ${redLedPwm.frequency}; ADC value: $redValue; voltage: $voltage")
                delay(100)
            }
            redLedPwm.off()
            i2c.close()
        }
    }
}