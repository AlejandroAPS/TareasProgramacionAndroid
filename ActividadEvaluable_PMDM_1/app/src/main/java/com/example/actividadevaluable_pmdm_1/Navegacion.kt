package com.example.actividadevaluable_pmdm_1

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.configurarBarras(titulo: String) {
    findViewById<TextView>(R.id.tvTitulo).text = titulo
    findViewById<View>(R.id.ivLogo).setOnClickListener {
        startActivity(
            Intent(this, TapasActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    val destinos = mapOf(
        R.id.btnTapas to TapasActivity::class.java,
        R.id.btnPlatosCombinados to PlatosCombinadosActivity::class.java,
        R.id.btnHamburgesas to HamburgesasActivity::class.java,
        R.id.btnBebidas to BebidasActivity::class.java,
        R.id.btnPostres to PostresActivity::class.java
    )

    destinos.forEach { (idBoton, destino) ->
        findViewById<View>(idBoton).setOnClickListener {
            if (this::class.java != destino) {
                startActivity(Intent(this, destino))
                // Si venimos de una categoría, la cerramos: así "atrás" vuelve al hub
                // y no se acumulan 10 pantallas al saltar entre categorías.
                if (this !is TapasActivity) finish()
            }
        }
    }
}