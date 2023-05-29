package dev.teogor.pixel.harvest.dictionary

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.util.SortedSet

class TextFileParser(private val packageName: String, private val outputFilePath: String) {

    fun parseTextFile(inputFilePath: String, datasetName: String): SortedSet<String> {
        val name = datasetName.toCamelCase()
        val nameLower = name.replaceFirstChar { it.lowercase() }
        val className = "${name}Dictionary"
        val inputFile = File(inputFilePath)
        val outputFile = File("$outputFilePath/$className.kt")
        val sortedItems: SortedSet<String>

        val categories = mutableMapOf<String, MutableSet<String>>()
        var currentCategory = ""
        var hasCategories = false // Track if any categories were found

        inputFile.forEachLine { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("::category::")) {
                currentCategory = trimmedLine.removePrefix("::category::")
                hasCategories = true
            } else if (trimmedLine.isNotBlank()) {
                categories.getOrPut(currentCategory) { mutableSetOf() }.add(trimmedLine)
            }
        }

        sortedItems = categories.values.flatten().toSortedSet()

        val listProperty = "list"
        val typeClassName = "${className}Types"

        val listPropertySpec = PropertySpec.builder(listProperty, MutableSet::class.parameterizedBy(String::class))
            .addModifiers(KModifier.PRIVATE)
            .initializer("setOf(${sortedItems.joinToString { "\"$it\"" }})")
            .build()

        // val builderLambdaTypeName = LambdaTypeName.get(Dictionary.Builder::class)
        // val companionObjectSpec = TypeSpec.companionObjectBuilder()
        //     .addFunction(
        //         FunSpec.builder("builder")
        //             .addParameter("block", builderLambdaTypeName)
        //             .returns(className)
        //             .addStatement("val builder = ${className}Builder()")
        //             .addStatement("builder.block()")
        //             .addStatement("return $className(builder.build().$listProperty)")
        //             .build()
        //     )
        //     .build()

        val dictionaryClassBuilder = TypeSpec.classBuilder(className)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(listProperty, MutableSet::class.parameterizedBy(String::class))
                    .build()
            )
            .superclass(Dictionary::class.parameterizedBy(String::class))
            .addType(TypeSpec.classBuilder("${className}Builder")
                .superclass(Dictionary.Builder::class)
                .build()
            )
            .addType(TypeSpec.enumBuilder(typeClassName)
                .addModifiers(KModifier.PRIVATE)
                .addSuperinterface(Dictionary.Type::class)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(nameLower, MutableSet::class.parameterizedBy(String::class))
                        .build()
                )
                .addProperty(PropertySpec.builder(nameLower, MutableSet::class.parameterizedBy(String::class))
                    .initializer(nameLower)
                    .build()
                )
                .addEnumConstant("ALL", TypeSpec.anonymousClassBuilder()
                    .addSuperclassConstructorParameter(
                        if (hasCategories) categories.keys.joinToString("Set + ") + "Set"
                        else listProperty
                    )
                    .addSuperclassConstructorParameter(CodeBlock.of("return %N", nameLower))
                    .build()
                )
                .apply {
                    if (hasCategories) {
                        categories.forEach { (category, _) ->
                            val categoryName = category.uppercase()
                            addEnumConstant(categoryName, TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter(category.toLowerCase() + "Set")
                                .addSuperclassConstructorParameter(CodeBlock.of("return %N", category.toLowerCase() + "Set"))
                                .build()
                            )
                        }
                    }
                }
                .addFunction(FunSpec.builder("getSet")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(MutableSet::class.parameterizedBy(String::class))
                    .addCode("return $nameLower")
                    .build()
                )
                .build()
            )
            .addProperty(listPropertySpec)
            // .addType(companionObjectSpec)
            .build()

        val fileSpec = FileSpec.builder(packageName, className)
            .addImport("dev.teogor.pixel.harvest.dictionary", "Dictionary")
            .addType(dictionaryClassBuilder)
            .build()

        fileSpec.writeTo(outputFile)

        return sortedItems
    }

    private fun String.toCamelCase(): String {
        return split(" ").joinToString("") { it.capitalize() }
    }
}
