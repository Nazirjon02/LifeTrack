package tj.mahram.lifetrack.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Parse a `#RRGGBB` hex string into a Compose [Color]. Falls back to the brand
 * violet on any malformed input. Shared across features (problems, goals,
 * finance categories) — the single place hex colors stored in the DB are decoded.
 */
fun parseHexColor(hex: String): Color = runCatching {
    val clean = hex.trimStart('#')
    val long = clean.toLong(16)
    Color(0xFF000000L or long)
}.getOrDefault(Color(0xFF7C3AED))
