package it.quotemonitor.app

import org.json.JSONObject
import java.text.Normalizer

data class Odd(
    val bookmaker: String,
    val market: String,
    val player: String,
    val line: Double,
    val price: Double,
    val url: String
) {
    fun key(): String = "$market|${canonicalPlayer(player)}|$line"
    fun toJson() = JSONObject()
        .put("bookmaker", bookmaker).put("market", market).put("player", player)
        .put("line", line).put("price", price).put("url", url)

    companion object {
        fun fromJson(o: JSONObject) = Odd(
            o.getString("bookmaker"), o.getString("market"), o.getString("player"),
            o.getDouble("line"), o.getDouble("price"), o.optString("url")
        )

        fun canonicalPlayer(raw: String): String {
            val withoutTeam = raw.replace(Regex("\\([^)]*\\)"), " ")
            val ascii = Normalizer.normalize(withoutTeam, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "").lowercase()
            return Regex("[a-z0-9]+").findAll(ascii).map { it.value }.distinct().sorted().joinToString(" ")
        }
    }
}
