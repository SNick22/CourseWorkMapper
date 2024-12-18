import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Mapper(
    val toClasses: Array<KClass<*>> = [],
    val fromClasses: Array<KClass<*>> = []
)
