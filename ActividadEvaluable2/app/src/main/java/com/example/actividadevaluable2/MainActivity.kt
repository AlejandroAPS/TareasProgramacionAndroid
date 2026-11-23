package com.example.actividadevaluable2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.actividadevaluable2.util.aplicarInsetsSistema
import com.example.actividadevaluable2.util.configurarEdgeToEdge
import com.google.android.material.card.MaterialCardView

/**
 * Pantalla principal de la aplicación.
 *
 * Muestra un menú con dos tarjetas: una para la sección de audio y otra para la
 * sección de imagen. Cada tarjeta abre su Activity mediante un Intent explícito.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dibuja el contenido detrás de las barras del sistema (edge-to-edge).
        configurarEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Añade el padding necesario para que nada quede bajo las barras del sistema.
        findViewById<View>(R.id.main).aplicarInsetsSistema()

        configurarNavegacion()
        animarTarjetas()
    }

    /**
     * Asocia cada tarjeta con su pantalla correspondiente usando Intents explícitos.
     * Al ser componentes de la misma app, no hace falta declarar filtros en el manifest.
     */
    private fun configurarNavegacion() {
        val cardAudio = findViewById<MaterialCardView>(R.id.cardAudio)
        val cardImagen = findViewById<MaterialCardView>(R.id.cardImagen)

        cardAudio.setOnClickListener {
            abrirSeccion(AudioActivity::class.java)
        }

        cardImagen.setOnClickListener {
            abrirSeccion(ImagenActivity::class.java)
        }
    }

    /** Abre la Activity indicada con una transición sencilla de navegación. */
    private fun abrirSeccion(destino: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destino))
    }

    /**
     * Pequeña animación de entrada de las tarjetas.
     * Usa ViewPropertyAnimator sobre alpha y translationY: las tarjetas aparecen
     * deslizándose ligeramente hacia arriba, con un retardo escalonado.
     */
    private fun animarTarjetas() {
        listOf(R.id.cardAudio, R.id.cardImagen).forEachIndexed { indice, id ->
            val tarjeta = findViewById<View>(id)
            tarjeta.alpha = 0f
            tarjeta.translationY = 28f
            tarjeta.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(120L * indice)
                .setDuration(420L)
                .start()
        }
    }
}
