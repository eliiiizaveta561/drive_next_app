package com.drivenext.app.domain.search

object CarSearch {

    private const val MAX_QUERY_LENGTH = 50

    // Символы, которые ломают фильтр PostgREST (or=(...), ilike %...%), заменяем пробелами
    private val forbiddenChars = Regex("""[%_*,()\\"'.:]""")
    private val spaces = Regex("""\s+""")

    fun normalize(query: String): String = query
        .replace(forbiddenChars, " ")
        .replace(spaces, " ")
        .trim()
        .take(MAX_QUERY_LENGTH)

    fun isValid(query: String): Boolean = normalize(query).isNotEmpty()
}
