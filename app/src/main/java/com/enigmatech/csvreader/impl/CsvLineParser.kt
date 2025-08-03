package com.enigmatech.csvreader.impl

class CsvLineParser(private val delimiterPattern: Regex = Regex("\\|")) {
    private val lineMap = mutableMapOf<Int, Array<String>>()

    fun parseLines(lines: List<String>): Map<Int, Array<String>> {
        var index = 0
        lines.forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.trim().split(delimiterPattern).toTypedArray()
            lineMap[index++] = parts
        }
        return lineMap
    }
}