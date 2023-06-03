import com.pi4j.ktx.console
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
    console {
        pi4j(builder = {
            add(LinuxFsI2CProviderImpl())
        }) {
            val pins = getPinMap()
            val redLed = pins.bcm(PinName.GPIO_22)
            val redLedPwm = pwm(redLed) {
                piGpioProvider()
            }
            val greenLed = pins.bcm(PinName.GPIO_27)
            val greenLedPwm = pwm(greenLed) {
                piGpioProvider()
            }
            val blueLed = pins.bcm(PinName.GPIO_17)
            val blueLedPwm = pwm(blueLed) {
                piGpioProvider()
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
                while (isRunning) {
                    val redValue = 255 - i2c.readRegister(0)
                    val greenValue = 255 - i2c.readRegister(64)
                    val blueValue = 255 - i2c.readRegister(31)
                    redLedPwm.on(redValue * 100 / 255)
                    greenLedPwm.on(greenValue * 100 / 255)
                    blueLedPwm.on(blueValue * 100 / 255)
                    delay(100)
                }
                redLedPwm.on(255)
                greenLedPwm.on(255)
                blueLedPwm.on(255)
                i2c.close()
            }
        }
    }
}