# Quote Monitor Android

App Android gratuita che usa le pagine pubbliche di GoldBet, DomusBet e
Betwin360 aperte direttamente sul telefono. Non salva credenziali e non tenta
di superare CAPTCHA, geoblocchi o altre protezioni.

## Mercati

- Marcatore Plus
- Quasi ammonito Plus
- Ammonito Plus
- Tiro in porta Plus

## Uso

1. Tocca il bookmaker.
2. Nella pagina apri l'evento e il mercato desiderato, in modo che l'elenco dei
   giocatori sia visibile.
3. Seleziona lo stesso mercato nel menu dell'app e tocca **Acquisisci quote**.
4. Ripeti per almeno due bookmaker e tocca **Migliori**.

L'app confronta lo stesso giocatore anche se nome e cognome sono invertiti e
mostra la quota che supera di almeno il 3% la mediana delle fonti disponibili.

## Compilazione

Apri la cartella con Android Studio (JDK 17, Android SDK 35) e scegli
**Build → Build APK(s)**. L'APK viene creato in
`app/build/outputs/apk/debug/app-debug.apk`.

## Limite tecnico

L'acquisizione funziona quando la pagina e il mercato sono aperti nell'app.
Android e i siti impediscono a una soluzione gratuita e priva di feed ufficiali
di garantire controlli automatici 24/7 a schermo spento. Le differenze di quota
non garantiscono valore o vincite.
