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
        borderArgb: Long? = null,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
        val stroke = strokeDp * density
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Borda de contraste (fundos custom/accent): stroke 1dp mais largo por lado atrás do anel,
        // igual ao `lineWidth + 2` do iOS. Recolhe o rect pra caber os 1dp extras sem clipar.
        val borderExtra = if (borderArgb != null) density else 0f
        val inset = stroke / 2f + borderExtra
        val rect = RectF(inset, inset, sizePx - inset, sizePx - inset)

        if (borderArgb != null) {
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = stroke + 2f * borderExtra
                strokeCap = Paint.Cap.ROUND
                color = borderArgb.toInt()
            }
            canvas.drawArc(rect, 0f, 360f, false, border)
        }

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
