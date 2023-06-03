import com.pi4j.ktx.console
import com.pi4j.ktx.io.i2c
import com.pi4j.ktx.io.linuxFsI2CProvider
import com.pi4j.ktx.io.piGpioProvider
import com.pi4j.ktx.io.pwm
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import kotlinx.coroutines.delay

private const val ADC_7830_ADDRESS = 0x4b

fun main() {
    softlight()
}

private fun softlight() = runUntilExit {
    console {
        pi4j(builder = {
            add(LinuxFsI2CProviderImpl())
        }) {
            val pins = getPinMap()
            val redLed = pins.bcm(PinName.GPIO_17)
            val redLedPwm = pwm(redLed) {
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
                    val adcValue = i2c.readRegister(0)
                    redLedPwm.on(adcValue * 100 / 255)
                    val voltage = adcValue / 255.0f * 3.3
                    println("ADC value: $adcValue; voltage: $voltage")
                    delay(30)
                }
            }
        }
    }
}