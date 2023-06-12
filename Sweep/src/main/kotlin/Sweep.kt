import com.pi4j.io.pwm.Pwm
import com.pi4j.ktx.io.piGpioProvider
import com.pi4j.ktx.io.pwm
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl
import com.pi4j.util.Console
import kotlinx.coroutines.delay

private const val OFFSET_DUTY_CYCLE = 3.0
private const val MIN_ANGLE = 0
private const val MAX_ANGLE = 180
private const val SERVO_MIN_DUTY_CYCLE = 5 + OFFSET_DUTY_CYCLE
private const val SERVO_MAX_DUTY_CYCLE = 25 + OFFSET_DUTY_CYCLE

fun main() {
    sweep()
}

private fun sweep() = runUntilExit {
    pi4j(builder = {
        add(LinuxFsI2CProviderImpl())
    }) {
        val pins = getPinMap()
        val servoPin = pins.bcm(PinName.GPIO_18)
        val servoPWM = pwm(servoPin) {
            piGpioProvider()
            frequency(50)
            initial(0)
        }
        for (i in 30 downTo 0) {
            servoPWM.on(i)
            delay(50)
        }

//        while (isRunning) {
////            for (angle in MIN_ANGLE..MAX_ANGLE) {
////                servoWrite(angle, servoPWM)
////                delay(10)
////            }
//            delay(500)
//            servoPWM.on()
////            for (angle in MAX_ANGLE downTo MIN_ANGLE) {
////                servoWrite(angle, servoPWM)
////                delay(10)
////            }
//            delay(500)
//        }
    }
}

fun Console.servoWrite(angle: Int, servoPWM: Pwm) {
    val angleLocal = when {
        angle < 0 -> 0
        angle > 180 -> 180
        else -> angle
    }
    val dutyCycle =
        mapAngleToDutyCycle(angleLocal, MIN_ANGLE, MAX_ANGLE, SERVO_MIN_DUTY_CYCLE, SERVO_MAX_DUTY_CYCLE)
    println("mapped duty cycle: $dutyCycle")
    servoPWM.on(dutyCycle)
}

fun Console.mapAngleToDutyCycle(
    angle: Int,
    minAngle: Int,
    maxAngle: Int,
    servoMinDutyCycle: Double,
    servoMaxDutyCycle: Double
): Double {
    println("Mapping angle: $angle; minAngle: $minAngle; maxAngle: $maxAngle; minDutyCycle: $servoMinDutyCycle; maxDutyCycle: $servoMaxDutyCycle")
    return (servoMaxDutyCycle - servoMinDutyCycle) * (angle - minAngle) / (maxAngle - minAngle) + servoMinDutyCycle
}
