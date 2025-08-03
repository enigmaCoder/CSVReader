package com.enigmatech.csvreader.impl

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RenderEngineRegistry @Inject constructor() {

    private val formatterMap: MutableMap<String, Formatter> = mutableMapOf()

    fun register(format: String, formatter: Formatter) {
        formatterMap[format.lowercase()] = formatter
    }

    fun render(format: String, data: Map<String, Any>): String {
        return formatterMap[format.lowercase()]
            ?.format(data)
            ?: throw IllegalArgumentException("Formatter for '$format' not found.")
    }
}