package com.example.actividadevaluable_pmdm_1

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import java.util.Locale

fun AppCompatActivity.configurarBarras(titulo: String) {
    findViewById<TextView>(R.id.tvTitulo).text = titulo
    findViewById<View>(R.id.ivLogo).setOnClickListener {
        startActivity(
            Intent(this, TapasActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    findViewById<View>(R.id.ivUsuario).setOnClickListener {
        if (this !is UsuarioActivity) {
            startActivity(Intent(this, UsuarioActivity::class.java))
        }
    }

    val destinos = mapOf(
        R.id.btnTapas to TapasActivity::class.java,
        R.id.btnPlatosCombinados to PlatosCombinadosActivity::class.java,
        R.id.btnHamburgesas to HamburguesasActivity::class.java,
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

fun Context.mostrarFicha(articulo: Articulo, onHacerPedido: (Articulo) -> Unit = {}) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_ficha)

    dialog.findViewById<ImageView>(R.id.ivFicha).setImageResource(articulo.imagen)
    dialog.findViewById<TextView>(R.id.tvFichaNombre).text = articulo.nombre
    dialog.findViewById<TextView>(R.id.tvFichaPrecio).text =
        String.format(Locale("es", "ES"), "· %.2f € ", articulo.precio)
    dialog.findViewById<TextView>(R.id.tvFichaDescripcion).text = articulo.descripcion
    dialog.findViewById<TextView>(R.id.tvFichaAlergenos).text = "Alérgenos: ${articulo.alergenos}"

    dialog.findViewById<ImageButton>(R.id.btnCerrarFicha).setOnClickListener { dialog.dismiss() }
    dialog.findViewById<Button>(R.id.btnHacerPedido).setOnClickListener {
        dialog.dismiss()
        onHacerPedido(articulo)
    }

    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // para que se vean las esquinas redondeadas
        setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    dialog.show()
}