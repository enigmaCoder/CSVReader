package com.enigmatech.csvreader.impl

interface Formatter {
    fun format(data: Map<String, Any>): String
}

interface Transformer {
    fun applyTransform(value: String, rule: String?): Any
}

interface Normalizer {
    fun normalize(parsedData: Map<Int, Array<String>>, config: NormalizationConfig): Map<String, Any>
}

interface ISchemaParser {
    fun parse(json: String): NormalizationConfig
}
