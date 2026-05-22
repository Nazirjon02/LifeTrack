package tj.mahram.lifetrack.ui.theme

import androidx.compose.ui.graphics.Color

// Dark theme core palette
val BackgroundDark = Color(0xFF0D1117)
val SurfaceDark = Color(0xFF161B22)
val SurfaceVariantDark = Color(0xFF21262D)
val OutlineDark = Color(0xFF30363D)

// Primary — Indigo/Violet
val PrimaryDark = Color(0xFF7C3AED)
val PrimaryVariantDark = Color(0xFF6D28D9)
val OnPrimaryDark = Color(0xFFFFFFFF)
val PrimaryContainerDark = Color(0xFF3B1F6E)
val OnPrimaryContainerDark = Color(0xFFDDD6FE)

// Secondary
val SecondaryDark = Color(0xFF4F46E5)
val OnSecondaryDark = Color(0xFFFFFFFF)
val SecondaryContainerDark = Color(0xFF1E1A4A)
val OnSecondaryContainerDark = Color(0xFFC7D2FE)

// Tertiary — Teal accent
val TertiaryDark = Color(0xFF06B6D4)
val TertiaryContainerDark = Color(0xFF0E4F5C)

// Text
val OnBackgroundDark = Color(0xFFE6EDF3)
val OnSurfaceDark = Color(0xFFCDD5E0)
val OnSurfaceVariantDark = Color(0xFF8B949E)

// Semantic colors
val SuccessColor = Color(0xFF3FB950)
val WarningColor = Color(0xFFD29922)
val ErrorColor = Color(0xFFF85149)
val ErrorContainerDark = Color(0xFF3E1519)

// Light theme (minimal, for system theme support)
val PrimaryLight = Color(0xFF5B21B6)
val BackgroundLight = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF0D1117)
val OnSurfaceLight = Color(0xFF1C2128)

// Chart colors
val ChartColors = listOf(
    Color(0xFF7C3AED),
    Color(0xFF3B82F6),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
    Color(0xFFEC4899),
    Color(0xFF06B6D4)
)

// Priority colors
val PriorityLow = Color(0xFF6B7280)
val PriorityMedium = Color(0xFF3B82F6)
val PriorityHigh = Color(0xFFF59E0B)
val PriorityCritical = Color(0xFFEF4444)

// ════════════════════════════════════════════════════════════════
//  AURORA GLASS palette  (adaptive light + dark, violet/indigo)
// ════════════════════════════════════════════════════════════════

// ── Brand spectrum (shared across themes) ───────────────────────
val BrandViolet      = Color(0xFF8B5CF6)
val BrandVioletDeep  = Color(0xFF7C3AED)
val BrandIndigo      = Color(0xFF6366F1)
val BrandIndigoDeep  = Color(0xFF4F46E5)
val BrandFuchsia     = Color(0xFFD946EF)
val BrandSky         = Color(0xFF38BDF8)
val BrandCyan        = Color(0xFF22D3EE)

// ── Dark scene ──────────────────────────────────────────────────
val SceneDarkBg        = Color(0xFF08070F) // deep indigo-black canvas
val SceneDarkSurface   = Color(0xFF14121F) // opaque fallback surface
val SceneDarkSurfaceHi = Color(0xFF1C1930)
val OnDarkHi  = Color(0xFFF4F2FB)
val OnDarkMid = Color(0xFFC7C3D6)
val OnDarkLow = Color(0xFF8C8799)

// ── Light scene ─────────────────────────────────────────────────
val SceneLightBg        = Color(0xFFF1EFFB) // soft lavender white
val SceneLightSurface   = Color(0xFFFFFFFF)
val SceneLightSurfaceHi = Color(0xFFFFFFFF)
val OnLightHi  = Color(0xFF15121F)
val OnLightMid = Color(0xFF49445C)
val OnLightLow = Color(0xFF7C7791)

// ── Semantic (refined, shared) ──────────────────────────────────
val SuccessV2 = Color(0xFF34D399)
val WarningV2 = Color(0xFFFBBF24)
val InfoV2    = Color(0xFF38BDF8)
val DangerV2  = Color(0xFFFB7185)

// ── Chart spectrum (vivid, harmonised) ──────────────────────────
val ChartColorsV2 = listOf(
    Color(0xFF8B5CF6),
    Color(0xFF6366F1),
    Color(0xFF22D3EE),
    Color(0xFF34D399),
    Color(0xFFFBBF24),
    Color(0xFFFB7185),
    Color(0xFFD946EF)
)
