package com.example.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Shapes & Geometry Design System.
 *
 * Provides a rich, production-grade library of tokenized expressive shapes:
 * - Standard M3 Expressive Scale: ExtraSmall (8.dp) -> ExtraLarge (36.dp)
 * - Specialized Music Shapes: Stadium, PillDock, Squircles, Superellipses
 * - Asymmetric & Sculpted Shapes: DynamicTab, AsymmetricCard, AsymmetricDock
 * - Geometric & Fidelity Badges: CloverShape, ScallopedBadgeShape, HexagonalBadgeShape,
 *   OctagonBadgeShape, TicketShape, WavyBlobShape
 */
object ExpressiveShapes {

    // 1. Core Material 3 Expressive Scale
    val ExtraSmall = RoundedCornerShape(8.dp)
    val Small = RoundedCornerShape(14.dp)
    val Medium = RoundedCornerShape(20.dp)
    val Large = RoundedCornerShape(28.dp)
    val ExtraLarge = RoundedCornerShape(36.dp)

    // 2. Specialized Expressive Music Shapes
    val Stadium = RoundedCornerShape(50)
    val Pill = RoundedCornerShape(100.dp)
    val PillDock = RoundedCornerShape(32.dp)
    val Squircle = RoundedCornerShape(22.dp)
    val HeroSquircle = RoundedCornerShape(32.dp)
    val AlbumArtRounded = RoundedCornerShape(18.dp)
    val MiniArtRounded = RoundedCornerShape(14.dp)
    val SuperellipseCard = RoundedCornerShape(26.dp)

    // 3. Asymmetric Expressive Shapes
    val AsymmetricCard = RoundedCornerShape(topStart = 28.dp, topEnd = 8.dp, bottomEnd = 28.dp, bottomStart = 8.dp)
    val AsymmetricPill = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp, topEnd = 16.dp, bottomEnd = 16.dp)
    val DynamicTab = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    val AsymmetricDock = RoundedCornerShape(topStart = 36.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 36.dp)

    // 4. Cut Corner & Cyber Shapes (for Waveform / Hi-Res Master styling)
    val CutCornerPill = CutCornerShape(14.dp)
    val CutCornerCard = CutCornerShape(topStart = 22.dp, bottomEnd = 22.dp, topEnd = 6.dp, bottomStart = 6.dp)
    val DiamondCutCard = CutCornerShape(18.dp)

    /**
     * Expressive 4-leaf Clover / Scallop Shape for avatars, artist discographies, and favorite tokens.
     */
    class CloverShape(private val cornerRadiusRatio: Float = 0.35f) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val r = (w.coerceAtMost(h) * cornerRadiusRatio)

            path.moveTo(r, 0f)
            path.lineTo(w - r, 0f)
            path.cubicTo(w, 0f, w, r, w, r)
            path.lineTo(w, h - r)
            path.cubicTo(w, h, w - r, h, w - r, h)
            path.lineTo(r, h)
            path.cubicTo(0f, h, 0f, h - r, 0f, h - r)
            path.lineTo(0f, r)
            path.cubicTo(0f, 0f, r, 0f, r, 0f)
            path.close()

            return Outline.Generic(path)
        }
    }

    /**
     * Expressive Scalloped Badge Shape (8-point or 12-point floral badge for audio fidelity).
     */
    class ScallopedBadgeShape(private val scallops: Int = 8, private val depthFactor: Float = 0.86f) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = cx.coerceAtMost(cy)
            val minR = maxR * depthFactor

            val points = scallops * 2
            val angleStep = (PI * 2 / points).toFloat()

            for (i in 0 until points) {
                val r = if (i % 2 == 0) maxR else minR
                val angle = i * angleStep - (PI / 2).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Hexagonal Hi-Res Audio Master Badge Shape.
     */
    class HexagonalBadgeShape(private val cornerRadius: Float = 12f) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (w.coerceAtMost(h) / 2f)

            for (i in 0..5) {
                val angle = (i * 60.0 - 30.0) * (PI / 180.0)
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Expressive Octagonal Badge Shape.
     */
    class OctagonBadgeShape : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val cut = (w.coerceAtMost(h) * 0.22f)

            path.moveTo(cut, 0f)
            path.lineTo(w - cut, 0f)
            path.lineTo(w, cut)
            path.lineTo(w, h - cut)
            path.lineTo(w - cut, h)
            path.lineTo(cut, h)
            path.lineTo(0f, h - cut)
            path.lineTo(0f, cut)
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Expressive Concert Ticket Shape with side notches (for playlists and albums).
     */
    class TicketShape(private val notchRadius: Float = 18f) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val r = 16f

            path.moveTo(r, 0f)
            path.lineTo(w - r, 0f)
            path.quadraticTo(w, 0f, w, r)
            path.lineTo(w, h / 2f - notchRadius)
            path.arcTo(
                rect = Rect(w - notchRadius, h / 2f - notchRadius, w + notchRadius, h / 2f + notchRadius),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            path.lineTo(w, h - r)
            path.quadraticTo(w, h, w - r, h)
            path.lineTo(r, h)
            path.quadraticTo(0f, h, 0f, h - r)
            path.lineTo(0f, h / 2f + notchRadius)
            path.arcTo(
                rect = Rect(-notchRadius, h / 2f - notchRadius, notchRadius, h / 2f + notchRadius),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            path.lineTo(0f, r)
            path.quadraticTo(0f, 0f, r, 0f)
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * 12-point expressive starburst badge shape.
     */
    class StarburstShape(private val points: Int = 12, private val innerRadiusFactor: Float = 0.82f) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = cx.coerceAtMost(cy)
            val minR = maxR * innerRadiusFactor
            val totalPoints = points * 2
            val angleStep = (PI * 2 / totalPoints).toFloat()

            for (i in 0 until totalPoints) {
                val r = if (i % 2 == 0) maxR else minR
                val angle = i * angleStep - (PI / 2).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Gothic Arched Portal Dome Shape.
     */
    class ArchPortalShape(private val cornerRadius: Float = 24f) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val r = w / 2f

            path.moveTo(0f, r)
            path.arcTo(
                rect = Rect(0f, 0f, w, r * 2f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(w, h - cornerRadius)
            path.quadraticTo(w, h, w - cornerRadius, h)
            path.lineTo(cornerRadius, h)
            path.quadraticTo(0f, h, 0f, h - cornerRadius)
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Organic Fluid Teardrop Shape.
     */
    class TeardropFluidShape : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val r = (w.coerceAtMost(h) * 0.45f)

            path.moveTo(r, 0f)
            path.lineTo(w - 12f, 0f)
            path.quadraticTo(w, 0f, w, 12f)
            path.lineTo(w, h - r)
            path.cubicTo(w, h, w - r, h, w - r, h)
            path.lineTo(r, h)
            path.cubicTo(0f, h, 0f, h - r, 0f, h - r)
            path.lineTo(0f, r)
            path.cubicTo(0f, 0f, r, 0f, r, 0f)
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Hi-Res Audio Fidelity Shield Crest Shape.
     */
    class ShieldCrestShape : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val w = size.width
            val h = size.height
            val r = 24f

            path.moveTo(r, 0f)
            path.lineTo(w - r, 0f)
            path.quadraticTo(w, 0f, w, r)
            path.lineTo(w, h * 0.65f)
            path.cubicTo(w, h * 0.88f, w * 0.5f, h, w * 0.5f, h)
            path.cubicTo(w * 0.5f, h, 0f, h * 0.88f, 0f, h * 0.65f)
            path.lineTo(0f, r)
            path.quadraticTo(0f, 0f, r, 0f)
            path.close()
            return Outline.Generic(path)
        }
    }

    /**
     * Default Material 3 Expressive Shapes instance for Theme.
     */
    val M3ExpressiveShapes = Shapes(
        extraSmall = ExtraSmall,
        small = Small,
        medium = Medium,
        large = Large,
        extraLarge = ExtraLarge
    )
}
