package io.offscale.converter

import io.offscale.lexer.Lexer
import io.offscale.parser.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

class Converter {

    val rootPath = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\converter\\"

    fun generateKotlinDataClass(kdocMap: MutableMap<String, Any?>, classIrMap: MutableMap<String, Any?>): String {
        val className = classIrMap["name"] as? String ?: "UnknownClass"
        val arguments = classIrMap["arguments"] as? List<Map<String, String>> ?: emptyList()
        val params = kdocMap["params"] as? Map<String, String> ?: emptyMap()
        val description = kdocMap["description"] as? String ?: ""

        val builder = StringBuilder()

        builder.appendLine("package io.offscale.example.models\n")
        builder.appendLine("import kotlinx.serialization.SerialName")
        builder.appendLine("import kotlinx.serialization.Serializable\n")

        // comments
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
            var paramType = arg["type"] ?: "Any"
            if (!(arg["required"] as Boolean)) paramType+="?"
            val serialName = paramName.lowercase()

            //builder.append("\t@SerialName(\"$serialName\")\n")
            builder.append("\tval $paramName: $paramType")

            if (index != arguments.lastIndex) {
                builder.append(",")
            }
            builder.append("\n")
        }

        builder.appendLine(")")

        return builder.toString()
    }






    fun generateKotlinKtorRoutes(map: Map<String, List<Map<String, Any?>>>): String {
        val result = StringBuilder()

        result.append(
            """
        |package com.example.routes
        |
        |import io.ktor.server.application.*
        |import io.ktor.server.response.*
        |import io.ktor.server.request.*
        |import io.ktor.server.routing.*
        |import io.ktor.http.*
        |
        |fun Application.configureRouting() {
        |    routing {
        |
        """.trimMargin()
        )


        map.forEach { (path, routes) ->
            result.append("        route(\"$path\") {\n")

            routes.forEach { route ->
                val method = route["method"] as? String ?: ""
                val summary = route["summary"] as? String ?: ""
                val description = route["description"] as? String ?: ""
                val operationId = route["operationId"] as? String ?: ""
                val tags = route["tags"] as? List<String> ?: emptyList()
                val parameters = route["parameters"] as? Map<String, Map<String, Any?>> ?: emptyMap()
                val responses = route["responses"] as? Map<String, Map<String, Any?>> ?: emptyMap()
                val requestBody = route["requestBody"] as? Map<String, Map<String, Any?>> ?: emptyMap()

                // kdoc (comments)
                result.append("            /**\n")
                if (summary.isNotEmpty()) result.append("             * $summary\n")
                if (description.isNotEmpty()) result.append("             *\n             * $description\n")
                if (operationId.isNotEmpty()) result.append("             *\n             * @operationId $operationId\n")
                if (tags.isNotEmpty()) result.append("             *\n             * @tags ${tags.joinToString(", ")}\n")

                // paramenters
                parameters.forEach { (paramName, paramDetails) ->
                    val paramDesc = paramDetails["description"] as? String ?: ""
                    val required = paramDetails["required"] as? Boolean ?: false
                    val type = paramDetails["type"] as? String ?: "unknown"
                    val inWhere = paramDetails["in"] as? String ?: "unknown"

                    result.append("             * @param $paramName ($type, required=$required, in=$inWhere) $paramDesc\n")
                }

                // request body
                requestBody.forEach { (contentType, schemaDetails) ->
                    val schemaRef = (schemaDetails["schema"] as? Map<String, String>)?.get("\$ref") ?: "unknown"
                    result.append("             * @body $contentType -> $schemaRef\n")
                }

                // responses
                responses.forEach { (statusCode, responseDetails) ->
                    val responseDesc = responseDetails["description"] as? String ?: ""
                    val content = responseDetails["content"] as? Map<String, Map<String, Any?>>
                    val schemaRef = content?.values?.firstOrNull()?.get("schema") as? Map<String, String> ?: emptyMap()
                    val ref = schemaRef["\$ref"] ?: "unknown"
                    result.append("             * @response $statusCode -> $responseDesc ($ref)\n")
                }

                result.append("             */\n")

                // Adiciona o método HTTP dentro da rota
                result.append("            $method {\n")
                result.append("                call.respondText(\"Handling $method at $path\", status = HttpStatusCode.OK)\n")
                result.append("            }\n\n")
            }

            result.append("        }\n")
        }

        result.append("    }\n")
        result.append("}\n")

        return result.toString()
    }



    fun generateOpenApiPaths(endpointsMap: MutableMap<String, MutableList<MutableMap<String, Any>>>): String {
        val openApiPaths = mutableMapOf<String, Any>()

        endpointsMap.forEach { (path, operations) ->
            val pathItem = mutableMapOf<String, Any>()

            operations.forEach { op ->
                val method = (op["method"] as String).lowercase()
                val operationObject = mutableMapOf<String, Any>()


                operationObject["operationId"] = op["operationId"] ?: "${method}_${path.replace("/", "_")}"

                operationObject["description"] = op["description"] ?: ""



                if (op.containsKey("parameters")) {
                    val params = op["parameters"] as List<Map<String, Any>>
                    val parametersList = params.map { param ->
                        mapOf(
                            "name" to param["name"],
                            "in" to (param["in"] ?: "query"),
                            "description" to param["description"],
                            "required" to (param["required"] ?: false),
                            "schema" to mapOf("type" to param["type"])
                        )
                    }
                    operationObject["parameters"] = parametersList
                }


                if (op.containsKey("responses")) {
                    val responses = op["responses"] as Map<String, Map<String, Any>>
                    val responsesTransformed = responses.mapValues { (_, responseDetails) ->
                        val responseObj = mutableMapOf<String, Any>(
                            "description" to (responseDetails["description"] ?: "")
                        )
                        if (responseDetails.containsKey("schema")) {
                            responseObj["content"] = mapOf(
                                "application/json" to mapOf(
                                    "schema" to mapOf("\$ref" to responseDetails["schema"])
                                )
                            )
                        }
                        responseObj
                    }
                    operationObject["responses"] = responsesTransformed
                }


                if (op.containsKey("security")) {
                    operationObject["security"] = op["security"] as? String ?: ""
                }

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

        val classBuilder = StringBuilder()
        classBuilder.append("package io.offscale.example.viewmodel\n\n")
        classBuilder.append("import kotlinx.coroutines.flow.MutableStateFlow\n")
        classBuilder.append("import kotlinx.coroutines.flow.StateFlow\n")
        classBuilder.append("import kotlinx.coroutines.CoroutineScope\n")
        classBuilder.append("import kotlinx.coroutines.Dispatchers\n")
        classBuilder.append("import kotlinx.coroutines.launch\n")
        classBuilder.append("import io.offscale.example.repository.${className}Repository\n")
        classBuilder.append("import io.offscale.example.models.${className}\n\n")

        classBuilder.append("class ${className}ViewModel(private val repository: ${className}Repository) {\n")
        classBuilder.append("    private val scope = CoroutineScope(Dispatchers.Main)\n\n")


        classBuilder.append("    private val _${className.lowercase()} = MutableStateFlow<${className}?>(null)\n")
        classBuilder.append("    val ${className.lowercase()}: StateFlow<${className}?> get() = _${className.lowercase()}\n\n")

        /*classBuilder.append("\t" + """
    /** Here should be implemented the functions for the view model:
    *
    * Example:
    *
    *  fun load${className}s() {
    *      scope.launch {
    *          try {
    *              val all${className}s = repository.getAll${className}s()
    *              _${className.lowercase()}s.value = all${className}s
    *          } catch (e: Exception) {
    *              _error.value = "Failed to load ${className}s"
    *          }
    *      }
    *  }
    *
    *  fun get${className}ById(id: String) {
    *      scope.launch {
    *          try {
    *              val ${className.lowercase()} = repository.get${className}ById(id)
    *              if (${className.lowercase()} != null) {
    *                  _${className.lowercase()}s.value = listOf(${className.lowercase()})
    *              } else {
    *                  _error.value = "${className} not found"
    *              }
    *          } catch (e: Exception) {
    *              _error.value = "Error retrieving ${className}"
    *          }
    *      }
    *  }
    *
    *  fun add${className}(${className.lowercase()}: ${className}) {
    *      scope.launch {
    *          try {
    *              repository.add${className}(${className.lowercase()})
    *              load${className}s()
    *          } catch (e: Exception) {
    *              _error.value = "Failed to add ${className}"
    *          }
    *      }
    *  }
    *
    *  fun remove${className}ById(id: String) {
    *      scope.launch {
    *          try {
    *              repository.remove${className}ById(id)
    *              load${className}s()
    *          } catch (e: Exception) {
    *              _error.value = "Failed to remove ${className}"
    *          }
    *      }
    *  }
    */
    """)*/

        classBuilder.append("\n}\n")
        return classBuilder.toString()
    }

    fun generateRepository(schema: Map<String, Any?>, additionalCode: String = ""): String {
        val className = schema["title"] as String
        val classBuilder = StringBuilder()

        classBuilder.append("package io.offscale.example.repository\n\n")
        classBuilder.append("import io.offscale.example.models.$className\n\n")

        classBuilder.append("class ${className}Repository{\n")
        classBuilder.append("\tprivate val ${className.lowercase()}s : MutableList<$className> = mutableListOf()\n\n")
        classBuilder.append("$additionalCode\n")
        classBuilder.append(""" 
    /** Here should be implemented the functions for the repository:
    *
    * Example:
    *
    *  fun get${className}ById(id: String): ${className}? { 
    *        // to get a ${className.lowercase()} by ID 
    *        return ${className.lowercase()}s.find { it.id == id }
    *  }
    *
    *  fun getAll${className}s(): List<${className}> {
    *        // to get all ${className.lowercase()}s
    *        return ${className.lowercase()}s
    *  }
    *
    *  fun add${className}(${className.lowercase()}: ${className}) {
    *        // to add a ${className.lowercase()}
    *        ${className.lowercase()}s.add(${className.lowercase()})
    *  }
    *
    *  fun remove${className}ById(id: String) { 
    *        // operation to remove a ${className.lowercase()} 
    *        val ${className.lowercase()}ToRemove = ${className.lowercase()}s.find { it.id == id }
    *        if (${className.lowercase()}ToRemove != null) {
    *            ${className.lowercase()}s.remove(${className.lowercase()}ToRemove)
    *        }
    *  }
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
            "object" -> "Map"
            "array" -> "List"
            else -> "Any"
        }
    }

    fun mapKotlinToJson(kotlinType: String): String {
        return when (kotlinType) {
            "String" -> "string"
            "Double" -> "number"
            "Int" -> "integer"
            "Boolean" -> "boolean"
            "Map" -> "object"
            "List" -> "array"
            else -> "None"
        }
    }

    fun yamlToJsonSchema(yamlString: String): Map<String, Any?> {
        val yaml = Yaml()
        val yamlMap = yaml.load<Map<String, Any>>(yamlString)
        val schema = mutableMapOf<String, Any?>()

        yamlMap.forEach { (key, value) ->
            if (value is Map<*, Any?>) {
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
                schema["description"] = value["description"]
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
            var type = argDetails["type"]
            if (argDetails["type"] is Map<*,*>){
                val type_ = argDetails["type"] as Map<*,*>
                type = type_["type"]
            }
            val kotlinType: Any = when (type) {
                "array" -> {
                    val itemType = (argDetails["items"] as? Map<*, *>)?.get("type") as? String ?: "Any"
                    mapOf("type" to "List", "value" to mapKotlinToJson(itemType))
                }
                "object" -> {
                    val valueType = (argDetails["additionalProperties"] as? Map<*, *>)?.get("type") as? String ?: "Any"
                    mapOf("type" to "Map", "value" to mapOf("key" to "String", "value" to mapKotlinToJson(valueType)))
                }
                else -> mapJsonTypeToKotlin(type as? String ?: "Any")
            }

            mapOf(
                "value" to argName,
                "type" to kotlinType,
                "default" to if (argName in required) null else argDetails["default"],
                "required" to if (argName in required) true else false
            )
        } ?: emptyList()

        properties?.forEach { (propName, propValue) ->
            params[propName] = propValue["description"] as? String ?: ""
        }

        kdocMap["params"] = params
        return mutableListOf(
            mutableMapOf("name" to title, "arguments" to arguments),
            kdocMap
        )
    }


    fun IrToJsonSchema(ir: Map<String, Any?>, kdoc: Map<String, Any>): Map<String, Any?> {
        val title = ir["name"] as? String ?: "None"

        val id = "https://example.com/$title.schema.json"
        val schemaUrl = "https://json-schema.org/draft/2020-12/schema"

        val description = kdoc["description"] as? String ?: "None"

        val properties = mutableMapOf<String, Any?>()
        val required = mutableListOf<String>()
        val parameters = kdoc["parameters"] as? Map<String, String> ?: emptyMap()
        val arguments = ir["arguments"] as? List<Map<String, Any?>>

        arguments?.forEach { arg ->
            var argName = arg["value"] as? String ?: "unknown"
            var description = parameters[argName] ?: "None"

            if (arg["alias"] != null) {
                argName = arg["alias"].toString()
            }

            val mapType = arg["type"] as? Map<String, Any> ?: emptyMap()
            val type = mapType["type"] as? String ?: "Any"

            val jsonType: Any = when (type) {
                "List" -> {
                    val itemType = mapKotlinToJson(mapType["value"] as? String ?: "Any")
                    mapOf(
                        "type" to "array",
                        "items" to mapOf("type" to itemType)
                    )
                }
                "Map" -> {
                    val valueType = (mapType["value"] as? Map<*, *>)?.get("value") as? String ?: "Any"
                    mapOf(
                        "type" to "object",
                        "additionalProperties" to mapOf("type" to mapKotlinToJson(valueType))
                    )
                }
                else -> mapKotlinToJson(type)
            }

            val defaultValue = arg["default"]

            if (defaultValue == null && arg["required"] as Boolean) required.add(argName)

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

    fun openAPISpecsExtract(yamlString: String): MutableMap<String, Any> {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
        val yaml = Yaml(options)
        val openApiYaml = yaml.load<Map<String, Any>>(yamlString)

        val openApiComponents = openApiYaml["components"] as Map<String, *>
        val schemas = openApiComponents["schemas"] as Map<String, MutableMap<String, Any>>
        val paths = openApiYaml["paths"] as Map<String, Any>

        val schemasFiltered = schemas .filterValues { schemaValue ->
            schemaValue["type"] == "object"
        }.mapValues { (schemaName, schemaValue) ->
            val schemaWithName = mapOf(schemaName to schemaValue)
            yaml.dump(schemaWithName)
        }
        val pathsYaml = yaml.dump(mapOf("paths" to paths))
        val pathsMap = mapOf("paths" to paths)

        return mutableMapOf(
            "SchemasFiltered" to schemasFiltered,
            "Schemas" to schemas,
            "Paths" to pathsYaml,
            "PathsMap" to pathsMap
        )
    }

    fun extractOperationIdToSchemas(pathsMap: Map<String, Any?>): Map<String, List<String>> {
        val operationToSchemas = mutableMapOf<String, MutableSet<String>>()

        val paths = pathsMap["paths"] as? Map<String, Any?> ?: return emptyMap()

        paths.forEach { (_, methods) ->
            val methodsMap = methods as? Map<String, Any?> ?: return@forEach

            methodsMap.forEach { (_, details) ->
                val detailsMap = details as? Map<String, Any?> ?: return@forEach

                val operationId = detailsMap["operationId"] as? String
                val responses = detailsMap["responses"] as? Map<String, Any?>

                if (operationId != null && responses != null) {
                    responses.forEach { (_, responseDetails) ->
                        val responseMap = responseDetails as? Map<String, Any?>
                        val content = responseMap?.get("content") as? Map<String, Any?>

                        content?.forEach { (_, mediaContent) ->
                            val schema = (mediaContent as? Map<String, Any?>)?.get("schema")

                            val schemaName = (schema as? Map<String, String>)?.get("\$ref")?.substringAfterLast("/")
                            if (schemaName != null) {
                                operationToSchemas.computeIfAbsent(operationId) { mutableSetOf() }.add(schemaName)
                            }
                        }
                    }
                }
            }
        }

        return operationToSchemas.mapValues { it.value.toList() }
    }


    fun generateOperationId(pathsmap: Map<String,Any?>,schemasMap : Map<String,Any?>) {
        var operationToSchema = extractOperationIdToSchemas(pathsmap)
        var schemasToOperation : MutableMap<String,Any> = mutableMapOf()
        for ((operationId,schemas) in operationToSchema) {
            for(schema in schemas) {
                if((schemasMap[schema] as Map<String,String>).get("type") != "object"){
                    // ignore in the meanwhile
                } else {
                    val classBuilder = StringBuilder()
                    classBuilder.append("\tfun $operationId() : $schema {\n\n")
                    classBuilder.append("\t}")
                    (schemasToOperation as MutableMap<String,MutableList<String>>).getOrPut(schema) { mutableListOf() }.add(classBuilder.toString())

                }
            }
        }
        schemasToOperation.forEach { (schema, functions) ->
            schemasToOperation[schema] = (functions as MutableList<String>).joinToString("\n\n")
        }

        schemasToOperation.forEach {(schema, function) ->
            val repo = generateRepository(mutableMapOf("title" to schema),function as String)
            createFileWithDirectories(rootPath + "example/repository/${schema}Repository.kt", repo)

        }


    }

    fun createFileWithDirectories(filePath: String, content: String) {
        val file = File(filePath)
        val parentDir = file.parentFile

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        file.writeText(content)
    }

    fun manage(yamlFile: String) {
        var yamlString = File(rootPath+yamlFile).readText().replace("\r\n", "\n")
        val extractingYaml = openAPISpecsExtract(yamlString)

        // Generating the models and viewmodels
        val schemasFiltered = extractingYaml["SchemasFiltered"] as MutableMap<String,String>
        schemasFiltered.forEach { (model, schema) -> OpenAPItoClassData(schema, model) }
        schemasFiltered.forEach { (model, schema) -> OpenApiToViewModel(schema,model)}

        // Generating the paths
        val paths = extractingYaml["Paths"] as String
        OpenAPIToRoute(paths)

        //Generating the functions for the OperationId ( in teh repostitories)
        val pathsMap = extractingYaml["PathsMap"] as Map<String, Any?>
        val schemas = extractingYaml["Schemas"] as Map<String, Any?>
        generateOperationId(pathsMap,schemas)




    }

    fun OpenApiPathToKtorRouteMap(yamlString: String): MutableMap<String, MutableList<MutableMap<String, Any?>>> {
        val yaml = Yaml()
        val openApiPaths = yaml.load<Map<String, Any>>(yamlString)
        val endpointsMap = mutableMapOf<String, MutableList<MutableMap<String, Any?>>>()


        if (openApiPaths.containsKey("paths") && openApiPaths["paths"] is Map<*, *>) {
            val paths = openApiPaths["paths"] as Map<String, Any>
            paths.forEach { (path, methods) ->
                if (methods is Map<*, *>) {
                    endpointsMap[path] = parseOperations(methods as Map<String, Any>)
                } else {
                    println("Unexpected Method $path: $methods")
                }
            }
        } else {
            println(" key 'paths' not found")
        }

        return endpointsMap
    }

    private fun parseOperations(methods: Map<String, Any>): MutableList<MutableMap<String, Any?>> {
        val operationsList = mutableListOf<MutableMap<String, Any?>>()

        methods.forEach { (method, operation) ->
            val operationMap = operation as Map<String, Any>
            val endpointInfo = mutableMapOf<String, Any?>()

            endpointInfo["method"] = method
            endpointInfo["summary"] = operationMap["summary"]
            endpointInfo["operationId"] = operationMap["operationId"]
            endpointInfo["description"] = operationMap["description"]
            endpointInfo["tags"] = operationMap["tags"] ?: listOf<String>()

            endpointInfo["parameters"] = parseParameters(operationMap)
            endpointInfo["responses"] = parseResponses(operationMap)
            endpointInfo["requestBody"] = parseRequestBody(operationMap)

            operationsList.add(endpointInfo)
        }

        return operationsList
    }

    private fun parseParameters(operationMap: Map<String, Any>): MutableMap<String, Any?> {
        val parametersMap = mutableMapOf<String, Any?>()

        if (operationMap.containsKey("parameters")) {
            val parametersList = operationMap["parameters"] as List<Map<String, Any>>
            parametersList.forEach { param ->
                val paramName = param["name"] as String
                val paramDetails = mutableMapOf<String, Any?>(
                    "in" to param["in"],
                    "required" to param["required"],
                    "description" to param["description"],
                    "type" to (param["schema"] as? Map<String, Any?>)?.get("type")
                )
                parametersMap[paramName] = paramDetails
            }
        }

        return parametersMap
    }

    private fun parseResponses(operationMap: Map<String, Any>): MutableMap<String, Any?> {
        val responsesMap = mutableMapOf<String, Any?>()

        if (operationMap.containsKey("responses")) {
            val responses = operationMap["responses"] as Map<String, Any>
            responses.forEach { (statusCode, response) ->
                val responseMap = mutableMapOf<String, Any?>(
                    "description" to (response as? Map<String, Any?>)?.get("description"),
                    "content" to (response as? Map<String, Any?>)?.get("content")
                )
                responsesMap[statusCode] = responseMap
            }
        }

        return responsesMap
    }

    private fun parseRequestBody(operationMap: Map<String, Any>): Any? {
        return (operationMap["requestBody"] as? Map<String, Any?>)?.get("content")
    }


    fun ClassDataToOpenAPI(fileModelPath: String) {
        val codeClass = File(fileModelPath).readText().replace("\r\n", "\n")
        val parserClass = Parser(Lexer(codeClass).tokenize())
        val sourceClass = parserClass.parseCode() as SourceBlock
        val dataClass = parserClass.findBlock(sourceClass, ClassBlock::class) as ClassBlock
        print(dataClass.structure)
        var classDict = IrToJsonSchema(dataClass.structure, sourceClass.kdoc)
        var yamlApi = generateOpenApiObject(classDict)
        File(rootPath + "model.yaml").writeText(yamlApi)
    }

    fun OpenApiToViewModel(yamlString: String,model:String) {
        val jsonSchema = yamlToJsonSchema(yamlString)
        var view = generateViewModel(jsonSchema)
        createFileWithDirectories(rootPath + "example/viewmodel/${model}ViewModel.kt", view)
    }

    fun OpenAPItoClassData(yamlString: String,model: String) {
        val jsonSchema = yamlToJsonSchema(yamlString)
        val (modelIR, kdoc) = jsonSchemaToIr(jsonSchema)
        val classText = generateKotlinDataClass(kdoc, modelIR)
        createFileWithDirectories(rootPath + "example/model/${model}.kt", classText)
    }

    fun RouteToOpenAPI(fileRoutePath: String) {
        val codeRoutes = File(fileRoutePath).readText().replace("\r\n", "\n")
        val parserRoutes = Parser(Lexer(codeRoutes).tokenize())
        val sourceRoutes = parserRoutes.parseCode() as SourceBlock
        val routes = parserRoutes.findBlock(sourceRoutes, RoutingBlock::class) as RoutingBlock
        //print(routes.routes)
        val yamlApi = generateOpenApiPaths(routes.routes)
        File(rootPath + "Troute.yaml").writeText(yamlApi)
    }

    fun OpenAPIToRoute(yamlString: String) {
        var routeMap = OpenApiPathToKtorRouteMap(yamlString)
        //print(routeMap)
        var routeKotlin = generateKotlinKtorRoutes(routeMap)
        createFileWithDirectories(rootPath + "example/client/routes.kt", routeKotlin)
    }
}

fun main() {
    val converter = Converter()
    val rootPath = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\converter\\"
    var filepathModel = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\example\\model\\Cat.kt"
    var filepathRoute = "C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\example\\api\\CatRoutes.kt"
    converter.ClassDataToOpenAPI("C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\converter\\example\\model\\Pet.kt")
    converter.RouteToOpenAPI("C:\\Users\\jeans\\Desktop\\cdd-kotlin\\src\\main\\kotlin\\converter\\example\\client\\routes.kt")
   // converter.OpenAPItoClassData(rootPath+"model.yaml")
    //converter.OpenAPIToRoute(rootPath+"route.yaml")
    val codeClass = File(filepathModel).readText().replace("\r\n", "\n")
    val parserClass = Parser(Lexer(codeClass).tokenize())
    val sourceClass = parserClass.parseCode() as SourceBlock
    val dataClass = parserClass.findBlock(sourceClass, ClassBlock::class) as ClassBlock
    var classDict = converter.IrToJsonSchema(dataClass.structure, sourceClass.kdoc)
    //var view = converter.generateViewModel(classDict)
    //var repo = converter.generateRepository(classDict)
    var testmodel = converter.generateTestModel(classDict)
    var testViewModel = converter.generateTestViewModel(classDict)
    //var testRepository = converter.generateTestRepository(classDict)
    //File(rootPath + "repository.kt").writeText(repo)
    //File(rootPath+ "viewmodel.kt").writeText(view)
    //File(rootPath+"testmodel.kt").writeText(testmodel)
    //File(rootPath+"testRepository.kt").writeText(testRepository)
    //File(rootPath+"testViewmodel.kt").writeText(testViewModel)

    //val yamlText = File(rootPath+"pet.yaml").readText()
    //print(converter.openAPISpecsExtract(yamlText))

    //converter.manage("pet.yaml")

}

