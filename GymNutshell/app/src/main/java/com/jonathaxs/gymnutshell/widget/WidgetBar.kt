package com.jonathaxs.gymnutshell.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

/**
 * Desenha a barra de progresso de uma meta como Bitmap — o Glance não tem largura fracionária
 * (defaultWeight só divide igual), então a proporção preenchida é assada no bitmap e a imagem é
 * esticada (FillBounds) pra ocupar a largura disponível. O cantinho arredondado vem do cornerRadius
 * aplicado no Image. Trilho cinza + porção preenchida na cor da escala do ProgressColors.
 */
object WidgetBar {

    fun bitmap(percent: Int, fillArgb: Long, trackArgb: Int = 0x33808080): Bitmap {
        val w = 300
        val h = 24
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = trackArgb }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), track)

        val fillW = (percent.coerceIn(0, 100) / 100f) * w
        if (fillW > 0f) {
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillArgb.toInt() }
            canvas.drawRect(0f, 0f, fillW, h.toFloat(), fill)
        }
        return bmp
    }
}
