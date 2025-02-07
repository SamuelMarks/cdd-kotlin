package io.offscale.converter

import io.offscale.lexer.Lexer
import io.offscale.parser.ClassBlock
import io.offscale.parser.KtorRouteBlock
import io.offscale.parser.Parser
import io.offscale.parser.SourceBlock
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

class Converter {

    val rootPath = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\converter\\"

    fun generateKotlinDataClass(kdocMap: MutableMap<String, Any?>, classIrMap: MutableMap<String, Any?>): String {
        val className = classIrMap["title"] as? String ?: "UnknownClass"
        val arguments = classIrMap["arguments"] as? List<Map<String, String>> ?: emptyList()
        val params = kdocMap["params"] as? Map<String, String> ?: emptyMap()
        val description = kdocMap["description"] as? String ?: ""

        val builder = StringBuilder()

        builder.appendLine("package com.example.parsing.example.models\n")
        builder.appendLine("import kotlinx.serialization.SerialName")
        builder.appendLine("import kotlinx.serialization.Serializable\n")

        // KDoc comment
        builder.appendLine("/**")
        builder.appendLine(" * $description")
        builder.appendLine(" *")
        for (arg in arguments) {
            val paramName = arg["value"] ?: continue
            val paramDesc = params[paramName] ?: ""
            builder.appendLine(" * @param $paramName $paramDesc")
        }
        builder.appendLine(" */")


        builder.appendLine("@Serializable")
        builder.appendLine("data class $className(")


        arguments.forEachIndexed { index, arg ->
            val paramName = arg["value"] ?: return@forEachIndexed
            val paramType = arg["type"] ?: "Any"
            val serialName = paramName.lowercase()

            builder.append("\t@SerialName(\"$serialName\")\n")
            builder.append("\tval $paramName: $paramType")

            if (index != arguments.lastIndex) {
                builder.append(",")
            }
            builder.append("\n")
        }

        builder.appendLine(")")

        return builder.toString()
    }

    fun generateKotlinKtorRoutes(map: Map<String, List<Map<String, Any>>>): String {
        val result = StringBuilder()

        map.forEach { (path, routes) ->
            result.append("route(\"$path\") {\n")

            routes.forEach { route ->
                // Descrição
                val description = route["description"] as? String ?: ""
                val responses = route["responses"] as? Map<String, String> ?: emptyMap()
                val parameters = route["parameters"] as? Map<String, String> ?: emptyMap()
                val produces = route["produces"] as? String
                val body = route["body"] as? String

                result.append("/**\n")
                result.append(" * $description\n")

                responses.forEach { (statusCode, message) ->
                    val code = statusCode.toString()
                    val msg = message.toString()
                    result.append(" * @response $code $msg\n")
                }

                parameters.forEach { (param, desc) ->
                    result.append(" * @param $param $desc\n")
                }

                produces?.let { result.append(" * @produces $it\n") }
                body?.let { result.append(" * @body $it\n") }

                result.append(" */\n")

                val method = route["method"] as? String ?: ""
                result.append("    $method {}\n")
            }

            result.append("}\n")
        }

        return result.toString()
    }

    fun generateOpenApiPaths(endpointsMap: MutableMap<String, MutableList<MutableMap<String, Any>>>): String {

        val openApiPaths = mutableMapOf<String, Any>()

        endpointsMap.forEach { (path, operations) ->

            val pathItem = mutableMapOf<String, Any>()
            operations.forEach { op ->

                val method = (op["method"] as String).lowercase()
                val operationObject = mutableMapOf<String, Any>()


                operationObject["description"] = op["description"]!!


                if (op.containsKey("produces")) {

                    operationObject["produces"] = listOf(op["produces"]!!)
                }


                if (op.containsKey("parameters")) {
                    val params = op["parameters"] as Map<String, String>

                    val parametersList = params.map { (name, description) ->
                        mapOf(
                            "name" to name,
                            "in" to "path",
                            "description" to description,
                            "required" to true
                        )
                    }
                    operationObject["parameters"] = parametersList
                }


                val responses = op["responses"] as Map<String, String>
                val responsesTransformed = responses.mapValues { (_, desc) ->
                    mapOf("description" to desc)
                }
                operationObject["responses"] = responsesTransformed


                pathItem[method] = operationObject
            }
            openApiPaths[path] = pathItem
        }


        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
        val yaml = Yaml(options)
        return yaml.dump(openApiPaths)
    }

    fun generateOpenApiObject(jsonSchema: Map<String, Any?>): String {

        val title = jsonSchema["title"] as String
        val type = jsonSchema["type"] as String
        val description = jsonSchema["description"] as String
        val required = jsonSchema["required"] as List<String>
        val properties = jsonSchema["properties"] as Map<String, Map<String, Any>>


        val propertiesTransformed = properties.mapValues { (_, prop) ->
            mapOf(
                "type" to prop["type"],
                "description" to prop["description"]
            )
        }


        var dictAPi = mutableMapOf(
            title to mutableMapOf(
                "type" to type,
                "description" to description,
                "required" to required,
                "properties" to propertiesTransformed
            )
        )
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
        val yaml = Yaml(options)

        return yaml.dump(dictAPi)


    }

    fun generateViewModel(schema: Map<String, Any?>): String {
        val className = schema["title"] as String
        val properties = schema["properties"] as? Map<String, Map<String, Any?>> ?: emptyMap()
        val requiredFields = (schema["required"] as? List<String>) ?: emptyList()

        val classBuilder = StringBuilder()
        classBuilder.append("import kotlinx.coroutines.flow.MutableStateFlow\n")
        classBuilder.append("import kotlinx.coroutines.flow.StateFlow\n")
        classBuilder.append("import kotlinx.coroutines.CoroutineScope\n")
        classBuilder.append("import kotlinx.coroutines.Dispatchers\n")
        classBuilder.append("import kotlinx.coroutines.launch\n")
        classBuilder.append("import io.offscale.example.repository.${className}Repository\n\n")
        classBuilder.append("class ${className}ViewModel(private val repository: ${className}Repository) {\n")
        classBuilder.append("    val scope = CoroutineScope(Dispatchers.Main)\n\n")

        properties.forEach { (key, property) ->
            val type = mapJsonTypeToKotlin(property["type"] as? String ?: "Any")
            classBuilder.append("    private val _${key} = MutableStateFlow<$type?>(null)\n")
            classBuilder.append("    val $key: StateFlow<$type?> get() = _${key}\n\n")
        }

        classBuilder.append("\t"+
            """
            /** Here should be implemented the functions for the view model:
            *
            * Example:
            * fun loadCat() {
            *     scope.launch {
            *         try {
            *             val allCat = repository.getAllCat()
            *             _cats.value = allCat
            *         }catch (e: Exception) {
            *             _error.value = "Failed to load Cat"
            *         }
            *     }
            * }
            */
        """
        )
        // Todo: Perhaps, should I also create a mode in which all the routes, repos and viewmodels come
        //  ready accordingly to the json Schema ?
        //classBuilder.append("    fun load$className() {\n")
        //classBuilder.append("        scope.launch {\n")
        //classBuilder.append("            try {\n")
        //classBuilder.append("                val all$className = repository.getAll$className()\n")
        //classBuilder.append("                _${className.lowercase()}s.value = all$className\n")
        //classBuilder.append("            } catch (e: Exception) {\n")
        //classBuilder.append("                _error.value = \"Failed to load $className\"\n")
        //classBuilder.append("            }\n")
        //classBuilder.append("        }\n")
        //classBuilder.append("    }\n")

        classBuilder.append("\n}\n")
        return classBuilder.toString()
    }
    fun generateRepository(schema: Map<String, Any?>): String {
        val className = schema["title"] as String
        val classBuilder = StringBuilder()

        classBuilder.append("package io.offscale.example.repository\n\n")
        classBuilder.append("import io.offscale.example.models.$className\n\n")

        classBuilder.append("class ${className}Repository{\n")
        classBuilder.append("    private val ${className.lowercase()}s : MutableList<$className> = mutableListOf()\n\n")

        classBuilder.append("\t"+
            """ 
            /** Here should be implemented the functions for the repository:
            *
            * Example:
            *  fun getAllCats(): List<Cat> = cats
            *
            *  fun getCatByName(name: String): Cat? = cats.find { it.name == name }
            *
            *  fun addCat(cat: Cat) {
            *        cats.add(cat)
            *    }
            */
        """
        )

        classBuilder.append("\n}\n")
        return classBuilder.toString()
    }

    fun generateTestModel(schema: Map<String, Any?>): String {
        val className = schema["title"] as String
        val classBuilder = StringBuilder()

        classBuilder.append("package io.offscale.example.tests\n\n")
        classBuilder.append("import kotlinx.serialization.json.Json\n")
        classBuilder.append("import org.junit.Test\n\n")

        classBuilder.append("class ${className}Test {\n")
        classBuilder.append("\tprivate val json = Json { prettyPrint = true }\n\n")
        classBuilder.append("\t@Test\n")
        classBuilder.append("\tfun modelTest() {\n" +
                "        // Here is where the tests should happen and you can add more. \n" +
                "    }\n")


        classBuilder.append("}\n")
        return classBuilder.toString()

    }




    fun generateTestViewModel(schema: Map<String, Any?>): String {
        val className = schema["title"] as String
        val classBuilder = StringBuilder()

        classBuilder.append("package io.offscale.example.tests\n\n")
        classBuilder.append("import io.offscale.example.repository.${className}Repository\n")
        classBuilder.append("import io.offscale.example.viewmodel.${className}ViewModel\n")
        classBuilder.append("import org.junit.Test\n")
        classBuilder.append("import org.junit.Before\n")
        classBuilder.append("import org.mockito.kotlin.mock\n\n")

        classBuilder.append("class ${className}ViewModelTest {\n")
        classBuilder.append("\tprivate lateinit var viewModel: ${className}ViewModel\n")
        classBuilder.append("\tprivate val repository: ${className}Repository = mock()\n\n")
        classBuilder.append("\t@Before\n")
        classBuilder.append("\tfun setUp() {\n" +
                "        // Here is where the viewmodel is initialized, before each test. \n" +
                "    }\n")
        classBuilder.append("\t@Test\n")
        classBuilder.append("\tfun ViewmodelTest() {\n" +
                "        // Here is where the tests should happen and you can add more. \n" +
                "    }\n")


        classBuilder.append("}\n")
        return classBuilder.toString()

    }

    fun generateTestRepository(schema: Map<String, Any?>): String {
        val className = schema["title"] as String
        val classBuilder = StringBuilder()

        classBuilder.append("package io.offscale.example.tests\n\n")
        classBuilder.append("import org.junit.Test\n")
        classBuilder.append("import kotlin.test.assertEquals\n")
        classBuilder.append("import kotlin.test.assertNull\n")
        classBuilder.append("import io.offscale.example.repository.${className}Repository\n\n")

        classBuilder.append("class ${className}RepositoryTest {\n")

        classBuilder.append("\tprivate val repository = ${className}Repository()\n\n")
        classBuilder.append("\t@Test\n")
        classBuilder.append("\tfun modelTest() {\n" +
                "        // Here is where the tests should happen and you can add more. \n" +
                "    }\n")



        classBuilder.append("}\n")

        return classBuilder.toString()
    }

    fun mapJsonTypeToKotlin(jsonType: String): String {
        return when (jsonType) {
            "string" -> "String"
            "number" -> "Double"
            "integer" -> "Int"
            "boolean" -> "Boolean"
            else -> "Any"
        }
    }

    fun mapKotlinToJson(kotlinType: String): String {
        return when (kotlinType) {
            "String" -> "string"
            "Double" -> "number"
            "Int" -> "integer"
            "Boolean" -> "boolean"
            else -> "None"
        }
    }

    fun yamlToJsonSchema(yamlString: String): Map<String, Any> {
        val yaml = Yaml()
        val yamlMap = yaml.load<Map<String, Any>>(yamlString)
        val schema = mutableMapOf<String, Any>()

        yamlMap.forEach { (key, value) ->
            if (value is Map<*, *>) {
                val properties = mutableMapOf<String, Map<String, Any>>()
                val required = mutableListOf<String>()

                (value["properties"] as? Map<*, *>)?.forEach { (propKey, propValue) ->
                    if (propKey is String && propValue is Map<*, *>) {
                        properties[propKey] = mapOf(
                            "type" to (propValue["type"] ?: "None"),
                            "description" to (propValue["description"] ?: "")
                        )
                    }
                }

                (value["required"] as? List<*>)?.forEach {
                    if (it is String) required.add(it)
                }

                schema["id"] = "https://example.com/$key.schema.json"
                schema["schema"] = "https://json-schema.org/draft/2020-12/schema"
                schema["title"] = key
                schema["description"] = value["description"] as Any
                schema["type"] = "object"
                schema["properties"] = properties
                schema["required"] = required
            }
        }

        return schema
    }

    fun jsonSchemaToIr(jsonSchema: Map<String, Any?>): MutableList<MutableMap<String, Any?>> {
        val kdocMap = mutableMapOf<String, Any?>()
        val params = mutableMapOf<String, String>()
        kdocMap["description"] = jsonSchema["description"]


        val id = jsonSchema["id"] as? String ?: "https://example.com/INFERRED.schema.json"
        val title = jsonSchema["title"]
        val properties = jsonSchema["properties"] as? Map<String, Map<String, Any?>>
        val required = jsonSchema["required"] as? List<String> ?: emptyList()

        val arguments = properties?.map { (argName, argDetails) ->
            mapOf(
                "value" to argName,
                "type" to mapKotlinToJson(argDetails["type"] as String),
                "default" to if (argName in required) null else argDetails["default"]
            )

        } ?: emptyList()

        properties?.forEach { (propName, propValue) ->
            params[propName] = propValue["description"] as? String ?: ""
        }

        kdocMap["params"] = params
        return mutableListOf(
            mutableMapOf("title" to title, "arguments" to arguments),
            kdocMap
        )
    }

    fun IrToJsonSchema(ir: Map<String, Any?>, kdoc: Map<String, Any>): Map<String, Any?> {
        val title = ir["name"] as? String ?: "None"

        val id = "https://example.com/$title.schema.json" // TODO: I need to understand better what is this
        val schemaUrl = "https://json-schema.org/draft/2020-12/schema"

        val description = kdoc["description"] as? String ?: "None"


        val properties = mutableMapOf<String, Any?>()
        val required = mutableListOf<String>()
        val parameters = kdoc["parameters"] as MutableMap<String, String>
        val arguments = ir["arguments"] as? List<Map<String, Any?>>
        arguments?.forEach { arg ->

            var argName = arg["value"] as? String ?: "unknown"
            var description = "None"
            if (parameters[argName] != null) {
                description = parameters[argName]?.toString() ?: "None"
            }
            if (arg["alias"] != null) {
                argName = arg["alias"].toString()
            }
            val typeStr = arg["type"] as? String ?: "None"
            val jsonType = mapJsonTypeToKotlin(typeStr)
            val defaultValue = arg["default"]

            if (defaultValue == null) required.add(argName)

            properties[argName] = mapOf(
                "type" to jsonType,
                "description" to description,
                "default" to defaultValue
            )
        }

        return mapOf(
            "id" to id,
            "schema" to schemaUrl,
            "title" to title,
            "description" to description,
            "type" to "object",
            "properties" to properties,
            "required" to required
        )
    }

    fun OpenApiPathToKtorRouteMap(yamlString: String): MutableMap<String, MutableList<MutableMap<String, Any>>> {
        val yaml = Yaml()
        val openApiPaths = yaml.load<Map<String, Any>>(yamlString)

        val endpointsMap = mutableMapOf<String, MutableList<MutableMap<String, Any>>>()

        openApiPaths.forEach { (path, methods) ->
            val operationsList = mutableListOf<MutableMap<String, Any>>()

            (methods as Map<String, Any>).forEach { (method, operation) ->
                val operationMap = operation as Map<String, Any>
                val endpointInfo = mutableMapOf<String, Any>()

                endpointInfo["method"] = method
                endpointInfo["description"] = operationMap["description"] as String

                // Extrai 'produces', que pode ser uma lista
                if (operationMap.containsKey("produces")) {
                    val producesList = operationMap["produces"] as List<String>
                    endpointInfo["produces"] = producesList.firstOrNull() ?: ""
                }


                if (operationMap.containsKey("parameters")) {
                    val parametersList = operationMap["parameters"] as List<Map<String, Any>>
                    val parametersMap = mutableMapOf<String, String>()

                    parametersList.forEach { param ->
                        val name = param["name"] as String
                        val description = param["description"] as String
                        parametersMap[name] = description
                    }

                    endpointInfo["parameters"] = parametersMap
                }


                val responsesMap = mutableMapOf<String, String>()
                if (operationMap.containsKey("responses")) {
                    val responses = operationMap["responses"] as Map<String, Map<String, String>>
                    responses.forEach { (statusCode, response) ->
                        responsesMap[statusCode] = response["description"] ?: ""
                    }
                }
                endpointInfo["responses"] = responsesMap

                operationsList.add(endpointInfo)
            }

            endpointsMap[path] = operationsList
        }

        return endpointsMap
    }


    fun ClassDataToOpenAPI(fileModelPath: String) {
        val codeClass = File(fileModelPath).readText().replace("\r\n", "\n")
        val parserClass = Parser(Lexer(codeClass).tokenize())
        val sourceClass = parserClass.parseCode() as SourceBlock
        val dataClass = parserClass.findBlock(sourceClass, ClassBlock::class) as ClassBlock
        var classDict = IrToJsonSchema(dataClass.structure, sourceClass.kdoc)
        var yamlApi = generateOpenApiObject(classDict)
        File(rootPath + "model.yaml").writeText(yamlApi)
    }

    fun OpenAPItoClassData(fileYamlPath: String) {
        var yamlfile = File(fileYamlPath).readText().replace("\r\n", "\n")
        val jsonSchema = yamlToJsonSchema(yamlfile)
        val (modelIR, kdoc) = jsonSchemaToIr(jsonSchema)
        val classText = generateKotlinDataClass(kdoc, modelIR)
        File(rootPath + "Model.kt").writeText(classText)
    }

    fun RouteToOpenAPI(fileRoutePath: String) {
        val codeRoutes = File(fileRoutePath).readText().replace("\r\n", "\n")
        val parserRoutes = Parser(Lexer(codeRoutes).tokenize())
        val sourceRoutes = parserRoutes.parseCode() as SourceBlock
        val routes = parserRoutes.findBlock(sourceRoutes, KtorRouteBlock::class) as KtorRouteBlock
        val yamlApi = generateOpenApiPaths(routes.routes)
        File(rootPath + "route.yaml").writeText(yamlApi)
    }

    fun OpenAPIToRoute(fileYamlPath: String) {
        var yamlfile = File(fileYamlPath).readText().replace("\r\n", "\n")
        var routeMap = OpenApiPathToKtorRouteMap(yamlfile)
        var routeKotlin = generateKotlinKtorRoutes(routeMap)
        File(rootPath + "routes.kt").writeText(routeKotlin)
    }
}

fun main() {
    val converter = Converter()
    val rootPath = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\converter\\"
    var filepathModel = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\example\\model\\Cat.kt"
    var filepathRoute = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\example\\api\\CatRoutes.kt"
    //converter.ClassDataToOpenAPI(filepathModel)
    //converter.RouteToOpenAPI(filepathRoute)
    //converter.OpenAPItoClassData(rootPath+"model.yaml")
    //converter.OpenAPIToRoute(rootPath+"route.yaml")
    val codeClass = File(filepathModel).readText().replace("\r\n", "\n")
    val parserClass = Parser(Lexer(codeClass).tokenize())
    val sourceClass = parserClass.parseCode() as SourceBlock
    val dataClass = parserClass.findBlock(sourceClass, ClassBlock::class) as ClassBlock
    var classDict = converter.IrToJsonSchema(dataClass.structure, sourceClass.kdoc)
    var view = converter.generateViewModel(classDict)
    var repo = converter.generateRepository(classDict)
    var testmodel = converter.generateTestModel(classDict)
    var testViewModel = converter.generateTestViewModel(classDict)
    var testRepository = converter.generateTestRepository(classDict)
    //File(rootPath + "repository.kt").writeText(repo)
    //File(rootPath+ "viewmodel.kt").writeText(view)
    //File(rootPath+"testmodel.kt").writeText(testmodel)
    File(rootPath+"testRepository.kt").writeText(testRepository)
    //File(rootPath+"testViewmodel.kt").writeText(testViewModel)

}