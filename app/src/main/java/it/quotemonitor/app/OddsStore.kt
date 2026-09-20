package it.quotemonitor.app

import android.content.Context
import org.json.JSONArray

class OddsStore(context: Context) {
    private val prefs = context.getSharedPreferences("quote-monitor", Context.MODE_PRIVATE)

    fun replace(bookmaker: String, market: String, odds: List<Odd>) {
        val kept = all().filterNot { it.bookmaker == bookmaker && it.market == market }
        save(kept + odds)
    }

    fun all(): List<Odd> = try {
        val array = JSONArray(prefs.getString("odds", "[]"))
        (0 until array.length()).map { Odd.fromJson(array.getJSONObject(it)) }
    } catch (_: Exception) { emptyList() }

    fun best(minEdge: Double = 3.0): List<Pair<Odd, Double>> = all().groupBy { it.key() }
        .values.filter { group -> group.map { it.bookmaker }.distinct().size >= 2 }
        .mapNotNull { group ->
            val best = group.maxBy { it.price }
            val sorted = group.map { it.price }.sorted()
            val median = if (sorted.size % 2 == 1) sorted[sorted.size / 2]
                else (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
            val edge = (best.price / median - 1.0) * 100
            if (edge >= minEdge) best to edge else null
        }.sortedByDescending { it.second }

    private fun save(odds: List<Odd>) {
        val array = JSONArray()
        odds.forEach { array.put(it.toJson()) }
        prefs.edit().putString("odds", array.toString()).apply()
    }
}
