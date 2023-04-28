package me.yokan.profanitypilot.classifier.regex

import java.util.regex.Pattern

class RegexFilterProvider {

    val matches = object {}::class.java.getResourceAsStream("/regex_filters.txt")
        ?.bufferedReader()
        ?.readText()
        ?.split("\n")
        ?.map { Pattern.compile(it) }
        ?: emptyList()

    fun blocked(message: String): Boolean = matches.any { it.matcher(message).find() }
}