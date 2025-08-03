package com.enigmatech.csvreader.formatter

import com.enigmatech.csvreader.impl.Formatter
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonFormatter @Inject constructor() : Formatter {
    override fun format(data: Map<String, Any>): String {
        val json = buildJsonObject {
            data.forEach { (key, value) ->
                put(key, value.toJsonElement())
            }
        }
        return Json.encodeToString(JsonObject.serializer(), json)
    }

    private fun Any?.toJsonElement(): JsonElement {
        return when (this) {
            null -> JsonNull
            is String -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is Map<*, *> -> buildJsonObject {
                this@toJsonElement.forEach { (k, v) ->
                    if (k is String) put(k, v.toJsonElement())
                }
            }
            is List<*> -> buildJsonArray {
                this@toJsonElement.forEach { add(it.toJsonElement()) }
            }
            else -> JsonPrimitive(this.toString()) // Fallback
        }
    }
}
