package processor

import Mapper
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import getName
import logAndThrowError
import visitor.MapperVisitor

class MyProcessor(
    private val fileGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val resolvedSymbols: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(annotationName = Mapper::class.qualifiedName!!)

        resolvedSymbols
            .filter { ksAnnotated -> ksAnnotated is KSClassDeclaration && ksAnnotated.validate() }
            .forEach { ksAnnotated: KSAnnotated ->
                val classDeclaration: KSClassDeclaration = (ksAnnotated as KSClassDeclaration)
                when (classDeclaration.classKind) {
                    ClassKind.INTERFACE,
                    ClassKind.ENUM_CLASS,
                    ClassKind.ENUM_ENTRY,
                    ClassKind.OBJECT,
                    ClassKind.ANNOTATION_CLASS -> {
                        logger.logAndThrowError(
                            errorMessage = "Cannot generate function for class `${classDeclaration.getName()}`, " +
                                    "class type `${classDeclaration.classKind}` is not supported.",
                            targetClass = classDeclaration
                        )
                    }

                    else -> {
                        val kcmVisitor = MapperVisitor(
                            fileGenerator = fileGenerator,
                            resolver = resolver,
                            logger = logger
                        )
                        ksAnnotated.accept(
                            visitor = kcmVisitor,
                            data = Unit
                        )
                    }
                }
            }

        return emptyList()
    }
}
