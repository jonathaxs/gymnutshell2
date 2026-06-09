package com.jonathaxs.gymnutshell.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Desenha o anel de progresso como Bitmap — o Glance não tem Canvas/desenho arbitrário,
 * então o anel (igual ao TodayProgressRing do app) vira uma imagem mostrada via Image(ImageProvider).
 *
 * Trilha de fundo cinza translúcida + arco colorido começando no topo (-90°), com o emoji do tier no centro.
 */
object WidgetRing {

    fun bitmap(
        context: Context,
        sizeDp: Int,
        progress: Float,
        ringArgb: Long,
        emoji: String,
        strokeDp: Float = 10f,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
        val stroke = strokeDp * density
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val inset = stroke / 2f
        val rect = RectF(inset, inset, sizePx - inset, sizePx - inset)

        // Trilha de fundo (círculo completo, cinza ~20%).
        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            color = 0x33808080
        }
        canvas.drawArc(rect, 0f, 360f, false, track)

        // Arco de progresso (começa no topo, cor da escala do ProgressColors).
        val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            color = ringArgb.toInt()
        }
        canvas.drawArc(rect, -90f, progress.coerceIn(0f, 1f) * 360f, false, arc)

        // Emoji do tier centralizado.
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.34f
        }
        val cx = sizePx / 2f
        val cy = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(emoji, cx, cy, textPaint)

        return bmp
    }
}
