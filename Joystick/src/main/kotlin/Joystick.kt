import com.pi4j.io.gpio.digital.PullResistance
import com.pi4j.ktx.io.digital.digitalInput
import com.pi4j.ktx.io.digital.piGpioProvider
import com.pi4j.ktx.io.i2c
import com.pi4j.ktx.io.linuxFsI2CProvider
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import kotlinx.coroutines.delay

private const val ADC_7830_ADDRESS = 0x4b

fun main() {
    joystick()
}

private fun joystick() = runUntilExit {
    pi4j(builder = {
        add(LinuxFsI2CProviderImpl())
    }) {
        val pins = getPinMap()
        val zInputAddress = pins.bcm(PinName.GPIO_18)
        val zInput = digitalInput(zInputAddress) {
            pull(PullResistance.PULL_UP)
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
                val x = i2c.readRegister(72)
                val y = i2c.readRegister(8)
                val isPressed = zInput.isLow
                println("x: $x; y: $y; isPressed: $isPressed")
                delay(100)
            }
            i2c.close()
        }
    }
}