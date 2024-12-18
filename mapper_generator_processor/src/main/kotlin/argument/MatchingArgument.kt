package argument

data class MatchingArgument(
    val targetClassPropertyName: String,
    val sourceClassPropertyName: String,
    val targetClassPropertyGenericTypeName: String? = null,
    val needsMap: Boolean = false
)
