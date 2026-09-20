package it.quotemonitor.app

object OddsParser {
    private val ignored = setOf(
        "si", "sì", "over", "casa", "ospite", "altre scommesse", "giocatori",
        "sport", "live", "scheda", "account", "eventi", "supercombo"
    )

    fun parse(bookmaker: String, market: String, body: String, url: String): List<Odd> {
        val lines = body.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<Odd>()
        for (i in lines.indices) {
            val price = decimal(lines[i]) ?: continue
            if (price <= 1.0 || price > 100.0) continue
            val window = lines.subList(maxOf(0, i - 4), i)
            val player = window.asReversed().firstOrNull(::looksLikePlayer) ?: continue
            val line = if (market == "shot_on_target_plus") {
                window.asReversed().mapNotNull(::decimal).firstOrNull { it == 0.5 || it == 1.5 || it == 2.5 } ?: 0.5
            } else 0.5
            val odd = Odd(bookmaker, market, player, line, price, url)
            if (result.none { it.key() == odd.key() }) result += odd
        }
        return result
    }

    private fun decimal(text: String): Double? {
        val clean = text.replace(',', '.')
        val match = Regex("(?<!\\d)(\\d{1,2}\\.\\d{1,2})(?!\\d)").find(clean) ?: return null
        return match.groupValues[1].toDoubleOrNull()
    }

    private fun looksLikePlayer(text: String): Boolean {
        val lower = text.lowercase()
        if (lower in ignored || decimal(text) != null) return false
        if (lower.contains("plus") || lower.contains("scommess") || lower.contains("juventus - atalanta")) return false
        return Regex("[A-Za-zÀ-ÿ]{2,}(?:[ '\\-]+[A-Za-zÀ-ÿ]{2,})+").containsMatchIn(text)
    }
}
