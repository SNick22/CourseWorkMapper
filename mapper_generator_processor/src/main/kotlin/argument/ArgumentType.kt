package argument

import com.google.devtools.ksp.symbol.KSType

sealed interface ArgumentType {
    class ArgumentClass(val ksType: KSType) : ArgumentType
    data object Asterix : ArgumentType
}
