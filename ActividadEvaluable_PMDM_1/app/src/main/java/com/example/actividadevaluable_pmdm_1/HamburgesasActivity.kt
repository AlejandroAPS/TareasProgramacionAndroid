package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HamburgesasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapas)
        configurarBarras("HAMBURGESAS")

        val tapas = listOf(
            Articulo("Hamburguesa Básica", 9.50, R.drawable.hamburguesa_basica),
            Articulo("Hamburguesa BBQ", 12.00, R.drawable.hamburguesa_bbq),
            Articulo("Hamburguesa Ibérica", 11.50, R.drawable.hamburguesa_iberica),
            Articulo("Hambruguesa `Cabrita`", 12.50, R.drawable.hamburguesa_cabrita),
            Articulo("Hamburguesa Pollo", 10.50, R.drawable.hamburguesa_pollo)
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@HamburgesasActivity)
            adapter = ArticuloAdapter(tapas) { articulo ->
                // Por ahora solo un aviso; luego irá a la ficha / formulario
                Toast.makeText(context, "Pedido: ${articulo.nombre}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}