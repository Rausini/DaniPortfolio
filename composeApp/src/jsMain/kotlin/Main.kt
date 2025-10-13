@file:androidx.compose.runtime.NoLiveLiterals
@file:Suppress("UnsafeCastFromDynamic")

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.fetch.RequestInit
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

// ======= COLOQUE SEUS VALORES =======
private const val SUPABASE_URL = "https://teiqgonxzpanpjdzxteb.supabase.co"
private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRlaXFnb254enBhbnBqZHp4dGViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAzMzY3MzgsImV4cCI6MjA3NTkxMjczOH0.h2fW3ZCPuEy0O4q1e-B0L8pvKpDnUNSR9uNeSzuWji0"
// ====================================

@Serializable
data class ImageRow(
    val id: Int? = null,
    val url: String,
    val title: String? = null
)

fun main() {
    renderComposable(rootElementId = "galeria-root") {
        Gallery()
    }
}

@Composable
fun Gallery() {
    var images by remember { mutableStateOf<List<ImageRow>>(emptyList()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Carrega da REST API do Supabase
    LaunchedEffect(Unit) {
        try {
            val endpoint =
                "$SUPABASE_URL/rest/v1/images" +
                        "?select=id,url,title,order,created_at" +
                        "&published=eq.true" +
                        "&order=order.desc,created_at.desc"

            // headers dinâmicos (forma simples em Kotlin/JS)
            val headers = js("{}")
            headers["apikey"] = SUPABASE_ANON_KEY
            headers["Authorization"] = "Bearer $SUPABASE_ANON_KEY"
            headers["Accept"] = "application/json"

            val init = js("{}")
            init["method"] = "GET"
            init["headers"] = headers

            val resp = window.fetch(endpoint, init.unsafeCast<RequestInit>()).await()
            if (!resp.ok) {
                errorMsg = "HTTP ${resp.status}: ${resp.statusText}"
            } else {
                val text = resp.text().await()
                images = Json { ignoreUnknownKeys = true }
                    .decodeFromString(text)
            }
        } catch (e: dynamic) {
            console.error("Erro buscando imagens:", e)
            errorMsg = e?.toString()
        } finally {
            loading = false
        }
    }

    // Atalhos de teclado quando o modal está aberto
    DisposableEffect(selected) {
        if (selected == null) onDispose { }
        else {
            val listener: (Event) -> Unit = { e ->
                val ke = e as KeyboardEvent
                when (ke.key) {
                    "ArrowRight" -> { ke.preventDefault(); selected = ((selected!! + 1) % images.size) }
                    "ArrowLeft"  -> { ke.preventDefault(); selected = ((selected!! - 1 + images.size) % images.size) }
                    "Escape"     -> { ke.preventDefault(); selected = null }
                }
            }
            window.addEventListener("keydown", listener)
            onDispose { window.removeEventListener("keydown", listener) }
        }
    }

    // UI
    when {
        loading -> {
            // esqueletinho simples
            Div({ classes("grid","grid-cols-2","gap-4","md:grid-cols-3","lg:grid-cols-4","px-4") }) {
                repeat(8) {
                    Div({
                        classes("animate-pulse","rounded-lg","bg-gray-200","dark:bg-gray-700","aspect-square")
                    })
                }
            }
        }
        errorMsg != null -> {
            P({ classes("px-4","text-red-500") }) { Text("Erro ao carregar galeria: $errorMsg") }
        }
        else -> {
            Div({ classes("grid","grid-cols-2","gap-4","md:grid-cols-3","lg:grid-cols-4","px-4") }) {
                images.forEachIndexed { i, img ->
                    Img(src = img.url, alt = img.title ?: "Trabalho ${i + 1}", attrs = {
                        classes("rounded-lg","transition-transform","hover:scale-105","cursor-pointer","aspect-square","object-cover")
                        onClick { selected = i }
                    })
                }
            }
        }
    }

    // Modal com Zoom
    if (selected != null && images.isNotEmpty()) {
        val img = images[selected!!]
        Div({
            classes("fixed","inset-0","bg-black/70","flex","items-center","justify-center","z-50")
            onClick { selected = null }
        }) {
            Img(src = img.url, alt = img.title ?: "Zoom", attrs = {
                classes("max-w-4xl","max-h-[80vh]","rounded-lg","shadow-2xl")
                onClick { ev -> ev.stopPropagation() } // não fechar ao clicar na imagem
            })
        }
    }
}
