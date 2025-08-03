package com.enigmatech.csvreader.formatter

import com.enigmatech.csvreader.impl.Formatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XmlFormatter @Inject constructor() : Formatter {
    override fun format(data: Map<String, Any>): String {
        return buildXml(data)
    }

    private fun buildXml(data: Map<String, Any>, indent: String = ""): String {
        return data.entries.joinToString("\n") { (key, value) ->
            when (value) {
                is Map<*, *> -> "${indent}<$key>\n" + buildXml(value as Map<String, Any>, "$indent  ") + "\n$indent</$key>"
                is List<*> -> value.joinToString("\n") { item ->
                    if (item is Map<*, *>) "${indent}<$key>\n" + buildXml(item as Map<String, Any>, "$indent  ") + "\n$indent</$key>"
                    else "${indent}<$key>$item</$key>"
                }
                else -> "${indent}<$key>$value</$key>"
            }
        }
    }
}