import com.pi4j.context.Context
import com.pi4j.ktx.KontextBuilder
import com.pi4j.ktx.buildContext

inline fun pi4j(builder: KontextBuilder.() -> Unit, block: Context.() -> Unit): Context {
    val context = buildContext(builder)
    context.run(block)
    context.shutdown()
    return context
}