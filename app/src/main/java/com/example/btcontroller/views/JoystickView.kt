/*
 * Copyright 2026 BT Control contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.btcontroller.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

/**
 * Simpele analoge joystick. Geeft via [onMoveListener] genormaliseerde
 * waarden terug in het bereik -127..127 voor zowel X als Y.
 */
class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onMoveListener: ((x: Int, y: Int) -> Unit)? = null

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 255, 255, 255)
        style = Paint.Style.FILL
    }
    private val stickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 255, 255, 255)
        style = Paint.Style.FILL
    }

    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var stickRadius = 0f

    private var stickX = 0f
    private var stickY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        baseRadius = min(w, h) / 2f
        stickRadius = baseRadius / 2.2f
        stickX = centerX
        stickY = centerY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(stickX, stickY, stickRadius, stickPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                var dx = event.x - centerX
                var dy = event.y - centerY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                val maxDist = baseRadius - stickRadius
                if (dist > maxDist && dist > 0f) {
                    val scale = maxDist / dist
                    dx *= scale
                    dy *= scale
                }
                stickX = centerX + dx
                stickY = centerY + dy
                invalidate()

                val normX = (dx / maxDist).coerceIn(-1f, 1f)
                val normY = (dy / maxDist).coerceIn(-1f, 1f)
                onMoveListener?.invoke((normX * 127).toInt(), (normY * 127).toInt())
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stickX = centerX
                stickY = centerY
                invalidate()
                onMoveListener?.invoke(0, 0)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
