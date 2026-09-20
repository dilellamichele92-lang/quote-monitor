package it.quotemonitor.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var marketSpinner: Spinner
    private lateinit var store: OddsStore
    private var bookmaker = "GoldBet"

    private val sources = linkedMapOf(
        "GoldBet" to "https://www.goldbet.it/scommesse/sport/calcio/italia/serie-a/juventus-atalanta?tid=93&eid=16161766",
        "DomusBet" to "https://www.domusbet.it/scommesse-sportive/calcio/serie-a/juventus-vs-atalanta_1_31_33_36381_23548",
        "Betwin360" to "https://betwin360.it/scommesse/prematch/calcio/1/palinsesto/serie-a/evento/juventus-vs-atalanta/71945272"
    )
    private val markets = linkedMapOf(
        "Marcatore Plus" to "scorer_plus",
        "Quasi ammonito Plus" to "almost_carded_plus",
        "Ammonito Plus" to "carded_plus",
        "Tiro in porta Plus" to "shot_on_target_plus"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        store = OddsStore(this)
        createNotificationChannel()
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        setContentView(buildUi())
        load("GoldBet")
    }

    @Suppress("SetJavaScriptEnabled")
    private fun buildUi(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 29, 73))
        }
        val title = TextView(this).apply {
            text = "Quote Monitor"
            textSize = 21f; setTextColor(Color.WHITE); setPadding(18, 14, 10, 10)
        }
        val books = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        sources.keys.forEach { name ->
            books.addView(Button(this).apply {
                text = name
                setOnClickListener { load(name) }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        marketSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, markets.keys.toList())
        }
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(Button(this).apply {
            text = "ACQUISISCI QUOTE"
            setOnClickListener { capture() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(Button(this).apply {
            text = "MIGLIORI"
            setOnClickListener { showBest() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        status = TextView(this).apply {
            setTextColor(Color.WHITE); setPadding(16, 8, 16, 8)
            text = "Apri un sito, scegli il mercato nella pagina e tocca Acquisisci quote."
        }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = settings.userAgentString.replace("; wv", "")
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
        root.addView(title)
        root.addView(books)
        root.addView(marketSpinner)
        root.addView(actions)
        root.addView(status)
        root.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    private fun load(name: String) {
        bookmaker = name
        status.text = "$name: carico la pagina…"
        web.loadUrl(sources.getValue(name))
    }

    private fun capture() {
        val market = markets.getValue(marketSpinner.selectedItem.toString())
        web.evaluateJavascript("document.body ? document.body.innerText : ''") { encoded ->
            val body = try { org.json.JSONArray("[$encoded]").getString(0) } catch (_: Exception) { "" }
            val odds = OddsParser.parse(bookmaker, market, body, web.url ?: sources.getValue(bookmaker))
            store.replace(bookmaker, market, odds)
            status.text = if (odds.isEmpty())
                "Nessuna quota letta. Apri prima l'elenco del mercato ${marketSpinner.selectedItem}."
            else "Salvate ${odds.size} quote da $bookmaker. Ora acquisisci lo stesso mercato dagli altri siti."
            if (odds.isNotEmpty()) showBest(notifyOnly = true)
        }
    }

    private fun showBest(notifyOnly: Boolean = false) {
        val best = store.best()
        if (best.isEmpty()) {
            if (!notifyOnly) status.text = "Servono lo stesso mercato e giocatore da almeno due siti; scarto minimo 3%."
            return
        }
        val text = best.take(5).joinToString("\n") { (odd, edge) ->
            "${odd.player}: ${"%.2f".format(odd.price)} ${odd.bookmaker} (+${"%.1f".format(edge)}%)"
        }
        status.text = text
        notifyBest(best.first().first, best.first().second)
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("best-odds", "Quote migliori", NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun notifyBest(odd: Odd, edge: Double) {
        if (android.os.Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(this, "best-odds")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Quota migliore: ${odd.bookmaker} ${"%.2f".format(odd.price)}")
            .setContentText("${odd.player} • +${"%.1f".format(edge)}% sulla mediana")
            .setAutoCancel(true).build()
        getSystemService(NotificationManager::class.java).notify(odd.key().hashCode(), notification)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
