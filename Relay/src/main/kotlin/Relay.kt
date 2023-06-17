import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.gpio.digital.PullResistance
import com.pi4j.ktx.io.digital.digitalInput
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.io.digital.piGpioProvider
import com.pi4j.ktx.pi4j

fun main() {
    relay()
}

private fun relay() = runUntilExit {
    pi4j {
        val pins = getPinMap()
        val buttonPin = pins.bcm(PinName.GPIO_18)
        val relayPin = pins.bcm(PinName.GPIO_17)
        val button = digitalInput(buttonPin) {
            piGpioProvider()
            pull(PullResistance.PULL_UP)
        }
        val relay = digitalOutput(relayPin) {
            piGpioProvider()
            initial(DigitalState.LOW)
        }
        val debounce = 50
        var relayState = false
        var buttonState = DigitalState.HIGH
        var lastButtonState = DigitalState.HIGH
        var lastChangedTime = System.currentTimeMillis()
        while (isRunning) {
            val buttonReading = button.state()
            if (buttonReading != lastButtonState) {
                lastChangedTime = System.currentTimeMillis()
            }
            if (System.currentTimeMillis() - lastChangedTime > debounce) {
                if (buttonReading != buttonState) {
                    buttonState = buttonReading
                    if (buttonState.isLow) {
                        println("Button is pressed!")
                        relayState = !relayState
                        if (relayState) {
                            println("Turn on relay...")
                        } else {
                            println("Turn off relay")
                        }
                    } else {
                        println("Button is released!")
                    }
                }
            }
            relay.setState(relayState)
            lastButtonState = buttonReading
        }
    }
}