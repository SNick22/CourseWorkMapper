package provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import processor.MyProcessor

class SimpleProcessorProvider: SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MyProcessor(
            fileGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
