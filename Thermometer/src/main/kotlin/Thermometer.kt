import com.pi4j.ktx.io.i2c
import com.pi4j.ktx.io.linuxFsI2CProvider
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import kotlinx.coroutines.delay
import kotlin.math.ln

private const val ADC_7830_ADDRESS = 0x4b

fun main() {
    thermometer()
}

private fun thermometer() = runUntilExit {
    pi4j(builder = {
        add(LinuxFsI2CProviderImpl())
    }) {
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
                val a0PinValue = 255 - i2c.readRegister(0)
                val voltage = a0PinValue / 255.0 * 3.3
                val resistance = 10 * voltage / (3.3 - voltage)
                val tempKelvin = 1 / (1 / (273.15 + 25) + ln(resistance / 10) / 3950.0)
                val tempCelsius = tempKelvin - 273.15
                println("a0PinValue: $a0PinValue; voltage: $voltage; resistance: $resistance; tempK: $tempKelvin; tempC: $tempCelsius")
                delay(100)
            }
            i2c.close()
        }
    }
}