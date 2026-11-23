package com.example.actividadevaluable2.util

import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


fun ComponentActivity.configurarEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(
            Color.TRANSPARENT,
            Color.argb(120, 0, 0, 0)
        )
    )
}

fun View.aplicarInsetsSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
