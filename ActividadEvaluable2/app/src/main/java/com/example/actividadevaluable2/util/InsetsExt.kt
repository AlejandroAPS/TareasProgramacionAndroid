package com.example.actividadevaluable2.util

import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Activa el modo edge-to-edge con iconos oscuros en las barras del sistema.
 *
 * Como la aplicación usa siempre una estética clara, se fuerza el estilo "light"
 * para que los iconos de la barra de estado y de navegación sean oscuros y se
 * vean correctamente sobre el fondo claro. En versiones antiguas sin soporte de
 * iconos oscuros se aplica un scrim oscuro de respaldo.
 */
fun ComponentActivity.configurarEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(
            Color.TRANSPARENT,
            Color.argb(120, 0, 0, 0)
        )
    )
}

/**
 * Aplica como padding los márgenes de las barras del sistema (estado y navegación).
 *
 * Se usa en las tres pantallas porque la aplicación dibuja de borde a borde.
 * Así ningún contenido queda oculto detrás de la barra de estado ni de los
 * gestos de navegación.
 */
fun View.aplicarInsetsSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
