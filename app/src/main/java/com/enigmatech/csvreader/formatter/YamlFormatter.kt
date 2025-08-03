package com.enigmatech.csvreader.formatter

import com.enigmatech.csvreader.impl.Formatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YamlFormatter @Inject constructor() : Formatter {
    override fun format(data: Map<String, Any>): String {
        return buildYaml(data)
    }

    private fun buildYaml(data: Map<String, Any>, indent: String = ""): String {
        return data.entries.joinToString("\n") { (key, value) ->
            when (value) {
                is Map<*, *> -> "$indent$key:\n" + buildYaml(value as Map<String, Any>, "$indent  ")
                is List<*> -> "$indent$key:\n" + value.joinToString("\n") { item ->
                    if (item is Map<*, *>) "$indent  -\n" + buildYaml(item as Map<String, Any>, "$indent    ")
                    else "$indent  - $item"
                }
                else -> "$indent$key: $value"
            }
        }
    }
}