package visitor

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import getName

class PackageImports(var targetClassTypeParameters: Set<KSTypeParameter> = mutableSetOf()) {

    private val imports = mutableSetOf<Pair<String, String>>()

    fun addImport(packageName: String, className: String) {
        imports.add(packageName to className)
    }

    fun addImport(ksType: KSType) {
        val declaration: KSDeclaration = ksType.declaration
        val packageName: String = declaration.packageName.asString()
        val className: String = declaration.getName()

        imports.add(packageName to className)
    }

    fun asFormattedImports(): String {
        var importText = ""
        val typeParams: List<String> = targetClassTypeParameters.map { parameter -> parameter.simpleName.asString() }

        imports.forEachIndexed { index: Int, import: Pair<String, String> ->
            val packageName = import.first
            val className = import.second

            if (typeParams.contains(className)) return@forEachIndexed

            importText += "import $packageName.$className\n"

            if (imports.size - 1 == index) {
                importText += "\n"
            }
        }

        return importText
    }
}
