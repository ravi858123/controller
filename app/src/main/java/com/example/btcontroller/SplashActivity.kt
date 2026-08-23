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
package com.example.btcontroller

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

/**
 * Eenvoudig, licht splash screen: logo + app-naam, met een korte fade/scale
 * animatie, dat vervolgens doorschakelt naar [MainActivity]. Bewust zonder
 * extra dependency (zoals androidx core-splashscreen) om de dependency-graph
 * minimaal te houden.
 */
class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BtController_Splash)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.widget.ImageView>(R.id.splashLogo)
        val title = findViewById<android.widget.TextView>(R.id.splashTitle)
        val tagline = findViewById<android.widget.TextView>(R.id.splashTagline)

        logo.scaleX = 0.8f
        logo.scaleY = 0.8f
        logo.alpha = 0f
        title.alpha = 0f
        tagline.alpha = 0f

        val logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.8f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.8f, 1f)
        val logoAlpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
        val titleAlpha = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f)
        val taglineAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha, titleAlpha, taglineAlpha)
            duration = 450
            interpolator = DecelerateInterpolator()
            start()
        }

        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DURATION_MS)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SPLASH_DURATION_MS = 1200L
    }
}
