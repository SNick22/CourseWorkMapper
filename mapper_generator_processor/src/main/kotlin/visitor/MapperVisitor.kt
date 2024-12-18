package visitor

import appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import generator.MapperFileGenerator
import logAndThrowError
import java.io.OutputStream


private const val FROM_CLASSES_ANNOTATION_ARG_NAME = "fromClasses"
private const val TARGET_CLASSES_ANNOTATION_ARG_NAME = "toClasses"
private const val GENERATED_CLASS_SUFFIX = "MapperExtensions"
private const val SUPPRESS_UNCHECKED_CAST_STATEMENT = "@file:Suppress(\"UNCHECKED_CAST\")\n\n"
private const val PACKAGE_STATEMENT = "package"
private const val GENERATED_FILE_PATH = "ru.snick22.mapper.generated"

class MapperVisitor(
    private val fileGenerator: CodeGenerator,
    private val resolver: Resolver,
    private val logger: KSPLogger,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotatedClass: KSClassDeclaration = classDeclaration
        val kcmAnnotation: KSAnnotation = extractKCMAnnotation(annotatedClass)
        val mapFromClasses: List<KSClassDeclaration> = extractArgumentClasses(kcmAnnotation, FROM_CLASSES_ANNOTATION_ARG_NAME)
        val mapToClasses: List<KSClassDeclaration> = extractArgumentClasses(kcmAnnotation, TARGET_CLASSES_ANNOTATION_ARG_NAME)

        if (mapFromClasses.isEmpty() && mapToClasses.isEmpty()) {
            logger.warn("Missing mapping functions for @Mapper annotated class $annotatedClass.")
            return
        }

        val mappingFunctionGenerator = MapperFileGenerator(
            resolver = resolver,
            logger = logger
        )

        var extensionFunctions = ""
        val packageImports = PackageImports()

        if (mapFromClasses.isNotEmpty()) {
            mapFromClasses.forEach { sourceClass: KSClassDeclaration ->
                extensionFunctions += mappingFunctionGenerator.generateMappingFunction(
                    targetClass = annotatedClass,
                    sourceClass = sourceClass,
                    packageImports = packageImports
                )
            }
        }

        if (mapToClasses.isNotEmpty()) {
            mapToClasses.forEach { targetClass: KSClassDeclaration ->
                extensionFunctions += mappingFunctionGenerator.generateMappingFunction(
                    targetClass = targetClass,
                    sourceClass = annotatedClass,
                    packageImports = packageImports
                )
            }
        }

        generateCode(
            containingFile = classDeclaration.containingFile!!,
            targetClassName = annotatedClass.simpleName.getShortName(),
            packageImports = packageImports,
            extensionFunctions = extensionFunctions
        )
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        annotation.annotationType.resolve().declaration.accept(this, data)
    }

    private fun generateCode(
        containingFile: KSFile,
        targetClassName: String,
        packageImports: PackageImports,
        extensionFunctions: String
    ) {
        fileGenerator.createNewFile(
            dependencies = Dependencies(true, containingFile),
            packageName = GENERATED_FILE_PATH,
            fileName = "${targetClassName}$GENERATED_CLASS_SUFFIX"
        ).use { generatedFileOutputStream: OutputStream ->
            if (packageImports.targetClassTypeParameters.isNotEmpty()) generatedFileOutputStream.appendText(SUPPRESS_UNCHECKED_CAST_STATEMENT)
            generatedFileOutputStream.appendText("$PACKAGE_STATEMENT $GENERATED_FILE_PATH\n\n")
            generatedFileOutputStream.appendText(packageImports.asFormattedImports())
            generatedFileOutputStream.appendText(extensionFunctions)
        }
    }

    private fun extractKCMAnnotation(targetClass: KSClassDeclaration): KSAnnotation {

        val kcmAnnotation: KSAnnotation = targetClass.annotations
            .first { targetClassAnnotations -> targetClassAnnotations.shortName.asString() == "Mapper" }

        kcmAnnotation.arguments.firstOrNull { constructorParam ->
            constructorParam.name?.asString() == FROM_CLASSES_ANNOTATION_ARG_NAME
        } ?: run {
            logger.logAndThrowError(
                errorMessage = "Classes annotated with @Mapper must contain " +
                        "at least one class as a parameter like: @Mapper(classes = [YourClassToMap::kt])",
                targetClass = targetClass
            )
        }

        return kcmAnnotation
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractArgumentClasses(kcmAnnotation: KSAnnotation, paramName: String): List<KSClassDeclaration> {
        return kcmAnnotation
            .arguments
            .find { annotationArgument: KSValueArgument -> annotationArgument.name?.asString() == paramName }
            ?.let { ksValueArgument -> ksValueArgument.value as List<KSType> }
            ?.mapNotNull { argumentClassType -> resolver.getClassDeclarationByName(argumentClassType.declaration.qualifiedName!!) } // TODO: Check if !! is okay here
            ?: emptyList()
    }
}
