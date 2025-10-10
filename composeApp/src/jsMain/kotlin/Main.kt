import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "galeria") {
        Gallery()
    }
}

@Composable
fun Gallery() {
    var selected by remember { mutableStateOf<Int?>(null) }
    Div(attrs = { classes("grid", "grid-cols-2", "gap-4", "md:grid-cols-3", "lg:grid-cols-4") }) {
        (1..8).forEach { i ->
            Img(
                src = "imgs/graffiti_$i.jpg",
                alt = "Trabalho $i",
                attrs = {
                    classes("rounded-lg")
                    onClick { selected = i }
                }
            )
        }
    }
    if (selected != null) {
        Div(attrs = {
            classes("fixed","inset-0","bg-black/70","flex","items-center","justify-center")
            onClick { selected = null }
        }) {
            Img(src = "imgs/graffiti_${selected}.jpg", alt = "Zoom", attrs = {
                classes("max-w-4xl","max-h-[80vh]","rounded-lg","shadow-2xl")
            })
        }
    }
}
