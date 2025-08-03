package com.enigmatech.csvreader.impl

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

// --- SchemaParser Implementation ---
class SchemaParserImpl @Inject constructor() : ISchemaParser {
    override fun parse(json: String): NormalizationConfig {
        return Json.decodeFromString(json)
    }
}

class DefaultTransformer @Inject constructor() : Transformer {
    override fun applyTransform(value: String, rule: String?): Any {
        return when (rule) {
            "uppercase" -> value.uppercase()
            "lowercase" -> value.lowercase()
            "int" -> value.toIntOrNull() ?: value
            "float" -> value.toFloatOrNull() ?: value
            else -> value
        }
    }
}

class DefaultNormalizer @Inject constructor(
    private val transformer: Transformer
) : Normalizer {
    override fun normalize(
        parsedData: Map<Int, Array<String>>,
        config: NormalizationConfig
    ): Map<String, Any> {
        val output = mutableMapOf<String, Any>()

        for ((_, line) in parsedData) {
            if (line.isEmpty()) continue
            val typeKey = line[0]
            val recordConfig = config.recordTypes[typeKey] ?: continue
            val obj = mutableMapOf<String, Any?>()

            for (field in recordConfig.fields) {
                val rawValue = line.getOrNull(field.position) ?: ""
                obj[field.key] = transformer.applyTransform(rawValue, field.transform)
            }

            val containerKey = recordConfig.container
            if (recordConfig.isArray) {
                val container = output.getOrPut(containerKey) { mutableListOf<Any>() } as MutableList<Any>
                container.add(obj)
            } else {
                output[containerKey] = obj.values.first() as Any
            }
        }

        config.root?.let {
            return mapOf(it.element to if (it.isArray) listOf(output) else output)
        }

        return output
    }
}

// --- Data Classes ---

@Serializable
data class NormalizationConfig(
    val outputFormat: String,
    val structure: List<String>,
    val root: RootConfig? = null,
    val recordTypes: Map<String, RecordType>
)

@Serializable
data class RootConfig(
    val element: String,
    val isArray: Boolean = false
)

@Serializable
data class RecordType(
    val type: String,
    val container: String,
    val itemTag: String? = null,
    val isArray: Boolean = false,
    val fields: List<FieldMapping>
)

@Serializable
data class FieldMapping(
    val key: String,
    val position: Int,
    val transform: String? = null
)
