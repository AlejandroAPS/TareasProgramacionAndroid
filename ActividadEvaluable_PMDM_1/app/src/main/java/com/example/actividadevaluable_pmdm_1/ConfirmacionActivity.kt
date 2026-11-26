package com.example.actividadevaluable_pmdm_1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class ConfirmacionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ARTICULO = "articulo"
        const val EXTRA_CANTIDAD = "cantidad"
        const val EXTRA_ENTREGA = "entrega"
        const val EXTRA_PAGO = "pago"
        const val EXTRA_NOTAS = "notas"
        const val EXTRA_ID = "id_pedido"
        const val EXTRA_FECHA = "fecha_pedido"

        private const val ENVIO = 2.50
        private const val ENVIO_GRATIS_DESDE = 20.0
    }

    private fun euros(valor: Double) =
        String.format(Locale("es", "ES"), "%.2f €", valor)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion)

        @Suppress("DEPRECATION")
        val articulo = intent.getSerializableExtra(EXTRA_ARTICULO) as? Articulo
        if (articulo == null) {
            irATapas()
            return
        }
        val cantidad = intent.getIntExtra(EXTRA_CANTIDAD, 1)

        configurarBarra()
        pintarTarjeta(articulo)

        // Cálculo del resultado a partir de los datos del usuario
        val subtotal = articulo.precio * cantidad
        val envio = if (subtotal >= ENVIO_GRATIS_DESDE) 0.0 else ENVIO
        val total = subtotal + envio
        val minutos = minOf(25 + 3 * cantidad, 90)

        findViewById<TextView>(R.id.tvIdPedido).text = intent.getStringExtra(EXTRA_ID)
        findViewById<TextView>(R.id.tvFechaPedido).text =
            getString(R.string.res_fecha, intent.getStringExtra(EXTRA_FECHA))

        findViewById<TextView>(R.id.tvResCantidad).text = getString(R.string.res_cantidad, cantidad)
        findViewById<TextView>(R.id.tvResEntrega).text =
            getString(R.string.res_entrega, intent.getStringExtra(EXTRA_ENTREGA))
        findViewById<TextView>(R.id.tvResPago).text =
            getString(R.string.res_pago, intent.getStringExtra(EXTRA_PAGO))
        findViewById<TextView>(R.id.tvResNotas).text =
            getString(R.string.res_notas, intent.getStringExtra(EXTRA_NOTAS))
        findViewById<TextView>(R.id.tvResSubtotal).text =
            getString(R.string.res_subtotal, euros(subtotal))
        findViewById<TextView>(R.id.tvResEnvio).text = getString(
            R.string.res_envio,
            if (envio == 0.0) "Gratis (pedidos desde 20 €)" else euros(envio)
        )
        findViewById<TextView>(R.id.tvResTotal).text = getString(R.string.res_total, euros(total))
        findViewById<TextView>(R.id.tvResTiempo).text = getString(R.string.res_tiempo, minutos)

        findViewById<Button>(R.id.btnOtroPedido).setOnClickListener { irATapas() }

        // "Atrás" no vuelve al formulario: lleva a Tapas
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = irATapas()
        })
    }

    private fun configurarBarra() {
        findViewById<TextView>(R.id.tvTitulo).text = getString(R.string.titulo_confirmacion)
        findViewById<View>(R.id.ivLogo).setOnClickListener { irATapas() }
        findViewById<View>(R.id.ivUsuario).setOnClickListener {
            startActivity(Intent(this, UsuarioActivity::class.java))
        }
    }

    private fun pintarTarjeta(articulo: Articulo) {
        findViewById<ImageView>(R.id.ivArticulo).setImageResource(articulo.imagen)
        findViewById<TextView>(R.id.tvNombre).text = articulo.nombre
        findViewById<TextView>(R.id.tvPrecio).text = euros(articulo.precio)
        findViewById<Button>(R.id.btnPedir).visibility = View.GONE
        findViewById<Button>(R.id.btnMas).setOnClickListener {
            mostrarFicha(articulo, mostrarPedido = false)
        }
    }

    // Vacía la pila para empezar un pedido nuevo desde cero
    private fun irATapas() {
        startActivity(
            Intent(this, TapasActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}