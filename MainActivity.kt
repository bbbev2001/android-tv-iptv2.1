package com.turkdizitv.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

private data class Site(
    val name: String,
    val url: String,
    val category: String = "Diğer",
    val favorite: Boolean = false
)

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private val prefs by lazy { getSharedPreferences("sites", MODE_PRIVATE) }
    private val sites = mutableListOf<Site>()
    private var web: WebView? = null
    private var editingIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadSites()
        showHome()
    }

    private fun loadSites() {
        val raw = prefs.getString("items", null)
        if (raw == null) {
            sites.addAll(listOf(
                Site("tabii", "https://www.tabii.com/tr/", "Dizi & Film"),
                Site("Netflix Türkiye", "https://www.netflix.com/tr/", "Dizi & Film"),
                Site("Disney+ Türkiye", "https://www.disneyplus.com/tr-tr/", "Dizi & Film")
            ))
            saveSites()
            return
        }
        try {
            val a = JSONArray(raw)
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                sites.add(Site(
                    o.getString("name"),
                    o.getString("url"),
                    o.optString("category", "Diğer"),
                    o.optBoolean("favorite")
                ))
            }
        } catch (_: Exception) { }
    }

    private fun saveSites() {
        val a = JSONArray()
        sites.forEach { s ->
            a.put(JSONObject().apply {
                put("name", s.name)
                put("url", s.url)
                put("category", s.category)
                put("favorite", s.favorite)
            })
        }
        prefs.edit().putString("items", a.toString()).apply()
    }

    private fun base(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(55, 35, 55, 30)
        setBackgroundColor(Color.rgb(10, 10, 10))
    }

    private fun text(value: String, size: Float, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = value
        setTextColor(Color.WHITE)
        textSize = size
        if (bold) setTypeface(null, 1)
        setPadding(10, 8, 10, 8)
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 17f
        isFocusable = true
        setOnClickListener { action() }
    }

    private fun showHome() {
        web?.let { (it.parent as? ViewGroup)?.removeView(it) }
        web = null
        root = base()

        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(text("TÜRK DİZİ TV", 30f, true), LinearLayout.LayoutParams(0, 75, 1f))
        header.addView(actionButton("＋ SİTE EKLE") { showSiteEditor(null) }, LinearLayout.LayoutParams(190, 70))
        root.addView(header)
        root.addView(text("Dizi ve film servisleri", 22f, true))

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val categories = sites.map { it.category }.distinct()
        categories.forEach { category ->
            content.addView(text(category, 21f, true))
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            sites.filter { it.category == category }.forEach { site ->
                val index = sites.indexOf(site)
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(14, 12, 14, 12)
                    setBackgroundColor(Color.rgb(30, 30, 30))
                    isFocusable = true
                    isClickable = true
                }
                val title = if (site.favorite) "★ ${site.name}" else site.name
                card.addView(text(title, 20f, true), LinearLayout.LayoutParams(-1, 65))
                card.addView(actionButton("AÇ") { openSite(site.url) }, LinearLayout.LayoutParams(-1, 58))
                card.addView(actionButton("DÜZENLE") { showSiteEditor(index) }, LinearLayout.LayoutParams(-1, 58))
                val lp = LinearLayout.LayoutParams(0, 225, 1f)
                lp.setMargins(8, 8, 8, 15)
                row.addView(card, lp)
            }
            content.addView(row)
        }
        if (sites.isEmpty()) content.addView(text("Henüz site yok. '＋ SİTE EKLE' ile başlayabilirsin.", 20f))
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(text("Kumanda: yön tuşları • OK: seç • Site kartındaki DÜZENLE ile değiştir veya sil", 15f))
        setContentView(root)
    }

    /** Uygulama içindeki tam ekran site ekleme/düzenleme ekranı. */
    private fun showSiteEditor(index: Int?) {
        editingIndex = index
        val old = index?.let { sites[it] }
        val screen = base()

        val title = if (old == null) "YENİ SİTE EKLE" else "SİTEYİ DÜZENLE"
        screen.addView(text(title, 29f, true))
        screen.addView(text("Site bilgilerini gir ve KAYDET seçeneğine bas.", 17f))

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 15, 10, 10)
        }
        val name = EditText(this).apply {
            hint = "Site adı (ör. TRT İzle)"
            setText(old?.name ?: "")
            setSingleLine(true)
            textSize = 20f
            isFocusable = true
        }
        val url = EditText(this).apply {
            hint = "Site adresi (https://...)"
            setText(old?.url ?: "")
            setSingleLine(true)
            textSize = 20f
            isFocusable = true
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        val category = EditText(this).apply {
            hint = "Kategori (Dizi & Film, Spor, Çocuk...)"
            setText(old?.category ?: "Dizi & Film")
            setSingleLine(true)
            textSize = 20f
            isFocusable = true
        }
        form.addView(name, LinearLayout.LayoutParams(-1, 70))
        form.addView(url, LinearLayout.LayoutParams(-1, 70))
        form.addView(category, LinearLayout.LayoutParams(-1, 70))
        screen.addView(form)

        val buttons = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        buttons.addView(actionButton("KAYDET") {
            var address = url.text.toString().trim()
            val siteName = name.text.toString().trim()
            val siteCategory = category.text.toString().trim().ifEmpty { "Diğer" }
            when {
                siteName.isEmpty() -> toast("Site adı boş bırakılamaz")
                address.isEmpty() -> toast("Site adresi boş bırakılamaz")
                else -> {
                    if (!address.startsWith("http://") && !address.startsWith("https://")) address = "https://$address"
                    val duplicate = sites.anyIndexed { i, s -> i != index && s.url.equals(address, true) }
                    if (duplicate) toast("Bu site zaten eklenmiş")
                    else {
                        val updated = Site(siteName, address, siteCategory, old?.favorite ?: false)
                        if (index == null) sites.add(updated) else sites[index] = updated
                        saveSites()
                        showHome()
                    }
                }
            }
        }, LinearLayout.LayoutParams(180, 70))
        buttons.addView(actionButton("VAZGEÇ") { showHome() }, LinearLayout.LayoutParams(170, 70))
        if (old != null) {
            buttons.addView(actionButton("SİL") {
                AlertDialog.Builder(this)
                    .setTitle("Site silinsin mi?")
                    .setMessage("${old.name} uygulamadan kaldırılacak.")
                    .setPositiveButton("SİL") { _, _ -> sites.removeAt(index!!); saveSites(); showHome() }
                    .setNegativeButton("İPTAL", null)
                    .show()
            }, LinearLayout.LayoutParams(150, 70))
            buttons.addView(actionButton(if (old.favorite) "★ FAVORİ" else "☆ FAVORİ") {
                sites[index!!] = old.copy(favorite = !old.favorite)
                saveSites()
                showSiteEditor(index)
            }, LinearLayout.LayoutParams(180, 70))
        }
        screen.addView(buttons)
        screen.addView(text("Not: Yalnızca yasal olarak erişmeye yetkili olduğun siteleri ekle.", 14f))
        setContentView(screen)
        name.requestFocus()
    }

    private fun openSite(url: String) {
        val w = WebView(this)
        web = w
        w.settings.javaScriptEnabled = true
        w.settings.domStorageEnabled = true
        w.settings.mediaPlaybackRequiresUserGesture = false
        w.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }
        }
        val layout = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        layout.addView(w, FrameLayout.LayoutParams(-1, -1))
        val back = actionButton("‹ Geri") { showHome() }
        layout.addView(back, FrameLayout.LayoutParams(150, 65).apply { leftMargin = 25; topMargin = 20 })
        setContentView(layout)
        w.loadUrl(url)
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private inline fun <T> List<T>.anyIndexed(predicate: (index: Int, value: T) -> Boolean): Boolean {
        forEachIndexed { i, value -> if (predicate(i, value)) return true }
        return false
    }

    override fun onBackPressed() {
        if (web != null) showHome() else showHome()
    }
}
