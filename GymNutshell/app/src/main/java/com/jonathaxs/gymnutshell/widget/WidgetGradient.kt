package com.jonathaxs.gymnutshell.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.jonathaxs.gymnutshell.core.widget.WidgetBackground

/**
 * Desenha o fundo em gradiente do widget como Bitmap — o Glance não tem gradiente nativo, então
 * geramos a imagem e usamos como background (esticada pra preencher). Diagonal claro→escuro
 * (topo-esquerda → base-direita), igual ao gradiente do iOS.
 */
object WidgetGradient {

    fun bitmap(baseArgb: Long, sizePx: Int = 512): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val shader = LinearGradient(
            0f, 0f, sizePx.toFloat(), sizePx.toFloat(),
            WidgetBackground.lighterArgb(baseArgb).toInt(),
            WidgetBackground.darkerArgb(baseArgb).toInt(),
            Shader.TileMode.CLAMP,
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), paint)
        return bmp
    }
}
